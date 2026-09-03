import React, { useState, useEffect } from 'react';
import {
  MapPin,
  Hash,
  Building,
  Calendar,
  User,
  ShieldCheck,
  ArrowRight,
  Plus,
  FileText,
  CheckCircle2,
  Sparkles,
  Info,
  Lock,
  FileSpreadsheet,
  Printer
} from 'lucide-react';
import { useQc } from '../context/QcContext';
import { ScreenTab } from '../types';

interface Phase1LocationScreenProps {
  onNavigateTab: (tab: ScreenTab) => void;
}

const BUILDING_TYPES = ['House', 'Camp', 'Public Facility', 'Office', 'Warehouse', 'Commercial', 'Other'];
const SERVICE_TYPES = ['1 PM', '3 PM', '6 PM', 'Deep check', 'CM'];

export const Phase1LocationScreen: React.FC<Phase1LocationScreenProps> = ({ onNavigateTab }) => {
  const {
    activeInspection,
    allBuildings,
    allUsers,
    startNewInspection,
    updateActiveInspection,
    selectInspection,
    allInspections
  } = useQc();

  const isSubmitted = activeInspection?.status === 'Submitted' || activeInspection?.status === 'Approved';

  const [address, setAddress] = useState(
    activeInspection?.address || activeInspection?.locationName || ''
  );
  const [addressNumber, setAddressNumber] = useState(
    activeInspection?.addressNumber || ''
  );
  const [buildingType, setBuildingType] = useState(activeInspection?.buildingType || 'House');
  const [serviceType, setServiceType] = useState(activeInspection?.serviceType || '1 PM');
  const [inspectionDate, setInspectionDate] = useState(
    activeInspection?.inspectionDate || new Date().toISOString().split('T')[0]
  );
  const [inspectorName, setInspectorName] = useState(
    activeInspection?.inspectorName || 'Ryan Pratama, PE'
  );
  const [supervisorName, setSupervisorName] = useState(
    activeInspection?.supervisorName || 'Robert Sutrisno, PE'
  );
  const [notes, setNotes] = useState(activeInspection?.notes || '');
  const [savedSuccess, setSavedSuccess] = useState(false);

  useEffect(() => {
    if (activeInspection) {
      setAddress(activeInspection.address || activeInspection.locationName || '');
      setAddressNumber(activeInspection.addressNumber || '');
      setBuildingType(activeInspection.buildingType);
      setServiceType(activeInspection.serviceType);
      setInspectionDate(activeInspection.inspectionDate);
      setInspectorName(activeInspection.inspectorName);
      setSupervisorName(activeInspection.supervisorName);
      setNotes(activeInspection.notes || '');
    }
  }, [activeInspection]);

  const getFullLocationName = (addr: string, num: string) => {
    const cleanAddr = addr.trim();
    const cleanNum = num.trim();
    if (!cleanAddr && !cleanNum) return '';
    if (!cleanNum) return cleanAddr;
    if (!cleanAddr) return cleanNum;
    if (
      cleanNum.toLowerCase().startsWith('no.') ||
      cleanNum.startsWith('#') ||
      cleanNum.toLowerCase().startsWith('unit') ||
      cleanNum.toLowerCase().startsWith('blok')
    ) {
      return `${cleanAddr} ${cleanNum}`;
    }
    return `${cleanAddr} No. ${cleanNum}`;
  };

  const handleSaveAndProceed = (e: React.FormEvent) => {
    e.preventDefault();
    if (isSubmitted) {
      onNavigateTab('review');
      return;
    }

    if (!address.trim()) {
      alert('Please provide the Address.');
      return;
    }

    const computedLocationName = getFullLocationName(address, addressNumber);

    if (activeInspection) {
      updateActiveInspection({
        locationName: computedLocationName,
        address: address.trim(),
        addressNumber: addressNumber.trim(),
        buildingType,
        serviceType,
        inspectionDate,
        inspectorName,
        supervisorName,
        notes
      });
    } else {
      startNewInspection({
        locationName: computedLocationName,
        address: address.trim(),
        addressNumber: addressNumber.trim(),
        buildingType,
        serviceType,
        inspectionDate,
        inspectorName,
        supervisorName
      });
    }

    setSavedSuccess(true);
    setTimeout(() => {
      setSavedSuccess(false);
      onNavigateTab('finding');
    }, 500);
  };

  const handleSelectBuildingPreset = (bName: string, bType: string, defLoc: string) => {
    setAddress(bName);
    setAddressNumber(defLoc);
    setBuildingType(bType);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Traveloka Hero Header Card */}
      <div className="bg-white rounded-3xl p-6 sm:p-7 border border-[#E5E7EB] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#F2F8FE] text-[#0194F3] font-extrabold text-xs mb-2">
            <span className="w-2 h-2 rounded-full bg-[#0194F3] animate-pulse"></span>
            Phase 1 • Address & Identification
          </div>
          <h1 className="text-xl sm:text-2xl font-black text-[#0B1E40]">
            Inspection Zone & Personnel Setup
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1 max-w-xl">
            Specify the property address, address number, service cadence, inspection schedule, and authorized supervisory engineers.
          </p>
        </div>

        <button
          onClick={() => {
            const dateStr = new Date().toISOString().split('T')[0];
            const newInsp = startNewInspection({
              locationName: 'New Project Unit No. 01',
              address: 'New Project Unit',
              addressNumber: '01',
              buildingType: 'House',
              serviceType: '1 PM',
              inspectionDate: dateStr,
              inspectorName: 'Ryan Pratama, PE',
              supervisorName: 'Robert Sutrisno, PE'
            });
            selectInspection(newInsp);
          }}
          className="flex items-center gap-2 px-4 py-2.5 bg-[#F2F8FE] hover:bg-[#E2F0FE] text-[#0194F3] rounded-2xl text-xs font-extrabold transition-all border border-[#BBE0FD] self-start sm:self-center cursor-pointer active:scale-95"
        >
          <Plus className="w-4 h-4" />
          <span>New Inspection Sheet</span>
        </button>
      </div>

      {/* Traveloka Switcher Bar */}
      {allInspections.length > 1 && (
        <div className="bg-white p-3.5 rounded-2xl border border-[#E5E7EB] text-xs flex items-center justify-between gap-3 overflow-x-auto shadow-2xs">
          <div className="flex items-center gap-2 text-slate-500 shrink-0 font-bold">
            <Info className="w-4 h-4 text-[#0194F3]" />
            <span>Switch Project:</span>
          </div>
          <div className="flex items-center gap-2">
            {allInspections.slice(0, 5).map(insp => (
              <button
                key={insp.id}
                onClick={() => selectInspection(insp)}
                className={`px-3 py-1.5 rounded-xl font-bold whitespace-nowrap transition-all text-xs cursor-pointer ${
                  activeInspection?.id === insp.id
                    ? 'bg-[#0194F3] text-white shadow-xs'
                    : 'bg-slate-50 text-slate-700 hover:bg-slate-100 border border-slate-200'
                }`}
              >
                {insp.locationName.split('-')[0].trim()} ({insp.status})
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Form Card */}
      <form
        onSubmit={handleSaveAndProceed}
        className="bg-white rounded-3xl border border-[#E5E7EB] shadow-xs p-6 sm:p-8 space-y-6"
      >
        {/* Submitted & Locked Notification Banner */}
        {isSubmitted && (
          <div className="p-4 sm:p-5 bg-emerald-50 border border-emerald-200 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 animate-in fade-in">
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-xl bg-emerald-600 text-white flex items-center justify-center shrink-0 mt-0.5 shadow-xs">
                <Lock className="w-5 h-5" />
              </div>
              <div>
                <h4 className="text-xs sm:text-sm font-black text-emerald-950">
                  Laporan Inspeksi Telah Di-submit & Terkunci
                </h4>
                <p className="text-xs text-emerald-800 font-medium mt-0.5">
                  Seluruh metadata lokasi dan temuan telah final (read-only). Silakan pilih opsi dokumen:
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2 w-full sm:w-auto">
              <button
                type="button"
                onClick={() => onNavigateTab('export')}
                className="flex-1 sm:flex-none flex items-center justify-center gap-1.5 px-3.5 py-2 bg-[#0194F3] hover:bg-[#0082D9] text-white rounded-xl text-xs font-black transition-all shadow-xs cursor-pointer"
              >
                <FileSpreadsheet className="w-3.5 h-3.5 text-emerald-300" />
                <span>Export ke Excel</span>
              </button>
              <button
                type="button"
                onClick={() => onNavigateTab('export')}
                className="flex-1 sm:flex-none flex items-center justify-center gap-1.5 px-3.5 py-2 bg-[#FF5E1F] hover:bg-[#E04F16] text-white rounded-xl text-xs font-black transition-all shadow-xs cursor-pointer"
              >
                <Printer className="w-3.5 h-3.5 text-white" />
                <span>Print PDF</span>
              </button>
            </div>
          </div>
        )}

        {/* Quick Building Presets Selection */}
        {!isSubmitted && (
          <div>
            <label className="block text-xs font-black text-[#0B1E40] uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5 text-[#0194F3]" />
              Quick Presets • Property Templates
            </label>
            <div className="flex flex-wrap gap-2">
              {allBuildings.map(b => (
                <button
                  key={b.id}
                  type="button"
                  onClick={() => handleSelectBuildingPreset(b.name, b.type, b.defaultLocation)}
                  className="px-3.5 py-2 rounded-xl bg-slate-50 hover:bg-[#F2F8FE] hover:text-[#0194F3] hover:border-[#BBE0FD] border border-slate-200/80 text-slate-700 text-xs font-bold transition-all cursor-pointer"
                >
                  {b.name}
                </button>
              ))}
            </div>
          </div>
        )}

        {!isSubmitted && <div className="h-px bg-slate-100" />}

        {/* Form Fields Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {/* Address & Address Number */}
          <div className="md:col-span-2 grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="sm:col-span-2">
              <label className="block text-xs font-bold text-[#0B1E40] mb-1.5 flex items-center gap-1.5">
                <MapPin className="w-4 h-4 text-[#0194F3]" />
                Address *
              </label>
              <input
                id="input-address"
                type="text"
                required
                disabled={isSubmitted}
                value={address}
                onChange={e => setAddress(e.target.value)}
                placeholder="e.g., Orchid Cluster, Palm Residence"
                className={`w-full px-4 py-3 text-sm rounded-2xl border font-semibold transition-all ${
                  isSubmitted
                    ? 'border-slate-200 bg-slate-100/70 text-slate-600 cursor-not-allowed'
                    : 'border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 text-[#0B1E40] bg-slate-50/50 hover:bg-white focus:bg-white'
                }`}
              />
            </div>

            <div className="sm:col-span-1">
              <label className="block text-xs font-bold text-[#0B1E40] mb-1.5 flex items-center gap-1.5">
                <Hash className="w-4 h-4 text-[#0194F3]" />
                Address Number *
              </label>
              <input
                id="input-address-number"
                type="text"
                required
                disabled={isSubmitted}
                value={addressNumber}
                onChange={e => setAddressNumber(e.target.value)}
                placeholder="e.g., 12, Unit 36, Blok B"
                className={`w-full px-4 py-3 text-sm rounded-2xl border font-semibold transition-all ${
                  isSubmitted
                    ? 'border-slate-200 bg-slate-100/70 text-slate-600 cursor-not-allowed'
                    : 'border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 text-[#0B1E40] bg-slate-50/50 hover:bg-white focus:bg-white'
                }`}
              />
            </div>
          </div>

          {/* Building Type */}
          <div>
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5 flex items-center gap-1.5">
              <Building className="w-4 h-4 text-[#0194F3]" />
              Building Classification *
            </label>
            <select
              id="select-building-type"
              disabled={isSubmitted}
              value={buildingType}
              onChange={e => setBuildingType(e.target.value)}
              className={`w-full px-4 py-3 text-sm rounded-2xl border font-semibold transition-all ${
                isSubmitted
                  ? 'border-slate-200 bg-slate-100/70 text-slate-600 cursor-not-allowed'
                  : 'border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 bg-white text-[#0B1E40]'
              }`}
            >
              {BUILDING_TYPES.map(t => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </div>

          {/* Service Type */}
          <div>
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5 flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4 text-[#0194F3]" />
              Maintenance Service Cadence *
            </label>
            <select
              id="select-service-type"
              disabled={isSubmitted}
              value={serviceType}
              onChange={e => setServiceType(e.target.value)}
              className={`w-full px-4 py-3 text-sm rounded-2xl border font-semibold transition-all ${
                isSubmitted
                  ? 'border-slate-200 bg-slate-100/70 text-slate-600 cursor-not-allowed'
                  : 'border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 bg-white text-[#0B1E40]'
              }`}
            >
              {SERVICE_TYPES.map(s => (
                <option key={s} value={s}>
                  {s} (Preventive / Corrective)
                </option>
              ))}
            </select>
          </div>

          {/* Inspection Date */}
          <div>
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5 flex items-center gap-1.5">
              <Calendar className="w-4 h-4 text-[#0194F3]" />
              Inspection Date *
            </label>
            <input
              id="input-inspection-date"
              type="date"
              required
              disabled={isSubmitted}
              value={inspectionDate}
              onChange={e => setInspectionDate(e.target.value)}
              className={`w-full px-4 py-3 text-sm rounded-2xl border font-semibold transition-all ${
                isSubmitted
                  ? 'border-slate-200 bg-slate-100/70 text-slate-600 cursor-not-allowed'
                  : 'border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 bg-white text-[#0B1E40]'
              }`}
            />
          </div>

          {/* Inspector Name */}
          <div>
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5 flex items-center gap-1.5">
              <User className="w-4 h-4 text-[#0194F3]" />
              Lead QC Inspector (Field Evaluator) *
            </label>
            <input
              id="input-inspector-name"
              type="text"
              required
              disabled={isSubmitted}
              value={inspectorName}
              onChange={e => setInspectorName(e.target.value)}
              list="inspector-list"
              placeholder="e.g., Ryan Pratama, PE"
              className={`w-full px-4 py-3 text-sm rounded-2xl border font-semibold transition-all ${
                isSubmitted
                  ? 'border-slate-200 bg-slate-100/70 text-slate-600 cursor-not-allowed'
                  : 'border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 text-[#0B1E40]'
              }`}
            />
            <datalist id="inspector-list">
              {allUsers.map(u => (
                <option key={u.id} value={u.name}>
                  {u.role}
                </option>
              ))}
            </datalist>
          </div>

          {/* Supervisor Name */}
          <div className="md:col-span-2">
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5 flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4 text-[#0194F3]" />
              QC Supervisor (Authorized Approval Authority)
            </label>
            <input
              id="input-supervisor-name"
              type="text"
              disabled={isSubmitted}
              value={supervisorName}
              onChange={e => setSupervisorName(e.target.value)}
              list="supervisor-list"
              placeholder="e.g., Robert Sutrisno, PE"
              className={`w-full px-4 py-3 text-sm rounded-2xl border font-semibold transition-all ${
                isSubmitted
                  ? 'border-slate-200 bg-slate-100/70 text-slate-600 cursor-not-allowed'
                  : 'border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 text-[#0B1E40]'
              }`}
            />
            <datalist id="supervisor-list">
              {allUsers.map(u => (
                <option key={u.id} value={u.name}>
                  {u.role}
                </option>
              ))}
            </datalist>
          </div>

          {/* Notes */}
          <div className="md:col-span-2">
            <label className="block text-xs font-bold text-[#0B1E40] mb-1.5 flex items-center gap-1.5">
              <FileText className="w-4 h-4 text-slate-400" />
              Special Scope, Weather & Site Access Notes
            </label>
            <textarea
              id="input-inspection-notes"
              rows={2}
              disabled={isSubmitted}
              value={notes}
              onChange={e => setNotes(e.target.value)}
              placeholder="e.g., Routine quarterly audit of partition walls, plumbing fixtures, and gypsum ceilings..."
              className={`w-full px-4 py-3 text-sm rounded-2xl border font-medium transition-all ${
                isSubmitted
                  ? 'border-slate-200 bg-slate-100/70 text-slate-600 cursor-not-allowed'
                  : 'border-slate-200 focus:outline-hidden focus:border-[#0194F3] focus:ring-3 focus:ring-[#0194F3]/15 text-[#0B1E40]'
              }`}
            />
          </div>
        </div>

        {savedSuccess && (
          <div className="p-3.5 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl flex items-center gap-2.5 text-xs font-bold animate-in fade-in">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
            <span>Location setup saved! Proceeding to Log Findings (Phase 2)...</span>
          </div>
        )}

        {/* Action CTA */}
        {isSubmitted ? (
          <div className="pt-2 flex flex-wrap items-center justify-between gap-3 border-t border-slate-100">
            <span className="text-xs font-bold text-slate-400 flex items-center gap-1.5">
              <Lock className="w-3.5 h-3.5" /> Read-Only Mode (Terkunci)
            </span>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => onNavigateTab('review')}
                className="flex items-center gap-1.5 px-5 py-3 bg-[#0194F3] hover:bg-[#0082D9] text-white rounded-2xl text-xs font-black shadow-sm cursor-pointer"
              >
                <span>Lihat Temuan Defect</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                onClick={() => onNavigateTab('export')}
                className="flex items-center gap-1.5 px-5 py-3 bg-[#FF5E1F] hover:bg-[#E04F16] text-white rounded-2xl text-xs font-black shadow-sm cursor-pointer"
              >
                <Printer className="w-3.5 h-3.5" />
                <span>Export / Print PDF</span>
              </button>
            </div>
          </div>
        ) : (
          <div className="pt-2 flex items-center justify-end">
            <button
              id="btn-proceed-phase2"
              type="submit"
              className="flex items-center gap-2 px-7 py-3.5 bg-[#FF5E1F] hover:bg-[#E04F16] active:scale-95 text-white rounded-2xl text-sm font-black shadow-lg shadow-orange-500/20 transition-all cursor-pointer"
            >
              <span>Save Location & Log Findings</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        )}
      </form>
    </div>
  );
};

