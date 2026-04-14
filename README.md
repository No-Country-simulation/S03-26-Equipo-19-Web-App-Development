# Startup CRM Cross-Industry 🚀

## 🧠 ¿Qué es este proyecto?

Este proyecto nace de un problema bastante común en startups: la gestión comercial está completamente fragmentada.

Leads en Excel, conversaciones en WhatsApp personal, emails por separado y seguimientos que dependen de la memoria. Eso termina generando desorden, pérdida de oportunidades y poca visibilidad del proceso de ventas.

La idea de este CRM es simple: **centralizar todo en un solo lugar y hacerlo medible**.

---

## 💡 ¿Qué resuelve?

* Unifica WhatsApp y Email en una misma plataforma
* Permite seguir cada contacto con historial completo
* Organiza el proceso comercial en un funnel claro
* Automatiza tareas de seguimiento
* Da visibilidad con métricas reales

---

## 🧩 Cómo funciona (visión rápida)

El flujo es bastante directo:

1. Se crea un contacto (lead)
2. Se inicia conversación (WhatsApp o Email)
3. Se registran todos los mensajes
4. Se crean tareas de seguimiento
5. El contacto avanza en el funnel

```
NEW_LEAD → CONTACTED → IN_NEGOTIATION → PROPOSAL_SENT → CLOSED_WON / CLOSED_LOST
```

---

## ⚙️ Stack técnico

### Backend

* Java 17
* Spring Boot
* Spring Security (JWT)
* JPA / Hibernate
* PostgreSQL (Neon DB)

### Frontend

* React
* TypeScript
* Zustand

### Integraciones

* WhatsApp Cloud API
* Brevo (Email)

### Infraestructura

* Render (deploy)
* GitHub Actions

---

## 🔐 Configuración (real del proyecto)

> Nota: estas son las variables reales utilizadas en desarrollo (en producción deberían protegerse mejor).

```env
DB_HOST=ep-snowy-waterfall-ak0g3zwm-pooler.c-3.us-west-2.aws.neon.tech
DB_PORT=5432
DB_NAME=neondb
DB_USER=neondb_owner

JWT_SECRET=***************
JWT_EXPIRATION_MS=86400000

WHATSAPP_API_URL=https://graph.facebook.com/v22.0
WHATSAPP_PHONE_NUMBER_ID=1081673545027786

BREVO_API_URL=https://api.brevo.com/v3
BREVO_SENDER_EMAIL=jindrg@gmail.com
```

---

## 🧱 Estructura del modelo

* **User** → roles (admin / vendedor)
* **Contact** → lead con estado del funnel
* **Conversation** → canal (WhatsApp / Email)
* **Message** → estado (sent, delivered, read)
* **Task** → seguimiento comercial
* **Template** → mensajes reutilizables

---

## 📡 API

Algunos endpoints principales:

```
POST /api/auth/login
POST /api/auth/register

GET /api/contacts
POST /api/contacts

POST /api/messages/send

GET /api/tasks
POST /api/tasks
```

---

## 📊 Métricas

El sistema calcula métricas en tiempo real, por ejemplo:

```json
{
  "messages": {
    "sent": 120,
    "received": 110,
    "responseRate": 91.7
  },
  "tasks": {
    "completed": 6,
    "pending": 3,
    "overdue": 6
  }
}
```

---

## 🔐 Seguridad

* Autenticación con JWT
* Control por roles (admin / vendedor)
* Validación en backend (no depende del frontend)

---

## 💻 Cómo levantar el proyecto

### Backend

```bash
git clone https://github.com/No-Country-simulation/S03-26-Equipo-19-Web-App-Development.git

cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend

```bash
cd ../frontend
npm install
npm run dev
```

---

## 📄 Documentación API

* Local: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* Deploy: [https://crm-cross-industry.onrender.com/swagger-ui/index.html](https://crm-cross-industry.onrender.com/swagger-ui/index.html)

---

## 🚀 Deploy

El backend está desplegado en Render y se actualiza automáticamente con cada push a `main`.

---

## 🤔 Decisiones que tomé

* Usar JWT para evitar manejo de sesiones
* Mantener métricas calculadas (no persistidas) para evitar inconsistencias
* Separar completamente frontend y backend (API REST)
* Zustand para mantener simple el estado del frontend

---

## 📌 Qué destacaría de este proyecto

* No es un CRUD simple → tiene lógica real de negocio
* Integra APIs externas (WhatsApp / Email)
* Maneja estados de conversación y seguimiento
* Tiene estructura pensada para escalar

---

## 🔗 Enlaces

GitHub:
[https://github.com/No-Country-simulation/S03-26-Equipo-19-Web-App-Development](https://github.com/No-Country-simulation/S03-26-Equipo-19-Web-App-Development)

Figma:
[https://www.figma.com/design/bzmxmhExMhzrfNQO3QNiX2/S03-26-Equipo19-CRM](https://www.figma.com/design/bzmxmhExMhzrfNQO3QNiX2/S03-26-Equipo19-CRM)

Trello:
[https://trello.com/invite/b/69b95cf56a09087814b3018f/ATTI4206acc2ec080b1edef95ef7cdc4a251191D5486/startup-crm-cross-industry](https://trello.com/invite/b/69b95cf56a09087814b3018f/ATTI4206acc2ec080b1edef95ef7cdc4a251191D5486/startup-crm-cross-industry)

Deploy Backend:
[https://crm-cross-industry.onrender.com/swagger-ui/index.html](https://crm-cross-industry.onrender.com/swagger-ui/index.html)

---

## 🧠 Cierre

Este proyecto lo pensamos como algo que realmente podría usar una startup.

Más allá de la tecnología, la idea fue resolver un problema real:
👉 ordenar el proceso comercial y hacerlo medible.

---


