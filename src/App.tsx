import React, { useState } from 'react';
import {
  MapPin,
  ClipboardPen,
  CheckCircle2,
  FileSpreadsheet,
  History,
  Database,
  Calendar,
  User,
  Clock,
  ArrowRight,
  ShieldCheck,
  ChevronRight
} from 'lucide-react';
import { QcProvider, useQc } from './context/QcContext';
import { AppTopBar } from './components/AppTopBar';
import { Phase1LocationScreen } from './screens/Phase1LocationScreen';
import { Phase2FindingInputScreen } from './screens/Phase2FindingInputScreen';
import { ReviewFindingsScreen } from './screens/ReviewFindingsScreen';
import { ExportReportScreen } from './screens/ExportReportScreen';
import { SavedInspectionsScreen } from './screens/SavedInspectionsScreen';
import { MasterDataScreen } from './screens/MasterDataScreen';
import { ScreenTab } from './types';

interface NavItemConfig {
  id: ScreenTab;
  label: string;
  subLabel: string;
  icon: React.ElementType;
  iconBg: string;
  iconColor: string;
}

const NAVIGATION_ITEMS: NavItemConfig[] = [
  {
    id: 'location',
    label: '1. Location Setup',
    subLabel: 'Project Zone',
    icon: MapPin,
    iconBg: 'bg-sky-500/10 text-[#0194F3]',
    iconColor: 'text-[#0194F3]'
  },
  {
    id: 'finding',
    label: '2. Log Findings',
    subLabel: 'Defects & Photo',
    icon: ClipboardPen,
    iconBg: 'bg-orange-500/10 text-[#FF5E1F]',
    iconColor: 'text-[#FF5E1F]'
  },
  {
    id: 'review',
    label: '3. Review & Verify',
    subLabel: 'Supervisor Sign',
    icon: CheckCircle2,
    iconBg: 'bg-emerald-500/10 text-[#00A651]',
    iconColor: 'text-[#00A651]'
  },
  {
    id: 'export',
    label: 'Excel Worksheet',
    subLabel: 'Download .xlsx',
    icon: FileSpreadsheet,
    iconBg: 'bg-teal-500/10 text-teal-600',
    iconColor: 'text-teal-600'
  },
  {
    id: 'history',
    label: 'History Archive',
    subLabel: 'All Records',
    icon: History,
    iconBg: 'bg-violet-500/10 text-violet-600',
    iconColor: 'text-violet-600'
  },
  {
    id: 'master',
    label: 'Master Database',
    subLabel: 'Presets & Team',
    icon: Database,
    iconBg: 'bg-amber-500/10 text-amber-600',
    iconColor: 'text-amber-600'
  }
];

const MainAppContent: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<ScreenTab>('location');
  const { activeInspection, activeFindings, activeTotalDuration } = useQc();

  const capacityPct = Math.min(100, Math.round((activeTotalDuration / 840) * 100));

  return (
    <div className="min-h-screen flex flex-col bg-[#F7F9FA] text-[#0B1E40] pb-24 sm:pb-12 print:p-0 print:pb-0 print:bg-white">
      {/* Top Application Header */}
      <div className="print:hidden">
        <AppTopBar currentTab={currentTab} onNavigateTab={setCurrentTab} />
      </div>

      {/* Services Sub-Navigation Bar */}
      <nav className="bg-white border-b border-[#E5E7EB] sticky top-16 z-30 shadow-xs print:hidden">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-1 sm:gap-3 overflow-x-auto py-3 no-scrollbar scroll-smooth">
            {NAVIGATION_ITEMS.map(item => {
              const Icon = item.icon;
              const isActive = currentTab === item.id;

              return (
                <button
                  key={item.id}
                  id={`nav-tab-${item.id}`}
                  onClick={() => setCurrentTab(item.id)}
                  className={`flex items-center gap-2.5 px-3 sm:px-4 py-2 rounded-2xl text-xs font-bold transition-all whitespace-nowrap cursor-pointer shrink-0 border ${
                    isActive
                      ? 'bg-[#F2F8FE] text-[#0194F3] border-[#BBE0FD] shadow-xs'
                      : 'bg-white text-slate-600 hover:text-slate-900 hover:bg-slate-50 border-transparent'
                  }`}
                >
                  <div
                    className={`w-7 h-7 rounded-xl flex items-center justify-center transition-colors ${
                      isActive ? 'bg-[#0194F3] text-white' : item.iconBg
                    }`}
                  >
                    <Icon className="w-3.5 h-3.5 shrink-0" />
                  </div>

                  <div className="text-left leading-tight">
                    <span className="block font-extrabold text-xs">{item.label}</span>
                    <span
                      className={`block text-[10px] font-medium ${
                        isActive ? 'text-[#0194F3]/80' : 'text-slate-400'
                      }`}
                    >
                      {item.subLabel}
                    </span>
                  </div>

                  {item.id === 'review' && activeFindings.length > 0 && (
                    <span
                      className={`ml-1 px-1.5 py-0.5 rounded-full text-[10px] font-black ${
                        isActive
                          ? 'bg-[#0194F3] text-white'
                          : 'bg-orange-100 text-[#FF5E1F]'
                      }`}
                    >
                      {activeFindings.length}
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </div>
      </nav>

      {/* Inspection Quick Status Card */}
      {activeInspection && (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-5 print:hidden">
          <div className="bg-white rounded-2xl p-4 border border-[#E5E7EB] shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-[#F2F8FE] text-[#0194F3] flex items-center justify-center font-bold">
                <MapPin className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold uppercase tracking-wider text-[#0194F3]">
                    Active Project Zone
                  </span>
                  <span
                    className={`px-2 py-0.5 rounded-full text-[10px] font-extrabold ${
                      activeInspection.status === 'Submitted' || activeInspection.status === 'Approved'
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-amber-100 text-amber-800'
                    }`}
                  >
                    {activeInspection.status === 'Approved' ? 'Submitted' : activeInspection.status}
                  </span>
                </div>
                <h2 className="text-sm sm:text-base font-extrabold text-[#0B1E40] mt-0.5">
                  {activeInspection.locationName}
                </h2>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-4 text-xs">
              <div className="flex items-center gap-1.5 text-slate-600">
                <Calendar className="w-3.5 h-3.5 text-slate-400" />
                <span>{activeInspection.inspectionDate}</span>
              </div>
              <div className="flex items-center gap-1.5 text-slate-600">
                <User className="w-3.5 h-3.5 text-slate-400" />
                <span>Inspector: <strong className="text-slate-800">{activeInspection.inspectorName}</strong></span>
              </div>
              <div className="flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-[#0194F3]" />
                <span className="font-extrabold text-[#0B1E40]">
                  {activeTotalDuration} / 840 min ({capacityPct}%)
                </span>
              </div>
              <button
                onClick={() => setCurrentTab('review')}
                className="px-3 py-1.5 rounded-xl text-xs font-bold text-[#0194F3] bg-[#F2F8FE] hover:bg-[#E2F0FE] transition-colors flex items-center gap-1 cursor-pointer"
              >
                <span>View Findings ({activeFindings.length})</span>
                <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Main Screen Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 print:p-0 print:m-0 print:max-w-none">
        {currentTab === 'location' && (
          <Phase1LocationScreen onNavigateTab={setCurrentTab} />
        )}
        {currentTab === 'finding' && (
          <Phase2FindingInputScreen onNavigateTab={setCurrentTab} />
        )}
        {currentTab === 'review' && (
          <ReviewFindingsScreen onNavigateTab={setCurrentTab} />
        )}
        {currentTab === 'export' && (
          <ExportReportScreen onNavigateTab={setCurrentTab} />
        )}
        {currentTab === 'history' && (
          <SavedInspectionsScreen onNavigateTab={setCurrentTab} />
        )}
        {currentTab === 'master' && (
          <MasterDataScreen onNavigateTab={setCurrentTab} />
        )}
      </main>

      {/* Mobile Bottom Navigation Bar */}
      <div className="sm:hidden fixed bottom-0 left-0 right-0 z-40 bg-white border-t border-[#E5E7EB] px-2 py-2 flex items-center justify-around shadow-2xl print:hidden">
        {NAVIGATION_ITEMS.map(item => {
          const Icon = item.icon;
          const isActive = currentTab === item.id;

          return (
            <button
              key={item.id}
              onClick={() => setCurrentTab(item.id)}
              className={`flex flex-col items-center justify-center p-1 rounded-xl text-[10px] font-bold transition-all relative ${
                isActive ? 'text-[#0194F3]' : 'text-slate-400 hover:text-slate-700'
              }`}
            >
              <div
                className={`p-1 rounded-xl mb-0.5 ${
                  isActive ? 'bg-[#F2F8FE]' : ''
                }`}
              >
                <Icon className="w-5 h-5" />
              </div>
              <span className="leading-none">{item.label.split(' ')[1] || item.label}</span>
              {item.id === 'review' && activeFindings.length > 0 && (
                <span className="absolute -top-1 right-2 w-4 h-4 bg-[#FF5E1F] text-white text-[9px] rounded-full flex items-center justify-center font-black">
                  {activeFindings.length}
                </span>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
};

export function App() {
  return (
    <QcProvider>
      <MainAppContent />
    </QcProvider>
  );
}

export default App;

