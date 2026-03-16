import { useState, useEffect, useCallback, useMemo } from "react";
import { Lead, LeadFormData, Stats } from "../types";
import {
  generateId,
  loadFromStorage,
  saveToStorage,
  STORAGE_KEY,
} from "../lib/utils";

const MOCK_LEADS: Lead[] = [
  {
    id: "1",
    name: "Sofia Rodriguez",
    email: "sofia@empresa.com",
    status: "Lead Activo",
    channel: "WhatsApp",
    timestamp: Date.now() - 86400000,
  },
  {
    id: "2",
    name: "Carlos Mendez",
    email: "carlos@startup.io",
    status: "En Seguimiento",
    channel: "Email",
    timestamp: Date.now() - 172800000,
  },
  {
    id: "3",
    name: "Ana García",
    email: "ana@tech.co",
    status: "Cliente Cerrado",
    channel: "WhatsApp",
    timestamp: Date.now() - 259200000,
  },
  {
    id: "4",
    name: "Luis Perez",
    email: "luis@dev.mx",
    status: "Lead Activo",
    channel: "Email",
    timestamp: Date.now() - 345600000,
  },
  {
    id: "5",
    name: "Maria Fernandez",
    email: "maria@digital.es",
    status: "En Seguimiento",
    channel: "WhatsApp",
    timestamp: Date.now() - 432000000,
  },
];

interface UseLeadsReturn {
  leads: Lead[];
  loading: boolean;
  stats: Stats;
  addLead: (data: LeadFormData) => void;
  deleteLead: (id: string) => void;
  updateLead: (id: string, data: Partial<LeadFormData>) => void;
}

export function useLeads(): UseLeadsReturn {
  const [leads, setLeads] = useState<Lead[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Load from localStorage or use mock data
    const stored = loadFromStorage<Lead[]>(STORAGE_KEY);
    if (stored && stored.length > 0) {
      setLeads(stored);
    } else {
      // Initialize with mock data
      setLeads(MOCK_LEADS);
      saveToStorage(STORAGE_KEY, MOCK_LEADS);
    }
    setLoading(false);
  }, []);

  // Save to localStorage whenever leads change
  useEffect(() => {
    if (!loading && leads.length > 0) {
      saveToStorage(STORAGE_KEY, leads);
    }
  }, [leads, loading]);

  const stats = useMemo(
    (): Stats => ({
      total: leads.length,
      active: leads.filter((l) => l.status === "Lead Activo").length,
      closed: leads.filter((l) => l.status === "Cliente Cerrado").length,
    }),
    [leads],
  );

  const addLead = useCallback((data: LeadFormData) => {
    const newLead: Lead = {
      id: generateId(),
      ...data,
      timestamp: Date.now(),
    };
    setLeads((prev) => [newLead, ...prev]);
  }, []);

  const deleteLead = useCallback((id: string) => {
    setLeads((prev) => prev.filter((lead) => lead.id !== id));
  }, []);

  const updateLead = useCallback((id: string, data: Partial<LeadFormData>) => {
    setLeads((prev) =>
      prev.map((lead) => (lead.id === id ? { ...lead, ...data } : lead)),
    );
  }, []);

  return {
    leads,
    loading,
    stats,
    addLead,
    deleteLead,
    updateLead,
  };
}
