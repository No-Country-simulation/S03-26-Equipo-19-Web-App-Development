# CoreCRM Startup Dashboard 🚀

Prototipo Frontend de un panel de control (Dashboard) para un CRM, construido con enfoque **Mobile-First**, componentes reutilizables y tipado estricto. Recrea un diseño moderno de alta conversión enfocado en visualización de datos (KPIs, gráficos y tablas).

## 🛠 Tecnologías Utilizadas

* **Framework:** React 19
* **Build Tool:** Vite
* **Lenguaje:** TypeScript
* **Estilos:** Tailwind CSS v4
* **Gráficos:** Chart.js + react-chartjs-2
* **Íconos:** Lucide React
* **Accesibilidad:** Prácticas a11y (aria-labels, titles, contrastes)

## ✨ Características Principales

* **Diseño 100% Responsivo:** Interfaz adaptativa con menú off-canvas (hamburguesa) para dispositivos móviles y layout optimizado para escritorio.
* **Componentización Modular:** Estructura de carpetas limpia (`layout`, `dashboard`, `ui`) pensada para escalar.
* **Gráficos Dinámicos:** Integración de Chart.js para gráficos de barras, líneas suavizadas y donas.
* **Tipado Estricto:** Uso de `verbatimModuleSyntax` e interfaces de TypeScript para prevenir errores en tiempo de desarrollo.
* **Tablas de Datos:** Tabla de contactos recientes con *badges* de estado condicionales y acciones.

## 📁 Estructura del Proyecto

```text
src/
├── components/
│   ├── dashboard/       # Componentes de métricas (KpiCard, Charts, Table)
│   └── layout/          # Estructura base (DashboardLayout, Sidebar, Header)
├── App.tsx              # Vista principal integradora
├── index.css            # Configuración raíz de Tailwind
└── main.tsx             # Punto de entrada de React