# Startup CRM — Roadmap (5 semanas)

## Duración

5 semanas — Metodología ágil basada en sprints.

Equipo:

* Frontend: Ariel, Liria
* Backend: Anthony, Esteban

---

# Semana 1 — Setup y Arquitectura

## Objetivo

Preparar el proyecto, definir arquitectura y construir la base del sistema.

### Backend

* Crear repositorio
* Configurar Node.js + Express
* Definir estructura del proyecto
* Crear conexión con base de datos
* Crear modelo de **Contact**
* Implementar endpoints básicos:

  * GET /contacts
  * POST /contacts
  * PUT /contacts/:id
  * DELETE /contacts/:id

### Frontend

* Crear proyecto con React
* Instalar Tailwind
* Definir estructura de carpetas
* Crear layout base del CRM
* Crear navegación principal

### Resultado esperado

* Proyecto corriendo
* API funcionando
* UI básica lista

---

# Semana 2 — Gestión de contactos (Core del CRM)

## Objetivo

Implementar el sistema de contactos.

### Backend

* CRUD completo de contactos
* Sistema de etiquetas
* Filtros por estado del funnel
* Validaciones

### Frontend

* Tabla de contactos
* Crear contacto
* Editar contacto
* Eliminar contacto
* Filtros de búsqueda

### Resultado esperado

* Gestión completa de contactos funcionando

---

# Semana 3 — Comunicaciones

## Objetivo

Centralizar comunicaciones.

### Backend

* Integración básica con API de email
* Registro de mensajes enviados
* Endpoint de conversaciones

Ejemplo endpoints:

GET /messages
POST /messages/send

### Frontend

* Bandeja de mensajes
* Vista de conversación
* Envío de emails desde el CRM

### Resultado esperado

* Comunicación básica desde el CRM

---

# Semana 4 — Automatizaciones y Métricas

## Objetivo

Agregar inteligencia al CRM.

### Backend

* Sistema de recordatorios
* Automatización de seguimientos
* Endpoint de métricas

Ejemplo métricas:

* contactos activos
* mensajes enviados
* tasa de respuesta

### Frontend

* Dashboard de métricas
* Gráficos
* Panel de recordatorios

### Resultado esperado

* Dashboard funcional

---

# Semana 5 — Integraciones y Deploy

## Objetivo

Pulir el sistema y preparar entrega.

### Backend

* Exportación de datos (CSV / PDF)
* Mejoras de seguridad
* Documentación de API
* Testing básico

### Frontend

* Mejoras de UI
* Manejo de errores
* Optimización
* Conexión final con backend

### Proyecto

* Deploy frontend
* Deploy backend
* Documentación final

### Resultado esperado

* Prototipo funcional listo para entrega

---

# MVP final esperado

El CRM debe permitir:

* Gestión de contactos
* Segmentación del funnel
* Envío de emails
* Registro de conversaciones
* Panel de métricas
* Exportación de datos

---

# Objetivo final

Entregar un **CRM funcional para startups** que centralice la gestión de leads y clientes con comunicación integrada y métricas básicas.
