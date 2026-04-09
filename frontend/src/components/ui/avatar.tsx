
import { getInitials } from '../../utils/getInitials'

interface AvatarProps {
    user: {
        name?: string,
        lastName?: string,
    }
}

export const Avatar = ({ user }: AvatarProps) => {
    return (
        <span className="w-10 h-10 bg-neutro-3 rounded-full flex items-center justify-center text-primary text-sm font-bold">
            {getInitials(user.name, user.lastName)}
        </span>
    )
}
