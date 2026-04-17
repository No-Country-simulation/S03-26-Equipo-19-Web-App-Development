// src/pages/TasksPage.tsx
import TitleSection from "../components/ui/TitleSection";
import { Button } from "../components/ui/Button";
import { Modal } from "../components/ui/Modal";
import { TaskForm } from "../components/tasks/TaskForm";
import TaskItem, { type Color } from "../components/tasks/TaskItem";
import { PaginationControls } from "../components/ui/PaginationControls";
import { useTasksController } from "../components/tasks/useTaskController";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "../components/ui/select";
import { useGetTasksMetrics } from "../services/use_queries/metrics-query";
import { KpiCard } from "../components/ui/KpiCard";
import { Calendar, CalendarCheck, CalendarX } from "lucide-react";

const TASK_GROUPS_CONFIG: {
    key: string
    label: string
    color: Color
}[] = [
        { key: "OVERDUE", label: "Vencidas", color: "error" },
        { key: "TODAY", label: "Para hoy", color: "primary" },
        { key: "UPCOMING", label: "Próximas", color: "secondary" },
        { key: "COMPLETED", label: "Completadas", color: "success" },
    ];

const colorMap: Record<Color, string> = {
    primary: "text-primary",
    secondary: "text-secondary",
    success: "text-success",
    error: "text-error",
};

interface TasksPageProps {
    isAdminView?: boolean;
}

const TasksPage = ({ isAdminView = false }: TasksPageProps) => {

    const {
        isLoading,
        groupedTasks,
        groupedTasksFull,
        contacts,
        contactsMap,
        tasksViews,
        selectedViewName,
        setSelectedViewName,
        resetPage,
        page,
        totalPages,
        nextPage,
        prevPage,
        openCreate,
        openEdit,
        handleSubmit,
        isSubmitting,
        isTaskModalOpen,
        setIsTaskModalOpen,
        selectedTask,
        mapTaskToForm,
    } = useTasksController();

    const { data: tasksMetrics } = useGetTasksMetrics();

    if (isLoading) return <p className="text-center mt-10">Cargando tareas...</p>;

    return (
        <>
            <div className="flex justify-between mb-6">
                <TitleSection
                    text={isAdminView ? "Todas las tareas" : "Mis tareas"}
                    className="hidden md:flex"
                />
                <Button variant='secondary' className="w-1/2 md:w-1/4 lg:w-1/6" onClick={openCreate}>
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
                    <SelectTrigger className="w-1/2 md:w-1/4 lg:w-1/6">
                        <SelectValue placeholder="Vistas guardadas" />
                    </SelectTrigger>
                    <SelectContent className="bg-white">
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
                    const visible = groupedTasks[group.key] || [];
                    const total = groupedTasksFull[group.key] || [];

                    if (!visible.length) return null;

                    return (
                        <div key={group.key}>
                            <h3 className={`mb-2 ${colorMap[group.color]}`}>
                                {group.label} ({total.length})
                            </h3>

                            <div className="flex flex-col gap-2 lg:px-10">
                                {visible.map((task) => (
                                    <TaskItem
                                        key={task.id}
                                        task={task}
                                        contact={contactsMap.get(task.contactId)}
                                        color={group.color}
                                        openModal={openEdit}
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
                    onSubmit={handleSubmit}
                    onCancel={() => setIsTaskModalOpen(false)}
                    isAdminView={isAdminView}
                    isLoading={isSubmitting}
                />
            </Modal>
        </>
    );
};

export default TasksPage;