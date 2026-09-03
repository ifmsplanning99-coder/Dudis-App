export interface BuildingEntity {
  id: number;
  name: string;
  type: string;
  defaultLocation: string;
}

export interface InspectionEntity {
  id: number;
  locationName: string;
  address?: string;
  addressNumber?: string;
  buildingType: string;
  serviceType: string;
  inspectionDate: string;
  inspectorName: string;
  supervisorName: string;
  totalDurationMinutes: number;
  status: string; // "Draft" | "Submitted"
  notes: string;
}

export interface FindingEntity {
  id: number;
  inspectionId: number;
  damageArea: string;
  damageDescription: string;
  damageDimension: string;
  severityLevel: string; // "Low" | "Medium" | "High"
  durationMinutes: number;
  photoUri: string | null; // data URL or path
  status: string; // "Open" | "Fixed" | "Verified"
  createdAt: number;
}

export interface MasterDurationEntity {
  id: number;
  category: string;
  subItem: string;
  severityLevel: string;
  defaultDurationMinutes: number;
  serviceType: string;
}

export interface UserEntity {
  id: number;
  name: string;
  email: string;
  role: string;
  phone: string;
}

export type ScreenTab =
  | 'location'
  | 'finding'
  | 'review'
  | 'export'
  | 'history'
  | 'master';
