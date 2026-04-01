import type { FunnelStage } from '../../types/contact.types';

type BadgeVariant = FunnelStage | 'default';

interface BadgeProps {
  label: string;
  variant?: BadgeVariant;
}

const variants: Record<BadgeVariant, string> = {
  lead:      'bg-yellow-500/20 text-yellow-400',
  active:    'bg-green-500/20 text-green-400',
  following: 'bg-sky-500/20 text-sky-400',
  closed:    'bg-gray-500/20 text-gray-400',
  default:   'bg-gray-500/20 text-gray-400',
};

export const Badge = ({ label, variant = 'default' }: BadgeProps) => {
  return (
    <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${variants[variant]}`}>
      {label}
    </span>
  );
};