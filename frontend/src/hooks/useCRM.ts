import { useContext } from 'react';
import { CRMContext } from '../context/CRMContext';

export const useCRM = () => {
  const ctx = useContext(CRMContext);
  if (!ctx) throw new Error('useCRM debe usarse dentro de CRMProvider');
  return ctx;
};