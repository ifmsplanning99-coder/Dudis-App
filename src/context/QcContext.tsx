import React, { createContext, useContext, useState, useEffect } from 'react';
import { InspectionEntity, FindingEntity, MasterDurationEntity, BuildingEntity, UserEntity } from '../types';
import { STANDARD_MASTER_PRESETS, INITIAL_BUILDINGS, INITIAL_USERS } from '../utils/masterPresets';

interface QcContextType {
  activeInspection: InspectionEntity | null;
  allInspections: InspectionEntity[];
  activeFindings: FindingEntity[];
  activeTotalDuration: number;
  isCapacityReached: boolean;
  allMasterDurations: MasterDurationEntity[];
  allBuildings: BuildingEntity[];
  allUsers: UserEntity[];

  // Actions
  selectInspection: (inspection: InspectionEntity) => void;
  startNewInspection: (data: {
    locationName: string;
    address?: string;
    addressNumber?: string;
    buildingType: string;
    serviceType: string;
    inspectionDate: string;
    inspectorName: string;
    supervisorName: string;
  }) => InspectionEntity;
  updateActiveInspection: (data: Partial<InspectionEntity>) => void;
  deleteInspection: (id: number) => void;
  submitActiveInspection: () => void;
  approveActiveInspection: () => void;

  // Finding Actions
  addFinding: (finding: {
    damageArea: string;
    damageDescription: string;
    damageDimension: string;
    severityLevel: string;
    durationMinutes: number;
    photoUri: string | null;
  }) => boolean;
  updateFinding: (finding: FindingEntity) => void;
  deleteFinding: (id: number) => void;

  // Master Preset Actions
  addMasterDuration: (preset: Omit<MasterDurationEntity, 'id'>) => void;
  updateMasterDuration: (preset: MasterDurationEntity) => void;
  deleteMasterDuration: (id: number) => void;
  importMasterDurations: (presets: Omit<MasterDurationEntity, 'id'>[], replaceExisting: boolean) => number;
  resetToStandardMasterPresets: () => void;

  // User Actions
  addUser: (user: Omit<UserEntity, 'id'>) => void;
}

const QcContext = createContext<QcContextType | null>(null);

const STORAGE_KEY_INSPECTIONS = 'ifms_qc_inspections_v2';
const STORAGE_KEY_FINDINGS = 'ifms_qc_findings_v2';
const STORAGE_KEY_PRESETS = 'ifms_qc_master_presets_v2';
const STORAGE_KEY_USERS = 'ifms_qc_users_v2';
const STORAGE_KEY_ACTIVE_ID = 'ifms_qc_active_id_v2';

export const CAPACITY_LIMIT_MINUTES = 840; // 14 Hours work capacity limit

export const QcProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // 1. Initialize Master Presets
  const [allMasterDurations, setAllMasterDurations] = useState<MasterDurationEntity[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEY_PRESETS);
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // ignore
      }
    }
    return STANDARD_MASTER_PRESETS.map((p, idx) => ({ ...p, id: idx + 1 }));
  });

  // 2. Initialize Users
  const [allUsers, setAllUsers] = useState<UserEntity[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEY_USERS);
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // ignore
      }
    }
    return INITIAL_USERS;
  });

  // 3. Buildings
  const [allBuildings] = useState<BuildingEntity[]>(INITIAL_BUILDINGS);

  // 4. Initialize Inspections with realistic English samples
  const [allInspections, setAllInspections] = useState<InspectionEntity[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEY_INSPECTIONS);
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // ignore
      }
    }
    return [
      {
        id: 101,
        locationName: 'Orchid Cluster No. 12',
        address: 'Orchid Cluster',
        addressNumber: '12',
        buildingType: 'House',
        serviceType: '1 PM',
        inspectionDate: new Date().toISOString().split('T')[0],
        inspectorName: 'Ryan Pratama, PE',
        supervisorName: 'Robert Sutrisno, PE',
        totalDurationMinutes: 195,
        status: 'Draft',
        notes: 'Routine periodic inspection of interior partition walls, sanitary fixtures, and ceiling plasterboard.'
      },
      {
        id: 102,
        locationName: 'Worker Camp Facility A Barracks 04',
        address: 'Worker Camp Facility A',
        addressNumber: 'Barracks 04',
        buildingType: 'Camp',
        serviceType: '3 PM',
        inspectionDate: new Date(Date.now() - 86400000 * 3).toISOString().split('T')[0],
        inspectorName: 'Ahmed Fauzi',
        supervisorName: 'Robert Sutrisno, PE',
        totalDurationMinutes: 320,
        status: 'Submitted',
        notes: 'Quarterly plumbing, sanitary, and roof drainage verification. Signed off by supervisor.'
      }
    ];
  });

  // 5. Initialize Findings with realistic English items
  const [allFindings, setAllFindings] = useState<FindingEntity[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEY_FINDINGS);
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // ignore
      }
    }
    return [
      {
        id: 201,
        inspectionId: 101,
        damageArea: 'Walls',
        damageDescription: 'Hairline plaster crack on living room partition wall',
        damageDimension: 'Length: 1.2 m, Width: < 0.5 mm',
        severityLevel: 'Low',
        durationMinutes: 30,
        photoUri: null,
        status: 'Open',
        createdAt: Date.now() - 3600000 * 2
      },
      {
        id: 202,
        inspectionId: 101,
        damageArea: 'Ceiling',
        damageDescription: 'Water leak stain and minor moisture discoloration on master bedroom gypsum board',
        damageDimension: '40 cm x 50 cm perimeter area',
        severityLevel: 'Medium',
        durationMinutes: 45,
        photoUri: null,
        status: 'Open',
        createdAt: Date.now() - 3600000
      },
      {
        id: 203,
        inspectionId: 101,
        damageArea: 'Sanitary',
        damageDescription: 'Toilet cistern base seal dripping water continuously',
        damageDimension: '1 slow drip point at bottom seal gasket',
        severityLevel: 'High',
        durationMinutes: 90,
        photoUri: null,
        status: 'Open',
        createdAt: Date.now() - 1800000
      },
      {
        id: 204,
        inspectionId: 101,
        damageArea: 'Doors',
        damageDescription: 'Door leaf dragging against floor tiles with squeaking bottom hinge',
        damageDimension: 'Lower hinge rubbing, 2 mm clearance gap',
        severityLevel: 'Low',
        durationMinutes: 30,
        photoUri: null,
        status: 'Open',
        createdAt: Date.now() - 900000
      }
    ];
  });

  // 6. Active inspection ID
  const [activeInspectionId, setActiveInspectionId] = useState<number>(() => {
    const saved = localStorage.getItem(STORAGE_KEY_ACTIVE_ID);
    if (saved) {
      const parsed = parseInt(saved, 10);
      if (!isNaN(parsed)) return parsed;
    }
    return 101;
  });

  // Synchronize localStorage
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY_PRESETS, JSON.stringify(allMasterDurations));
  }, [allMasterDurations]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY_USERS, JSON.stringify(allUsers));
  }, [allUsers]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY_INSPECTIONS, JSON.stringify(allInspections));
  }, [allInspections]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY_FINDINGS, JSON.stringify(allFindings));
  }, [allFindings]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY_ACTIVE_ID, activeInspectionId.toString());
  }, [activeInspectionId]);

  // Derived active state
  const activeInspection = allInspections.find(i => i.id === activeInspectionId) || allInspections[0] || null;
  const activeFindings = allFindings.filter(f => f.inspectionId === (activeInspection?.id ?? -1));
  const activeTotalDuration = activeFindings.reduce((sum, f) => sum + f.durationMinutes, 0);
  const isCapacityReached = activeTotalDuration >= CAPACITY_LIMIT_MINUTES;

  // Keep inspection total duration updated
  useEffect(() => {
    if (activeInspection && activeInspection.totalDurationMinutes !== activeTotalDuration) {
      setAllInspections(prev =>
        prev.map(i => i.id === activeInspection.id ? { ...i, totalDurationMinutes: activeTotalDuration } : i)
      );
    }
  }, [activeTotalDuration, activeInspection?.id]);

  // Actions
  const selectInspection = (inspection: InspectionEntity) => {
    setActiveInspectionId(inspection.id);
  };

  const startNewInspection = (data: {
    locationName: string;
    address?: string;
    addressNumber?: string;
    buildingType: string;
    serviceType: string;
    inspectionDate: string;
    inspectorName: string;
    supervisorName: string;
  }): InspectionEntity => {
    const newId = Date.now();
    const newInsp: InspectionEntity = {
      id: newId,
      locationName: data.locationName,
      address: data.address,
      addressNumber: data.addressNumber,
      buildingType: data.buildingType,
      serviceType: data.serviceType,
      inspectionDate: data.inspectionDate,
      inspectorName: data.inspectorName,
      supervisorName: data.supervisorName,
      totalDurationMinutes: 0,
      status: 'Draft',
      notes: ''
    };
    setAllInspections(prev => [newInsp, ...prev]);
    setActiveInspectionId(newId);
    return newInsp;
  };

  const updateActiveInspection = (data: Partial<InspectionEntity>) => {
    if (!activeInspection) return;
    // Disallow editing content if already submitted, unless updating status
    const isSubmitted = activeInspection.status === 'Submitted' || activeInspection.status === 'Approved';
    if (isSubmitted && !('status' in data)) {
      console.warn('Inspection is submitted and locked against editing.');
      return;
    }
    setAllInspections(prev =>
      prev.map(i => i.id === activeInspection.id ? { ...i, ...data } : i)
    );
  };

  const deleteInspection = (id: number) => {
    setAllInspections(prev => prev.filter(i => i.id !== id));
    setAllFindings(prev => prev.filter(f => f.inspectionId !== id));
    if (activeInspectionId === id) {
      const remaining = allInspections.filter(i => i.id !== id);
      if (remaining.length > 0) {
        setActiveInspectionId(remaining[0].id);
      }
    }
  };

  const submitActiveInspection = () => {
    if (!activeInspection) return;
    setAllInspections(prev =>
      prev.map(i => i.id === activeInspection.id ? { ...i, status: 'Submitted' } : i)
    );
  };

  const approveActiveInspection = () => {
    submitActiveInspection();
  };

  // Finding Actions
  const addFinding = (finding: {
    damageArea: string;
    damageDescription: string;
    damageDimension: string;
    severityLevel: string;
    durationMinutes: number;
    photoUri: string | null;
  }): boolean => {
    if (!activeInspection) return false;
    // Hard block if inspection is submitted / finalized
    if (activeInspection.status === 'Submitted' || activeInspection.status === 'Approved') {
      console.warn('Cannot add defect: Inspection is submitted and locked.');
      return false;
    }

    if (activeTotalDuration + finding.durationMinutes > CAPACITY_LIMIT_MINUTES && activeTotalDuration >= CAPACITY_LIMIT_MINUTES) {
      return false; // Hard block when 840 min capacity is reached
    }

    const newFinding: FindingEntity = {
      id: Date.now(),
      inspectionId: activeInspection.id,
      damageArea: finding.damageArea,
      damageDescription: finding.damageDescription,
      damageDimension: finding.damageDimension,
      severityLevel: finding.severityLevel,
      durationMinutes: finding.durationMinutes,
      photoUri: finding.photoUri,
      status: 'Open',
      createdAt: Date.now()
    };

    setAllFindings(prev => [...prev, newFinding]);
    return true;
  };

  const updateFinding = (updated: FindingEntity) => {
    const parent = allInspections.find(i => i.id === updated.inspectionId);
    if (parent && (parent.status === 'Submitted' || parent.status === 'Approved')) {
      console.warn('Cannot edit finding: Inspection is submitted and locked.');
      return;
    }
    setAllFindings(prev => prev.map(f => f.id === updated.id ? updated : f));
  };

  const deleteFinding = (id: number) => {
    const target = allFindings.find(f => f.id === id);
    if (target) {
      const parent = allInspections.find(i => i.id === target.inspectionId);
      if (parent && (parent.status === 'Submitted' || parent.status === 'Approved')) {
        console.warn('Cannot delete finding: Inspection is submitted and locked.');
        return;
      }
    }
    setAllFindings(prev => prev.filter(f => f.id !== id));
  };

  // Master Preset Actions
  const addMasterDuration = (preset: Omit<MasterDurationEntity, 'id'>) => {
    const newItem: MasterDurationEntity = {
      ...preset,
      id: Date.now()
    };
    setAllMasterDurations(prev => [newItem, ...prev]);
  };

  const updateMasterDuration = (updated: MasterDurationEntity) => {
    setAllMasterDurations(prev => prev.map(m => m.id === updated.id ? updated : m));
  };

  const deleteMasterDuration = (id: number) => {
    setAllMasterDurations(prev => prev.filter(m => m.id !== id));
  };

  const importMasterDurations = (presets: Omit<MasterDurationEntity, 'id'>[], replaceExisting: boolean): number => {
    if (replaceExisting) {
      const items = presets.map((p, idx) => ({ ...p, id: Date.now() + idx }));
      setAllMasterDurations(items);
      return items.length;
    } else {
      const items = presets.map((p, idx) => ({ ...p, id: Date.now() + idx }));
      setAllMasterDurations(prev => [...items, ...prev]);
      return items.length;
    }
  };

  const resetToStandardMasterPresets = () => {
    const standard = STANDARD_MASTER_PRESETS.map((p, idx) => ({ ...p, id: idx + 1 }));
    setAllMasterDurations(standard);
  };

  // User Actions
  const addUser = (user: Omit<UserEntity, 'id'>) => {
    const newUser: UserEntity = {
      ...user,
      id: Date.now()
    };
    setAllUsers(prev => [...prev, newUser]);
  };

  return (
    <QcContext.Provider
      value={{
        activeInspection,
        allInspections,
        activeFindings,
        activeTotalDuration,
        isCapacityReached,
        allMasterDurations,
        allBuildings,
        allUsers,
        selectInspection,
        startNewInspection,
        updateActiveInspection,
        deleteInspection,
        submitActiveInspection,
        approveActiveInspection,
        addFinding,
        updateFinding,
        deleteFinding,
        addMasterDuration,
        updateMasterDuration,
        deleteMasterDuration,
        importMasterDurations,
        resetToStandardMasterPresets,
        addUser
      }}
    >
      {children}
    </QcContext.Provider>
  );
};

export const useQc = (): QcContextType => {
  const context = useContext(QcContext);
  if (!context) {
    throw new Error('useQc must be used within a QcProvider');
  }
  return context;
};
