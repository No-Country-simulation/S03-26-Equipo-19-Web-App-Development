type TaskType = "CALL" | "EMAIL" | "MEETING" | "DEMO" | "OTHER";
type TaskStatus = "PENDING" | "OVERDUE" | "COMPLETED";

export interface TaskResType {
  id: number;
  title: string;
  description: string;
  type: TaskType;
  status: TaskStatus;
  dueDate: string;
  completeAt: string;
  contactId: number;
  assignedTo: number;
  createdAt: string;
}

export interface TaskReqType {
  title: string;
  description: string;
  type: TaskType;
  dueDate: string;
  contactId: number;
  assignedTo?: number; // 👈 Agregar este campo opcional
}
