import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  createSalesperson,
  updateSalesperson,
  deleteSalesperson,
} from '../use_cases/salespersons-service';
import type {
  CreateSalespersonRequest,
  UpdateSalespersonRequest,
} from '../../types/admin.types';

export const SalespersonsMutationsService = () => {
  const queryClient = useQueryClient();

  const mutationCreateSalesperson = useMutation({
    mutationFn: (data: CreateSalespersonRequest) => createSalesperson(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['salespersons'] });
    },
  });

  const mutationUpdateSalesperson = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateSalespersonRequest }) =>
      updateSalesperson(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['salespersons'] });
      queryClient.invalidateQueries({ queryKey: ['salesperson', variables.id] });
    },
  });

  const mutationDeleteSalesperson = useMutation({
    mutationFn: (id: number) => deleteSalesperson(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['salespersons'] });
    },
  });

  return {
    mutationCreateSalesperson,
    mutationUpdateSalesperson,
    mutationDeleteSalesperson,
  };
};