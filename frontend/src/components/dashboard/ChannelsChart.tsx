import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';

// Registramos los elementos para gráficos circulares
ChartJS.register(ArcElement, Tooltip, Legend);

export const ChannelsChart = () => {
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '65%', // Define el grosor de la dona (qué tan hueco es el centro)
    plugins: {
      legend: {
        position: 'right' as const, // Leyenda a la derecha como en el diseño
        labels: {
          usePointStyle: true,
          boxWidth: 8,
          padding: 20,
          font: { size: 12, family: 'system-ui' }
        }
      },
    },
  };

  const data = {
    labels: ['WhatsApp', 'Email', 'SMS', 'Chat'],
    datasets: [
      {
        data: [45, 30, 15, 10], // Los porcentajes de la imagen
        backgroundColor: [
          '#13316b', // WhatsApp - Azul oscuro
          '#2563eb', // Email - Azul medio
          '#38bdf8', // SMS - Celeste
          '#e0f2fe', // Chat - Gris/Celeste muy claro
        ],
        borderWidth: 0, // Sin bordes entre las porciones para un look más moderno
      },
    ],
  };

  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex flex-col h-full">
      <h3 className="text-sm font-bold text-slate-700 mb-6">Distribución de Canales de Respuesta</h3>
      {/* Aplicamos la clase min-h-62.5 sugerida por Tailwind */}
      <div className="flex-1 relative min-h-62.5 flex items-center justify-center">
        <Doughnut data={data} options={options} />
      </div>
    </div>
  );
};