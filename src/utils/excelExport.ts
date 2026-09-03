import * as XLSX from 'xlsx';
import { InspectionEntity, FindingEntity } from '../types';

export function generateCsvContent(inspection: InspectionEntity, findings: FindingEntity[]): string {
  const lines: string[] = [];

  // Header Metadata
  lines.push('IFMS QUALITY CONTROL INSPECTION REPORT');
  lines.push(`Address,${escapeCsv(inspection.address || inspection.locationName)}`);
  lines.push(`Address Number,${escapeCsv(inspection.addressNumber || '-')}`);
  lines.push(`Full Location / Unit,${escapeCsv(inspection.locationName)}`);
  lines.push(`Building Type,${escapeCsv(inspection.buildingType)}`);
  lines.push(`Service Type,${escapeCsv(inspection.serviceType)}`);
  lines.push(`Inspection Date,${escapeCsv(inspection.inspectionDate)}`);
  lines.push(`Inspector Name,${escapeCsv(inspection.inspectorName)}`);
  lines.push(`Supervisor Name,${escapeCsv(inspection.supervisorName || '-')}`);
  lines.push(`Inspection Status,${escapeCsv(inspection.status)}`);
  lines.push('');

  // Findings Table Header
  lines.push('No,Defect Area,Defect Description,Dimension / Boundary,Severity Level,Duration (Minutes),Status,Has Photo');

  // Findings Rows
  findings.forEach((finding, idx) => {
    lines.push(
      [
        idx + 1,
        escapeCsv(finding.damageArea),
        escapeCsv(finding.damageDescription),
        escapeCsv(finding.damageDimension || '-'),
        escapeCsv(finding.severityLevel),
        finding.durationMinutes,
        escapeCsv(finding.status),
        finding.photoUri ? 'Yes' : 'No'
      ].join(',')
    );
  });

  // Summary Row
  const totalDuration = findings.reduce((acc, f) => acc + f.durationMinutes, 0);
  const totalHours = Math.floor(totalDuration / 60);
  const remainingMins = totalDuration % 60;
  lines.push('');
  lines.push(`TOTAL FINDINGS,${findings.length} points`);
  lines.push(`TOTAL ESTIMATED REPAIR DURATION,${totalDuration} Minutes (${totalHours} hrs ${remainingMins} mins)`);
  lines.push(`CAPACITY LIMIT THRESHOLD,840 Minutes (${Math.min(100, Math.round((totalDuration / 840) * 100))}%)`);

  return lines.join('\n');
}

export function exportInspectionToExcel(inspection: InspectionEntity, findings: FindingEntity[]): void {
  const totalDuration = findings.reduce((acc, f) => acc + f.durationMinutes, 0);
  const totalHours = Math.floor(totalDuration / 60);
  const remainingMins = totalDuration % 60;

  // Build workbook data
  const data: (string | number)[][] = [
    ['IFMS PROPERTY INSPECTION - QUALITY CONTROL (QC) FINDINGS REPORT'],
    [],
    ['Inspection Date:', inspection.inspectionDate, '', 'Building Type:', inspection.buildingType],
    ['Inspector Name:', inspection.inspectorName, '', 'Service Type:', inspection.serviceType],
    ['Address:', inspection.address || inspection.locationName, '', 'Address Number:', inspection.addressNumber || '-'],
    ['Full Location / Unit:', inspection.locationName, '', 'Supervisor:', inspection.supervisorName || '-'],
    ['Inspection Status:', inspection.status, '', 'Total Points:', findings.length],
    [],
    ['No', 'Defect Area', 'Defect Description', 'Dimension / Boundary', 'Severity Level', 'Duration (Min)', 'Status', 'Photo Attached']
  ];

  findings.forEach((f, idx) => {
    data.push([
      idx + 1,
      f.damageArea,
      f.damageDescription,
      f.damageDimension || '-',
      f.severityLevel,
      f.durationMinutes,
      f.status,
      f.photoUri ? 'Yes' : 'No'
    ]);
  });

  data.push([]);
  data.push(['TOTAL FINDINGS:', `${findings.length} Points`, '', '', 'TOTAL DURATION:', `${totalDuration} Minutes`]);
  data.push(['ESTIMATED WORK HOURS:', `${totalHours} hrs ${remainingMins} mins`, '', '', 'CAPACITY UTILIZATION:', `${Math.min(100, Math.round((totalDuration / 840) * 100))}% of 840 mins`]);
  data.push([]);
  data.push(['PREPARED BY (QC INSPECTOR):', inspection.inspectorName, '', 'APPROVED BY (QC SUPERVISOR):', inspection.supervisorName || 'QC Supervisor']);

  // Create worksheet
  const ws = XLSX.utils.aoa_to_sheet(data);

  // Set column widths
  ws['!cols'] = [
    { wch: 6 },  // No
    { wch: 18 }, // Area
    { wch: 36 }, // Description
    { wch: 22 }, // Dimension
    { wch: 14 }, // Severity
    { wch: 16 }, // Duration
    { wch: 12 }, // Status
    { wch: 15 }  // Photo
  ];

  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'QC Inspection Report');

  const safeFilename = `QC_Report_${inspection.locationName.replace(/[^a-zA-Z0-9_-]/g, '_')}_${inspection.inspectionDate || 'draft'}.xlsx`;
  XLSX.writeFile(wb, safeFilename);
}

export function downloadCsv(filename: string, content: string): void {
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function escapeCsv(str: string): string {
  if (str.includes(',') || str.includes('"') || str.includes('\n')) {
    return `"${str.replace(/"/g, '""')}"`;
  }
  return str;
}
