package com.crm.app.config;

import com.crm.app.model.*;
import com.crm.app.model.enums.*;
import com.crm.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {
    
    ObjectMapper mapper = new ObjectMapper();

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final TagRepository tagRepository;
    private final TemplateRepository templateRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final TaskRepository taskRepository;
    private final SavedViewRepository savedViewRepository;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (alreadySeeded()) {
            log.info("Data already seeded. Skipping...");
            return;
        }

        log.info("Starting data seeding...");

        // 1. Seed users (ya lo tenés)
        seedAdmin();
        seedSalespersons();

        // 2. Seed tags
        List<Tag> tags = seedTags();

        // 3. Get users
        User admin = userRepository.findByEmail("admin@crm.com").orElseThrow();
        List<User> salespersons = userRepository.findByRole(Role.SALESPERSON);

        // 4. Seed templates
        List<Template> templates = seedTemplates(admin);

        // 5. Seed contacts with relationships
        List<Contact> contacts = seedContacts(salespersons, tags);

        // 6. Seed conversations and messages
        seedConversationsAndMessages(contacts, salespersons, templates);

        // 7. Seed tasks
        seedTasks(contacts, salespersons);

        // 8. Seed saved views
       seedSavedViews(admin, salespersons.getFirst(), tags);

        log.info("Data seeding completed successfully!");
    }

    private boolean alreadySeeded() {
        return savedViewRepository.count() > 0;
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@crm.com")) return;

        User admin = User.builder()
                .name("Administrator")
                .email("admin@crm.com")
                .passwordHash(encoder.encode("Admin1234!"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);
        log.info("Admin seeded: admin@crm.com / Admin1234!");
    }

    private void seedSalespersons() {
        record Seed(String name, String email, String password) {}

        List<Seed> salespersons = List.of(
                new Seed("Alice Johnson",  "alice@crm.com",  "Sales001!"),
                new Seed("Bob Martinez",   "bob@crm.com",    "Sales002!"),
                new Seed("Carol Smith",    "carol@crm.com",  "Sales003!"),
                new Seed("David Brown",    "david@crm.com",  "Sales004!"),
                new Seed("Emma Wilson",    "emma@crm.com",   "Sales005!")
        );

        salespersons.forEach(s -> {
            if (userRepository.existsByEmail(s.email())) return;

            User user = User.builder()
                    .name(s.name())
                    .email(s.email())
                    .passwordHash(encoder.encode(s.password()))
                    .role(Role.SALESPERSON)
                    .active(true)
                    .build();

            userRepository.save(user);
            log.info("Salesperson seeded: {} / {}", s.email(), s.password());
        });
    }

    private List<Tag> seedTags() {
        List<Tag> tags = Arrays.asList(
                Tag.builder().name("Alta prioridad").color("#EF4444").description("Clientes con urgencia de compra").build(),
                Tag.builder().name("Lead caliente").color("#F59E0B").description("Alto interés, cerca de cerrar").build(),
                Tag.builder().name("Fintech").color("#10B981").description("Industria financiera/tecnológica").build(),
                Tag.builder().name("E-commerce").color("#3B82F6").description("Tiendas online").build(),
                Tag.builder().name("Lead frío").color("#6B7280").description("Poco interés, seguimiento largo").build(),
                Tag.builder().name("Referido").color("#8B5CF6").description("Llegó por recomendación").build()
        );

        List<Tag> savedTags = new ArrayList<>();
        for (Tag tag : tags) {
            if (!tagRepository.existsByName(tag.getName())) {
                savedTags.add(tagRepository.save(tag));
                log.info("Tag seeded: {}", tag.getName());
            } else {
                savedTags.add(tagRepository.findByName(tag.getName()).orElseThrow());
            }
        }
        return savedTags;
    }

    private List<Template> seedTemplates(User admin) {
        List<Template> templates = Arrays.asList(
                Template.builder()
                        .name("Email de bienvenida")
                        .channel(Channel.EMAIL)
                        .subject("✅ Hemos recibido tu consulta - {{company}}")
                        .body("""
                                Estimado/a {{name}},
                                
                                Gracias por contactarte con {{company}}.
                                
                                Hemos recibido tu mensaje correctamente y será derivado a nuestro equipo de ventas.
                                
                                En las próximas horas, uno de nuestros asesores se comunicará contigo para brindarte la información que necesitas.
                                
                                Mientras tanto, puedes responder este mismo correo si tienes alguna pregunta adicional.
                                
                                """)
                        .variables("{\"name\":\"string\",\"company\":\"string\"}")
                        .createdBy(admin)
                        .build(),

                Template.builder()
                        .name("Seguimiento post-reunión")
                        .channel(Channel.EMAIL)
                        .subject("Seguimiento reunión - {{date}}")
                        .body("Hola {{name}},\n\nFue un placer reunirnos. Como quedamos, te envío la propuesta en archivo adjunto.\n\nQuedo atento a tus comentarios.\n\nSaludos,\n{{salesperson}}")
                        .variables("{\"name\":\"string\",\"date\":\"string\",\"salesperson\":\"string\"}")
                        .createdBy(admin)
                        .build(),

                Template.builder()
                        .name("WhatsApp - Primer contacto")
                        .channel(Channel.WHATSAPP)
                        .body("""
                                👋 Hola {{name}}! Gracias por escribirnos.
                                
                                Hemos recibido tu mensaje correctamente. 🚀
                                
                                En breve, uno de nuestros asesores se pondrá en contacto contigo para atender tu consulta.
                                
                                📌 Mientras tanto, si necesitas algo más, no dudes en escribirnos.
                                
                                ¡Gracias por contactarnos!""")
                        .variables("{\"name\":\"string\"}")
                        .createdBy(admin)
                        .build(),

                Template.builder()
                        .name("WhatsApp - Recordatorio demo")
                        .channel(Channel.WHATSAPP)
                        .body("Hola {{name}}! Te recuerdo que tenemos la demo agendada para mañana a las {{time}}. ¿Confirmás?")
                        .variables("{\"name\":\"string\",\"time\":\"string\"}")
                        .createdBy(admin)
                        .build(),

                Template.builder()
                        .name("Email - Propuesta comercial")
                        .channel(Channel.EMAIL)
                        .subject("Propuesta comercial para {{company}}")
                        .body("Hola {{name}},\n\nAdjunto encontrarás la propuesta comercial para {{company}}.\n\nEl valor total es de {{amount}} con un descuento del {{discount}}%.\n\nQuedo atento a tu confirmación.\n\nSaludos,\n{{salesperson}}")
                        .variables("{\"name\":\"string\",\"company\":\"string\",\"amount\":\"string\",\"discount\":\"string\",\"salesperson\":\"string\"}")
                        .createdBy(admin)
                        .build(),

                Template.builder()
                        .name("WhatsApp - Cierre ganado")
                        .channel(Channel.WHATSAPP)
                        .body("🎉 Excelente {{name}}! Bienvenido a CRM Cross-Industry. En las próximas horas recibirás los accesos. Cualquier duda, estoy acá. 🚀")
                        .variables("{\"name\":\"string\"}")
                        .createdBy(admin)
                        .build(),

                Template.builder()
                        .name("Email - Newsletter mensual")
                        .channel(Channel.EMAIL)
                        .subject("Newsletter {{month}} - Novedades CRM")
                        .body("Hola {{name}},\n\nEste mes lanzamos:\n• Nueva integración con WhatsApp\n• Reportes avanzados\n• Plantillas dinámicas\n\n¿Querés una demo? Respondé este mail.\n\nSaludos,\nEquipo CRM")
                        .variables("{\"name\":\"string\",\"month\":\"string\"}")
                        .createdBy(admin)
                        .build(),

                Template.builder()
                        .name("Bienvenida automática - WhatsApp")
                        .channel(Channel.WHATSAPP)
                        .body("👋 Hola {{name}}! Gracias por contactarte con nosotros.\n\n" +
                                "Soy el asistente virtual de CRM Cross-Industry. 🚀\n\n" +
                                "Te informo que tu consulta ha sido recibida y será derivada a uno de nuestros asesores comerciales en breve.\n\n" +
                                "Mientras tanto, ¿podrías contarnos un poco más sobre lo que necesitas? Así podemos ayudarte mejor.\n\n" +
                                "📌 *Importante:* Un agente te responderá a la brevedad.\n\n" +
                                "¡Gracias por tu paciencia!")
                        .variables("{\"name\":\"string\"}")
                        .createdBy(admin)
                        .build()
        );

        List<Template> savedTemplates = new ArrayList<>();
        for (Template template : templates) {
            // Verificar si ya existe una plantilla con el mismo nombre creada por ADMIN
            if (!templateRepository.existsByNameAndCreatedByRole(template.getName(), Role.ADMIN)) {
                savedTemplates.add(templateRepository.save(template));
                log.info("Template seeded: {} (global, creada por ADMIN)", template.getName());
            } else {
                savedTemplates.add(templateRepository.findByName(template.getName()).orElseThrow());
                log.info("Template already exists: {}", template.getName());
            }
        }
        return savedTemplates;
    }

    private List<Contact> seedContacts(List<User> salespersons, List<Tag> tags) {
        List<Contact> contacts = new ArrayList<>();

        // Contact data: name, email, phone, company, source, funnelStatus, preferredChannel, ownerIndex, tagIndices
        Object[][] contactData = {
                {"Carlos", "Rodríguez", "carlos@techcorp.com", "541123456701", "TechCorp", FunnelStatus.IN_NEGOTIATION, Channel.WHATSAPP, 0, new int[]{0, 1}},
                {"Ana", "Martínez", "ana@ecomstore.com", "541123456702", "EcomStore", FunnelStatus.CONTACTED, Channel.EMAIL, 0, new int[]{1, 3}},
                {"Martín", "González", "martin@fintech.io", "541123456703", "Fintech IO", FunnelStatus.PROPOSAL_SENT, Channel.WHATSAPP, 1, new int[]{0, 2}},
                {"Laura", "Fernández", "laura@startup.com", "541123456704", "StartupX", FunnelStatus.NEW_LEAD, Channel.EMAIL, 1, new int[]{4}},
                {"Javier", " López", "javier@saas.com", "541123456705", "SaaS Solutions", FunnelStatus.CLOSED_WON, Channel.WHATSAPP, 2, new int[]{1, 5}},
                {"Sofía", "Díaz", "sofia@retail.com", "541123456706", "Retail Plus", FunnelStatus.CLOSED_LOST, Channel.EMAIL, 2, new int[]{4}},
                {"Diego", "Sánchez", "diego@logistica.com", "541123456707", "Logística Express", FunnelStatus.IN_NEGOTIATION, Channel.WHATSAPP, 3, new int[]{0, 2}},
                {"Valentina", "Pérez", "valentina@health.com", "541123456708", "Health Tech", FunnelStatus.CONTACTED, Channel.EMAIL, 3, new int[]{2, 5}},
                {"Nicolás", "Romero", "nico@marketing.com", "541123456709", "Marketing Pro", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 4, new int[]{3, 4}},
                {"Camila", "Morales", "camila@consulting.com", "541123456710", "Consulting Group", FunnelStatus.PROPOSAL_SENT, Channel.EMAIL, 4, new int[]{0, 1, 2}}
        };

        for (Object[] data : contactData) {
            User owner = salespersons.get((Integer) data[7]);

            Contact contact = Contact.builder()
                    .name((String) data[0])
                    .lastName((String) data[1])
                    .email((String) data[2])
                    .phone((String) data[3])
                    .company((String) data[4])
                    .funnelStatus((FunnelStatus) data[5])
                    .preferredChannel((Channel) data[6])
                    .owner(owner)
                    .build();

            // Add tags
            int[] tagIndices = (int[]) data[8];
            Set<Tag> contactTags = new HashSet<>();
            for (int idx : tagIndices) {
                contactTags.add(tags.get(idx));
            }
            contact.setTags(contactTags);

            contacts.add(contactRepository.save(contact));
            log.info("Contact seeded: {}", contact.getName());
        }

        return contacts;
    }

    private void seedConversationsAndMessages(List<Contact> contacts, List<User> salespersons, List<Template> templates) {
        Random random = new Random();

        String[] inboundMessages = {
                "Perfecto, gracias!",
                "¿Me podés contar más?",
                "¿Cuánto cuesta?",
                "Me interesa avanzar.",
                "¿Tienen demo?",
                "Lo reviso y te digo.",
                "Buenísimo!",
                "¿Cómo seguimos?",
                "¿Hay algún descuento?",
                "Gracias por la info."
        };

        String[] outboundMessages = {
                "Genial, te cuento.",
                "Te paso más detalles.",
                "Podemos coordinar una demo.",
                "Te envío info ahora.",
                "Trabajamos con empresas similares.",
                "Quedo atento.",
                "Te explico cómo funciona.",
                "Avancemos si querés.",
                "Te paso propuesta.",
                "Gracias por tu interés."
        };

        for (Contact contact : contacts) {

            Conversation whatsappConv = Conversation.builder()
                    .contact(contact)
                    .channel(Channel.WHATSAPP)
                    .status(ConversationStatus.OPEN)
                    .assignedTo(contact.getOwner())
                    .lastInteraction(LocalDateTime.now().minusDays(random.nextInt(10)))
                    .build();
            conversationRepository.save(whatsappConv);

            Conversation emailConv = Conversation.builder()
                    .contact(contact)
                    .channel(Channel.EMAIL)
                    .status(ConversationStatus.OPEN)
                    .assignedTo(contact.getOwner())
                    .lastInteraction(LocalDateTime.now().minusDays(random.nextInt(10)))
                    .build();
            conversationRepository.save(emailConv);

            Template whatsappTemplate = templates.stream()
                    .filter(t -> t.getChannel() == Channel.WHATSAPP)
                    .findFirst()
                    .orElse(null);

            Message outbound = Message.builder()
                    .conversation(whatsappConv)
                    .direction(MessageDirection.OUTBOUND)
                    .body("Hola " + contact.getName() + "! Soy " + contact.getOwner().getName() + " de CRM. ¿Cómo estás? Me contacto para conocer más sobre " + contact.getCompany() + ".")
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .template(whatsappTemplate)
                    .sender(contact.getOwner())
                    .providerId(generateWhatsAppProviderId())
                    .sentAt(LocalDateTime.now().minusDays(5))
                    .build();
            messageRepository.save(outbound);

            Message inbound = Message.builder()
                    .conversation(whatsappConv)
                    .direction(MessageDirection.INBOUND)
                    .body("Hola! Gracias por contactarte. Me interesa saber más sobre sus servicios.")
                    .deliveryStatus(DeliveryStatus.READ)
                    .providerId(generateWhatsAppProviderId())
                    .sentAt(LocalDateTime.now().minusDays(4))
                    .build();
            messageRepository.save(inbound);

            // 🔥 NUEVOS 20 MENSAJES (solo WhatsApp, misma conversación)
            LocalDateTime baseTime = LocalDateTime.now().minusDays(3);

            for (int i = 0; i < 20; i++) {
                boolean isOutbound = i % 2 == 0;

                Message extraMessage = Message.builder()
                        .conversation(whatsappConv)
                        .direction(isOutbound ? MessageDirection.OUTBOUND : MessageDirection.INBOUND)
                        .body(isOutbound
                                ? outboundMessages[random.nextInt(outboundMessages.length)]
                                : inboundMessages[random.nextInt(inboundMessages.length)])
                        .deliveryStatus(isOutbound ? DeliveryStatus.DELIVERED : DeliveryStatus.READ)
                        .template(isOutbound ? whatsappTemplate : null)
                        .sender(isOutbound ? contact.getOwner() : null)
                        .providerId(generateWhatsAppProviderId())
                        .sentAt(baseTime.plusHours(i * 2))
                        .build();

                messageRepository.save(extraMessage);
            }

            Template emailTemplate = templates.stream()
                    .filter(t -> t.getChannel() == Channel.EMAIL && t.getName().contains("Bienvenida"))
                    .findFirst()
                    .orElse(null);

            Message emailOutbound = Message.builder()
                    .conversation(emailConv)
                    .direction(MessageDirection.OUTBOUND)
                    .body("Hola " + contact.getName() + ",\n\nTe escribo para presentarte nuestras soluciones. Quedo atento a tu respuesta.\n\nSaludos,\n" + contact.getOwner().getName())
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .template(emailTemplate)
                    .sender(contact.getOwner())
                    .providerId(generateEmailProviderId())
                    .sentAt(LocalDateTime.now().minusDays(3))
                    .build();
            messageRepository.save(emailOutbound);

            log.info("Conversations and messages seeded for contact: {}", contact.getName());
        }
    }

    private String generateWhatsAppProviderId() {
        return "wamid.HBgM" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    private String generateEmailProviderId() {
        return "<" + System.currentTimeMillis() + "." + new Random().nextInt(100000) + "@smtp-relay.brevo.com>";
    }

    private void seedTasks(List<Contact> contacts, List<User> salespersons) {
        Random random = new Random();

        for (int i = 0; i < 15; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();

            TaskType[] taskTypes = {TaskType.CALL, TaskType.EMAIL, TaskType.MEETING, TaskType.DEMO, TaskType.OTHER};
            TaskStatus[] taskStatuses = {TaskStatus.PENDING, TaskStatus.COMPLETED, TaskStatus.OVERDUE};

            Task task = Task.builder()
                    .title(getRandomTaskTitle(taskTypes[random.nextInt(taskTypes.length)]))
                    .description("Seguimiento con " + contact.getName() + " de " + contact.getCompany())
                    .type(taskTypes[random.nextInt(taskTypes.length)])
                    .status(taskStatuses[random.nextInt(3)])
                    .dueDate(LocalDateTime.now().plusDays(random.nextInt(10) - 3))
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .build();

            if (task.getStatus() == TaskStatus.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now().minusDays(random.nextInt(5)));
            }

            taskRepository.save(task);
            log.info("Task seeded: {} for contact {}", task.getTitle(), contact.getName());
        }
    }

    private String getRandomTaskTitle(TaskType type) {
        return switch (type) {
            case CALL -> "Llamada de seguimiento";
            case EMAIL -> "Enviar propuesta por email";
            case MEETING -> "Coordinar reunión";
            case DEMO -> "Agendar demo del producto";
            default -> "Contactar para actualizar información";
        };
    }

    private void seedSavedViews(User admin, User salesperson, List<Tag> tags) {

        Long tag1 = tags.get(0).getId();
        Long tag2 = tags.get(1).getId();

        // Admin global view - CONTACTS
        SavedView adminView = SavedView.builder()
                .name("Contactos en negociación")
                .filters(json("{\"funnelStatus\":\"IN_NEGOTIATION\"}"))
                .entity(EntityType.CONTACTS)
                .sortBy("createdAt")
                .sortOrder(SortOrder.DESC)
                .global(true)
                .user(admin)
                .build();
        savedViewRepository.save(adminView);

        // Salesperson personal view - CONTACTS
        SavedView sellerView = SavedView.builder()
                .name("Mis leads calientes")
                .filters(json("{\"tagIds\":[" + tag1 + "," + tag2 + "],\"funnelStatus\":\"IN_NEGOTIATION\"}"))
                .entity(EntityType.CONTACTS)
                .sortBy("createdAt") // ⚠️ cambiado
                .sortOrder(SortOrder.DESC)
                .global(false)
                .user(salesperson)
                .build();
        savedViewRepository.save(sellerView);

        // Tasks view - TASKS
        SavedView tasksView = SavedView.builder()
                .name("Tareas vencidas")
                .filters(json("{\"status\":\"PENDING\",\"dueDateTo\":\"2026-04-08\"}"))
                .entity(EntityType.TASKS)
                .sortBy("dueDate")
                .sortOrder(SortOrder.ASC)
                .global(true)
                .user(admin)
                .build();
        savedViewRepository.save(tasksView);
        }

    private JsonNode json(String value) {
        try {
                return mapper.readTree(value);
        } catch (Exception e) {
                throw new RuntimeException("JSON inválido en seed", e);
        }
        }
}