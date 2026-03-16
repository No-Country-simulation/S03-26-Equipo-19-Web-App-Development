# Startup CRM — Propuesta UX/UI y MVP

##  Información del Proyecto
- **Nombre:** Startup CRM  
- **Objetivo:** Sistema para startups que gestionan leads y clientes en tiempo real, integrando WhatsApp y correo electrónico, con panel de métricas y automatizaciones.  
- **Usuarios:** Empleados de startups que gestionan relaciones comerciales.  

### Stack Tecnológico (Frontend)
- **Framework:** React 19  
- **Lenguaje:** TypeScript  
- **Estilos:** Tailwind CSS  
- **Arquitectura:** Componentes reutilizables y tipados

---

# Identidad Visual

## Paleta de Colores
- **Primario:** `#1E3A8A` → Azul oscuro (botones principales, sidebar, encabezados)
- **Secundario:** `#38BDF8` → Azul claro (acciones, indicadores, links)
- **Fondo:** `#F7F9FC` → Gris muy claro (background general)
- **Texto principal:** `#1F2937`
- **Texto secundario:** `#6B7280`

## Tipografía
- **Principal:** Inter  
- **Alternativa:** Roboto

## Estilo Visual
- Diseño **moderno y minimalista**
- Uso de **espacios amplios y jerarquía visual clara**
- Componentes tipo **cards**
- Gráficos limpios con colores suaves
- Interfaz optimizada para **desktop first**

---

# 🖥 Propuesta de UX/UI

## Dashboard Principal
Pantalla principal del sistema.

Elementos:
- Tarjetas de métricas (KPI):
  - Contactos activos
  - Mensajes enviados
  - Tasa de respuesta
- Gráficos:
  - Barras
  - Líneas
  - Circular
- Alertas de seguimiento
- Botones de acción rápida

Componentes React sugeridos:
components/
KPIcard.tsx
Charts/
BarChart.tsx
LineChart.tsx
PieChart.tsx
Alerts.tsx


---

## Gestión de Contactos

Tabla principal de leads y clientes.

Campos:
- Nombre
- Email
- Estado del funnel
- Última interacción
- Lead score

Estados del funnel:


Lead → Contactado → Propuesta → Cliente


Funciones:
- Crear contacto
- Editar contacto
- Eliminar contacto
- Filtrar por estado
- Etiquetas
- Vistas guardadas

Componentes:


components/
ContactsTable.tsx
ContactFormModal.tsx
Filters.tsx


---

##  Bandeja de Mensajes

Vista unificada de conversaciones.

Integraciones simuladas:

- WhatsApp
- Email

Características:

- Timeline de conversación
- Burbujas de chat
- Estado de lectura
- Sidebar con contactos

Componentes:


components/
MessageSidebar.tsx
ChatWindow.tsx
MessageBubble.tsx
SendMessageInput.tsx


---

## Automatización de Seguimientos

Sistema de recordatorios y automatizaciones.

Funciones:

- Recordatorios automáticos
- Campañas de reactivación
- Seguimiento de leads inactivos

Ejemplo:


Si lead no responde en 48h
→ enviar recordatorio automático


Componentes:


components/
AutomationRules.tsx
ReminderCard.tsx
