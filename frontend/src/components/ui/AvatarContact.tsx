import { getInitials } from "../../utils/getInitials"

interface AvatarProps {    
  name?: string
  lastName?: string
  size?: "sm" | "md" | "lg"
}

const sizeClasses = {
  sm: "w-8 h-8 text-xs p-1",
  md: "w-10 h-10 text-sm",
  lg: "w-18 h-18 text-2xl",
}

const AvatarContact = ({ name, lastName, size = "md" }: AvatarProps) => {
  return (
    <span
      className={`${sizeClasses[size]} flex-shrink-0 border-2 border-primary rounded-full flex items-center justify-center text-primary font-bold`}
    >
      {getInitials(name, lastName)}
    </span>
  )
}

export default AvatarContact