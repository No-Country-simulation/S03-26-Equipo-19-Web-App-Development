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
import java.util.concurrent.ThreadLocalRandom;

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

    private static final Random RANDOM = new Random();
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final LocalDateTime TWO_MONTHS_AGO = NOW.minusDays(60);

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (alreadySeeded()) {
            log.info("Data already seeded. Skipping...");
            return;
        }

        log.info("Starting data seeding...");

        seedAdmin();
        seedSalespersons();

        List<Tag> tags = seedTags();

        User admin = userRepository.findByEmail("admin@crm.com").orElseThrow();
        List<User> salespersons = userRepository.findByRole(Role.SALESPERSON);

        List<Template> templates = seedTemplates(admin, salespersons);

        List<Contact> contacts = seedContactsWithProgressiveDates(salespersons, tags, admin);

        seedConversationsWithProgressiveMessages(contacts, salespersons, templates, admin);

        seedTasksWithProgressiveDates(contacts, salespersons);

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
                .createdAt(TWO_MONTHS_AGO.plusDays(10))
                .build();

        userRepository.save(admin);
        log.info("Admin seeded");
    }

    private void seedSalespersons() {
        record Seed(String name, String email, String password, boolean active, LocalDateTime createdAt) {}

        List<Seed> salespersons = List.of(
                new Seed("Alice Johnson", "alice@crm.com", "Sales001!", true, TWO_MONTHS_AGO.plusDays(15)),
                new Seed("Bob Martinez", "bob@crm.com", "Sales002!", true, TWO_MONTHS_AGO.plusDays(25)),
                new Seed("Carol Smith", "carol@crm.com", "Sales003!", true, TWO_MONTHS_AGO.plusDays(35)),
                new Seed("David Brown", "david@crm.com", "Sales004!", true, TWO_MONTHS_AGO.plusDays(45)),
                new Seed("Emma Wilson", "emma@crm.com", "Sales005!", true, TWO_MONTHS_AGO.plusDays(50)),
                new Seed("Frank Miller", "frank@crm.com", "Sales006!", false, TWO_MONTHS_AGO.plusDays(40)),
                new Seed("Grace Lee", "grace@crm.com", "Sales007!", true, TWO_MONTHS_AGO.plusDays(55)),
                new Seed("Henry Clark", "henry@crm.com", "Sales008!", false, TWO_MONTHS_AGO.plusDays(5)),
                new Seed("Ivy Adams", "ivy@crm.com", "Sales009!", true, TWO_MONTHS_AGO.plusDays(58)),
                new Seed("Jack Turner", "jack@crm.com", "Sales010!", true, TWO_MONTHS_AGO.plusDays(60))
        );

        salespersons.forEach(s -> {
            if (userRepository.existsByEmail(s.email())) return;
            userRepository.save(User.builder()
                    .name(s.name())
                    .email(s.email())
                    .passwordHash(encoder.encode(s.password()))
                    .role(Role.SALESPERSON)
                    .active(s.active())
                    .createdAt(s.createdAt())
                    .build());
            log.info("Salesperson seeded: {}", s.email());
        });
    }

    private List<Tag> seedTags() {
        List<Tag> tags = List.of(
                Tag.builder().name("alta prioridad").color("#EF4444").build(),
                Tag.builder().name("lead caliente").color("#F59E0B").build(),
                Tag.builder().name("fintech").color("#10B981").build(),
                Tag.builder().name("e-commerce").color("#3B82F6").build(),
                Tag.builder().name("lead frío").color("#6B7280").build(),
                Tag.builder().name("referido").color("#8B5CF6").build()
        );

        List<Tag> savedTags = new ArrayList<>();
        for (Tag tag : tags) {
            String normalizedName = tag.getName().trim();
            if (!tagRepository.existsByNameIgnoreCase(normalizedName)) {
                savedTags.add(tagRepository.save(tag));
            } else {
                savedTags.add(tagRepository.findByNameIgnoreCase(normalizedName).orElseThrow());
            }
            log.info("Tag seeded: {}", tag.getName());
        }
        return savedTags;
    }

    private List<Template> seedTemplates(User admin, List<User> salespersons) {
        // Mantener templates existentes sin cambios
        List<Template> templates = new ArrayList<>();

        // ==================== PLANTILLAS ADMIN (GLOBALES) ====================

        templates.add(createTemplate(admin, "Email de bienvenida - Lead", Channel.EMAIL,
                "✅ Bienvenido a nuestro ecosistema",
                "Hola {{name}},\n\nGracias por contactarte con nosotros. Hemos recibido tu consulta y será derivada a nuestro equipo de ventas.\n\nEn las próximas horas, un asesor se comunicará contigo.\n\nSaludos cordiales,\nEquipo de Ventas",
                "{\"name\":\"string\"}"));

        templates.add(createTemplate(admin, "Bienvenida automática - WhatsApp", Channel.WHATSAPP,
                null,
                "👋 Hola {{name}}! Gracias por contactarte con nosotros.\n\nSoy el asistente virtual de CRM Cross-Industry. 🚀\n\nTe informo que tu consulta ha sido recibida y será derivada a uno de nuestros asesores comerciales en breve.\n\n¡Gracias por tu paciencia!",
                "{\"name\":\"string\"}"));

        templates.add(createTemplate(admin, "WhatsApp - Primer contacto", Channel.WHATSAPP,
                null,
                "👋 Hola {{name}}! Soy {{salesperson}}, asesor de la empresa.\n\nRecibimos tu consulta y queremos ayudarte.\n\n¿Podrías contarnos un poco más sobre lo que necesitas?\n\n¡Quedo atento a tu respuesta!",
                "{\"name\":\"string\",\"salesperson\":\"string\"}"));

        templates.add(createTemplate(admin, "Email - Seguimiento post-reunión", Channel.EMAIL,
                "📌 Seguimiento de nuestra reunión",
                "Hola {{name}},\n\nFue un placer reunirnos. Quedo atento a tus comentarios.\n\nSaludos,\n{{salesperson}}",
                "{\"name\":\"string\",\"salesperson\":\"string\"}"));

        templates.add(createTemplate(admin, "Email - Propuesta comercial", Channel.EMAIL,
                "📊 Propuesta comercial para {{company}}",
                "Hola {{name}},\n\nAdjunto encontrarás la propuesta comercial para {{company}}.\n\nSaludos,\n{{salesperson}}",
                "{\"name\":\"string\",\"company\":\"string\",\"salesperson\":\"string\"}"));

        templates.add(createTemplate(admin, "WhatsApp - Bienvenida y propósito", Channel.WHATSAPP,
                null,
                "🎉 Hola {{name}}! Bienvenido a CRM Cross-Industry.\n\nSoy {{salesperson}}, tu asesor comercial.\n\n¿Podemos coordinar una breve charla?\n\n¡Quedo atento!",
                "{\"name\":\"string\",\"salesperson\":\"string\"}"));

        templates.add(createTemplate(admin, "Email - Bienvenida y propósito", Channel.EMAIL,
                "🎯 Te contactamos para ayudarte a crecer",
                "Hola {{name}},\n\nNos comunicamos para ayudarte a potenciar tus resultados.\n\nSoy {{salesperson}}, tu asesor comercial.\n\n¿Podemos coordinar una breve reunión?\n\nSaludos,\n{{salesperson}}",
                "{\"name\":\"string\",\"salesperson\":\"string\"}"));

        // Mantener plantillas de vendedores
        User alice = salespersons.stream().filter(u -> u.getEmail().equals("alice@crm.com")).findFirst().orElse(null);
        if (alice != null) {
            templates.add(createTemplate(alice, "Alice - Demo agendada", Channel.EMAIL,
                    "Demo CRM - {{date}}",
                    "Hola {{name}}, agendamos la demo para el {{date}} a las {{time}}. Te espero.",
                    "{\"name\":\"string\",\"date\":\"string\",\"time\":\"string\"}"));
        }

        List<Template> savedTemplates = new ArrayList<>();
        for (Template template : templates) {
            if (!templateRepository.existsByNameAndCreatedByRole(template.getName(), template.getCreatedBy().getRole())) {
                savedTemplates.add(templateRepository.save(template));
            }
        }
        return savedTemplates;
    }

    private Template createTemplate(User createdBy, String name, Channel channel, String subject, String body, String variables) {
        return Template.builder()
                .name(name)
                .channel(channel)
                .subject(subject)
                .body(body)
                .variables(variables)
                .createdBy(createdBy)
                .createdAt(NOW.minusDays(RANDOM.nextInt(50)))
                .build();
    }

    private List<Contact> seedContactsWithProgressiveDates(List<User> salespersons, List<Tag> tags, User admin) {
        List<Contact> contacts = new ArrayList<>();

        // 30 contactos totales
        // NEW_LEAD: 10 (creados recientemente, owner = admin)
        // CONTACTED: 5
        // IN_NEGOTIATION: 5
        // PROPOSAL_SENT: 4
        // CLOSED_WON: 3
        // CLOSED_LOST: 3

        String[][] contactNames = {
                {"TechCorp", "Carlos", "Rodríguez", "carlos@techcorp.com", "541123456701"},
                {"EcomStore", "Ana", "Martínez", "ana@ecomstore.com", "541123456702"},
                {"Fintech IO", "Martín", "González", "martin@fintech.io", "541123456703"},
                {"StartupX", "Laura", "Fernández", "laura@startup.com", "541123456704"},
                {"SaaS Solutions", "Javier", "López", "javier@saas.com", "541123456705"},
                {"Retail Plus", "Sofía", "Díaz", "sofia@retail.com", "541123456706"},
                {"Logística Express", "Diego", "Sánchez", "diego@logistica.com", "541123456707"},
                {"Health Tech", "Valentina", "Pérez", "valentina@health.com", "541123456708"},
                {"Marketing Pro", "Nicolás", "Romero", "nico@marketing.com", "541123456709"},
                {"Consulting Group", "Camila", "Morales", "camila@consulting.com", "541123456710"},
                {"Cloud Solutions", "Pedro", "Ramírez", "pedro@cloud.com", "541123456711"},
                {"Dev House", "Lucía", "Castillo", "lucia@dev.com", "541123456712"},
                {"AI Labs", "Mateo", "Ortiz", "mateo@ai.com", "541123456713"},
                {"Data Corp", "Renata", "Silva", "renata@data.com", "541123456714"},
                {"Blockchain Tech", "Facundo", "Núñez", "facundo@blockchain.com", "541123456715"},
                {"BioTech", "Agustina", "Paz", "agustina@biotech.com", "541123456716"},
                {"Green Energy", "Tomás", "Ríos", "tomas@green.com", "541123456717"},
                {"Media Group", "Florencia", "Molina", "flor@media.com", "541123456718"},
                {"Game Studio", "Santiago", "Vega", "santiago@games.com", "541123456719"},
                {"Travel Tech", "Victoria", "Luna", "victoria@travel.com", "541123456720"},
                {"Real Estate", "Gabriel", "Flores", "gabriel@realestate.com", "541123456721"},
                {"Legal Tech", "Julieta", "Aguirre", "julieta@legal.com", "541123456722"},
                {"Construcción", "Emiliano", "Correa", "emiliano@construction.com", "541123456723"},
                {"Fashion Tech", "Mora", "Giménez", "mora@fashion.com", "541123456724"},
                {"Logistics Pro", "Bruno", "Rojas", "bruno@logistics.com", "541123456725"},
                {"EdTech", "Clara", "Vidal", "clara@edtech.com", "541123456726"},
                {"AgroTech", "Daniel", "Ponce", "daniel@agrotech.com", "541123456727"},
                {"Cyber Security", "Elena", "Suárez", "elena@cyber.com", "541123456728"},
                {"Robotics", "Fabián", "Luna", "fabian@robotics.com", "541123456729"},
                {"Space Tech", "Gloria", "Paz", "gloria@space.com", "541123456730"}
        };

        List<FunnelStatus> funnelDistribution = new ArrayList<>();
        // 10 NEW_LEAD (owner = admin, creados recientemente)
        for (int i = 0; i < 10; i++) funnelDistribution.add(FunnelStatus.NEW_LEAD);
        // 5 CONTACTED
        for (int i = 0; i < 5; i++) funnelDistribution.add(FunnelStatus.CONTACTED);
        // 5 IN_NEGOTIATION
        for (int i = 0; i < 5; i++) funnelDistribution.add(FunnelStatus.IN_NEGOTIATION);
        // 4 PROPOSAL_SENT
        for (int i = 0; i < 4; i++) funnelDistribution.add(FunnelStatus.PROPOSAL_SENT);
        // 3 CLOSED_WON
        for (int i = 0; i < 3; i++) funnelDistribution.add(FunnelStatus.CLOSED_WON);
        // 3 CLOSED_LOST
        for (int i = 0; i < 3; i++) funnelDistribution.add(FunnelStatus.CLOSED_LOST);

        Collections.shuffle(funnelDistribution);

        for (int i = 0; i < 30 && i < contactNames.length; i++) {
            String[] data = contactNames[i];
            FunnelStatus funnelStatus = funnelDistribution.get(i);

            LocalDateTime createdAt;
            User owner;

            if (funnelStatus == FunnelStatus.NEW_LEAD) {
                // NEW_LEAD: creados en los últimos 3 días, owner = admin
                createdAt = NOW.minusDays(RANDOM.nextInt(3));
                owner = admin;
            } else {
                // Contactos avanzados: fechas distribuidas en los últimos 2 meses
                createdAt = TWO_MONTHS_AGO.plusDays(RANDOM.nextInt(55));
                // Asignar a un vendedor aleatorio
                owner = salespersons.get(RANDOM.nextInt(salespersons.size()));
            }

            Channel preferredChannel = RANDOM.nextBoolean() ? Channel.WHATSAPP : Channel.EMAIL;
            int tagIndex = RANDOM.nextInt(tags.size());

            Contact contact = Contact.builder()
                    .name(data[1])
                    .lastName(data[2])
                    .email(data[3])
                    .phone(data[4])
                    .company(data[0])
                    .funnelStatus(funnelStatus)
                    .preferredChannel(preferredChannel)
                    .owner(owner)
                    .createdAt(createdAt)
                    .build();

            Set<Tag> contactTags = new HashSet<>();
            contactTags.add(tags.get(tagIndex));
            if (RANDOM.nextBoolean() && tagIndex + 1 < tags.size()) {
                contactTags.add(tags.get(tagIndex + 1));
            }
            contact.setTags(contactTags);

            contacts.add(contactRepository.save(contact));
            log.info("Contact seeded: {} - {} - creado: {}", contact.getName(), contact.getFunnelStatus(), contact.getCreatedAt());
        }

        log.info("Contacts seeded: {} total", contacts.size());
        return contacts;
    }

    private void seedConversationsWithProgressiveMessages(List<Contact> contacts, List<User> salespersons,
                                                          List<Template> templates, User admin) {
        List<String> inboundPhrases = List.of(
                "Hola, me interesa saber más sobre sus servicios.",
                "¿Podrían enviarme más información?",
                "¿Cuánto cuesta la implementación?",
                "¿Tienen disponible una demo?",
                "Me interesa avanzar con la propuesta.",
                "¿Hay algún descuento por pago anual?",
                "Lo reviso con mi equipo y te confirmo.",
                "Perfecto, gracias por la información.",
                "¿Cómo seguimos con el contrato?",
                "¡Excelente! Me parece muy bien."
        );

        List<String> outboundPhrases = List.of(
                "Gracias por contactarte. Te cuento los detalles del plan.",
                "Te paso la propuesta comercial completa.",
                "Podemos coordinar una demo para esta semana.",
                "Te envío el contrato para revisar.",
                "Quedo atento a tu respuesta.",
                "Te explico cómo funciona la integración.",
                "Avancemos con la firma del contrato.",
                "¡Bienvenido! Me alegra que te guste.",
                "Te paso el link de pago.",
                "Gracias por tu interés."
        );

        for (Contact contact : contacts) {
            boolean hasPhone = contact.getPhone() != null && !contact.getPhone().isBlank();
            boolean hasEmail = contact.getEmail() != null && !contact.getEmail().isBlank();

            boolean isNewLead = contact.getFunnelStatus() == FunnelStatus.NEW_LEAD;
            User owner = contact.getOwner();
            LocalDateTime contactCreatedAt = contact.getCreatedAt();

            // Determinar número de mensajes según el estado del funnel
            int messageCount = switch (contact.getFunnelStatus()) {
                case NEW_LEAD -> 1;
                case CONTACTED -> 3 + RANDOM.nextInt(3);
                case IN_NEGOTIATION -> 6 + RANDOM.nextInt(4);
                case PROPOSAL_SENT -> 10 + RANDOM.nextInt(5);
                case CLOSED_WON, CLOSED_LOST -> 15 + RANDOM.nextInt(8);
                default -> 2 + RANDOM.nextInt(3);
            };

            // Si es NEW_LEAD, solo crear conversación WhatsApp (o email) con un mensaje inbound
            if (isNewLead) {
                if (hasPhone) {
                    createWhatsAppConversationWithMessages(contact, admin, List.of(inboundPhrases.get(0)),
                            contactCreatedAt.plusHours(1), true, 0);
                }
                if (hasEmail) {
                    createEmailConversationWithMessages(contact, admin, List.of(inboundPhrases.get(0)),
                            contactCreatedAt.plusHours(2), true, 0);
                }
                continue;
            }

            // Para contactos avanzados, crear conversaciones completas
            List<String> inboundMessages = new ArrayList<>();
            List<String> outboundMessages = new ArrayList<>();

            // Generar mensajes según el estado
            int inboundCount = messageCount / 2;
            int outboundCount = messageCount - inboundCount;

            for (int i = 0; i < inboundCount; i++) {
                inboundMessages.add(inboundPhrases.get(RANDOM.nextInt(inboundPhrases.size())));
            }
            for (int i = 0; i < outboundCount; i++) {
                outboundMessages.add(outboundPhrases.get(RANDOM.nextInt(outboundPhrases.size())));
            }

            // Intercalar mensajes
            List<String> allMessages = new ArrayList<>();
            for (int i = 0; i < Math.max(inboundCount, outboundCount); i++) {
                if (i < inboundMessages.size()) allMessages.add("INBOUND:" + inboundMessages.get(i));
                if (i < outboundMessages.size()) allMessages.add("OUTBOUND:" + outboundMessages.get(i));
            }

            // Crear conversaciones y mensajes
            if (hasPhone) {
                createWhatsAppConversationWithMessages(contact, owner, allMessages, contactCreatedAt, false, messageCount);
            }
            if (hasEmail) {
                createEmailConversationWithMessages(contact, owner, allMessages, contactCreatedAt, false, messageCount);
            }
        }

        log.info("Conversations and messages seeded");
    }

    private void createWhatsAppConversationWithMessages(Contact contact, User sender, List<String> messages,
                                                        LocalDateTime startTime, boolean onlyInbound, int expectedCount) {
        Conversation conversation = Conversation.builder()
                .contact(contact)
                .channel(Channel.WHATSAPP)
                .status(ConversationStatus.OPEN)
                .assignedTo(sender)
                .lastInteraction(startTime)
                .createdAt(startTime.minusMinutes(5))
                .build();
        conversation = conversationRepository.save(conversation);

        LocalDateTime messageTime = startTime;
        int messageIndex = 0;

        for (String msg : messages) {
            boolean isInbound = msg.startsWith("INBOUND:") || (onlyInbound && messageIndex == 0);
            String body = msg.replace("INBOUND:", "").replace("OUTBOUND:", "");

            Message message = Message.builder()
                    .conversation(conversation)
                    .direction(isInbound ? MessageDirection.INBOUND : MessageDirection.OUTBOUND)
                    .body(body)
                    .deliveryStatus(isInbound ? DeliveryStatus.READ : DeliveryStatus.DELIVERED)
                    .sender(isInbound ? null : sender)
                    .providerId(generateWhatsAppProviderId())
                    .sentAt(messageTime)
                    .build();
            messageRepository.save(message);

            messageTime = messageTime.plusHours(RANDOM.nextInt(24) + 1);
            messageIndex++;
        }

        conversation.setLastInteraction(messageTime.minusHours(1));
        conversationRepository.save(conversation);
    }

    private void createEmailConversationWithMessages(Contact contact, User sender, List<String> messages,
                                                     LocalDateTime startTime, boolean onlyInbound, int expectedCount) {
        Conversation conversation = Conversation.builder()
                .contact(contact)
                .channel(Channel.EMAIL)
                .status(ConversationStatus.OPEN)
                .assignedTo(sender)
                .lastInteraction(startTime)
                .createdAt(startTime.minusMinutes(5))
                .build();
        conversation = conversationRepository.save(conversation);

        LocalDateTime messageTime = startTime;

        for (String msg : messages) {
            boolean isInbound = msg.startsWith("INBOUND:") || onlyInbound;
            String body = msg.replace("INBOUND:", "").replace("OUTBOUND:", "");

            Message message = Message.builder()
                    .conversation(conversation)
                    .direction(isInbound ? MessageDirection.INBOUND : MessageDirection.OUTBOUND)
                    .body(body)
                    .deliveryStatus(isInbound ? DeliveryStatus.READ : DeliveryStatus.DELIVERED)
                    .sender(isInbound ? null : sender)
                    .providerId(generateEmailProviderId())
                    .sentAt(messageTime)
                    .build();
            messageRepository.save(message);

            messageTime = messageTime.plusHours(RANDOM.nextInt(24) + 1);
        }

        conversation.setLastInteraction(messageTime.minusHours(1));
        conversationRepository.save(conversation);
    }

    private void seedTasksWithProgressiveDates(List<Contact> contacts, List<User> salespersons) {
        int totalTasks = 0;

        // Tareas vencidas (fechas pasadas)
        for (int i = 0; i < 15; i++) {
            Contact contact = contacts.get(RANDOM.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();
            LocalDateTime dueDate = NOW.minusDays(RANDOM.nextInt(30) + 1);

            Task task = Task.builder()
                    .title("Llamada de seguimiento pendiente")
                    .description("Contactar a " + contact.getName() + " para resolver dudas")
                    .type(TaskType.CALL)
                    .status(TaskStatus.OVERDUE)
                    .dueDate(dueDate)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(dueDate.minusDays(RANDOM.nextInt(5)))
                    .build();
            taskRepository.save(task);
            totalTasks++;
        }

        // Tareas para hoy
        for (int i = 0; i < 8; i++) {
            Contact contact = contacts.get(RANDOM.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();

            Task task = Task.builder()
                    .title("Enviar propuesta comercial")
                    .description("Enviar propuesta a " + contact.getName() + " de " + contact.getCompany())
                    .type(TaskType.EMAIL)
                    .status(TaskStatus.PENDING)
                    .dueDate(NOW.plusHours(RANDOM.nextInt(12)))
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(NOW.minusDays(1))
                    .build();
            taskRepository.save(task);
            totalTasks++;
        }

        // Tareas futuras (próximos días)
        String[] taskTitles = {
                "Coordinar reunión de seguimiento",
                "Enviar materiales adicionales",
                "Llamada de cierre",
                "Revisar contrato con el cliente",
                "Agendar demo del producto",
                "Seguimiento post-venta"
        };

        for (int i = 0; i < 20; i++) {
            Contact contact = contacts.get(RANDOM.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();
            LocalDateTime dueDate = NOW.plusDays(RANDOM.nextInt(14) + 1);

            Task task = Task.builder()
                    .title(taskTitles[RANDOM.nextInt(taskTitles.length)])
                    .description("Seguimiento con " + contact.getName() + " - " + contact.getCompany())
                    .type(RANDOM.nextBoolean() ? TaskType.CALL : TaskType.EMAIL)
                    .status(TaskStatus.PENDING)
                    .dueDate(dueDate)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(NOW.minusDays(RANDOM.nextInt(3)))
                    .build();
            taskRepository.save(task);
            totalTasks++;
        }

        // Tareas completadas
        for (int i = 0; i < 12; i++) {
            Contact contact = contacts.get(RANDOM.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();
            LocalDateTime dueDate = NOW.minusDays(RANDOM.nextInt(20) + 1);
            LocalDateTime completedAt = dueDate.plusHours(RANDOM.nextInt(48));

            Task task = Task.builder()
                    .title("Reunión con cliente completada")
                    .description("Reunión con " + contact.getName() + " para presentar la solución")
                    .type(TaskType.MEETING)
                    .status(TaskStatus.COMPLETED)
                    .dueDate(dueDate)
                    .completedAt(completedAt)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(dueDate.minusDays(RANDOM.nextInt(5)))
                    .build();
            taskRepository.save(task);
            totalTasks++;
        }

        log.info("Tasks seeded: {} total tasks", totalTasks);
    }

    private String generateWhatsAppProviderId() {
        return "wamid.HBgM" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    private String generateEmailProviderId() {
        return "<" + System.currentTimeMillis() + "." + RANDOM.nextInt(100000) + "@smtp-relay.brevo.com>";
    }

    private void seedSavedViews(User admin, User salesperson, List<Tag> tags) {
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

        SavedView tasksView = SavedView.builder()
                .name("Tareas pendientes")
                .filters(json("{\"status\":\"PENDING\"}"))
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