import React from 'react';
import {
  Building2,
  Calendar,
  FileSpreadsheet,
  Plus,
  ShieldCheck,
  Clock,
  ChevronRight
} from 'lucide-react';
import { useQc } from '../context/QcContext';
import { ScreenTab } from '../types';

interface AppTopBarProps {
  currentTab: ScreenTab;
  onNavigateTab: (tab: ScreenTab) => void;
}

export const AppTopBar: React.FC<AppTopBarProps> = ({ currentTab, onNavigateTab }) => {
  const {
    activeInspection,
    activeFindings,
    activeTotalDuration,
    startNewInspection,
    selectInspection
  } = useQc();

  const handleStartNewInspection = () => {
    const today = new Date().toISOString().split('T')[0];
    const newInsp = startNewInspection({
      locationName: 'New Project Unit No. 01',
      address: 'New Project Unit',
      addressNumber: '01',
      buildingType: 'House',
      serviceType: '1 PM',
      inspectionDate: today,
      inspectorName: 'Ryan Pratama, PE',
      supervisorName: 'Robert Sutrisno, PE'
    });
    selectInspection(newInsp);
    onNavigateTab('location');
  };

  const capacityPct = Math.min(100, Math.round((activeTotalDuration / 840) * 100));

  return (
    <header className="bg-[#0194F3] text-white sticky top-0 z-40 shadow-md">
      {/* Primary Brand Bar */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 gap-3">
          {/* Brand Logo & Tagline */}
          <div
            onClick={() => onNavigateTab('location')}
            className="flex items-center gap-3 cursor-pointer select-none group shrink-0"
          >
            {/* IFMS QC Brand Emblem */}
            <div className="w-10 h-10 rounded-2xl bg-white/15 backdrop-blur-xs flex items-center justify-center text-white border border-white/25 shadow-inner transition-transform group-hover:scale-105">
              <ShieldCheck className="w-6 h-6 text-white" />
            </div>

            <div>
              <div className="flex items-center gap-2">
                <span className="font-black text-lg sm:text-xl tracking-tight text-white flex items-center gap-1.5">
                  IFMS <span className="font-light text-white/90">QC</span>
                </span>
              </div>
              <p className="text-[11px] text-white/80 font-medium hidden sm:block">
                Field Inspection & Quality Control
              </p>
            </div>
          </div>

          {/* Center: Active Inspection Ticket Pill (Desktop) */}
          {activeInspection && (
            <div
              onClick={() => onNavigateTab('review')}
              className="hidden lg:flex items-center gap-3 bg-white/15 hover:bg-white/25 border border-white/25 px-3.5 py-1.5 rounded-full text-xs cursor-pointer transition-all max-w-md shadow-xs"
              title="Click to view inspection summary and findings"
            >
              <div className="flex items-center gap-1.5 text-white truncate">
                <Building2 className="w-3.5 h-3.5 text-white/90 shrink-0" />
                <span className="font-bold truncate max-w-[150px]">
                  {activeInspection.locationName}
                </span>
              </div>

              <span className="text-white/40">•</span>

              <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-white/20 text-white">
                {activeInspection.serviceType}
              </span>

              <span className="text-white/40">•</span>

              <div className="flex items-center gap-1 text-white/90">
                <Clock className="w-3 h-3 text-white/80" />
                <span className="font-bold">{activeFindings.length} pts</span>
                <span className="text-[11px] text-white/80">({capacityPct}%)</span>
              </div>

              <span
                className={`px-2 py-0.5 rounded-full text-[10px] font-black ${
                  activeInspection.status === 'Submitted' || activeInspection.status === 'Approved'
                    ? 'bg-emerald-400 text-slate-900'
                    : 'bg-amber-300 text-slate-900'
                }`}
              >
                {activeInspection.status === 'Approved' ? 'Submitted' : activeInspection.status}
              </span>
            </div>
          )}

          {/* Right Controls: Action Button & Quick Links */}
          <div className="flex items-center gap-2">
            {/* Start New Inspection */}
            <button
              id="topbar-btn-new-inspection"
              onClick={handleStartNewInspection}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-[#FF5E1F] hover:bg-[#E04F16] active:scale-95 text-white rounded-xl text-xs font-extrabold transition-all shadow-md cursor-pointer shrink-0"
            >
              <Plus className="w-4 h-4" />
              <span className="hidden sm:inline">New Inspection</span>
            </button>

            {/* Quick Excel Export */}
            <button
              id="topbar-btn-export"
              onClick={() => onNavigateTab('export')}
              className={`flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                currentTab === 'export'
                  ? 'bg-white text-[#0194F3] shadow-md'
                  : 'bg-white/15 hover:bg-white/25 text-white border border-white/25'
              }`}
              title="Download Excel Report"
            >
              <FileSpreadsheet className="w-4 h-4" />
              <span className="hidden md:inline">Excel</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

