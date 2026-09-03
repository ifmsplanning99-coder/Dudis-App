import React, { useState } from 'react';
import {
  FileSpreadsheet,
  Plus,
  Trash2,
  Edit2,
  Eye,
  X,
  Camera,
  AlertTriangle,
  Clock,
  Layers,
  CheckCircle2,
  Lock,
  Printer,
  Send,
  FileDown
} from 'lucide-react';
import { useQc, CAPACITY_LIMIT_MINUTES } from '../context/QcContext';
import { DurationCapacityBar } from '../components/DurationCapacityBar';
import { FindingEntity, ScreenTab } from '../types';
import { DAMAGE_AREAS } from '../components/ExcelDataBoxDialog';
import { exportInspectionToExcel } from '../utils/excelExport';

interface ReviewFindingsScreenProps {
  onNavigateTab: (tab: ScreenTab) => void;
}

export const ReviewFindingsScreen: React.FC<ReviewFindingsScreenProps> = ({ onNavigateTab }) => {
  const {
    activeInspection,
    activeFindings,
    activeTotalDuration,
    updateFinding,
    deleteFinding,
    submitActiveInspection
  } = useQc();

  const [editingFinding, setEditingFinding] = useState<FindingEntity | null>(null);
  const [photoModalUri, setPhotoModalUri] = useState<string | null>(null);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);
  const [showSubmitModal, setShowSubmitModal] = useState(false);
  const [excelExportSuccess, setExcelExportSuccess] = useState(false);

  const isSubmitted = activeInspection?.status === 'Submitted' || activeInspection?.status === 'Approved';

  const handleSaveEdit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingFinding || isSubmitted) return;
    updateFinding(editingFinding);
    setEditingFinding(null);
  };

  const handleConfirmSubmit = () => {
    submitActiveInspection();
    setShowSubmitModal(false);
  };

  const handleExportExcel = () => {
    if (!activeInspection) return;
    exportInspectionToExcel(activeInspection, activeFindings);
    setExcelExportSuccess(true);
    setTimeout(() => setExcelExportSuccess(false), 3000);
  };

  const handlePrintPdf = () => {
    onNavigateTab('export');
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Top Capacity Bar */}
      <DurationCapacityBar
        totalDurationMinutes={activeTotalDuration}
        maxCapacityMinutes={CAPACITY_LIMIT_MINUTES}
        findingCount={activeFindings.length}
      />

      {/* Screen Header */}
      <div className="bg-white rounded-3xl p-6 sm:p-7 border border-[#E5E7EB] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#F2F8FE] text-[#0194F3] font-extrabold text-xs mb-2">
            <span className="w-2 h-2 rounded-full bg-[#0194F3]"></span>
            Phase 3 • Review, Verification & Submission
          </div>
          <h1 className="text-xl sm:text-2xl font-black text-[#0B1E40]">
            Audit Findings Review ({activeFindings.length} Logged Items)
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Unit: <strong className="text-slate-800">{activeInspection?.locationName}</strong> • Audit Status:{' '}
            <span
              className={`font-black px-2.5 py-0.5 rounded-full text-xs inline-flex items-center gap-1 ${
                isSubmitted
                  ? 'bg-emerald-100 text-emerald-800'
                  : 'bg-amber-100 text-amber-800'
              }`}
            >
              {isSubmitted && <Lock className="w-3 h-3 text-emerald-700" />}
              <span>{isSubmitted ? 'Submitted (Locked)' : 'Draft'}</span>
            </span>
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {isSubmitted ? (
            <>
              {/* Option 1: Export ke Excel */}
              <button
                id="review-btn-download-xlsx"
                onClick={handleExportExcel}
                className="flex items-center gap-2 px-4 py-2.5 bg-[#0194F3] hover:bg-[#0082D9] active:scale-95 text-white rounded-2xl text-xs font-black transition-all shadow-md shadow-blue-500/25 cursor-pointer"
              >
                <FileSpreadsheet className="w-4 h-4 text-emerald-300" />
                <span>Export ke Excel</span>
              </button>

              {/* Option 2: Print PDF */}
              <button
                id="review-btn-print-pdf"
                onClick={handlePrintPdf}
                className="flex items-center gap-2 px-4 py-2.5 bg-[#FF5E1F] hover:bg-[#E04F16] active:scale-95 text-white rounded-2xl text-xs font-black transition-all shadow-md shadow-orange-500/25 cursor-pointer"
              >
                <Printer className="w-4 h-4 text-white" />
                <span>Print PDF</span>
              </button>
            </>
          ) : (
            <>
              <button
                id="review-btn-add-finding"
                onClick={() => onNavigateTab('finding')}
                className="flex items-center gap-1.5 px-4 py-2.5 bg-[#FF5E1F] hover:bg-[#E04F16] active:scale-95 text-white rounded-2xl text-xs font-black transition-all shadow-md shadow-orange-500/20 cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Add Defect</span>
              </button>

              {activeFindings.length > 0 && (
                <button
                  id="review-btn-submit"
                  onClick={() => setShowSubmitModal(true)}
                  className="flex items-center gap-1.5 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 active:scale-95 text-white rounded-2xl text-xs font-black transition-all shadow-md shadow-emerald-600/25 cursor-pointer"
                >
                  <Send className="w-4 h-4" />
                  <span>Submit</span>
                </button>
              )}

              <button
                id="review-btn-export"
                onClick={() => onNavigateTab('export')}
                className="flex items-center gap-1.5 px-4 py-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-2xl text-xs font-black transition-all shadow-xs cursor-pointer"
              >
                <FileSpreadsheet className="w-4 h-4 text-emerald-400" />
                <span>Excel Report</span>
              </button>
            </>
          )}
        </div>
      </div>

      {/* Submitted & Locked Banner with requested choices */}
      {isSubmitted && (
        <div className="bg-emerald-50 border border-emerald-200 rounded-3xl p-5 sm:p-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div className="flex items-start gap-3.5">
            <div className="w-11 h-11 rounded-2xl bg-emerald-600 text-white flex items-center justify-center shrink-0 shadow-sm mt-0.5">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-sm sm:text-base font-black text-emerald-950">
                  Audit Telah Di-submit & Dikunci
                </h3>
                <span className="px-2 py-0.5 bg-emerald-200 text-emerald-900 rounded-md text-[10px] font-black uppercase tracking-wider flex items-center gap-1">
                  <Lock className="w-3 h-3" /> Read-Only
                </span>
              </div>
              <p className="text-xs sm:text-sm text-emerald-800 font-medium mt-1 leading-relaxed">
                Laporan inspeksi ini telah final dan tidak dapat diedit kembali. Silakan pilih opsi dokumen resmi:
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2.5 w-full md:w-auto shrink-0">
            <button
              onClick={handleExportExcel}
              className="flex-1 md:flex-none flex items-center justify-center gap-2 px-4 py-2.5 bg-[#0194F3] hover:bg-[#0082D9] active:scale-95 text-white rounded-2xl text-xs font-black transition-all shadow-sm shadow-blue-500/25 cursor-pointer"
            >
              <FileSpreadsheet className="w-4 h-4 text-emerald-300" />
              <span>Export ke Excel (.xlsx)</span>
            </button>
            <button
              onClick={handlePrintPdf}
              className="flex-1 md:flex-none flex items-center justify-center gap-2 px-4 py-2.5 bg-[#FF5E1F] hover:bg-[#E04F16] active:scale-95 text-white rounded-2xl text-xs font-black transition-all shadow-sm shadow-orange-500/25 cursor-pointer"
            >
              <Printer className="w-4 h-4 text-white" />
              <span>Print PDF</span>
            </button>
          </div>
        </div>
      )}

      {excelExportSuccess && (
        <div className="p-3.5 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl flex items-center gap-2.5 text-xs font-bold animate-in fade-in">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          <span>Excel worksheet (.xlsx) berhasil diexport dan diunduh!</span>
        </div>
      )}

      {/* Findings List */}
      <div className="space-y-3.5">
        {activeFindings.length === 0 ? (
          <div className="bg-white rounded-3xl p-12 text-center border border-[#E5E7EB] shadow-xs space-y-4">
            <div className="w-14 h-14 rounded-2xl bg-[#F2F8FE] text-[#0194F3] flex items-center justify-center mx-auto">
              <Layers className="w-7 h-7" />
            </div>
            <div>
              <h3 className="text-base sm:text-lg font-black text-[#0B1E40]">No Defect Findings Recorded Yet</h3>
              <p className="text-xs sm:text-sm text-slate-500 mt-1 max-w-md mx-auto">
                Begin inspecting the site and logging defect findings, photographic evidence, and labor estimations in Phase 2.
              </p>
            </div>
            <button
              onClick={() => onNavigateTab('finding')}
              className="px-6 py-3 bg-[#FF5E1F] hover:bg-[#E04F16] text-white rounded-2xl text-xs font-black inline-flex items-center gap-2 shadow-md shadow-orange-500/20 cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Log First Defect Finding</span>
            </button>
          </div>
        ) : (
          activeFindings.map((finding, idx) => {
            const isHigh = finding.severityLevel === 'High';
            const isMed = finding.severityLevel === 'Medium';

            return (
              <div
                key={finding.id}
                id={`finding-card-${finding.id}`}
                className="bg-white rounded-3xl p-5 border border-[#E5E7EB] shadow-xs hover:border-[#0194F3]/40 hover:shadow-md transition-all flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4"
              >
                {/* Left: Thumbnail & Details */}
                <div className="flex items-start gap-4 flex-1">
                  {/* Photo Thumbnail */}
                  {finding.photoUri ? (
                    <div
                      onClick={() => setPhotoModalUri(finding.photoUri)}
                      className="w-18 h-18 sm:w-20 sm:h-20 rounded-2xl overflow-hidden bg-slate-100 shrink-0 border border-slate-200 cursor-pointer relative group"
                    >
                      <img
                        src={finding.photoUri}
                        alt="Defect Evidence"
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                      />
                      <div className="absolute inset-0 bg-black/35 opacity-0 group-hover:opacity-100 flex items-center justify-center text-white transition-opacity">
                        <Eye className="w-4 h-4" />
                      </div>
                    </div>
                  ) : (
                    <div className="w-18 h-18 sm:w-20 sm:h-20 rounded-2xl bg-slate-50 shrink-0 border border-slate-200/80 flex flex-col items-center justify-center text-slate-400">
                      <Camera className="w-6 h-6 mb-0.5 text-slate-300" />
                      <span className="text-[9px] font-bold">No Photo</span>
                    </div>
                  )}

                  {/* Text Details */}
                  <div className="space-y-1.5 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-xs font-mono font-black text-slate-400">
                        #{idx + 1}
                      </span>
                      <span className="px-3 py-0.5 rounded-full text-xs font-black bg-[#F2F8FE] text-[#0194F3] border border-[#BBE0FD]">
                        {finding.damageArea}
                      </span>
                      <span
                        className={`px-3 py-0.5 rounded-full text-[11px] font-black ${
                          isHigh
                            ? 'bg-red-50 text-red-700 border border-red-200'
                            : isMed
                            ? 'bg-amber-50 text-amber-700 border border-amber-200'
                            : 'bg-sky-50 text-sky-700 border border-sky-200'
                        }`}
                      >
                        {finding.severityLevel} Severity
                      </span>
                      <span className="text-xs font-bold text-slate-700 flex items-center gap-1 bg-slate-100 px-2 py-0.5 rounded-md font-mono">
                        <Clock className="w-3.5 h-3.5 text-slate-400" />
                        {finding.durationMinutes} min
                      </span>
                    </div>

                    <h4 className="text-sm font-black text-[#0B1E40] leading-snug">
                      {finding.damageDescription}
                    </h4>

                    {finding.damageDimension && (
                      <p className="text-xs text-slate-500 font-medium">
                        Dimension / Scope: <span className="text-slate-800 font-bold">{finding.damageDimension}</span>
                      </p>
                    )}
                  </div>
                </div>

                {/* Right: Actions */}
                <div className="flex items-center gap-2 self-end sm:self-center shrink-0 border-t sm:border-t-0 pt-2 sm:pt-0 w-full sm:w-auto justify-end">
                  {isSubmitted ? (
                    <div
                      className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 text-slate-500 rounded-xl text-xs font-bold border border-slate-200/80"
                      title="Item is locked after audit submission"
                    >
                      <Lock className="w-3.5 h-3.5 text-slate-400" />
                      <span>Locked</span>
                    </div>
                  ) : (
                    <>
                      <button
                        onClick={() => setEditingFinding(finding)}
                        className="p-2.5 text-slate-500 hover:text-[#0194F3] hover:bg-[#F2F8FE] rounded-xl transition-colors cursor-pointer"
                        title="Edit Finding"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => setDeleteConfirmId(finding.id)}
                        className="p-2.5 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-xl transition-colors cursor-pointer"
                        title="Delete Finding"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Delete Confirmation Modal */}
      {deleteConfirmId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-3xl p-6 sm:p-7 max-w-sm w-full border border-[#E5E7EB] shadow-2xl space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-red-100 text-red-600 flex items-center justify-center">
              <Trash2 className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-black text-[#0B1E40]">Delete This Finding?</h3>
              <p className="text-xs text-slate-500 mt-1 leading-relaxed">
                This defect item and its associated photo evidence will be permanently deleted from the active inspection.
              </p>
            </div>
            <div className="flex items-center justify-end gap-2.5 pt-2">
              <button
                onClick={() => setDeleteConfirmId(null)}
                className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-2xl text-xs font-bold transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  deleteFinding(deleteConfirmId);
                  setDeleteConfirmId(null);
                }}
                className="px-5 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-2xl text-xs font-black transition-colors shadow-xs cursor-pointer"
              >
                Yes, Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Finding Modal */}
      {editingFinding && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-3xl shadow-2xl max-w-lg w-full overflow-hidden border border-[#E5E7EB]">
            <div className="bg-[#0194F3] text-white px-6 py-4.5 flex items-center justify-between">
              <h3 className="text-sm font-black flex items-center gap-2">
                <Edit2 className="w-4 h-4" /> Edit Defect Finding
              </h3>
              <button
                onClick={() => setEditingFinding(null)}
                className="text-white/80 hover:text-white cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSaveEdit} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#0B1E40] mb-1">Structural Trade Area</label>
                <select
                  value={editingFinding.damageArea}
                  onChange={e =>
                    setEditingFinding({ ...editingFinding, damageArea: e.target.value })
                  }
                  className="w-full p-3 text-xs rounded-xl border border-slate-200 bg-white font-semibold"
                >
                  {DAMAGE_AREAS.map(a => (
                    <option key={a} value={a}>
                      {a}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-[#0B1E40] mb-1">
                  Defect Description *
                </label>
                <textarea
                  rows={3}
                  required
                  value={editingFinding.damageDescription}
                  onChange={e =>
                    setEditingFinding({ ...editingFinding, damageDescription: e.target.value })
                  }
                  className="w-full p-3 text-xs rounded-xl border border-slate-200 font-semibold"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#0B1E40] mb-1">
                  Dimensions / Physical Boundary
                </label>
                <input
                  type="text"
                  value={editingFinding.damageDimension}
                  onChange={e =>
                    setEditingFinding({ ...editingFinding, damageDimension: e.target.value })
                  }
                  className="w-full p-3 text-xs rounded-xl border border-slate-200 font-semibold"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-[#0B1E40] mb-1">
                    Severity Level
                  </label>
                  <select
                    value={editingFinding.severityLevel}
                    onChange={e =>
                      setEditingFinding({ ...editingFinding, severityLevel: e.target.value })
                    }
                    className="w-full p-3 text-xs rounded-xl border border-slate-200 bg-white font-semibold"
                  >
                    <option value="Low">Low (Cosmetic)</option>
                    <option value="Medium">Medium (Functional)</option>
                    <option value="High">High (Urgent)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#0B1E40] mb-1">
                    Duration (Minutes)
                  </label>
                  <input
                    type="number"
                    min="5"
                    max="840"
                    required
                    value={editingFinding.durationMinutes}
                    onChange={e =>
                      setEditingFinding({
                        ...editingFinding,
                        durationMinutes: parseInt(e.target.value, 10) || 0
                      })
                    }
                    className="w-full p-3 text-xs rounded-xl border border-slate-200 font-black"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setEditingFinding(null)}
                  className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-2xl text-xs font-bold cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 bg-[#0194F3] hover:bg-[#0082D9] text-white rounded-2xl text-xs font-black shadow-md shadow-blue-500/20 cursor-pointer"
                >
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Photo Modal */}
      {photoModalUri && (
        <div
          onClick={() => setPhotoModalUri(null)}
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm cursor-pointer animate-in fade-in"
        >
          <div className="relative max-w-3xl max-h-[85vh] rounded-3xl overflow-hidden shadow-2xl bg-black border border-slate-800">
            <img
              src={photoModalUri}
              alt="Defect Finding Photo"
              className="w-full h-full object-contain max-h-[85vh]"
            />
            <button
              onClick={() => setPhotoModalUri(null)}
              className="absolute top-3 right-3 p-2 bg-black/60 text-white rounded-full hover:bg-black cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>
      )}

      {/* Submit Confirmation Modal */}
      {showSubmitModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-3xl p-6 sm:p-7 max-w-md w-full border border-[#E5E7EB] shadow-2xl space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-600 flex items-center justify-center">
              <Send className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base sm:text-lg font-black text-[#0B1E40]">
                Submit & Kunci Laporan Audit?
              </h3>
              <p className="text-xs sm:text-sm text-slate-600 mt-2 leading-relaxed">
                Setelah di-submit, laporan inspeksi ini akan <strong>dikunci permanen dan tidak dapat diedit kembali</strong>.
              </p>
              <div className="mt-3 p-3 rounded-2xl bg-[#F7F9FA] border border-[#E5E7EB] text-xs text-slate-600 space-y-1">
                <p className="font-bold text-[#0B1E40]">Opsi yang tersedia setelah submit:</p>
                <div className="flex items-center gap-2 text-slate-700">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                  <span><strong>Export ke Excel (.xlsx)</strong> - spreadsheet lengkap</span>
                </div>
                <div className="flex items-center gap-2 text-slate-700">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#FF5E1F]"></span>
                  <span><strong>Print PDF</strong> - berkas cetak resmi A4</span>
                </div>
              </div>
            </div>
            <div className="flex items-center justify-end gap-2.5 pt-2">
              <button
                type="button"
                onClick={() => setShowSubmitModal(false)}
                className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-2xl text-xs font-bold transition-colors cursor-pointer"
              >
                Batal
              </button>
              <button
                type="button"
                onClick={handleConfirmSubmit}
                className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-2xl text-xs font-black transition-colors shadow-md shadow-emerald-600/25 cursor-pointer flex items-center gap-1.5"
              >
                <Send className="w-4 h-4" />
                <span>Ya, Submit Audit</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

