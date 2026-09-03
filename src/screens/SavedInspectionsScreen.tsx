import React, { useState } from 'react';
import {
  Building2,
  Calendar,
  User,
  Plus,
  Search,
  Trash2,
  FileSpreadsheet,
  ArrowRight,
  ShieldCheck,
  Clock,
  Layers,
  Filter,
  ChevronRight
} from 'lucide-react';
import { useQc, CAPACITY_LIMIT_MINUTES } from '../context/QcContext';
import { InspectionEntity, ScreenTab } from '../types';

interface SavedInspectionsScreenProps {
  onNavigateTab: (tab: ScreenTab) => void;
}

export const SavedInspectionsScreen: React.FC<SavedInspectionsScreenProps> = ({
  onNavigateTab
}) => {
  const {
    allInspections,
    activeInspection,
    selectInspection,
    deleteInspection,
    startNewInspection
  } = useQc();

  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('All');
  const [filterStatus, setFilterStatus] = useState('All');
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  const filtered = allInspections.filter(i => {
    const q = searchQuery.toLowerCase().trim();
    const matchQuery =
      !q ||
      i.locationName.toLowerCase().includes(q) ||
      (i.address && i.address.toLowerCase().includes(q)) ||
      (i.addressNumber && i.addressNumber.toLowerCase().includes(q)) ||
      i.inspectorName.toLowerCase().includes(q) ||
      i.buildingType.toLowerCase().includes(q);

    const matchType = filterType === 'All' || i.buildingType === filterType;
    const matchStatus =
      filterStatus === 'All' ||
      i.status === filterStatus ||
      (filterStatus === 'Submitted' && i.status === 'Approved');

    return matchQuery && matchType && matchStatus;
  });

  const handleStartNew = () => {
    const dateStr = new Date().toISOString().split('T')[0];
    const newInsp = startNewInspection({
      locationName: 'New Project Unit No. 01',
      address: 'New Project Unit',
      addressNumber: '01',
      buildingType: 'House',
      serviceType: '1 PM',
      inspectionDate: dateStr,
      inspectorName: 'Ryan Pratama, PE',
      supervisorName: 'Bambang Sutrisno, Lead'
    });
    selectInspection(newInsp);
    onNavigateTab('location');
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-[#E5E7EB] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-black text-[#0B1E40]">
            Inspection Archives & Project Logs ({allInspections.length})
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Access past property audits, check supervisor approval status, and export sheets.
          </p>
        </div>

        <button
          onClick={handleStartNew}
          className="flex items-center gap-2 px-5 py-3 bg-[#FF5E1F] hover:bg-[#E04F16] active:scale-95 text-white rounded-2xl text-xs font-black transition-all shadow-lg shadow-orange-500/25 self-start sm:self-center cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>New Inspection</span>
        </button>
      </div>

      {/* Filter and Search Bar (Traveloka Search style) */}
      <div className="bg-white rounded-3xl p-4 sm:p-5 border border-[#E5E7EB] shadow-xs flex flex-col sm:flex-row items-center gap-3">
        <div className="relative w-full sm:flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-4 top-3.5" />
          <input
            type="text"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            placeholder="Search unit location, inspector name, or building type..."
            className="w-full pl-11 pr-4 py-2.5 text-xs sm:text-sm rounded-2xl border border-[#E5E7EB] bg-[#F7F9FA] focus:bg-white focus:outline-hidden focus:border-[#0194F3]"
          />
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto overflow-x-auto text-xs">
          <select
            value={filterType}
            onChange={e => setFilterType(e.target.value)}
            className="px-3.5 py-2.5 rounded-2xl border border-[#E5E7EB] bg-white font-bold text-[#0B1E40] cursor-pointer"
          >
            <option value="All">All Types</option>
            <option value="House">House</option>
            <option value="Camp">Camp</option>
            <option value="Public Facility">Public Facility</option>
            <option value="Office">Office</option>
            <option value="Warehouse">Warehouse</option>
          </select>

          <select
            value={filterStatus}
            onChange={e => setFilterStatus(e.target.value)}
            className="px-3.5 py-2.5 rounded-2xl border border-[#E5E7EB] bg-white font-bold text-[#0B1E40] cursor-pointer"
          >
            <option value="All">All Statuses</option>
            <option value="Draft">Draft</option>
            <option value="Submitted">Submitted</option>
          </select>
        </div>
      </div>

      {/* Inspections Cards List */}
      <div className="space-y-3.5">
        {filtered.length === 0 ? (
          <div className="bg-white rounded-3xl p-14 text-center border border-[#E5E7EB] shadow-xs">
            <p className="text-slate-400 text-sm font-medium">No property inspections match your search criteria.</p>
          </div>
        ) : (
          filtered.map(inspection => {
            const isActive = activeInspection?.id === inspection.id;
            const capacityPct = Math.min(
              100,
              Math.round((inspection.totalDurationMinutes / CAPACITY_LIMIT_MINUTES) * 100)
            );

            return (
              <div
                key={inspection.id}
                className={`bg-white rounded-3xl p-5 sm:p-6 border transition-all shadow-xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 ${
                  isActive
                    ? 'border-[#0194F3] ring-3 ring-blue-100 bg-[#F2F8FE]/30'
                    : 'border-[#E5E7EB] hover:border-slate-300'
                }`}
              >
                {/* Info Block */}
                <div className="space-y-2.5 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="px-3 py-0.5 rounded-full text-xs font-black bg-slate-100 text-slate-700">
                      {inspection.buildingType}
                    </span>
                    <span className="px-3 py-0.5 rounded-full text-xs font-black bg-[#F2F8FE] text-[#0194F3] border border-blue-200">
                      {inspection.serviceType}
                    </span>
                    <span
                      className={`px-3 py-0.5 rounded-full text-xs font-black ${
                        inspection.status === 'Submitted' || inspection.status === 'Approved'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-amber-100 text-amber-800'
                      }`}
                    >
                      {inspection.status === 'Approved' ? 'Submitted' : inspection.status}
                    </span>
                    {isActive && (
                      <span className="px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase bg-[#0194F3] text-white">
                        Active
                      </span>
                    )}
                  </div>

                  <h3 className="text-base sm:text-lg font-black text-[#0B1E40] leading-snug">
                    {inspection.locationName}
                  </h3>

                  <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400 font-semibold">
                    <div className="flex items-center gap-1.5">
                      <Calendar className="w-3.5 h-3.5 text-[#0194F3]" />
                      <span>{inspection.inspectionDate}</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <User className="w-3.5 h-3.5 text-[#0194F3]" />
                      <span>QC: {inspection.inspectorName}</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5 text-emerald-500" />
                      <span className="font-bold text-slate-700">
                        {inspection.totalDurationMinutes} mins ({capacityPct}% limit)
                      </span>
                    </div>
                  </div>
                </div>

                {/* Actions Block */}
                <div className="flex items-center gap-2 shrink-0 self-end sm:self-center">
                  <button
                    onClick={() => {
                      selectInspection(inspection);
                      onNavigateTab('review');
                    }}
                    className="flex items-center gap-1.5 px-4 py-2.5 bg-[#0194F3] hover:bg-[#0082D9] text-white rounded-2xl text-xs font-black transition-all shadow-sm shadow-blue-500/20 cursor-pointer"
                  >
                    <span>Open Audit</span>
                    <ChevronRight className="w-3.5 h-3.5" />
                  </button>

                  <button
                    onClick={() => {
                      selectInspection(inspection);
                      onNavigateTab('export');
                    }}
                    className="p-2.5 text-slate-500 hover:text-emerald-600 hover:bg-emerald-50 rounded-2xl transition-all cursor-pointer"
                    title="Export Excel"
                  >
                    <FileSpreadsheet className="w-4 h-4" />
                  </button>

                  <button
                    onClick={() => setDeleteConfirmId(inspection.id)}
                    className="p-2.5 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-2xl transition-all cursor-pointer"
                    title="Delete Inspection"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Delete Confirmation Modal */}
      {deleteConfirmId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-3xl p-6 sm:p-7 max-w-sm w-full border border-slate-200 shadow-2xl space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-red-100 text-red-600 flex items-center justify-center">
              <Trash2 className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-black text-[#0B1E40]">Delete Inspection Archive?</h3>
              <p className="text-xs text-slate-500 mt-1">
                All defect finding items, photos, and export records for this project unit will be permanently deleted.
              </p>
            </div>
            <div className="flex items-center justify-end gap-2.5 pt-2">
              <button
                onClick={() => setDeleteConfirmId(null)}
                className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-2xl text-xs font-black transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  deleteInspection(deleteConfirmId);
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
    </div>
  );
};

