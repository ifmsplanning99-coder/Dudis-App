import React, { useState } from 'react';
import {
  Camera,
  Upload,
  Clock,
  AlertTriangle,
  CheckCircle2,
  FileSpreadsheet,
  Plus,
  ArrowRight,
  Trash2,
  Layers,
  Sparkles,
  ChevronRight,
  Lock,
  Printer
} from 'lucide-react';
import { useQc, CAPACITY_LIMIT_MINUTES } from '../context/QcContext';
import { DurationCapacityBar } from '../components/DurationCapacityBar';
import { ExcelDataBoxDialog, DAMAGE_AREAS } from '../components/ExcelDataBoxDialog';
import { MasterDurationEntity, ScreenTab } from '../types';

interface Phase2FindingInputScreenProps {
  onNavigateTab: (tab: ScreenTab) => void;
}

export const Phase2FindingInputScreen: React.FC<Phase2FindingInputScreenProps> = ({
  onNavigateTab
}) => {
  const {
    activeInspection,
    activeFindings,
    activeTotalDuration,
    isCapacityReached,
    allMasterDurations,
    addFinding
  } = useQc();

  const isSubmitted = activeInspection?.status === 'Submitted' || activeInspection?.status === 'Approved';

  const [isExcelBoxOpen, setIsExcelBoxOpen] = useState(false);

  // Form Fields
  const [damageArea, setDamageArea] = useState<string>('Walls');
  const [damageDescription, setDamageDescription] = useState('');
  const [damageDimension, setDamageDimension] = useState('');
  const [severityLevel, setSeverityLevel] = useState<string>('Medium');
  const [durationMinutes, setDurationMinutes] = useState<number>(45);
  const [photoUri, setPhotoUri] = useState<string | null>(null);

  const [successNotice, setSuccessNotice] = useState('');
  const [errorNotice, setErrorNotice] = useState('');

  // Presets available for currently selected area
  const areaPresets = allMasterDurations.filter(m => m.category === damageArea);

  // Handler for picking preset from dropdown or Excel Box
  const handleSelectPreset = (preset: MasterDurationEntity) => {
    setDamageArea(preset.category);
    setDamageDescription(preset.subItem);
    setSeverityLevel(preset.severityLevel);
    setDurationMinutes(preset.defaultDurationMinutes);
  };

  const handlePhotoCapture = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      alert('Photo file size exceeds limit (maximum 5MB).');
      return;
    }

    const reader = new FileReader();
    reader.onload = evt => {
      setPhotoUri(evt.target?.result as string);
    };
    reader.readAsDataURL(file);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isSubmitted) {
      setErrorNotice('Audit has been submitted and locked. New findings cannot be added.');
      return;
    }
    setErrorNotice('');

    if (!damageDescription.trim()) {
      setErrorNotice('Please provide a specific defect description.');
      return;
    }

    if (durationMinutes <= 0) {
      setErrorNotice('Labor duration must be greater than 0 minutes.');
      return;
    }

    const projectedTotal = activeTotalDuration + durationMinutes;
    if (projectedTotal > CAPACITY_LIMIT_MINUTES && activeTotalDuration >= CAPACITY_LIMIT_MINUTES) {
      setErrorNotice(
        `Field labor capacity has reached the maximum daily threshold (${CAPACITY_LIMIT_MINUTES} minutes). Please review or export your current inspection batch before logging additional items.`
      );
      return;
    }

    const added = addFinding({
      damageArea,
      damageDescription: damageDescription.trim(),
      damageDimension: damageDimension.trim(),
      severityLevel,
      durationMinutes,
      photoUri
    });

    if (added) {
      setSuccessNotice(
        `Defect "${damageDescription.slice(0, 32)}..." (${durationMinutes}m) logged successfully!`
      );
      // Reset input form
      setDamageDescription('');
      setDamageDimension('');
      setPhotoUri(null);
      setTimeout(() => setSuccessNotice(''), 3000);
    } else {
      setErrorNotice('Failed to log defect finding. The 840-minute capacity threshold has been reached.');
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Top Capacity Bar Gauge */}
      <DurationCapacityBar
        totalDurationMinutes={activeTotalDuration}
        maxCapacityMinutes={CAPACITY_LIMIT_MINUTES}
        findingCount={activeFindings.length}
      />

      {/* Traveloka Header Info */}
      <div className="bg-white rounded-3xl p-6 sm:p-7 border border-[#E5E7EB] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#F2F8FE] text-[#0194F3] font-extrabold text-xs mb-2">
            <span className="w-2 h-2 rounded-full bg-[#0194F3] animate-pulse"></span>
            Phase 2 • Defect Logging & Evidence Capture
          </div>
          <h1 className="text-xl sm:text-2xl font-black text-[#0B1E40]">
            Log Defect Findings & Evidence
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            Unit: <strong className="text-slate-800">{activeInspection?.locationName || 'Not specified'}</strong> • Cadence: <strong className="text-slate-800">{activeInspection?.serviceType}</strong>
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            id="btn-open-excel-databox"
            type="button"
            onClick={() => setIsExcelBoxOpen(true)}
            className="flex items-center gap-2 px-4 py-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-2xl text-xs font-black transition-all shadow-xs cursor-pointer active:scale-95"
          >
            <FileSpreadsheet className="w-4 h-4 text-emerald-400" />
            <span>Excel Presets</span>
          </button>

          <button
            id="btn-goto-review"
            type="button"
            onClick={() => onNavigateTab('review')}
            className="flex items-center gap-1.5 px-4 py-2.5 bg-[#F2F8FE] text-[#0194F3] hover:bg-[#E2F0FE] rounded-2xl text-xs font-black transition-all border border-[#BBE0FD] cursor-pointer"
          >
            <span>Review ({activeFindings.length})</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Main Form or Locked Screen */}
      {isSubmitted ? (
        <div className="bg-white rounded-3xl border border-[#E5E7EB] shadow-xs p-8 text-center space-y-5 animate-in fade-in">
          <div className="w-16 h-16 rounded-3xl bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto shadow-xs">
            <Lock className="w-8 h-8" />
          </div>
          <div className="max-w-md mx-auto">
            <h3 className="text-xl font-black text-[#0B1E40]">
              Audit Telah Di-submit & Terkunci
            </h3>
            <p className="text-xs sm:text-sm text-slate-500 mt-1.5 leading-relaxed">
              Laporan inspeksi untuk unit <strong className="text-slate-800">{activeInspection?.locationName}</strong> telah final. Penambahan dan pengeditan temuan defect tidak diizinkan. Silakan pilih opsi dokumen berikut:
            </p>
          </div>

          <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
            <button
              type="button"
              onClick={() => onNavigateTab('review')}
              className="flex items-center gap-2 px-5 py-3 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded-2xl text-xs font-black transition-all cursor-pointer"
            >
              <span>Lihat Daftar Temuan ({activeFindings.length})</span>
            </button>
            <button
              type="button"
              onClick={() => onNavigateTab('export')}
              className="flex items-center gap-2 px-5 py-3 bg-[#0194F3] hover:bg-[#0082D9] text-white rounded-2xl text-xs font-black shadow-md shadow-blue-500/20 transition-all cursor-pointer"
            >
              <FileSpreadsheet className="w-4 h-4 text-emerald-300" />
              <span>Export ke Excel (.xlsx)</span>
            </button>
            <button
              type="button"
              onClick={() => onNavigateTab('export')}
              className="flex items-center gap-2 px-5 py-3 bg-[#FF5E1F] hover:bg-[#E04F16] text-white rounded-2xl text-xs font-black shadow-md shadow-orange-500/20 transition-all cursor-pointer"
            >
              <Printer className="w-4 h-4 text-white" />
              <span>Print PDF</span>
            </button>
          </div>
        </div>
      ) : (
        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-3xl border border-[#E5E7EB] shadow-xs p-6 sm:p-8 space-y-6"
        >
        {/* Damage Area Category Selector */}
        <div>
          <label className="block text-xs font-black text-[#0B1E40] uppercase tracking-wider mb-2.5 flex items-center justify-between">
            <span className="flex items-center gap-1.5">
              <Layers className="w-4 h-4 text-[#0194F3]" />
              1. Structural Trade & Defect Zone *
            </span>
            <span className="text-[11px] text-slate-400 font-semibold">Select inspection trade</span>
          </label>

          <div className="flex flex-wrap gap-2">
            {DAMAGE_AREAS.map(area => (
              <button
                key={area}
                type="button"
                onClick={() => {
                  setDamageArea(area);
                  // Find first preset in this area
                  const first = allMasterDurations.find(m => m.category === area);
                  if (first) {
                    setDamageDescription(first.subItem);
                    setSeverityLevel(first.severityLevel);
                    setDurationMinutes(first.defaultDurationMinutes);
                  }
                }}
                className={`px-4 py-2.5 rounded-2xl text-xs font-bold transition-all cursor-pointer border ${
                  damageArea === area
                    ? 'bg-[#0194F3] text-white border-[#0194F3] shadow-md shadow-blue-500/20'
                    : 'bg-slate-50 text-slate-700 hover:bg-[#F2F8FE] hover:text-[#0194F3] border-slate-200/80'
                }`}
              >
                {area}
              </button>
            ))}
          </div>
        </div>

        {/* Preset Selector for Selected Area */}
        {areaPresets.length > 0 && (
          <div className="bg-[#F8FAFC] p-4 rounded-2xl border border-slate-200/80">
            <div className="flex items-center justify-between mb-2.5">
              <label className="text-xs font-bold text-slate-700 flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-[#FF5E1F]" />
                Recommended Master Presets ({damageArea})
              </label>
              <button
                type="button"
                onClick={() => setIsExcelBoxOpen(true)}
                className="text-[11px] font-extrabold text-[#0194F3] hover:underline cursor-pointer"
              >
                Browse All ({allMasterDurations.length})
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 max-h-36 overflow-y-auto pr-1">
              {areaPresets.map(preset => (
                <button
                  key={preset.id}
                  type="button"
                  onClick={() => handleSelectPreset(preset)}
                  className={`text-left p-2.5 rounded-xl border text-xs transition-all flex items-center justify-between gap-2 cursor-pointer ${
                    damageDescription === preset.subItem
                      ? 'bg-[#F2F8FE] border-[#0194F3] text-[#0194F3] font-bold shadow-2xs'
                      : 'bg-white border-slate-200/80 text-slate-700 hover:border-slate-300'
                  }`}
                >
                  <span className="truncate">{preset.subItem}</span>
                  <span className="shrink-0 text-[10px] font-mono font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-600">
                    {preset.defaultDurationMinutes}m
                  </span>
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {/* Description */}
          <div className="md:col-span-2">
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5">
              2. Defect Description & Specific Findings *
            </label>
            <textarea
              id="input-damage-description"
              rows={2}
              required
              value={damageDescription}
              onChange={e => setDamageDescription(e.target.value)}
              placeholder="e.g., Hairline diagonal plaster shrinkage crack across the north-facing living room partition"
              className="w-full px-4 py-3 text-sm rounded-2xl border border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 font-semibold text-[#0B1E40] transition-all bg-slate-50/50 hover:bg-white focus:bg-white"
            />
          </div>

          {/* Dimension */}
          <div>
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5">
              3. Dimension / Physical Boundary / Quantity
            </label>
            <input
              id="input-damage-dimension"
              type="text"
              value={damageDimension}
              onChange={e => setDamageDimension(e.target.value)}
              placeholder="e.g., Length 1.5 m, width 0.5 mm / 2 floor tiles"
              className="w-full px-4 py-3 text-sm rounded-2xl border border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 font-semibold text-[#0B1E40] transition-all"
            />
          </div>

          {/* Severity Level */}
          <div>
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5">
              4. Severity Classification *
            </label>
            <div className="grid grid-cols-3 gap-2">
              {(['Low', 'Medium', 'High'] as const).map(level => {
                const isSelected = severityLevel === level;
                let activeStyle = 'bg-sky-500 text-white border-sky-500 shadow-xs';
                let desc = 'Cosmetic';
                if (level === 'Medium') {
                  activeStyle = 'bg-amber-500 text-white border-amber-500 shadow-xs';
                  desc = 'Functional';
                }
                if (level === 'High') {
                  activeStyle = 'bg-red-500 text-white border-red-500 shadow-xs';
                  desc = 'Urgent';
                }

                return (
                  <button
                    key={level}
                    type="button"
                    onClick={() => setSeverityLevel(level)}
                    className={`py-2 px-2 rounded-2xl text-xs font-bold text-center border transition-all cursor-pointer ${
                      isSelected
                        ? activeStyle
                        : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
                    }`}
                  >
                    <div>{level}</div>
                    <div className="text-[10px] font-normal opacity-90">{desc}</div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Duration Minutes */}
          <div>
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5 flex items-center justify-between">
              <span className="flex items-center gap-1.5">
                <Clock className="w-4 h-4 text-[#0194F3]" />
                5. Repair Labor Duration (Minutes) *
              </span>
              <span className="text-[11px] text-slate-400 font-semibold">
                Remaining Quota: {Math.max(0, CAPACITY_LIMIT_MINUTES - activeTotalDuration)}m
              </span>
            </label>
            <div className="relative">
              <input
                id="input-duration-minutes"
                type="number"
                min="5"
                max="840"
                required
                value={durationMinutes}
                onChange={e => setDurationMinutes(parseInt(e.target.value, 10) || 0)}
                className="w-full px-4 py-3 text-sm rounded-2xl border border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 font-black text-[#0B1E40] transition-all"
              />
              <span className="absolute right-4 top-3.5 text-xs text-slate-400 font-bold">
                Minutes
              </span>
            </div>
          </div>

          {/* Photo Capture & Upload */}
          <div>
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5 flex items-center gap-1.5">
              <Camera className="w-4 h-4 text-[#0194F3]" />
              6. Field Photographic Evidence (Optional)
            </label>

            {photoUri ? (
              <div className="relative rounded-2xl border border-slate-200 overflow-hidden bg-slate-900 group h-32 flex items-center justify-center">
                <img
                  src={photoUri}
                  alt="Defect Evidence"
                  className="w-full h-full object-cover"
                />
                <button
                  type="button"
                  onClick={() => setPhotoUri(null)}
                  className="absolute top-2.5 right-2.5 p-2 bg-red-600/90 text-white rounded-xl hover:bg-red-700 shadow-md transition-all cursor-pointer"
                  title="Remove Photo"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="flex gap-2">
                {/* Take Photo with Camera */}
                <label className="flex-1 flex flex-col items-center justify-center p-3.5 border-2 border-dashed border-slate-200 hover:border-[#0194F3] rounded-2xl cursor-pointer bg-slate-50/70 hover:bg-[#F2F8FE] transition-all text-center">
                  <Camera className="w-5 h-5 text-[#0194F3] mb-1" />
                  <span className="text-xs font-bold text-slate-800">Use Camera</span>
                  <span className="text-[10px] text-slate-400">Direct snapshot</span>
                  <input
                    type="file"
                    accept="image/*"
                    capture="environment"
                    onChange={handlePhotoCapture}
                    className="hidden"
                  />
                </label>

                {/* Upload from Gallery / Files */}
                <label className="flex-1 flex flex-col items-center justify-center p-3.5 border-2 border-dashed border-slate-200 hover:border-[#0194F3] rounded-2xl cursor-pointer bg-slate-50/70 hover:bg-[#F2F8FE] transition-all text-center">
                  <Upload className="w-5 h-5 text-slate-600 mb-1" />
                  <span className="text-xs font-bold text-slate-800">Upload File</span>
                  <span className="text-[10px] text-slate-400">JPG, PNG, WebP</span>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handlePhotoCapture}
                    className="hidden"
                  />
                </label>
              </div>
            )}
          </div>
        </div>

        {/* Notifications */}
        {successNotice && (
          <div className="p-3.5 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl flex items-center gap-2.5 text-xs font-bold animate-in fade-in">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
            <span>{successNotice}</span>
          </div>
        )}

        {errorNotice && (
          <div className="p-3.5 bg-red-50 border border-red-200 text-red-800 rounded-2xl flex items-center gap-2.5 text-xs font-bold animate-in fade-in">
            <AlertTriangle className="w-4 h-4 text-red-600 shrink-0" />
            <span>{errorNotice}</span>
          </div>
        )}

        {/* Submit Actions */}
        <div className="pt-2 flex flex-col sm:flex-row items-center justify-between gap-3 border-t border-slate-100">
          <div className="text-xs text-slate-500 font-medium">
            Active Findings:{' '}
            <strong className="text-[#0B1E40] font-black">{activeFindings.length} items</strong> (
            {activeTotalDuration} / {CAPACITY_LIMIT_MINUTES} mins)
          </div>

          <div className="flex items-center gap-3 w-full sm:w-auto">
            <button
              id="btn-save-finding"
              type="submit"
              disabled={isCapacityReached}
              className="flex-1 sm:flex-none flex items-center justify-center gap-2 px-7 py-3.5 bg-[#FF5E1F] hover:bg-[#E04F16] disabled:opacity-50 text-white rounded-2xl text-sm font-black shadow-lg shadow-orange-500/20 transition-all cursor-pointer active:scale-95"
            >
              <Plus className="w-4 h-4" />
              <span>Log Defect Finding</span>
            </button>
          </div>
        </div>
      </form>
      )}

      {/* Excel Data Box Dialog */}
      <ExcelDataBoxDialog
        isOpen={isExcelBoxOpen}
        onClose={() => setIsExcelBoxOpen(false)}
        onSelectPreset={handleSelectPreset}
      />
    </div>
  );
};

