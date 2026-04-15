import { useMemo, useState } from "react";
import { Button } from "../components/ui/Button";
import { Modal } from "../components/ui/Modal";
import TitleSection from "../components/ui/TitleSection";
import { KpiCard } from "../components/ui/KpiCard";
import { Calendar, CalendarCheck, CalendarX } from "lucide-react";
import { useGetTasksMetrics } from "../services/use_queries/metrics-query";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/ui/select";
import { useGetSavedViews } from "../services/use_queries/saved-views-query";
import { TaskForm } from "../components/tasks/TaskForm";
import { useTasksMutationsService } from "../services/use_mutations/tasks-mutation";
import TaskItem, { type Color } from "../components/tasks/TaskItem";
import { useGetTasks } from "../services/use_queries/tasks-query";
import { useGetContacts } from "../services/use_queries/contacts-query";
import type { TaskReqType } from "../types/task.types";

const TASK_GROUPS_CONFIG: {
  key: string
  label: string
  color: Color
}[] = [
  { key: "OVERDUE", label: "Vencidas", color: "error" },
  { key: "TODAY", label: "Para hoy", color: "primary" },
  { key: "UPCOMING", label: "Próximas", color: "secondary" },
  { key: "COMPLETED", label: "Completadas", color: "success" },
]

const colorMap: Record<Color, string> = {
  primary: "text-primary",
  secondary: "text-secondary",
  success: "text-success",
  error: "text-error",
}

const TasksPage = () => {

    const { data: tasksMetrics } = useGetTasksMetrics()
    const { data: views = [] } = useGetSavedViews();
    const { data: tasks, isLoading: isLoadingTasks } = useGetTasks()
    const { data: contacts } = useGetContacts()

    console.log({ tasks });

    const { mutationPostTask } = useTasksMutationsService()


    const handleCreateTask = (data: Record<string, unknown>) => {
        mutationPostTask.mutate(data as unknown as TaskReqType, {
            onSuccess: () => {
                setModalOpen(false);
            },
        });
    };

    const tasksViews = views.filter(view => view.entity === "TASKS");

    const [modalOpen, setModalOpen] = useState(false);
    const [selectedViewName, setSelectedViewName] = useState<string | null>(null);

    const contactsMap = useMemo(() => {
        const map = new Map()

        contacts?.forEach(contact => {
            map.set(contact.id, contact)
        })
        return map
    }, [contacts])

    const getTaskGroup = (task: TaskReqType & { status?: string; dueDate?: string }) => {
        const status = task.status

        if (status === "COMPLETED") return "COMPLETED"
        if (status === "OVERDUE") return "OVERDUE"

        if (status === "PENDING") {
            const TODAY = new Date()
            const dueDate = new Date(task.dueDate || "")

            // normalizar
            TODAY.setHours(0, 0, 0, 0)
            dueDate.setHours(0, 0, 0, 0)

            if (dueDate.getTime() === TODAY.getTime()) {
                return "TODAY"
            }

            if (dueDate > TODAY) {
                return "UPCOMING"
            }
            return "OVERDUE"
        }

        return "other"
    }

    const groupedTasks = useMemo(() => {
        if (!tasks) return {}

        return tasks.reduce((acc, task) => {
            const group = getTaskGroup(task)

            if (!acc[group]) acc[group] = []

            acc[group].push(task)

            return acc
        }, {} as Record<string, typeof tasks>)
    }, [tasks])


    if (isLoadingTasks) return <div className="flex items-center justify-center">
        <p className="text-lg font-medium text-primary">Cargando tareas...</p>
    </div>;


    return (
        <>
            <div className="flex justify-center md:justify-between mb-6">
                <TitleSection text="Mis tareas" className='hidden md:flex' />
                <Button variant='secondary' className="w-1/2 md:w-1/4 lg:w-1/6" onClick={() => setModalOpen(true)}>
                    Nueva tarea
                </Button>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
                <KpiCard
                    title="Tareas para hoy"
                    metric={tasksMetrics?.tasks?.dueToday ?? { value: 0, changePercent: 0, trend: 'stable' }}
                    color="primary"
                    icon={<CalendarCheck size={28} />}
                />
                <KpiCard
                    title="Tareas vencidas"
                    metric={tasksMetrics?.tasks?.overdue ?? { value: 0, changePercent: 0, trend: 'stable' }}
                    color="error"
                    icon={<CalendarX size={28} />}
                    impact="negative"
                />
                <KpiCard
                    title="Próximas tareas"
                    metric={tasksMetrics?.tasks?.pending ?? { value: 0, changePercent: 0, trend: 'stable' }}
                    color="secondary"
                    icon={<Calendar size={28} />}
                />
            </div>

            <div className="flex justify-center md:justify-end mb-6">
                <Select
                    value={selectedViewName ?? ""}
                    onValueChange={(value) =>
                        setSelectedViewName(value || null)
                    }
                >
                    <SelectTrigger className="w-1/2 md:w-1/4 lg:w-1/6">
                        <SelectValue placeholder="Vistas guardadas" />
                    </SelectTrigger>

                    <SelectContent className={"bg-white"}>
                        <SelectItem value="">Sin selección</SelectItem>

                        {tasksViews.map((view) => (
                            <SelectItem key={view.id} value={view.name}>
                                {view.name}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>
          <div className="flex flex-col gap-6">
  {TASK_GROUPS_CONFIG.map((group) => {
    const tasksInGroup = groupedTasks[group.key] || []

    if (tasksInGroup.length === 0) return null

    return (
      <div key={group.key}>
        
        <h3 className={`text-sm font-semibold mb-3 ${colorMap[group.color]}`}>
          {group.label} ({tasksInGroup.length})
        </h3>

        <div className="flex flex-col gap-2 lg:px-10">
          {tasksInGroup.map((task: TaskReqType & { status?: string; dueDate?: string; id?: string; contactId?: number }) => {
            const contact = contactsMap.get(task.contactId)

            return (
              <TaskItem
                key={task.id}
                task={task}
                contact={contact}
                color={group.color}
              />
            )
          })}
        </div>

      </div>
    )
  })}
</div>

            {/* Modal para crear nueva tarea */}
            <Modal
                isOpen={modalOpen}
                onClose={() => setModalOpen(false)}
                title="Nueva tarea">
                <TaskForm
                    contactId={1}
                    onSubmit={handleCreateTask}
                    onCancel={() => setModalOpen(false)}
                />
            </Modal>

        </>
    )

}

export default TasksPage
