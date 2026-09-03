import React from 'react';
import { Timer, AlertTriangle, ShieldCheck, Zap } from 'lucide-react';
import { CAPACITY_LIMIT_MINUTES } from '../context/QcContext';

interface DurationCapacityBarProps {
  totalDurationMinutes: number;
  maxCapacityMinutes?: number;
  findingCount?: number;
  className?: string;
}

export const DurationCapacityBar: React.FC<DurationCapacityBarProps> = ({
  totalDurationMinutes,
  maxCapacityMinutes = CAPACITY_LIMIT_MINUTES,
  findingCount,
  className = ''
}) => {
  const percentage = Math.min(100, Math.round((totalDurationMinutes / maxCapacityMinutes) * 100));
  const isFull = totalDurationMinutes >= maxCapacityMinutes;
  const remainingMinutes = Math.max(0, maxCapacityMinutes - totalDurationMinutes);
  const totalHours = Math.floor(totalDurationMinutes / 60);
  const remainingHoursMins = totalDurationMinutes % 60;

  // Traveloka color steps
  let progressColor = 'bg-[#0194F3]';
  let badgeStyle = 'bg-[#F2F8FE] text-[#0194F3] border-[#BBE0FD]';
  let iconColor = 'text-[#0194F3]';

  if (totalDurationMinutes >= maxCapacityMinutes) {
    progressColor = 'bg-[#EF4444]';
    badgeStyle = 'bg-red-50 text-red-600 border-red-200';
    iconColor = 'text-red-500';
  } else if (totalDurationMinutes >= 600) {
    progressColor = 'bg-[#FF5E1F]';
    badgeStyle = 'bg-orange-50 text-[#FF5E1F] border-orange-200';
    iconColor = 'text-[#FF5E1F]';
  }

  return (
    <div id="duration-capacity-section" className={`w-full ${className}`}>
      <div className="bg-white rounded-2xl p-4.5 border border-[#E5E7EB] shadow-xs">
        {/* Header Info */}
        <div className="flex items-center justify-between gap-2 mb-3">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-[#F2F8FE] flex items-center justify-center">
              <Timer className={`w-4 h-4 ${iconColor}`} />
            </div>
            <div>
              <h3 className="text-xs sm:text-sm font-extrabold text-[#0B1E40]">
                Daily Inspection Work Capacity
              </h3>
              <p className="text-[11px] text-slate-400 font-medium">
                Standard 14-Hour (840 mins) Labor Quota Limit
              </p>
            </div>
          </div>

          <span className={`px-2.5 py-1 text-xs font-black rounded-full border ${badgeStyle}`}>
            {percentage}%
          </span>
        </div>

        {/* Traveloka-style Progress Bar */}
        <div className="w-full bg-slate-100 rounded-full h-3 overflow-hidden p-0.5 border border-slate-200/50">
          <div
            className={`h-full rounded-full transition-all duration-500 shadow-xs ${progressColor}`}
            style={{ width: `${Math.min(100, Math.max(0, percentage))}%` }}
          />
        </div>

        {/* Metrics Row */}
        <div className="flex flex-wrap items-center justify-between text-xs mt-3 gap-2">
          <div className="flex items-center gap-2">
            <span className="font-black text-[#0B1E40] text-sm font-mono">
              {totalDurationMinutes}
            </span>
            <span className="text-slate-400 font-medium">/ {maxCapacityMinutes} min</span>
            <span className="text-slate-500 font-bold bg-slate-100 px-2 py-0.5 rounded-md text-[11px]">
              {totalHours}h {remainingHoursMins}m logged
            </span>
          </div>

          {findingCount !== undefined ? (
            <span className="bg-[#F2F8FE] text-[#0194F3] font-extrabold px-2.5 py-1 rounded-xl border border-[#BBE0FD] text-xs">
              {findingCount} Logged Findings
            </span>
          ) : (
            <span
              className={`font-bold ${
                isFull ? 'text-red-600 font-black' : 'text-emerald-700'
              }`}
            >
              {isFull ? 'Quota Exhausted' : `Available Quota: ${remainingMinutes} min`}
            </span>
          )}
        </div>
      </div>

      {/* Warning banner when >= 840 mins */}
      {isFull && (
        <div
          id="capacity-limit-warning"
          className="mt-3 p-3.5 rounded-2xl bg-red-50 border border-red-200 flex items-start gap-3 text-red-700 text-xs shadow-xs animate-in fade-in"
        >
          <div className="w-8 h-8 rounded-xl bg-red-100 text-red-600 flex items-center justify-center shrink-0">
            <AlertTriangle className="w-4 h-4" />
          </div>
          <div>
            <p className="font-extrabold text-red-800">
              Maximum Labor Capacity Limit Reached (840 Minutes / 14 Hours)
            </p>
            <p className="text-red-700 font-medium mt-0.5 leading-relaxed">
              You have reached the maximum daily inspection capacity threshold. Please review logged items, finalize the report, or export before queuing additional repairs.
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

