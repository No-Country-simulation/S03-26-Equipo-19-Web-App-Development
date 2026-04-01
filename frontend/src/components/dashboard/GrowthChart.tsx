import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

export const GrowthChart = () => {
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
        labels: {
          usePointStyle: true,
          boxWidth: 8,
          font: { size: 12, family: 'system-ui' }
        }
      },
    },
    scales: {
      x: {
        grid: { display: false },
      },
      y: {
        border: { display: false },
        // Grilla casi invisible para un diseño más limpio
        grid: { color: 'rgba(241, 245, 249, 0.3)' },
        min: 0,
        max: 250,
        ticks: { stepSize: 50 }
      }
    },
    elements: {
      line: {
        tension: 0.4
      },
      point: {
        radius: 0,
        hitRadius: 10,
        hoverRadius: 4
      }
    }
  };

  const data = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
    datasets: [
      {
        label: 'Dalians activos',
        data: [30, 80, 140, 80, 160, 240],
        borderColor: '#13316b',
        borderWidth: 2,
      },
      {
        label: 'Contactos',
        data: [20, 50, 100, 110, 120, 190],
        borderColor: '#38bdf8',
        borderWidth: 2,
      },
    ],
  };

  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex flex-col h-full">
      <h3 className="text-sm font-bold text-slate-700 mb-6">Crecimiento de Contactos (Últimos 6 Meses)</h3>
      <div className="flex-1 relative min-h-62.5">
        <Line options={options} data={data} />
      </div>
    </div>
  );
};