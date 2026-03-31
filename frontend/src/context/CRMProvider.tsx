import { useState } from 'react';
import { CRMContext } from './CRMContext';
import type { Filters } from './CRMContext';

const defaultFilters: Filters = {
  stage: 'all',
  channel: 'all',
  search: '',
};

export const CRMProvider = ({ children }: { children: React.ReactNode }) => {
  const [filters, setFiltersState] = useState<Filters>(defaultFilters);

  const setFilters = (partial: Partial<Filters>) => {
    setFiltersState(prev => ({ ...prev, ...partial }));
  };

  const resetFilters = () => setFiltersState(defaultFilters);

  return (
    <CRMContext.Provider value={{ filters, setFilters, resetFilters }}>
      {children}
    </CRMContext.Provider>
  );
};