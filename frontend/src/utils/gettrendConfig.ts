import type { Metric } from "../types/metric.types";

export const getTrendConfig = (metric: Metric) => {
  const { trend, changePercent } = metric;

  const formattedValue =
    trend === 'stable' ? '-' : `${changePercent}%`;

  const isPositive = trend === 'up';

  const color =
    trend === 'up'
      ? 'success'
      : trend === 'down'
      ? 'error'
      : 'secondary';

  const icon =
    trend === 'up'
      ? 'up'
      : trend === 'down'
      ? 'down'
      : 'neutral';

  return {
    formattedValue,
    isPositive,
    color,
    icon,
  };
};