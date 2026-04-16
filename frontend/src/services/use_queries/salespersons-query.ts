import { useQuery } from '@tanstack/react-query';
import { getSalespersons, getSalespersonById } from '../use_cases/salespersons-service';
import type { SalespersonResponse } from '../../types/admin.types';

export const useGetSalespersons = () =>
  useQuery<SalespersonResponse[]>({
    queryKey: ['salespersons'],
    queryFn: getSalespersons,
  });

export const useGetSalespersonById = (id: number) =>
  useQuery<SalespersonResponse>({
    queryKey: ['salesperson', id],
    queryFn: () => getSalespersonById(id),
    enabled: !!id,
  });