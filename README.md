# IFMS QC Inspection (Web Application)

Field Quality Control Inspection web application for property and building projects:
- **Phase 1: Location & Inspection Setup**: Location identification, building type presets, inspection service types (1 PM, 3 PM, 6 PM, Deep check, CM), inspector and supervisor assignments.
- **Phase 2: Finding Input & Defect Photos**: Defect category selection (Walls, Flooring, Roofing, Ceiling, Doors, Windows, Sanitary, Electrical, Mechanical, Landscaping, Others), defect description, damage dimensions, severity levels (Low, Medium, High), duration calculation with preset auto-fill, camera capture and photo upload.
- **840-Minute Daily Capacity Gauge**: Live visual progress bar monitoring total estimated repair duration against the standard 840-minute (14 hours) capacity threshold limit, with safety warnings and overload prevention.
- **Phase 3: Review & Verification**: Detailed findings cards with photos, quick edits, status updates, and inspection approval by supervisors.
- **Dashboard & Analytics**: Real-time KPI summaries, defect distribution by area, and severity level breakdowns.
- **Excel Spreadsheet Export**: Real-time A4 landscape live preview matching Microsoft Excel format, genuine `.xlsx` spreadsheet download, CSV export, and clipboard copy with supervisor sign-off blocks.
- **Master Defect Presets (Excel Data Box)**: Fast search, custom additions, CSV/JSON import & export, and reset to standard presets.
- **Project Archives**: Filter, search, and manage historical inspections.

## Built with
- React 18 + TypeScript + Vite
- Tailwind CSS
- SheetJS (xlsx) + JSZip
- Lucide React
