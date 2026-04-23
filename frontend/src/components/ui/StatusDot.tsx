interface StatusDotProps {
  status: string;
  activeValue?: string;   // valor que se considera "activo", default 'ACTIVE'
  activeLabel?: string;
  inactiveLabel?: string;
}

export const StatusDot = ({
  status,
  activeValue = 'ACTIVE',
  activeLabel = 'Active',
  inactiveLabel = 'Inactive',
}: StatusDotProps) => {
  const isActive = status === activeValue;
  return (
    <span className="flex items-center gap-1.5 text-sm">
      <span className={`w-2 h-2 rounded-full ${isActive ? 'bg-green-500' : 'bg-red-400'}`} />
      <span className={isActive ? 'text-green-600' : 'text-red-500'}>
        {isActive ? activeLabel : inactiveLabel}
      </span>
    </span>
  );
};