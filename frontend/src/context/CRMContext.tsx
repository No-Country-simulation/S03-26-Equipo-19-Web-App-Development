import { createContext } from 'react';
import type { FunnelStage, Channel } from '../types/contact.types';

export interface Filters {
  stage: FunnelStage | 'all';
  channel: Channel | 'all';
  search: string;
}

export interface CRMContextType {
  filters: Filters;
  setFilters: (filters: Partial<Filters>) => void;
  resetFilters: () => void;
}

export const CRMContext = createContext<CRMContextType | null>(null);