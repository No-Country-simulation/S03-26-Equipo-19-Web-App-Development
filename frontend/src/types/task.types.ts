type TaskType = "follow-up" | "reminder";
type TaskStatus = "pending" | "done";

export interface Task {
  id: number;
  contactId: number;
  userId: number;
  title: string;
  description: string;
  type: TaskType;
  status: TaskStatus;
  dueDate: Date;
  createdAt: Date;
}
