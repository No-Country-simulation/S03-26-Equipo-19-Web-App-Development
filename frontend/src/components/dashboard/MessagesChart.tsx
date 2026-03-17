import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Bar } from 'react-chartjs-2';

// Registramos los elementos de Chart.js necesarios
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

export const MessagesChart = () => {
  // Configuración visual del gráfico para que coincida con el mockup
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
        align: 'end' as const, // Alinea la leyenda a la derecha
        labels: {
          usePointStyle: true, // Hace que los cuadritos de la leyenda sean circulares/puntos
          boxWidth: 8,
          font: { size: 12, family: 'system-ui' }
        }
      },
    },
    scales: {
      x: {
        grid: { display: false }, // Oculta las líneas verticales del fondo
      },
      y: {
        border: { display: false }, // Oculta la línea sólida del eje Y
        grid: { color: '#f1f5f9' }, // Color suave para las líneas horizontales
      }
    }
  };

  // Datos mockeados basados en la imagen
  const data = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
    datasets: [
      {
        label: 'Mensajes',
        data: [600, 500, 780, 850, 700, 880],
        backgroundColor: '#13316b', // Azul marino
        borderRadius: 4, // Bordes redondeados en las barras
        barPercentage: 0.7,
      },
      {
        label: 'Opened',
        data: [250, 380, 500, 580, 420, 500],
        backgroundColor: '#3b82f6', // Azul claro
        borderRadius: 4,
        barPercentage: 0.7,
      },
      {
        label: 'Replies',
        data: [150, 200, 350, 400, 300, 380],
        backgroundColor: '#7dd3fc', // Celeste
        borderRadius: 4,
        barPercentage: 0.7,
      },
    ],
  };

  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex flex-col h-full">
      <h3 className="text-sm font-bold text-slate-700 mb-6">Rendimiento de Mensajes (Mensual)</h3>
      {/* Contenedor relativo con la clase de Tailwind optimizada */}
      <div className="flex-1 relative min-h-62.5">
        <Bar options={options} data={data} />
      </div>
    </div>
  );
};