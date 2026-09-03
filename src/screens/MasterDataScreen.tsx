import React, { useState } from 'react';
import {
  Database,
  Users,
  Plus,
  FileSpreadsheet,
  Download,
  Upload,
  RefreshCw,
  Search,
  Trash2,
  Edit2,
  Mail,
  Phone,
  ShieldCheck,
  CheckCircle2
} from 'lucide-react';
import { useQc } from '../context/QcContext';
import { MasterDurationEntity, UserEntity, ScreenTab } from '../types';
import { DAMAGE_AREAS, SERVICE_TYPES } from '../components/ExcelDataBoxDialog';
import {
  exportMasterToCsv,
  exportMasterToJson,
  parseMasterDurations
} from '../utils/masterTemplateHelper';
import { downloadCsv } from '../utils/excelExport';

interface MasterDataScreenProps {
  onNavigateTab: (tab: ScreenTab) => void;
}

export const MasterDataScreen: React.FC<MasterDataScreenProps> = () => {
  const {
    allMasterDurations,
    allUsers,
    addMasterDuration,
    updateMasterDuration,
    deleteMasterDuration,
    importMasterDurations,
    resetToStandardMasterPresets,
    addUser
  } = useQc();

  const [activeTab, setActiveTab] = useState<'presets' | 'users'>('presets');

  // Presets filters
  const [selectedServiceFilter, setSelectedServiceFilter] = useState('All');
  const [selectedAreaFilter, setSelectedAreaFilter] = useState('All');
  const [searchQuery, setSearchQuery] = useState('');

  // Add / Edit Preset Modal
  const [isPresetModalOpen, setIsPresetModalOpen] = useState(false);
  const [editingPreset, setEditingPreset] = useState<MasterDurationEntity | null>(null);
  const [presetCategory, setPresetCategory] = useState('Walls');
  const [presetSubItem, setPresetSubItem] = useState('');
  const [presetSeverity, setPresetSeverity] = useState('Medium');
  const [presetDuration, setPresetDuration] = useState('45');
  const [presetService, setPresetService] = useState('1 PM');

  // Add User Modal
  const [isUserModalOpen, setIsUserModalOpen] = useState(false);
  const [userName, setUserName] = useState('');
  const [userEmail, setUserEmail] = useState('');
  const [userRole, setUserRole] = useState('QC Inspector');
  const [userPhone, setUserPhone] = useState('');

  // Filtered presets
  const filteredPresets = allMasterDurations.filter(p => {
    const matchService =
      selectedServiceFilter === 'All' ||
      p.serviceType === selectedServiceFilter ||
      p.serviceType === 'All';
    const matchArea = selectedAreaFilter === 'All' || p.category === selectedAreaFilter;
    const matchQuery =
      !searchQuery.trim() ||
      p.subItem.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.category.toLowerCase().includes(searchQuery.toLowerCase());

    return matchService && matchArea && matchQuery;
  });

  const handleOpenAddPreset = () => {
    setEditingPreset(null);
    setPresetCategory('Walls');
    setPresetSubItem('');
    setPresetSeverity('Medium');
    setPresetDuration('45');
    setPresetService('1 PM');
    setIsPresetModalOpen(true);
  };

  const handleOpenEditPreset = (p: MasterDurationEntity) => {
    setEditingPreset(p);
    setPresetCategory(p.category);
    setPresetSubItem(p.subItem);
    setPresetSeverity(p.severityLevel);
    setPresetDuration(p.defaultDurationMinutes.toString());
    setPresetService(p.serviceType);
    setIsPresetModalOpen(true);
  };

  const handleSavePreset = (e: React.FormEvent) => {
    e.preventDefault();
    if (!presetSubItem.trim()) return;

    if (editingPreset) {
      updateMasterDuration({
        ...editingPreset,
        category: presetCategory,
        subItem: presetSubItem.trim(),
        severityLevel: presetSeverity,
        defaultDurationMinutes: parseInt(presetDuration, 10) || 30,
        serviceType: presetService
      });
    } else {
      addMasterDuration({
        category: presetCategory,
        subItem: presetSubItem.trim(),
        severityLevel: presetSeverity,
        defaultDurationMinutes: parseInt(presetDuration, 10) || 30,
        serviceType: presetService
      });
    }
    setIsPresetModalOpen(false);
  };

  const handleSaveUser = (e: React.FormEvent) => {
    e.preventDefault();
    if (!userName.trim()) return;

    addUser({
      name: userName.trim(),
      email: userEmail.trim(),
      role: userRole,
      phone: userPhone.trim()
    });

    setUserName('');
    setUserEmail('');
    setUserPhone('');
    setIsUserModalOpen(false);
  };

  const handleExportCsv = () => {
    const csv = exportMasterToCsv(allMasterDurations);
    downloadCsv('IFMS_Master_Defect_Presets.csv', csv);
  };

  const handleImportFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = evt => {
      const content = evt.target?.result as string;
      if (content) {
        const parsed = parseMasterDurations(content);
        if (parsed.length > 0) {
          const count = importMasterDurations(parsed, false);
          alert(`Successfully imported ${count} new defect presets!`);
        } else {
          alert('Could not detect valid defect presets from file.');
        }
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-[#E5E7EB] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-black text-[#0B1E40]">
            Master Standards & Operational Benchmarks
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Configure defect standard durations, trade classification rules, and authorized personnel directory.
          </p>
        </div>

        {/* Tab Toggle (Traveloka pill style) */}
        <div className="flex bg-[#F2F8FE] p-1.5 rounded-2xl self-start sm:self-center border border-blue-100">
          <button
            onClick={() => setActiveTab('presets')}
            className={`flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-black transition-all cursor-pointer ${
              activeTab === 'presets'
                ? 'bg-[#0194F3] text-white shadow-sm'
                : 'text-[#0B1E40] hover:text-[#0194F3]'
            }`}
          >
            <Database className="w-3.5 h-3.5" />
            <span>Defect Presets ({allMasterDurations.length})</span>
          </button>
          <button
            onClick={() => setActiveTab('users')}
            className={`flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-black transition-all cursor-pointer ${
              activeTab === 'users'
                ? 'bg-[#0194F3] text-white shadow-sm'
                : 'text-[#0B1E40] hover:text-[#0194F3]'
            }`}
          >
            <Users className="w-3.5 h-3.5" />
            <span>Team Directory ({allUsers.length})</span>
          </button>
        </div>
      </div>

      {/* TAB 1: PRESETS */}
      {activeTab === 'presets' && (
        <div className="space-y-4">
          {/* Controls Bar */}
          <div className="bg-white rounded-3xl p-4 sm:p-5 border border-[#E5E7EB] shadow-xs flex flex-col lg:flex-row items-center justify-between gap-3">
            <div className="relative w-full lg:w-72">
              <Search className="w-4 h-4 text-slate-400 absolute left-4 top-3.5" />
              <input
                type="text"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                placeholder="Search defect item..."
                className="w-full pl-11 pr-4 py-2.5 text-xs rounded-2xl border border-[#E5E7EB] bg-[#F7F9FA] focus:bg-white focus:outline-hidden focus:border-[#0194F3]"
              />
            </div>

            <div className="flex flex-wrap items-center gap-2 w-full lg:w-auto text-xs">
              {/* Service filter */}
              <select
                value={selectedServiceFilter}
                onChange={e => setSelectedServiceFilter(e.target.value)}
                className="px-3.5 py-2.5 rounded-2xl border border-[#E5E7EB] bg-white font-bold text-[#0B1E40] cursor-pointer"
              >
                <option value="All">All Services</option>
                {SERVICE_TYPES.map(s => (
                  <option key={s} value={s}>
                    Service {s}
                  </option>
                ))}
              </select>

              {/* Area filter */}
              <select
                value={selectedAreaFilter}
                onChange={e => setSelectedAreaFilter(e.target.value)}
                className="px-3.5 py-2.5 rounded-2xl border border-[#E5E7EB] bg-white font-bold text-[#0B1E40] cursor-pointer"
              >
                <option value="All">All Trades / Areas</option>
                {DAMAGE_AREAS.map(a => (
                  <option key={a} value={a}>
                    {a}
                  </option>
                ))}
              </select>

              <button
                onClick={handleOpenAddPreset}
                className="flex items-center gap-1.5 px-4 py-2.5 bg-[#0194F3] hover:bg-[#0082D9] active:scale-95 text-white rounded-2xl font-black transition-all shadow-md shadow-blue-500/20 cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>+ New Preset</span>
              </button>

              <button
                onClick={handleExportCsv}
                className="p-2.5 text-slate-600 hover:text-emerald-700 hover:bg-emerald-50 rounded-2xl border border-[#E5E7EB] transition-all cursor-pointer"
                title="Export CSV"
              >
                <Download className="w-4 h-4" />
              </button>

              <label
                className="p-2.5 text-slate-600 hover:text-[#0194F3] hover:bg-blue-50 rounded-2xl border border-[#E5E7EB] transition-all cursor-pointer"
                title="Import CSV/JSON File"
              >
                <Upload className="w-4 h-4" />
                <input
                  type="file"
                  accept=".csv,.tsv,.json"
                  onChange={handleImportFile}
                  className="hidden"
                />
              </label>

              <button
                onClick={() => {
                  if (confirm('Reset defect presets to official standard defaults?')) {
                    resetToStandardMasterPresets();
                    alert('Preset benchmarks reset successfully.');
                  }
                }}
                className="p-2.5 text-slate-600 hover:text-amber-700 hover:bg-amber-50 rounded-2xl border border-[#E5E7EB] transition-all cursor-pointer"
                title="Reset to Factory Presets"
              >
                <RefreshCw className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Presets Table */}
          <div className="bg-white rounded-3xl border border-[#E5E7EB] shadow-xs overflow-hidden">
            <div className="overflow-x-auto max-h-[550px]">
              <table className="w-full text-left text-xs">
                <thead className="bg-[#F8FAFC] text-[#0B1E40] font-black uppercase text-[11px] sticky top-0 z-10 border-b border-[#E5E7EB]">
                  <tr>
                    <th className="py-3.5 px-4 w-12 text-center">No</th>
                    <th className="py-3.5 px-4">Trade / Category</th>
                    <th className="py-3.5 px-4">Defect Classification Item</th>
                    <th className="py-3.5 px-4">Severity</th>
                    <th className="py-3.5 px-4 text-center">Standard Labor</th>
                    <th className="py-3.5 px-4">Service</th>
                    <th className="py-3.5 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {filteredPresets.map((preset, idx) => (
                    <tr key={preset.id} className="hover:bg-[#F2F8FE]/40 transition-colors">
                      <td className="py-3 px-4 text-center text-slate-400 font-mono font-black">
                        {idx + 1}
                      </td>
                      <td className="py-3 px-4 font-black text-[#0B1E40]">{preset.category}</td>
                      <td className="py-3 px-4 font-medium text-slate-800">{preset.subItem}</td>
                      <td className="py-3 px-4">
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-black ${
                            preset.severityLevel === 'High'
                              ? 'bg-red-100 text-red-700'
                              : preset.severityLevel === 'Medium'
                              ? 'bg-amber-100 text-amber-700'
                              : 'bg-sky-100 text-sky-700'
                          }`}
                        >
                          {preset.severityLevel}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-center font-black text-[#0194F3] font-mono">
                        {preset.defaultDurationMinutes} mins
                      </td>
                      <td className="py-3 px-4">
                        <span className="bg-slate-100 text-slate-700 px-2.5 py-0.5 rounded-full text-[10px] font-bold">
                          {preset.serviceType}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right space-x-1">
                        <button
                          onClick={() => handleOpenEditPreset(preset)}
                          className="p-2 text-slate-400 hover:text-[#0194F3] rounded-xl transition-colors cursor-pointer"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => deleteMasterDuration(preset.id)}
                          className="p-2 text-slate-400 hover:text-red-600 rounded-xl transition-colors cursor-pointer"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                  {filteredPresets.length === 0 && (
                    <tr>
                      <td colSpan={7} className="py-14 text-center text-slate-400 font-medium">
                        No preset benchmark items matched your filter criteria.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: USERS */}
      {activeTab === 'users' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-black text-[#0B1E40]">
              Authorized QC Inspectors & Project Supervisors
            </h2>
            <button
              onClick={() => setIsUserModalOpen(true)}
              className="flex items-center gap-1.5 px-4 py-2.5 bg-[#0194F3] hover:bg-[#0082D9] text-white rounded-2xl text-xs font-black transition-all shadow-md shadow-blue-500/20 cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Add Personnel</span>
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {allUsers.map(u => (
              <div
                key={u.id}
                className="bg-white rounded-3xl p-6 border border-[#E5E7EB] shadow-xs space-y-3"
              >
                <div className="flex items-center justify-between">
                  <div className="w-11 h-11 rounded-2xl bg-[#F2F8FE] text-[#0194F3] flex items-center justify-center font-black text-base">
                    {u.name.charAt(0)}
                  </div>
                  <span className="px-3 py-1 rounded-full text-[10px] font-black bg-blue-50 text-[#0194F3] border border-blue-200">
                    {u.role}
                  </span>
                </div>

                <div>
                  <h3 className="text-sm font-black text-[#0B1E40]">{u.name}</h3>
                  <div className="text-xs text-slate-400 font-medium flex items-center gap-1.5 mt-1.5">
                    <Mail className="w-3.5 h-3.5 text-slate-400" />
                    <span>{u.email}</span>
                  </div>
                  {u.phone && (
                    <div className="text-xs text-slate-400 font-medium flex items-center gap-1.5 mt-1">
                      <Phone className="w-3.5 h-3.5 text-slate-400" />
                      <span>{u.phone}</span>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Preset Modal */}
      {isPresetModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 sm:p-7 border border-slate-200 shadow-2xl space-y-4">
            <h3 className="text-base font-black text-[#0B1E40]">
              {editingPreset ? 'Edit Benchmark Preset' : 'Add New Defect Benchmark'}
            </h3>

            <form onSubmit={handleSavePreset} className="space-y-3.5 text-xs">
              <div>
                <label className="block font-bold text-[#0B1E40] mb-1">Trade / Location Category</label>
                <select
                  value={presetCategory}
                  onChange={e => setPresetCategory(e.target.value)}
                  className="w-full p-3 rounded-2xl border border-[#E5E7EB] bg-white font-medium"
                >
                  {DAMAGE_AREAS.map(a => (
                    <option key={a} value={a}>
                      {a}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-bold text-[#0B1E40] mb-1">Defect Item Name</label>
                <input
                  type="text"
                  required
                  value={presetSubItem}
                  onChange={e => setPresetSubItem(e.target.value)}
                  placeholder="e.g., Cracked ceramic tile"
                  className="w-full p-3 rounded-2xl border border-[#E5E7EB]"
                />
              </div>

              <div className="grid grid-cols-3 gap-2.5">
                <div>
                  <label className="block font-bold text-[#0B1E40] mb-1">Severity</label>
                  <select
                    value={presetSeverity}
                    onChange={e => setPresetSeverity(e.target.value)}
                    className="w-full p-3 rounded-2xl border border-[#E5E7EB] bg-white font-medium"
                  >
                    <option value="Low">Low</option>
                    <option value="Medium">Medium</option>
                    <option value="High">High</option>
                  </select>
                </div>

                <div>
                  <label className="block font-bold text-[#0B1E40] mb-1">Labor (min)</label>
                  <input
                    type="number"
                    min="5"
                    max="840"
                    required
                    value={presetDuration}
                    onChange={e => setPresetDuration(e.target.value)}
                    className="w-full p-3 rounded-2xl border border-[#E5E7EB] font-black text-[#0194F3]"
                  />
                </div>

                <div>
                  <label className="block font-bold text-[#0B1E40] mb-1">Service</label>
                  <select
                    value={presetService}
                    onChange={e => setPresetService(e.target.value)}
                    className="w-full p-3 rounded-2xl border border-[#E5E7EB] bg-white font-medium"
                  >
                    {SERVICE_TYPES.map(s => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsPresetModalOpen(false)}
                  className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-2xl font-bold cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 bg-[#0194F3] hover:bg-[#0082D9] text-white rounded-2xl font-black shadow-md cursor-pointer"
                >
                  Save Preset
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* User Modal */}
      {isUserModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 sm:p-7 border border-slate-200 shadow-2xl space-y-4">
            <h3 className="text-base font-black text-[#0B1E40]">Add QC Personnel Account</h3>

            <form onSubmit={handleSaveUser} className="space-y-3.5 text-xs">
              <div>
                <label className="block font-bold text-[#0B1E40] mb-1">Full Name & Certification</label>
                <input
                  type="text"
                  required
                  value={userName}
                  onChange={e => setUserName(e.target.value)}
                  placeholder="e.g., Ryan Pratama, PE"
                  className="w-full p-3 rounded-2xl border border-[#E5E7EB]"
                />
              </div>

              <div>
                <label className="block font-bold text-[#0B1E40] mb-1">Email Address</label>
                <input
                  type="email"
                  required
                  value={userEmail}
                  onChange={e => setUserEmail(e.target.value)}
                  placeholder="ryan.qc@ifms.com"
                  className="w-full p-3 rounded-2xl border border-[#E5E7EB]"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-[#0B1E40] mb-1">Assigned Role</label>
                  <select
                    value={userRole}
                    onChange={e => setUserRole(e.target.value)}
                    className="w-full p-3 rounded-2xl border border-[#E5E7EB] bg-white font-medium"
                  >
                    <option value="QC Inspector">QC Inspector</option>
                    <option value="QC Supervisor">QC Supervisor</option>
                    <option value="Field Engineer">Field Engineer</option>
                    <option value="Project Manager">Project Manager</option>
                  </select>
                </div>

                <div>
                  <label className="block font-bold text-[#0B1E40] mb-1">Contact Phone</label>
                  <input
                    type="text"
                    value={userPhone}
                    onChange={e => setUserPhone(e.target.value)}
                    placeholder="+1 (555) 019-2834"
                    className="w-full p-3 rounded-2xl border border-[#E5E7EB]"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsUserModalOpen(false)}
                  className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-2xl font-bold cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 bg-[#0194F3] hover:bg-[#0082D9] text-white rounded-2xl font-black shadow-md cursor-pointer"
                >
                  Save Personnel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

