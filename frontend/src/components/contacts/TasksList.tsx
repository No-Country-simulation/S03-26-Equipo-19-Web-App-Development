import { Circle, CheckCircle2, Clock, Calendar, Plus } from "lucide-react"
import { cn } from "../../lib/utils"
import { useState } from "react"
import { Button } from "../ui/Button"
import { useGetTaskByContactId} from "../../services/use_queries/tasks-query"
import { Modal } from "../ui/Modal"
import { TaskForm } from "../tasks/TaskForm"
import { useTasksMutationsService } from "../../services/use_mutations/tasks-mutation"

interface TasksListProps {
  contactId?: number
}

export function TaskList({ contactId }: TasksListProps) {

  const [isNewTaskOpen, setIsNewTaskOpen] = useState(false)

  const { data: tasks } = useGetTaskByContactId(contactId!)

  const { mutationPostTask,
    mutationUpdateTaskStatusById } = useTasksMutationsService()


  const handleCreateTask = (data: any) => {
    mutationPostTask.mutate(data, {
      onSuccess: () => {
        setIsNewTaskOpen(false);
      },
    });
  };

  const onToggleTask = (id: number) => {
  mutationUpdateTaskStatusById.mutate({ id });
};

  return (
    <>
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-foreground">Tareas</h3>
          <button
            onClick={() => setIsNewTaskOpen(true)}
            className="flex h-7 gap-1 text-xs text-primary hover:text-primary mt-3"
          >
            <Plus className="h-3.5 w-3.5" />
            Tarea
          </button>
        </div>
        <div className="space-y-2">
          {tasks?.map((task) => (
            <button
              key={task.id}
              onClick={() => onToggleTask?.(task.id)}
              className={cn(
                "flex w-full items-start gap-3 rounded-lg p-3 text-left transition-all hover:bg-muted/50 group",
                task.status === "COMPLETED" && "opacity-60"
              )}
            >
              {task.status === "COMPLETED" ? (
                <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-success" />
              ) : (
                <Circle className="mt-0.5 h-4 w-4 shrink-0 text-muted-foreground group-hover:text-primary transition-colors" />
              )}
              <div className="flex-1 min-w-0">
                <p
                  className={cn(
                    "text-sm font-medium",
                    task.status === "COMPLETED" && "line-through text-muted-foreground"
                  )}
                >
                  {task.title}
                </p>
                <p className="text-xs text-muted-foreground flex items-center gap-1 mt-0.5">
                  <Clock className="h-3 w-3" />
                  {new Date(task.dueDate).toLocaleDateString()}
                </p>
              </div>
            </button>
          ))}
          {tasks?.length === 0 && (
            <div className="py-6 text-center">
              <Calendar className="h-8 w-8 mx-auto text-muted-foreground/50 mb-2" />
              <p className="text-sm text-muted-foreground">No hay tareas creadas aún</p>
            </div>
          )}
        </div>
      </div>
      <Modal
        isOpen={isNewTaskOpen}
        onClose={() => setIsNewTaskOpen(false)}
        title="Nueva tarea">
        <TaskForm
          contactId={contactId}
          onSubmit={handleCreateTask}
          onCancel={() => setIsNewTaskOpen(false)}
        />
      </Modal>
    </>
  )
}