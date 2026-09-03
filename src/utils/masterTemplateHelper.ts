import { MasterDurationEntity } from '../types';

export function parseMasterDurations(content: string): Omit<MasterDurationEntity, 'id'>[] {
  const trimmed = content.trim();
  if (!trimmed) return [];

  // 1. Try JSON
  if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
    try {
      const parsed = JSON.parse(trimmed);
      if (Array.isArray(parsed)) {
        return parsed
          .filter((item: Record<string, unknown>) => item && (item.category || item.area) && (item.subItem || item.defect || item.name))
          .map((item: Record<string, unknown>) => ({
            category: String(item.category || item.area || 'General'),
            subItem: String(item.subItem || item.defect || item.name || 'Defect'),
            severityLevel: String(item.severityLevel || item.severity || 'Medium'),
            defaultDurationMinutes: Number(item.defaultDurationMinutes || item.durationMinutes || item.duration || 45),
            serviceType: String(item.serviceType || item.service || 'All')
          }));
      }
    } catch {
      // Not valid JSON, fallback to CSV / TSV
    }
  }

  // 2. Try CSV or TSV (tab-separated from Excel)
  const lines = trimmed.split(/\r?\n/).filter(line => line.trim().length > 0);
  const results: Omit<MasterDurationEntity, 'id'>[] = [];

  for (let i = 0; i < lines.length; i++) {
    const rawLine = lines[i];
    // Check if line is header
    if (i === 0 && (rawLine.toLowerCase().includes('category') || rawLine.toLowerCase().includes('defect') || rawLine.toLowerCase().includes('area'))) {
      continue;
    }

    const delimiter = rawLine.includes('\t') ? '\t' : ',';
    const parts = splitCsvLine(rawLine, delimiter);
    if (parts.length >= 2) {
      const cat = parts[0].trim();
      const sub = parts[1].trim();
      const sev = parts.length > 2 && parts[2].trim() ? parts[2].trim() : 'Medium';
      const dur = parts.length > 3 ? (parseInt(parts[3].trim(), 10) || 45) : 45;
      const srv = parts.length > 4 && parts[4].trim() ? parts[4].trim() : 'All';

      if (cat && sub) {
        results.push({
          category: cat,
          subItem: sub,
          severityLevel: normalizeSeverity(sev),
          defaultDurationMinutes: dur,
          serviceType: srv
        });
      }
    }
  }

  return results;
}

export function exportMasterToCsv(presets: MasterDurationEntity[]): string {
  const header = 'Category,Defect Item,Severity,Duration (Minutes),Service Type\n';
  const rows = presets.map(p =>
    `"${p.category.replace(/"/g, '""')}","${p.subItem.replace(/"/g, '""')}","${p.severityLevel}",${p.defaultDurationMinutes},"${p.serviceType}"`
  ).join('\n');
  return header + rows;
}

export function exportMasterToJson(presets: MasterDurationEntity[]): string {
  return JSON.stringify(presets, null, 2);
}

function normalizeSeverity(sev: string): string {
  const lower = sev.toLowerCase();
  if (lower.includes('high') || lower.includes('berat') || lower.includes('tinggi')) return 'High';
  if (lower.includes('low') || lower.includes('ringan') || lower.includes('rendah')) return 'Low';
  return 'Medium';
}

function splitCsvLine(line: string, delimiter: string): string[] {
  const result: string[] = [];
  let current = '';
  let inQuotes = false;

  for (let i = 0; i < line.length; i++) {
    const char = line[i];
    if (char === '"') {
      inQuotes = !inQuotes;
    } else if (char === delimiter && !inQuotes) {
      result.push(current);
      current = '';
    } else {
      current += char;
    }
  }
  result.push(current);
  return result;
}
