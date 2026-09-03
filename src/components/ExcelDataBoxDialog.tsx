import React, { useState } from 'react';
import {
  X,
  Database,
  PlusCircle,
  FileSpreadsheet,
  Download,
  Upload,
  RefreshCw,
  Search,
  CheckCircle2,
  Trash2,
  Copy,
  Check
} from 'lucide-react';
import { useQc } from '../context/QcContext';
import { MasterDurationEntity } from '../types';
import {
  parseMasterDurations,
  exportMasterToCsv,
  exportMasterToJson
} from '../utils/masterTemplateHelper';
import { downloadCsv } from '../utils/excelExport';

interface ExcelDataBoxDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSelectPreset?: (preset: MasterDurationEntity) => void;
}

export const DAMAGE_AREAS = [
  'Walls',
  'Flooring',
  'Roofing',
  'Ceiling',
  'Doors',
  'Windows',
  'Sanitary',
  'Electrical',
  'Mechanical',
  'Landscaping',
  'Others'
];

export const SERVICE_TYPES = ['1 PM', '3 PM', '6 PM', 'Deep check', 'CM', 'All'];

export const ExcelDataBoxDialog: React.FC<ExcelDataBoxDialogProps> = ({
  isOpen,
  onClose,
  onSelectPreset
}) => {
  const {
    allMasterDurations,
    addMasterDuration,
    deleteMasterDuration,
    importMasterDurations,
    resetToStandardMasterPresets
  } = useQc();

  const [activeTab, setActiveTab] = useState<'data' | 'add' | 'import'>('data');
  const [selectedAreaFilter, setSelectedAreaFilter] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState('');

  // Add Item form state
  const [newCategory, setNewCategory] = useState('Walls');
  const [newSubItem, setNewSubItem] = useState('');
  const [newSeverity, setNewSeverity] = useState('Medium');
  const [newDuration, setNewDuration] = useState('45');
  const [newService, setNewService] = useState('1 PM');
  const [addSuccessMessage, setAddSuccessMessage] = useState('');

  // Import / Export state
  const [importText, setImportText] = useState('');
  const [replaceExisting, setReplaceExisting] = useState(false);
  const [importNotice, setImportNotice] = useState<{ count: number; text: string } | null>(null);

  if (!isOpen) return null;

  // Filtered data
  const filteredPresets = allMasterDurations.filter(item => {
    const matchArea = selectedAreaFilter === 'All' || item.category === selectedAreaFilter;
    const matchQuery =
      !searchQuery.trim() ||
      item.subItem.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.category.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.serviceType.toLowerCase().includes(searchQuery.toLowerCase());
    return matchArea && matchQuery;
  });

  const handleAddItem = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newSubItem.trim()) return;

    addMasterDuration({
      category: newCategory,
      subItem: newSubItem.trim(),
      severityLevel: newSeverity,
      defaultDurationMinutes: parseInt(newDuration, 10) || 30,
      serviceType: newService
    });

    setAddSuccessMessage(`Successfully added defect benchmark "${newSubItem.trim()}"`);
    setNewSubItem('');
    setTimeout(() => {
      setAddSuccessMessage('');
      setActiveTab('data');
    }, 1200);
  };

  const handleImportAnalyze = (content: string) => {
    setImportText(content);
    const parsed = parseMasterDurations(content);
    if (parsed.length > 0) {
      setImportNotice({
        count: parsed.length,
        text: `${parsed.length} valid defect benchmark items detected.`
      });
    } else {
      setImportNotice(null);
    }
  };

  const handleExecuteImport = () => {
    const parsed = parseMasterDurations(importText);
    if (parsed.length === 0) {
      alert('No valid defect benchmark items detected from text or file.');
      return;
    }

    const count = importMasterDurations(parsed, replaceExisting);
    alert(`Successfully imported ${count} benchmark presets to database.`);
    setImportText('');
    setImportNotice(null);
    setActiveTab('data');
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = evt => {
      const content = evt.target?.result as string;
      if (content) {
        handleImportAnalyze(content);
      }
    };
    reader.readAsText(file);
  };

  const handleExportCsv = () => {
    const csv = exportMasterToCsv(allMasterDurations);
    downloadCsv('IFMS_Master_Defect_Presets.csv', csv);
  };

  const handleExportJson = () => {
    const json = exportMasterToJson(allMasterDurations);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'IFMS_Master_Defect_Presets.json';
    a.click();
    URL.revokeObjectURL(a.href);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div
        id="excel-data-box-dialog"
        className="bg-white rounded-3xl shadow-2xl w-full max-w-4xl max-h-[90vh] flex flex-col overflow-hidden border border-[#E5E7EB]"
      >
        {/* Modal Header */}
        <div className="bg-white px-6 sm:px-8 py-5 flex items-center justify-between border-b border-[#E5E7EB]">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-[#F2F8FE] text-[#0194F3] flex items-center justify-center">
              <Database className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-black text-[#0B1E40]">Master Defect Library (Data Box)</h2>
              <p className="text-xs text-slate-500">
                Official repair benchmark durations, trade classifications, and service templates
              </p>
            </div>
          </div>
          <button
            id="close-excel-databox"
            onClick={onClose}
            className="p-2 rounded-2xl text-slate-400 hover:text-[#0B1E40] hover:bg-slate-100 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Navigation (Traveloka style pills) */}
        <div className="flex border-b border-[#E5E7EB] bg-[#F7F9FA] px-6 sm:px-8 pt-3 gap-2">
          <button
            id="databox-tab-data"
            onClick={() => setActiveTab('data')}
            className={`flex items-center gap-2 pb-3 px-4 text-xs font-black border-b-2 transition-all cursor-pointer ${
              activeTab === 'data'
                ? 'border-[#0194F3] text-[#0194F3] bg-white rounded-t-xl'
                : 'border-transparent text-slate-500 hover:text-slate-900'
            }`}
          >
            <FileSpreadsheet className="w-4 h-4" />
            <span>Browse Library ({allMasterDurations.length})</span>
          </button>
          <button
            id="databox-tab-add"
            onClick={() => setActiveTab('add')}
            className={`flex items-center gap-2 pb-3 px-4 text-xs font-black border-b-2 transition-all cursor-pointer ${
              activeTab === 'add'
                ? 'border-[#0194F3] text-[#0194F3] bg-white rounded-t-xl'
                : 'border-transparent text-slate-500 hover:text-slate-900'
            }`}
          >
            <PlusCircle className="w-4 h-4" />
            <span>+ Add Benchmark Item</span>
          </button>
          <button
            id="databox-tab-import"
            onClick={() => setActiveTab('import')}
            className={`flex items-center gap-2 pb-3 px-4 text-xs font-black border-b-2 transition-all cursor-pointer ${
              activeTab === 'import'
                ? 'border-[#0194F3] text-[#0194F3] bg-white rounded-t-xl'
                : 'border-transparent text-slate-500 hover:text-slate-900'
            }`}
          >
            <Upload className="w-4 h-4" />
            <span>Import / Export CSV</span>
          </button>
        </div>

        {/* Tab Body */}
        <div className="flex-1 overflow-y-auto p-6 sm:p-8">
          {/* TAB 1: DATA LIST */}
          {activeTab === 'data' && (
            <div className="space-y-4">
              {/* Search & Area Filter Bar */}
              <div className="flex flex-col sm:flex-row items-center gap-3">
                <div className="relative w-full sm:w-72">
                  <Search className="w-4 h-4 text-slate-400 absolute left-4 top-3" />
                  <input
                    id="databox-search"
                    type="text"
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                    placeholder="Search defect description..."
                    className="w-full pl-11 pr-4 py-2 text-xs rounded-2xl border border-[#E5E7EB] bg-[#F7F9FA] focus:bg-white focus:outline-hidden focus:border-[#0194F3]"
                  />
                </div>

                <div className="flex items-center gap-1.5 overflow-x-auto w-full pb-1 text-xs">
                  <button
                    onClick={() => setSelectedAreaFilter('All')}
                    className={`px-3.5 py-1.5 rounded-full font-black whitespace-nowrap transition-colors cursor-pointer ${
                      selectedAreaFilter === 'All'
                        ? 'bg-[#0194F3] text-white shadow-xs'
                        : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                    }`}
                  >
                    All ({allMasterDurations.length})
                  </button>
                  {DAMAGE_AREAS.map(area => (
                    <button
                      key={area}
                      onClick={() => setSelectedAreaFilter(area)}
                      className={`px-3.5 py-1.5 rounded-full font-bold whitespace-nowrap transition-colors cursor-pointer ${
                        selectedAreaFilter === area
                          ? 'bg-[#0194F3] text-white shadow-xs'
                          : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                      }`}
                    >
                      {area}
                    </button>
                  ))}
                </div>
              </div>

              {/* Table of Presets */}
              <div className="border border-[#E5E7EB] rounded-2xl overflow-hidden shadow-xs">
                <div className="overflow-x-auto max-h-[420px]">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-[#F8FAFC] text-[#0B1E40] font-black uppercase text-[11px] sticky top-0 z-10 border-b border-[#E5E7EB]">
                      <tr>
                        <th className="py-3 px-3.5 w-10">No</th>
                        <th className="py-3 px-3.5">Category</th>
                        <th className="py-3 px-3.5">Defect Sub-Item</th>
                        <th className="py-3 px-3.5">Severity</th>
                        <th className="py-3 px-3.5 text-center">Labor</th>
                        <th className="py-3 px-3.5">Service</th>
                        <th className="py-3 px-3.5 text-right">Action</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {filteredPresets.map((item, idx) => (
                        <tr
                          key={item.id}
                          className="hover:bg-[#F2F8FE]/60 transition-colors group cursor-pointer"
                          onClick={() => {
                            if (onSelectPreset) {
                              onSelectPreset(item);
                              onClose();
                            }
                          }}
                        >
                          <td className="py-2.5 px-3.5 text-slate-400 font-mono font-bold">{idx + 1}</td>
                          <td className="py-2.5 px-3.5 font-black text-[#0B1E40]">{item.category}</td>
                          <td className="py-2.5 px-3.5 text-slate-800 font-medium">{item.subItem}</td>
                          <td className="py-2.5 px-3.5">
                            <span
                              className={`px-2 py-0.5 rounded text-[10px] font-black ${
                                item.severityLevel === 'High'
                                  ? 'bg-red-100 text-red-700'
                                  : item.severityLevel === 'Medium'
                                  ? 'bg-amber-100 text-amber-700'
                                  : 'bg-sky-100 text-sky-700'
                              }`}
                            >
                              {item.severityLevel}
                            </span>
                          </td>
                          <td className="py-2.5 px-3.5 font-mono font-black text-center text-[#0194F3]">
                            {item.defaultDurationMinutes}m
                          </td>
                          <td className="py-2.5 px-3.5">
                            <span className="bg-slate-100 text-slate-700 px-2 py-0.5 rounded-full text-[10px] font-medium">
                              {item.serviceType}
                            </span>
                          </td>
                          <td className="py-2.5 px-3.5 text-right space-x-1" onClick={e => e.stopPropagation()}>
                            {onSelectPreset && (
                              <button
                                onClick={() => {
                                  onSelectPreset(item);
                                  onClose();
                                }}
                                className="px-3 py-1 bg-[#FF5E1F] hover:bg-[#E04F16] text-white rounded-xl text-[11px] font-black cursor-pointer shadow-xs"
                              >
                                Select
                              </button>
                            )}
                            <button
                              onClick={() => deleteMasterDuration(item.id)}
                              className="p-1.5 text-slate-400 hover:text-red-600 rounded-xl transition-colors cursor-pointer"
                              title="Delete Item"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </td>
                        </tr>
                      ))}
                      {filteredPresets.length === 0 && (
                        <tr>
                          <td colSpan={7} className="py-10 text-center text-slate-400 font-medium">
                            No defect benchmark items match your search.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>

              {onSelectPreset && (
                <p className="text-xs text-slate-500 text-center font-medium">
                  💡 Tip: Click any row above to autofill the current defect finding form instantly.
                </p>
              )}
            </div>
          )}

          {/* TAB 2: ADD ITEM */}
          {activeTab === 'add' && (
            <form onSubmit={handleAddItem} className="max-w-xl mx-auto space-y-4">
              {addSuccessMessage && (
                <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 rounded-2xl flex items-center gap-2 text-xs font-bold">
                  <CheckCircle2 className="w-4 h-4" />
                  {addSuccessMessage}
                </div>
              )}

              <div>
                <label className="block text-xs font-bold text-[#0B1E40] mb-1">
                  Damage Trade / Category
                </label>
                <select
                  value={newCategory}
                  onChange={e => setNewCategory(e.target.value)}
                  className="w-full p-3 text-xs rounded-2xl border border-[#E5E7EB] bg-white font-medium focus:border-[#0194F3]"
                >
                  {DAMAGE_AREAS.map(area => (
                    <option key={area} value={area}>
                      {area}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-[#0B1E40] mb-1">
                  Defect Sub-Item Description
                </label>
                <input
                  type="text"
                  required
                  value={newSubItem}
                  onChange={e => setNewSubItem(e.target.value)}
                  placeholder="e.g., Hollow plaster beneath bathroom window"
                  className="w-full p-3 text-xs rounded-2xl border border-[#E5E7EB] focus:border-[#0194F3]"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-bold text-[#0B1E40] mb-1">Severity Level</label>
                  <select
                    value={newSeverity}
                    onChange={e => setNewSeverity(e.target.value)}
                    className="w-full p-3 text-xs rounded-2xl border border-[#E5E7EB] bg-white font-medium focus:border-[#0194F3]"
                  >
                    <option value="Low">Low (Minor)</option>
                    <option value="Medium">Medium (Moderate)</option>
                    <option value="High">High (Severe)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#0B1E40] mb-1">
                    Standard Labor (Minutes)
                  </label>
                  <input
                    type="number"
                    min="5"
                    max="840"
                    required
                    value={newDuration}
                    onChange={e => setNewDuration(e.target.value)}
                    className="w-full p-3 text-xs rounded-2xl border border-[#E5E7EB] font-black text-[#0194F3] focus:border-[#0194F3]"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#0B1E40] mb-1">Service Type</label>
                  <select
                    value={newService}
                    onChange={e => setNewService(e.target.value)}
                    className="w-full p-3 text-xs rounded-2xl border border-[#E5E7EB] bg-white font-medium focus:border-[#0194F3]"
                  >
                    {SERVICE_TYPES.map(srv => (
                      <option key={srv} value={srv}>
                        {srv}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="pt-3">
                <button
                  type="submit"
                  className="w-full py-3 px-4 bg-[#0194F3] hover:bg-[#0082D9] text-white rounded-2xl text-xs font-black transition-all shadow-md shadow-blue-500/25 cursor-pointer"
                >
                  Save Benchmark to Library
                </button>
              </div>
            </form>
          )}

          {/* TAB 3: IMPORT / EXPORT */}
          {activeTab === 'import' && (
            <div className="space-y-6">
              {/* Export section */}
              <div className="bg-[#F7F9FA] p-5 rounded-3xl border border-[#E5E7EB]">
                <h3 className="text-xs font-black text-[#0B1E40] mb-1">Export Library Benchmarks</h3>
                <p className="text-xs text-slate-500 mb-3.5">
                  Download the complete repair standard dataset for backup or offline analysis in Excel/Sheets.
                </p>
                <div className="flex gap-2.5">
                  <button
                    onClick={handleExportCsv}
                    className="flex items-center gap-1.5 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-2xl text-xs font-black transition-all cursor-pointer shadow-xs"
                  >
                    <Download className="w-3.5 h-3.5" />
                    <span>Download CSV (.csv)</span>
                  </button>
                  <button
                    onClick={handleExportJson}
                    className="flex items-center gap-1.5 px-4 py-2.5 bg-slate-800 hover:bg-slate-900 text-white rounded-2xl text-xs font-black transition-all cursor-pointer shadow-xs"
                  >
                    <Download className="w-3.5 h-3.5" />
                    <span>Download JSON (.json)</span>
                  </button>
                </div>
              </div>

              {/* Import section */}
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <h3 className="text-xs font-black text-[#0B1E40]">
                    Import Benchmark Data (Paste Text or Upload File)
                  </h3>
                  <label className="text-xs font-bold text-[#0194F3] hover:underline cursor-pointer">
                    <span>Choose CSV / JSON File</span>
                    <input
                      type="file"
                      accept=".csv,.tsv,.json,.txt"
                      onChange={handleFileUpload}
                      className="hidden"
                    />
                  </label>
                </div>

                <textarea
                  rows={5}
                  value={importText}
                  onChange={e => handleImportAnalyze(e.target.value)}
                  placeholder={`CSV format: Category, Item Description, Severity, Duration, Service\nExample:\nWalls,Hairline plaster crack,Low,30,1 PM\nFlooring,Chipped floor tile,Medium,60,3 PM`}
                  className="w-full p-3 font-mono text-xs rounded-2xl border border-[#E5E7EB] focus:outline-hidden focus:border-[#0194F3]"
                />

                {importNotice && (
                  <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-2xl flex items-center gap-2 text-xs font-bold text-emerald-700">
                    <CheckCircle2 className="w-4 h-4 shrink-0" />
                    <span>{importNotice.text}</span>
                  </div>
                )}

                <div className="flex items-center justify-between pt-2">
                  <label className="flex items-center gap-2 text-xs text-slate-700 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={replaceExisting}
                      onChange={e => setReplaceExisting(e.target.checked)}
                      className="rounded border-slate-300 text-[#0194F3] focus:ring-blue-500"
                    />
                    <span>Overwrite existing benchmarks (Replace mode)</span>
                  </label>

                  <button
                    onClick={handleExecuteImport}
                    disabled={!importText.trim()}
                    className="px-5 py-2.5 bg-[#0194F3] hover:bg-[#0082D9] disabled:opacity-50 text-white rounded-2xl text-xs font-black transition-all shadow-md cursor-pointer"
                  >
                    Execute Import
                  </button>
                </div>
              </div>

              {/* Reset to standard */}
              <div className="pt-4 border-t border-slate-100 flex items-center justify-between text-xs">
                <span className="text-slate-500">Need to restore original standard presets?</span>
                <button
                  onClick={() => {
                    if (confirm('Restore defect presets to official standard defaults?')) {
                      resetToStandardMasterPresets();
                      alert('Benchmarks have been reset.');
                      setActiveTab('data');
                    }
                  }}
                  className="flex items-center gap-1.5 text-amber-700 hover:text-amber-800 font-black cursor-pointer hover:underline"
                >
                  <RefreshCw className="w-3.5 h-3.5" />
                  <span>Restore Factory Presets</span>
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="bg-[#F7F9FA] px-6 sm:px-8 py-3.5 border-t border-[#E5E7EB] flex items-center justify-between text-xs">
          <span className="text-slate-500 font-medium">
            Total {allMasterDurations.length} defect benchmarks registered
          </span>
          <button
            onClick={onClose}
            className="px-4 py-2 bg-white hover:bg-slate-100 border border-[#E5E7EB] text-slate-700 rounded-2xl font-black transition-colors cursor-pointer"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

