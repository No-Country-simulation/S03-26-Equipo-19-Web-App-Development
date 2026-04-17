import { useMemo, useState } from "react";
import type { TaskReqType, TaskResType } from "../../types/task.types";
import { useGetTasks } from "../../services/use_queries/tasks-query";
import { useGetContacts } from "../../services/use_queries/contacts-query";
import { useGetSavedViews } from "../../services/use_queries/saved-views-query";
import { useTasksMutationsService } from "../../services/use_mutations/tasks-mutation";
import { usePagination } from "../../hooks/usePagination";
import type { SavedViewResponse } from "../../types/admin.types";
import { is } from "zod/v4/locales";


const GROUP_ORDER = {
    OVERDUE: 0,
    TODAY: 1,
    UPCOMING: 2,
    COMPLETED: 3,
};

export const useTasksController = () => {
    const [selectedTask, setSelectedTask] = useState<TaskResType | null>(null);
    const [selectedViewName, setSelectedViewName] = useState<string | null>(null);
    const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);

    const { data: tasks = [], isLoading } = useGetTasks();
    const { data: contacts } = useGetContacts();
    const { data: views = [] } = useGetSavedViews();

    const { mutationPostTask, mutationUpdateTaskById } =
        useTasksMutationsService();

    const tasksViews = views.filter((v) => v.entity === "TASKS");

    const selectedView = tasksViews.find(
        (v) => v.name === selectedViewName
    );

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

    const applyViewFilters = (
        tasks: TaskResType[],
        view?: SavedViewResponse
    ) => {
        if (!view?.filters) return tasks;

        const { status, dueDateFrom, dueDateTo } = view.filters;

        const from = dueDateFrom ? new Date(dueDateFrom) : null;
        const to = dueDateTo ? new Date(dueDateTo) : null;

        return tasks.filter((task) => {
            if (status && task.status !== status) return false;

            const taskDate = new Date(task.dueDate);

            if (from && taskDate < from) return false;
            if (to && taskDate > to) return false;

            return true;
        });
    };


    const processedTasks = useMemo(() => {
        if (!tasks) return [];

        let result = [...tasks];

        result = applyViewFilters(result, selectedView);

        return result.sort((a, b) => {
            const groupA = GROUP_ORDER[getTaskGroup(a)];
            const groupB = GROUP_ORDER[getTaskGroup(b)];

            if (groupA !== groupB) return groupA - groupB;

            return (
                new Date(a.dueDate).getTime() -
                new Date(b.dueDate).getTime()
            );
        });
    }, [tasks, selectedView]);

    const groupedTasksFull = useMemo(() => {
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

    const openCreate = () => {
        setSelectedTask(null);
        setIsTaskModalOpen(true);
    };

    const openEdit = (task: TaskResType) => {
        setSelectedTask(task);
        setIsTaskModalOpen(true);
    };

    const isSubmitting =
        mutationPostTask.isPending || mutationUpdateTaskById.isPending;

    const handleSubmit = async (data: TaskReqType) => {
        if (selectedTask) {
            await mutationUpdateTaskById.mutateAsync({
                id: selectedTask.id,
                data,
            });
        } else {
            await mutationPostTask.mutateAsync(data);
        }

        setIsTaskModalOpen(false);
    };

    const mapTaskToForm = (task: TaskResType): TaskReqType => ({
        title: task.title,
        description: task.description,
        type: task.type,
        dueDate: task.dueDate.slice(0, 16),
        contactId: task.contactId,
    });

    return {
        // state
        isLoading,
        isTaskModalOpen,
        selectedTask,
        setSelectedTask,
        mapTaskToForm,
        selectedViewName,
        setSelectedViewName,
        isSubmitting,

        // data
        tasksViews,
        groupedTasks,
        groupedTasksFull,
        contacts,
        contactsMap,

        // pagination
        page,
        totalPages,
        nextPage,
        prevPage,
        resetPage,

        // actions
        openCreate,
        openEdit,
        handleSubmit,
        setIsTaskModalOpen,
    };
};