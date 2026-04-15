type TaskType = "CALL" | "EMAIL" | "MEETING" | "DEMO" | "OTHER";
type TaskStatus = "PENDING" | "OVERDUE" | "COMPLETED";

// ✅ definís el tipo del backend acá mismo
export interface TaskResponse {
  id: number;
  title: string;
  description: string;
  type: TaskType;
  status: TaskStatus;
  dueDate: string;
  completedAt: string;
  contactId: number;
  assignedTo: number;
  createdAt: string;
}

// ✅ alias opcional (podés incluso no usarlo)
export type Task = TaskResponse;

export interface TaskReqType {
  title: string;
  description: string;
  type: TaskType;
  dueDate: string;
}