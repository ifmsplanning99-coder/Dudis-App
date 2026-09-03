import { MasterDurationEntity, BuildingEntity, UserEntity } from '../types';

export const INITIAL_BUILDINGS: BuildingEntity[] = [
  { id: 1, name: 'Residential Villa Type 36', type: 'House', defaultLocation: 'Sector A - Plot 12' },
  { id: 2, name: 'Residential Villa Type 45', type: 'House', defaultLocation: 'Sector B - Plot 05' },
  { id: 3, name: 'Worker Camp Facility A', type: 'Camp', defaultLocation: 'Camp Block 1' },
  { id: 4, name: 'Worker Camp Facility B', type: 'Camp', defaultLocation: 'Camp Block 2' },
  { id: 5, name: 'Clubhouse & Fitness Center', type: 'Public Facility', defaultLocation: 'Recreation Zone' },
  { id: 6, name: 'Medical Clinic & First Aid', type: 'Public Facility', defaultLocation: 'Health Center' },
  { id: 7, name: 'Main Site Project Office', type: 'Office', defaultLocation: 'HQ Building 2nd Floor' },
];

export const INITIAL_USERS: UserEntity[] = [
  { id: 1, name: 'Ryan Pratama, PE', email: 'ryan.qc@ifms.com', role: 'QC Inspector', phone: '+62 812-3456-7890' },
  { id: 2, name: 'Robert Sutrisno, PE', email: 'robert.sup@ifms.com', role: 'QC Supervisor', phone: '+62 813-9876-5432' },
  { id: 3, name: 'Ahmed Fauzi', email: 'ahmed.f@ifms.com', role: 'Field Engineer', phone: '+62 811-2233-4455' },
];

export const STANDARD_MASTER_PRESETS: Omit<MasterDurationEntity, 'id'>[] = [
  // Walls
  { category: 'Walls', subItem: 'Hairline plaster crack on living room wall', severityLevel: 'Low', defaultDurationMinutes: 30, serviceType: '1 PM' },
  { category: 'Walls', subItem: 'Peeling or bubbling wall paint finish', severityLevel: 'Medium', defaultDurationMinutes: 45, serviceType: '3 PM' },
  { category: 'Walls', subItem: 'Moisture dampness / active water seepage', severityLevel: 'High', defaultDurationMinutes: 120, serviceType: '6 PM' },
  { category: 'Walls', subItem: 'Severe structural through-wall crack', severityLevel: 'High', defaultDurationMinutes: 180, serviceType: 'CM' },
  { category: 'Walls', subItem: 'Surface mold and fungal growth on perimeter wall', severityLevel: 'Medium', defaultDurationMinutes: 60, serviceType: 'Deep check' },
  { category: 'Walls', subItem: 'Hollow plastering / drummy wall finish', severityLevel: 'Medium', defaultDurationMinutes: 90, serviceType: '3 PM' },

  // Flooring
  { category: 'Flooring', subItem: 'Missing or deteriorated floor tile grout', severityLevel: 'Low', defaultDurationMinutes: 30, serviceType: '1 PM' },
  { category: 'Flooring', subItem: 'Broken or chipped ceramic floor tile', severityLevel: 'Medium', defaultDurationMinutes: 60, serviceType: '3 PM' },
  { category: 'Flooring', subItem: 'Popping / hollow floor tiles uplifting', severityLevel: 'High', defaultDurationMinutes: 120, serviceType: 'CM' },
  { category: 'Flooring', subItem: 'Loose or damaged baseboard / skirting plinth', severityLevel: 'Low', defaultDurationMinutes: 40, serviceType: '1 PM' },
  { category: 'Flooring', subItem: 'Scratched or bubbled vinyl flooring strip', severityLevel: 'Medium', defaultDurationMinutes: 45, serviceType: 'Deep check' },

  // Roofing
  { category: 'Roofing', subItem: 'Displaced roof tiles / minor water infiltration', severityLevel: 'Medium', defaultDurationMinutes: 60, serviceType: '3 PM' },
  { category: 'Roofing', subItem: 'Clogged rainwater gutter / leaking joint', severityLevel: 'Medium', defaultDurationMinutes: 90, serviceType: '3 PM' },
  { category: 'Roofing', subItem: 'Sagging roof truss / heavy metal corrosion', severityLevel: 'High', defaultDurationMinutes: 240, serviceType: 'CM' },
  { category: 'Roofing', subItem: 'Loose roof sheet flashing / perimeter leak', severityLevel: 'High', defaultDurationMinutes: 90, serviceType: '6 PM' },

  // Ceiling
  { category: 'Ceiling', subItem: 'Water leak stains / discoloration on ceiling', severityLevel: 'Medium', defaultDurationMinutes: 45, serviceType: '3 PM' },
  { category: 'Ceiling', subItem: 'Sagging or compromised gypsum ceiling board', severityLevel: 'High', defaultDurationMinutes: 120, serviceType: 'CM' },
  { category: 'Ceiling', subItem: 'Cracked gypsum board joint compound', severityLevel: 'Low', defaultDurationMinutes: 30, serviceType: '1 PM' },
  { category: 'Ceiling', subItem: 'Flaking or blistering ceiling coat paint', severityLevel: 'Low', defaultDurationMinutes: 45, serviceType: '1 PM' },

  // Doors
  { category: 'Doors', subItem: 'Door leaf sticking / dragging against floor', severityLevel: 'Low', defaultDurationMinutes: 30, serviceType: '1 PM' },
  { category: 'Doors', subItem: 'Jammed or defective door handle / lockset', severityLevel: 'Medium', defaultDurationMinutes: 45, serviceType: '1 PM' },
  { category: 'Doors', subItem: 'Loose or squeaking door hinge screws', severityLevel: 'Low', defaultDurationMinutes: 20, serviceType: '1 PM' },
  { category: 'Doors', subItem: 'Termite infestation / decayed timber door frame', severityLevel: 'High', defaultDurationMinutes: 180, serviceType: 'CM' },
  { category: 'Doors', subItem: 'Warped or bowed solid timber door leaf', severityLevel: 'High', defaultDurationMinutes: 120, serviceType: 'CM' },

  // Windows
  { category: 'Windows', subItem: 'Cracked or broken window glass panel', severityLevel: 'High', defaultDurationMinutes: 90, serviceType: 'CM' },
  { category: 'Windows', subItem: 'Hardened or leaking window perimeter sealant', severityLevel: 'Medium', defaultDurationMinutes: 45, serviceType: '3 PM' },
  { category: 'Windows', subItem: 'Damaged window stay latch / friction hinge', severityLevel: 'Medium', defaultDurationMinutes: 40, serviceType: '1 PM' },
  { category: 'Windows', subItem: 'Loose aluminum window subframe / degraded rubber gasket', severityLevel: 'Medium', defaultDurationMinutes: 60, serviceType: 'Deep check' },

  // Sanitary
  { category: 'Sanitary', subItem: 'Leaking or dripping washbasin water tap', severityLevel: 'Low', defaultDurationMinutes: 25, serviceType: '1 PM' },
  { category: 'Sanitary', subItem: 'Leaking sink bottle trap / drain pipe connection', severityLevel: 'Medium', defaultDurationMinutes: 45, serviceType: '1 PM' },
  { category: 'Sanitary', subItem: 'Clogged floor waste drain pipe', severityLevel: 'Medium', defaultDurationMinutes: 60, serviceType: '3 PM' },
  { category: 'Sanitary', subItem: 'Toilet cistern tank leak / faulty flush valve', severityLevel: 'High', defaultDurationMinutes: 90, serviceType: '3 PM' },
  { category: 'Sanitary', subItem: 'Corroded floor drain grating / missing trap strainer', severityLevel: 'Low', defaultDurationMinutes: 20, serviceType: '1 PM' },
  { category: 'Sanitary', subItem: 'Burst or weeping clean water supply line', severityLevel: 'High', defaultDurationMinutes: 150, serviceType: 'CM' },

  // Electrical
  { category: 'Electrical', subItem: 'Inoperative or loose light toggle switch', severityLevel: 'Low', defaultDurationMinutes: 25, serviceType: '1 PM' },
  { category: 'Electrical', subItem: 'Loose power outlet socket / zero voltage', severityLevel: 'Medium', defaultDurationMinutes: 35, serviceType: '1 PM' },
  { category: 'Electrical', subItem: 'Flickering LED lamp fitting / burned socket', severityLevel: 'Low', defaultDurationMinutes: 20, serviceType: '1 PM' },
  { category: 'Electrical', subItem: 'Frequent breaker tripping (MCB) / circuit overload', severityLevel: 'High', defaultDurationMinutes: 90, serviceType: 'CM' },
  { category: 'Electrical', subItem: 'Exposed live wiring without protective conduit', severityLevel: 'High', defaultDurationMinutes: 60, serviceType: 'Deep check' },

  // Mechanical
  { category: 'Mechanical', subItem: 'AC indoor unit leaking condensate water', severityLevel: 'Medium', defaultDurationMinutes: 60, serviceType: '3 PM' },
  { category: 'Mechanical', subItem: 'AC low cooling efficiency / refrigerant loss', severityLevel: 'Medium', defaultDurationMinutes: 90, serviceType: '3 PM' },
  { category: 'Mechanical', subItem: 'Noisy ventilation exhaust fan / bearing failure', severityLevel: 'Low', defaultDurationMinutes: 45, serviceType: '1 PM' },
  { category: 'Mechanical', subItem: 'Booster water pump failure / motor humming', severityLevel: 'High', defaultDurationMinutes: 120, serviceType: 'CM' },

  // Landscaping
  { category: 'Landscaping', subItem: 'Sunken or undulating interlocking paving blocks', severityLevel: 'Medium', defaultDurationMinutes: 90, serviceType: '3 PM' },
  { category: 'Landscaping', subItem: 'External stormwater trench blocked by sediment', severityLevel: 'Medium', defaultDurationMinutes: 60, serviceType: '1 PM' },
  { category: 'Landscaping', subItem: 'Loose perimeter boundary fence / metal corrosion', severityLevel: 'Medium', defaultDurationMinutes: 60, serviceType: '6 PM' },

  // Others
  { category: 'Others', subItem: 'Post-construction debris & scrap disposal', severityLevel: 'Low', defaultDurationMinutes: 30, serviceType: '1 PM' },
  { category: 'Others', subItem: 'Installation of safety warning signage & barricade', severityLevel: 'Low', defaultDurationMinutes: 20, serviceType: '1 PM' },
];
