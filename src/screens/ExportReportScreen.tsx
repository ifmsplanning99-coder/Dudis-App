import React, { useState } from 'react';
import {
  FileSpreadsheet,
  Download,
  Copy,
  Printer,
  FileDown,
  Loader2,
  CheckCircle2,
  AlertCircle,
  Calendar,
  Building,
  User,
  ShieldCheck,
  Eye,
  Camera,
  Layers,
  ArrowLeft
} from 'lucide-react';
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas-pro';
import { useQc, CAPACITY_LIMIT_MINUTES } from '../context/QcContext';
import {
  exportInspectionToExcel,
  generateCsvContent,
  downloadCsv
} from '../utils/excelExport';
import { ScreenTab } from '../types';

interface ExportReportScreenProps {
  onNavigateTab: (tab: ScreenTab) => void;
}

export const ExportReportScreen: React.FC<ExportReportScreenProps> = ({ onNavigateTab }) => {
  const { activeInspection, activeFindings, activeTotalDuration } = useQc();
  const [copySuccess, setCopySuccess] = useState(false);
  const [isGeneratingPdf, setIsGeneratingPdf] = useState(false);
  const [pdfSuccess, setPdfSuccess] = useState(false);
  const [pdfError, setPdfError] = useState<string | null>(null);

  if (!activeInspection) {
    return (
      <div className="max-w-4xl mx-auto p-12 bg-white rounded-3xl border border-[#E5E7EB] text-center shadow-xs">
        <p className="text-slate-500 text-sm font-medium">No active inspection available for export.</p>
        <button
          onClick={() => onNavigateTab('location')}
          className="mt-4 px-6 py-3 bg-[#0194F3] text-white rounded-2xl text-xs font-black shadow-md shadow-blue-500/20"
        >
          Start New Inspection
        </button>
      </div>
    );
  }

  const totalFindings = activeFindings.length;
  const totalHours = Math.floor(activeTotalDuration / 60);
  const remainingMins = activeTotalDuration % 60;
  const capacityPercent = Math.min(
    100,
    Math.round((activeTotalDuration / CAPACITY_LIMIT_MINUTES) * 100)
  );

  const handleExportXlsx = () => {
    exportInspectionToExcel(activeInspection, activeFindings);
  };

  const handleExportCsv = () => {
    const csv = generateCsvContent(activeInspection, activeFindings);
    const filename = `QC_Report_${activeInspection.locationName.replace(/[^a-zA-Z0-9_-]/g, '_')}.csv`;
    downloadCsv(filename, csv);
  };

  const handleCopyToClipboard = () => {
    const csv = generateCsvContent(activeInspection, activeFindings);
    navigator.clipboard.writeText(csv).then(() => {
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 2500);
    });
  };

  const handleDownloadPdf = async () => {
    const element = document.getElementById('printable-qc-report');
    if (!element) return;
    setIsGeneratingPdf(true);
    setPdfError(null);
    try {
      // Render the report card into a canvas with html2canvas-pro (supports oklch, oklab, lch, and modern CSS color models)
      const canvas = await html2canvas(element, {
        scale: 2,
        useCORS: true,
        allowTaint: true,
        logging: false,
        backgroundColor: '#ffffff',
        scrollX: 0,
        scrollY: 0
      });

      const imgData = canvas.toDataURL('image/jpeg', 0.95);
      const pdf = new jsPDF({
        orientation: 'portrait',
        unit: 'mm',
        format: 'a4'
      });

      const pdfWidth = pdf.internal.pageSize.getWidth();
      const pdfHeight = pdf.internal.pageSize.getHeight();
      const imgWidth = pdfWidth;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;

      let heightLeft = imgHeight;
      let position = 0;

      pdf.addImage(imgData, 'JPEG', 0, position, imgWidth, imgHeight);
      heightLeft -= pdfHeight;

      while (heightLeft > 0) {
        position = heightLeft - imgHeight;
        pdf.addPage();
        pdf.addImage(imgData, 'JPEG', 0, position, imgWidth, imgHeight);
        heightLeft -= pdfHeight;
      }

      const cleanLocation = activeInspection.locationName.replace(/[^a-zA-Z0-9_-]/g, '_');
      pdf.save(`QC_Report_${cleanLocation}_${activeInspection.inspectionDate}.pdf`);
      setPdfSuccess(true);
      setTimeout(() => setPdfSuccess(false), 3500);
    } catch (error: any) {
      console.error('PDF export error:', error);
      setPdfError(error?.message || 'Terjadi kesalahan saat membuat file PDF.');
      setTimeout(() => setPdfError(null), 5000);
      // Fallback to window print if generation encountered an issue
      window.print();
    } finally {
      setIsGeneratingPdf(false);
    }
  };

  const handlePrint = () => {
    try {
      window.print();
    } catch (e) {
      console.warn('window.print unavailable, triggering PDF download', e);
      handleDownloadPdf();
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Top Controls Bar */}
      <div className="bg-white rounded-3xl p-6 sm:p-7 border border-[#E5E7EB] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 print:hidden">
        <div>
          <button
            onClick={() => onNavigateTab('review')}
            className="flex items-center gap-1.5 text-xs font-extrabold text-[#0194F3] hover:underline mb-1.5 cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Audit Review</span>
          </button>
          <h1 className="text-xl sm:text-2xl font-black text-[#0B1E40]">
            Excel Report Preview & Official Export
          </h1>
          <p className="text-xs sm:text-sm text-slate-500">
            Standard IFMS QC inspection document with supervisor verification sign-off.
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Excel XLSX */}
          <button
            id="btn-download-xlsx"
            onClick={handleExportXlsx}
            className="flex items-center gap-2 px-4 py-2.5 sm:py-3 bg-[#0194F3] hover:bg-[#0082D9] active:scale-95 text-white rounded-2xl text-xs font-black transition-all shadow-md shadow-blue-500/25 cursor-pointer"
          >
            <FileSpreadsheet className="w-4 h-4 text-emerald-300" />
            <span>Download Excel (.xlsx)</span>
          </button>

          {/* Download PDF (.pdf) */}
          <button
            id="btn-download-pdf"
            onClick={handleDownloadPdf}
            disabled={isGeneratingPdf}
            className="flex items-center gap-2 px-4 py-2.5 sm:py-3 bg-[#FF5E1F] hover:bg-[#E04F16] disabled:opacity-75 active:scale-95 text-white rounded-2xl text-xs font-black transition-all shadow-md shadow-orange-500/25 cursor-pointer"
          >
            {isGeneratingPdf ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin text-white" />
                <span>Generating PDF...</span>
              </>
            ) : (
              <>
                <FileDown className="w-4 h-4" />
                <span>Download PDF (.pdf)</span>
              </>
            )}
          </button>

          {/* Direct Print */}
          <button
            id="btn-print-report"
            onClick={handlePrint}
            className="flex items-center gap-1.5 px-3.5 py-2.5 sm:py-3 bg-white hover:bg-slate-50 text-slate-700 rounded-2xl text-xs font-black transition-all border border-[#D1D5DB] cursor-pointer shadow-2xs"
            title="Open browser print dialog"
          >
            <Printer className="w-4 h-4 text-slate-600" />
            <span>Print</span>
          </button>

          {/* CSV File */}
          <button
            id="btn-download-csv"
            onClick={handleExportCsv}
            className="flex items-center gap-1.5 px-3.5 py-2.5 sm:py-3 bg-slate-900 hover:bg-slate-800 text-white rounded-2xl text-xs font-black transition-all shadow-xs cursor-pointer"
          >
            <Download className="w-4 h-4" />
            <span>CSV</span>
          </button>

          {/* Copy Table */}
          <button
            id="btn-copy-clipboard"
            onClick={handleCopyToClipboard}
            className="flex items-center gap-1.5 px-3.5 py-2.5 sm:py-3 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-2xl text-xs font-black transition-all border border-slate-200 cursor-pointer"
          >
            <Copy className="w-4 h-4" />
            <span>{copySuccess ? 'Copied!' : 'Copy'}</span>
          </button>
        </div>
      </div>

      {pdfSuccess && (
        <div className="p-3.5 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl flex items-center gap-2.5 text-xs font-bold animate-in fade-in print:hidden">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          <span>Official QC Inspection PDF generated and downloaded successfully!</span>
        </div>
      )}

      {pdfError && (
        <div className="p-3.5 bg-rose-50 border border-rose-200 text-rose-800 rounded-2xl flex items-center gap-2.5 text-xs font-bold animate-in fade-in print:hidden">
          <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
          <span>{pdfError}</span>
        </div>
      )}

      {copySuccess && (
        <div className="p-3.5 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl flex items-center gap-2.5 text-xs font-bold animate-in fade-in print:hidden">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          <span>Inspection dataset table copied to clipboard!</span>
        </div>
      )}

      {/* SPREADSHEET CANVAS CONTAINER */}
      <div
        id="printable-qc-report"
        className="bg-white rounded-3xl border border-[#D1D5DB] shadow-lg overflow-hidden p-6 sm:p-10 space-y-6 print:p-0 print:border-none print:shadow-none"
      >
        {/* Spreadsheet Header */}
        <div className="border-b-2 border-[#0B1E40] pb-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="text-[11px] font-mono tracking-widest text-[#0194F3] font-black uppercase">
              PT INTEGRATED FACILITY MANAGEMENT SOLUTIONS (IFMS)
            </div>
            <h2 className="text-xl sm:text-2xl font-black text-[#0B1E40] uppercase tracking-tight">
              PROPERTY QUALITY CONTROL (QC) INSPECTION REPORT
            </h2>
            <p className="text-xs text-slate-500 font-medium">
              Field Defect Assessment, Labor Standard Durations & Formal Validation
            </p>
          </div>

          <div className="text-right shrink-0">
            <span
              className={`px-3.5 py-1 rounded-full text-xs font-black uppercase tracking-wider border ${
                activeInspection.status === 'Submitted' || activeInspection.status === 'Approved'
                  ? 'bg-emerald-50 text-emerald-800 border-emerald-300'
                  : 'bg-amber-50 text-amber-800 border-amber-300'
              }`}
            >
              STATUS: {activeInspection.status === 'Approved' ? 'SUBMITTED' : activeInspection.status.toUpperCase()}
            </span>
            <div className="text-[11px] font-mono text-slate-400 mt-1 font-semibold">
              Doc ID: IFMS-QC-{activeInspection.id}
            </div>
          </div>
        </div>

        {/* Metadata Panel (Key-Value Grid) */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3.5 bg-[#F8FAFC] p-5 rounded-2xl border border-slate-200 text-xs">
          <div>
            <span className="text-slate-400 uppercase font-black text-[10px] block">
              Inspection Date
            </span>
            <span className="font-black text-[#0B1E40]">{activeInspection.inspectionDate}</span>
          </div>

          <div>
            <span className="text-slate-400 uppercase font-black text-[10px] block">
              Building Type
            </span>
            <span className="font-black text-[#0B1E40]">{activeInspection.buildingType}</span>
          </div>

          <div>
            <span className="text-slate-400 uppercase font-black text-[10px] block">
              QC Service Cadence
            </span>
            <span className="font-black text-[#0B1E40]">{activeInspection.serviceType}</span>
          </div>

          <div>
            <span className="text-slate-400 uppercase font-black text-[10px] block">
              Total Defect Findings
            </span>
            <span className="font-black text-[#0194F3]">{totalFindings} Recorded Items</span>
          </div>

          <div>
            <span className="text-slate-400 uppercase font-black text-[10px] block">
              Address
            </span>
            <span className="font-black text-[#0B1E40] text-sm">
              {activeInspection.address || activeInspection.locationName}
            </span>
          </div>

          <div>
            <span className="text-slate-400 uppercase font-black text-[10px] block">
              Address Number
            </span>
            <span className="font-black text-[#0B1E40] text-sm">
              {activeInspection.addressNumber || '-'}
            </span>
          </div>

          <div>
            <span className="text-slate-400 uppercase font-black text-[10px] block">
              QC Lead Inspector
            </span>
            <span className="font-black text-[#0B1E40]">{activeInspection.inspectorName}</span>
          </div>

          <div>
            <span className="text-slate-400 uppercase font-black text-[10px] block">
              QC Area Supervisor
            </span>
            <span className="font-black text-[#0B1E40]">
              {activeInspection.supervisorName || 'Pending'}
            </span>
          </div>
        </div>

        {/* Findings Table */}
        <div className="border border-slate-200 rounded-2xl overflow-hidden shadow-2xs">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead className="bg-[#0B1E40] text-white font-black uppercase text-[11px]">
                <tr>
                  <th className="py-3 px-3.5 border-r border-slate-800 w-10 text-center">No</th>
                  <th className="py-3 px-3.5 border-r border-slate-800 w-28">Trade / Area</th>
                  <th className="py-3 px-3.5 border-r border-slate-800">Defect Description & Findings</th>
                  <th className="py-3 px-3.5 border-r border-slate-800 w-36">Dimensions / Scope</th>
                  <th className="py-3 px-3.5 border-r border-slate-800 w-24 text-center">Severity</th>
                  <th className="py-3 px-3.5 border-r border-slate-800 w-24 text-right">Labor (m)</th>
                  <th className="py-3 px-3.5 border-r border-slate-800 w-20 text-center">Status</th>
                  <th className="py-3 px-3.5 w-16 text-center">Photo</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {activeFindings.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="py-8 text-center text-slate-400 font-medium">
                      No defects recorded for this property inspection unit.
                    </td>
                  </tr>
                ) : (
                  activeFindings.map((finding, idx) => (
                    <tr
                      key={finding.id}
                      className={idx % 2 === 0 ? 'bg-white' : 'bg-slate-50/70'}
                    >
                      <td className="py-2.5 px-3.5 border-r border-slate-200 text-center font-mono font-black text-slate-400">
                        {idx + 1}
                      </td>
                      <td className="py-2.5 px-3.5 border-r border-slate-200 font-bold text-[#0B1E40]">
                        {finding.damageArea}
                      </td>
                      <td className="py-2.5 px-3.5 border-r border-slate-200 font-medium text-slate-800">
                        {finding.damageDescription}
                      </td>
                      <td className="py-2.5 px-3.5 border-r border-slate-200 text-slate-600">
                        {finding.damageDimension || '-'}
                      </td>
                      <td className="py-2.5 px-3.5 border-r border-slate-200 text-center">
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-black ${
                            finding.severityLevel === 'High'
                              ? 'bg-red-100 text-red-700'
                              : finding.severityLevel === 'Medium'
                              ? 'bg-amber-100 text-amber-700'
                              : 'bg-sky-100 text-sky-700'
                          }`}
                        >
                          {finding.severityLevel}
                        </span>
                      </td>
                      <td className="py-2.5 px-3.5 border-r border-slate-200 text-right font-mono font-black text-[#0B1E40]">
                        {finding.durationMinutes}
                      </td>
                      <td className="py-2.5 px-3.5 border-r border-slate-200 text-center font-bold text-slate-600">
                        {finding.status}
                      </td>
                      <td className="py-2.5 px-3.5 text-center">
                        {finding.photoUri ? (
                          <span className="text-emerald-600 font-black text-[11px]">Yes</span>
                        ) : (
                          <span className="text-slate-300 text-[11px]">-</span>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
              {/* Summary Rows Footer */}
              <tfoot className="bg-slate-100 font-black text-[#0B1E40] border-t-2 border-slate-300">
                <tr>
                  <td colSpan={4} className="py-3 px-3.5 border-r border-slate-300 uppercase">
                    Total Defect Items & Estimated Labor Duration:
                  </td>
                  <td className="py-3 px-3.5 border-r border-slate-300 text-center">
                    {totalFindings} Items
                  </td>
                  <td className="py-3 px-3.5 border-r border-slate-300 text-right font-mono text-[#0194F3]">
                    {activeTotalDuration} min
                  </td>
                  <td colSpan={2} className="py-3 px-3.5 text-right text-slate-600">
                    ~ {totalHours}h {remainingMins}m
                  </td>
                </tr>
                <tr className="bg-[#F2F8FE] text-[11px]">
                  <td colSpan={4} className="py-2.5 px-3.5 border-r border-slate-300">
                    IFMS Daily Operational Labor Capacity Limit:
                  </td>
                  <td colSpan={4} className="py-2.5 px-3.5 text-right text-[#0194F3] font-black">
                    {activeTotalDuration} / {CAPACITY_LIMIT_MINUTES} Minutes ({capacityPercent}% Quota)
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>

        {/* Photos Attached Section */}
        {activeFindings.some(f => f.photoUri) && (
          <div className="pt-4 border-t border-slate-200">
            <h3 className="text-xs font-black text-[#0B1E40] uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <Camera className="w-4 h-4 text-[#0194F3]" />
              Attached Field Photographic Evidence
            </h3>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              {activeFindings
                .filter(f => f.photoUri)
                .map((f, i) => (
                  <div
                    key={f.id}
                    className="border border-slate-200 rounded-2xl overflow-hidden bg-slate-50 text-xs shadow-2xs"
                  >
                    <div className="h-32 bg-slate-200 overflow-hidden">
                      <img
                        src={f.photoUri!}
                        alt={f.damageDescription}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div className="p-2.5">
                      <div className="font-bold text-[#0B1E40] truncate">
                        #{i + 1} - {f.damageArea}
                      </div>
                      <div className="text-[10px] text-slate-500 truncate">
                        {f.damageDescription}
                      </div>
                    </div>
                  </div>
                ))}
            </div>
          </div>
        )}

        {/* Signatures Block (Standard QC Protocol) */}
        <div className="pt-6 border-t-2 border-slate-200 grid grid-cols-2 gap-8 text-center text-xs">
          <div className="space-y-12">
            <span className="text-slate-400 font-black uppercase tracking-wider block">
              Prepared By (QC Inspector):
            </span>
            <div>
              <div className="font-black text-[#0B1E40] text-sm">
                ( {activeInspection.inspectorName} )
              </div>
              <div className="text-[11px] text-slate-400 mt-0.5">QC Field Inspector</div>
            </div>
          </div>

          <div className="space-y-12">
            <span className="text-slate-400 font-black uppercase tracking-wider block">
              Approved By (QC Supervisor):
            </span>
            <div>
              <div className="font-black text-[#0B1E40] text-sm">
                ( {activeInspection.supervisorName || '................................'} )
              </div>
              <div className="text-[11px] text-slate-400 mt-0.5">QC Area Supervisor</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

