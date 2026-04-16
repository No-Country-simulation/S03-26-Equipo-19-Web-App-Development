import { useMemo, useState } from "react";
import { Button } from "../components/ui/Button";
import { Modal } from "../components/ui/Modal";
import TitleSection from "../components/ui/TitleSection";
import { KpiCard } from "../components/ui/KpiCard";
import { Calendar, CalendarCheck, CalendarX } from "lucide-react";
import { useGetTasksMetrics } from "../services/use_queries/metrics-query";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "../components/ui/select";
import { useGetSavedViews } from "../services/use_queries/saved-views-query";
import { TaskForm } from "../components/tasks/TaskForm";
import { useTasksMutationsService } from "../services/use_mutations/tasks-mutation";
import TaskItem, { type Color } from "../components/tasks/TaskItem";
import { useGetTasks } from "../services/use_queries/tasks-query";
import { useGetContacts } from "../services/use_queries/contacts-query";
import type { TaskReqType, TaskResType } from "../types/task.types";
import { usePagination } from "../hooks/usePagination";
import { PaginationControls } from "../components/ui/PaginationControls";

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
    const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
    const [selectedTask, setSelectedTask] = useState<TaskResType | null>(null);
    const [selectedViewName, setSelectedViewName] = useState<string | null>(null);


    const { data: tasksMetrics } = useGetTasksMetrics();
    const { data: views = [] } = useGetSavedViews();
    const { data: tasks, isLoading: isLoadingTasks } = useGetTasks();
    const { data: contacts } = useGetContacts();

    const { mutationPostTask, mutationUpdateTaskById } =
        useTasksMutationsService();

    const handleOpenCreate = () => {
        setSelectedTask(null);
        setIsTaskModalOpen(true);
    };

    const handleOpenEdit = (task: TaskResType) => {
        setSelectedTask(task);
        setIsTaskModalOpen(true);
    };

    const handleCreateTask = (data: TaskReqType) => {
        mutationPostTask.mutate(data, {
            onSuccess: () => setIsTaskModalOpen(false),
        });
    };

    const handleUpdateTask = (data: TaskReqType) => {
        if (!selectedTask) return;

        mutationUpdateTaskById.mutate(
            { id: selectedTask.id, data },
            { onSuccess: () => setIsTaskModalOpen(false) }
        );
    };

    const tasksViews = views.filter((view) => view.entity === "TASKS");

    const selectedView = tasksViews.find(
        (view) => view.name === selectedViewName
    );


    const applyViewFilters = (tasks: TaskResType[], view: any) => {
        if (!view?.filters) return tasks;

        const { status, dueDateFrom, dueDateTo } = view.filters;

        return tasks.filter((task) => {
            if (status && task.status !== status) return false;

            const taskDate = new Date(task.dueDate);

            if (dueDateFrom && taskDate < new Date(dueDateFrom)) return false;
            if (dueDateTo && taskDate > new Date(dueDateTo)) return false;

            return true;
        });
    };


    const applySorting = (tasks: TaskResType[], view: any) => {
        if (!view?.sortBy) return tasks;

        const sorted = [...tasks].sort((a, b) => {
            if (view.sortBy === "dueDate") {
                return (
                    new Date(a.dueDate).getTime() -
                    new Date(b.dueDate).getTime()
                );
            }
            return 0;
        });

        return view.sortOrder === "DESC" ? sorted.reverse() : sorted;
    };

    const GROUP_ORDER = {
        OVERDUE: 0,
        TODAY: 1,
        UPCOMING: 2,
        COMPLETED: 3,
    };
    const getTaskGroup = (task: TaskResType) => {
        if (task.status === "COMPLETED") return "COMPLETED";

        const today = new Date();
        const due = new Date(task.dueDate);

        today.setHours(0, 0, 0, 0);
        due.setHours(0, 0, 0, 0);

        if (due < today) return "OVERDUE";
        if (due.getTime() === today.getTime()) return "TODAY";

        return "UPCOMING";
    };



    const mapTaskToForm = (task: TaskResType): TaskReqType => ({
        title: task.title,
        description: task.description,
        type: task.type,
        dueDate: task.dueDate.slice(0, 16),
        contactId: task.contactId,
    });

    const processedTasks = useMemo(() => {
        if (!tasks) return [];

        let result = [...tasks];

        if (selectedView) {
            result = applyViewFilters(result, selectedView);
        }

        return result.sort((a, b) => {
            const groupA = GROUP_ORDER[getTaskGroup(a)];
            const groupB = GROUP_ORDER[getTaskGroup(b)];

            if (groupA !== groupB) {
                return groupA - groupB;
            }

            const dateA = new Date(a.dueDate).getTime();
            const dateB = new Date(b.dueDate).getTime();

            return dateA - dateB;
        });
    }, [tasks, selectedView]);

    const groupedTasksFull = useMemo(() => {
        if (!processedTasks.length) return {};

        return processedTasks.reduce((acc, task) => {
            const group = getTaskGroup(task);

            if (!acc[group]) acc[group] = [];
            acc[group].push(task);

            return acc;
        }, {} as Record<string, TaskResType[]>);
    }, [processedTasks]);

    const {
        paginatedData,
        page,
        totalPages,
        nextPage,
        prevPage,
        resetPage,
    } = usePagination({
        data: processedTasks,
        pageSize: 6,
    });

    const groupedTasks = useMemo(() => {
        return paginatedData.reduce((acc, task) => {
            const group = getTaskGroup(task);

            if (!acc[group]) acc[group] = [];
            acc[group].push(task);

            return acc;
        }, {} as Record<string, TaskResType[]>);
    }, [paginatedData]);


    const contactsMap = useMemo(() => {
        const map = new Map();
        contacts?.forEach((c) => map.set(c.id, c));
        return map;
    }, [contacts]);




    if (isLoadingTasks)
        return <p className="text-center mt-10">Cargando tareas...</p>;

    return (
        <>

            <div className="flex justify-between mb-6">
                <TitleSection text="Mis tareas" className='hidden md:flex' />
                <Button variant='secondary' className="w-1/2 md:w-1/4 lg:w-1/6" onClick={handleOpenCreate}>
                    Nueva tarea
                </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
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
                    onValueChange={(value) => {
                        setSelectedViewName(value || null);
                        resetPage();
                    }}
                >
                    <SelectTrigger className="w-60 mb-6">
                        <SelectValue placeholder="Vistas guardadas" />
                    </SelectTrigger>
                    <SelectContent>
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
                    const tasksInGroup = groupedTasks[group.key] || [];
                    const totalTasksInGroup = groupedTasksFull[group.key] || [];

                    if (!tasksInGroup.length) return null;


                    return (
                        <div key={group.key}>
                            <h3 className={`mb-2 ${colorMap[group.color]}`}>
                                {group.label} ({totalTasksInGroup.length})
                            </h3>

                            <div className="flex flex-col gap-2 lg:px-10">
                                {tasksInGroup.map((task) => (
                                    <TaskItem
                                        key={task.id}
                                        task={task}
                                        contact={contactsMap.get(task.contactId)}
                                        color={group.color}
                                        openModal={handleOpenEdit}
                                    />
                                ))}
                            </div>
                        </div>
                    );
                })}
            </div>


            <PaginationControls
                page={page}
                totalPages={totalPages}
                onNext={nextPage}
                onPrev={prevPage}
            />

            {/* MODAL */}
            <Modal
                isOpen={isTaskModalOpen}
                onClose={() => setIsTaskModalOpen(false)}
                title={selectedTask ? "Editar tarea" : "Nueva tarea"}
            >
                <TaskForm
                    contacts={contacts}
                    initialData={
                        selectedTask ? mapTaskToForm(selectedTask) : undefined
                    }
                    onSubmit={
                        selectedTask ? handleUpdateTask : handleCreateTask
                    }
                    onCancel={() => setIsTaskModalOpen(false)}
                />
            </Modal>
        </>
    );
};

export default TasksPage;