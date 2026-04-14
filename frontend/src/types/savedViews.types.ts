export type SavedViewForm = {
  name: string;
  entity: 'CONTACTS' | 'TASKS';
  filters: Record<string, any>;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  global: boolean;
};