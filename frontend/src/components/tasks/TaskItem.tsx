import type { ContactResType } from '../../types/contact.types'
import type { TaskResType } from '../../types/task.types'
import { Badge } from '../ui/Badge'
import { Circle, CircleCheck, Clock, Edit, Trash2, User } from 'lucide-react'


const colorMap: Record<Color, {
  bg: string
  border: string
  text: string
}> = {
  primary: {
    bg: "bg-primary/10",
    border: "border-primary",
    text: "text-primary",
  },
  secondary: {
    bg: "bg-secondary/10",
    border: "border-secondary",
    text: "text-secondary",
  },
  success: {
    bg: "bg-success/10",
    border: "border-success",
    text: "text-success",
  },
  error: {
    bg: "bg-error/10",
    border: "border-error",
    text: "text-error",
  },
}

export type Color = "primary" | "secondary" | "success" | "error"

interface TaskItemProps {
  task: TaskResType
  contact: ContactResType
  color: Color
}

const TaskItem = ({ task, contact, color }: TaskItemProps) => {
  const styles = colorMap[color as keyof typeof colorMap] ?? colorMap.primary

  console.log({task});
  
  return (
    <div className={`grid grid-cols-1 md:grid-cols-[2fr_1fr_1fr] gap-4 justify-between ${styles.bg} ${styles.border} rounded-2xl py-4 px-6`}>
      <div className='flex items-center gap-6'>
        {task.status !== "COMPLETED" ? (
          <button className={`flex items-center gap-2 ${styles.text}`}>
            <Circle className='h-5 w-5' />
          </button>
        ) : (
          <CircleCheck className='h-5 w-5 shrink-0 text-success' />
        )}


        <div>
          <div className='flex items-center gap-4'>
            <p className='text-md font-bold'>{task.title}</p>
            <Badge variant='outline'>{task.type}</Badge>
            <span className='text-sm text-muted-foreground'>{task.status}</span>
          </div>

          <p className='text-sm mb-2'>{task.description}</p>

          <span className='text-sm flex items-center gap-2'>
            <User className='h-4 w-4' />
            {contact?.name} {contact?.lastName}
          </span>
        </div>
      </div>

      <p className='flex items-center gap-2 text-sm ml-11 md:ml-0'>
        <Clock className='h-4 w-4' />
        {new Date(task.dueDate).toLocaleDateString()}
      </p>

      <div className='flex items-center gap-6 justify-end'>
        <Edit className={`h-6 w-6 ${styles.text}`} />
        <Trash2 className={`h-6 w-6 ${styles.text}`} />
      </div>
    </div>
  )
}

export default TaskItem