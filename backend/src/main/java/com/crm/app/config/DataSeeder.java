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

        seedAdmin();
        seedSalespersons();

        List<Tag> tags = seedTags();

        User admin = userRepository.findByEmail("admin@crm.com").orElseThrow();
        List<User> salespersons = userRepository.findByRole(Role.SALESPERSON);

        List<Template> templates = seedTemplates(admin);

        List<Contact> contacts = seedContactsWithDates(salespersons, tags);

        seedConversationsAndMessages(contacts, salespersons, templates);

        seedTasksWithDates(contacts, salespersons);

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
                .createdAt(LocalDateTime.of(2026, 1, 15, 10, 0))
                .build();

        userRepository.save(admin);
        log.info("Admin seeded");
    }

    private void seedSalespersons() {
        record Seed(String name, String email, String password, boolean active, LocalDateTime createdAt) {}

        List<Seed> salespersons = List.of(
                new Seed("Alice Johnson", "alice@crm.com", "Sales001!", true, LocalDateTime.of(2026, 1, 20, 9, 0)),
                new Seed("Bob Martinez", "bob@crm.com", "Sales002!", true, LocalDateTime.of(2026, 2, 1, 10, 30)),
                new Seed("Carol Smith", "carol@crm.com", "Sales003!", true, LocalDateTime.of(2026, 2, 15, 14, 0)),
                new Seed("David Brown", "david@crm.com", "Sales004!", true, LocalDateTime.of(2026, 3, 1, 11, 15)),
                new Seed("Emma Wilson", "emma@crm.com", "Sales005!", true, LocalDateTime.of(2026, 3, 10, 9, 45)),
                new Seed("Frank Miller", "frank@crm.com", "Sales006!", false, LocalDateTime.of(2026, 2, 20, 8, 0)),
                new Seed("Grace Lee", "grace@crm.com", "Sales007!", true, LocalDateTime.of(2026, 3, 25, 13, 30)),
                new Seed("Ivy Adams", "ivy@crm.com", "Sales008!", true, LocalDateTime.of(2026, 4, 1, 10, 0)),
                new Seed("Jack Turner", "jack@crm.com", "Sales009!", true, LocalDateTime.of(2026, 4, 5, 11, 30))
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

    private List<Template> seedTemplates(User admin) {
        List<Template> templates = Arrays.asList(
                Template.builder().name("Email de bienvenida").channel(Channel.EMAIL)
                        .subject("✅ Hemos recibido tu consulta")
                        .body("Gracias por contactarte con nosotros.")
                        .variables("{}").createdBy(admin).build(),
                Template.builder().name("WhatsApp - Primer contacto").channel(Channel.WHATSAPP)
                        .body("👋 Hola! Gracias por escribirnos.")
                        .variables("{}").createdBy(admin).build(),
                Template.builder().name("Bienvenida automática - WhatsApp").channel(Channel.WHATSAPP)
                        .body("👋 Bienvenido! Gracias por contactarte.")
                        .variables("{}").createdBy(admin).build()
        );

        List<Template> savedTemplates = new ArrayList<>();
        for (Template template : templates) {
            if (!templateRepository.existsByNameAndCreatedByRole(template.getName(), Role.ADMIN)) {
                savedTemplates.add(templateRepository.save(template));
            } else {
                savedTemplates.add(templateRepository.findByName(template.getName()).orElseThrow());
            }
            log.info("Template seeded: {}", template.getName());
        }
        return savedTemplates;
    }

    private List<Contact> seedContactsWithDates(List<User> salespersons, List<Tag> tags) {
        List<Contact> contacts = new ArrayList<>();

        // PERÍODO ANTERIOR (Marzo - para base de comparación)
        LocalDateTime march1 = LocalDateTime.of(2026, 3, 1, 10, 0);
        LocalDateTime march5 = LocalDateTime.of(2026, 3, 5, 10, 0);
        LocalDateTime march10 = LocalDateTime.of(2026, 3, 10, 14, 0);
        LocalDateTime march15 = LocalDateTime.of(2026, 3, 15, 10, 0);
        LocalDateTime march18 = LocalDateTime.of(2026, 3, 18, 11, 0);
        LocalDateTime march20 = LocalDateTime.of(2026, 3, 20, 9, 0);
        LocalDateTime march22 = LocalDateTime.of(2026, 3, 22, 15, 0);
        LocalDateTime march25 = LocalDateTime.of(2026, 3, 25, 16, 0);
        LocalDateTime march28 = LocalDateTime.of(2026, 3, 28, 11, 0);
        LocalDateTime march30 = LocalDateTime.of(2026, 3, 30, 13, 0);

// PERÍODO ACTUAL (Abril - para mostrar cambios)
        LocalDateTime april1 = LocalDateTime.of(2026, 4, 1, 8, 0);
        LocalDateTime april2 = LocalDateTime.of(2026, 4, 2, 10, 0);
        LocalDateTime april3 = LocalDateTime.of(2026, 4, 3, 10, 0);
        LocalDateTime april4 = LocalDateTime.of(2026, 4, 4, 9, 0);
        LocalDateTime april5 = LocalDateTime.of(2026, 4, 5, 14, 0);
        LocalDateTime april6 = LocalDateTime.of(2026, 4, 6, 11, 0);
        LocalDateTime april7 = LocalDateTime.of(2026, 4, 7, 9, 30);
        LocalDateTime april8 = LocalDateTime.of(2026, 4, 8, 13, 0);
        LocalDateTime april9 = LocalDateTime.of(2026, 4, 9, 11, 0);
        LocalDateTime april10 = LocalDateTime.of(2026, 4, 10, 15, 0);
        LocalDateTime april11 = LocalDateTime.of(2026, 4, 11, 15, 0);
        LocalDateTime april12 = LocalDateTime.of(2026, 4, 12, 10, 0);
        LocalDateTime april13 = LocalDateTime.of(2026, 4, 13, 10, 0);

        Object[][] contactData = {
                // ========== CONTACTOS MARZO (período anterior) ==========
                // NEW_LEAD en marzo: 4 contactos
                {"Carlos", "Rodríguez", "carlos@techcorp.com", "541123456701", "TechCorp", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 0, new int[]{0}, march1},
                {"Ana", "Martínez", "ana@ecomstore.com", "541123456702", "EcomStore", FunnelStatus.NEW_LEAD, Channel.EMAIL, 1, new int[]{1}, march10},
                {"Martín", "González", "martin@fintech.io", "541123456703", "Fintech IO", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 2, new int[]{2}, march20},
                {"Laura", "Fernández", "laura@startup.com", "541123456704", "StartupX", FunnelStatus.NEW_LEAD, Channel.EMAIL, 3, new int[]{3}, march28},

                // CONTACTED en marzo: 3 contactos
                {"Javier", "López", "javier@saas.com", "541123456705", "SaaS Solutions", FunnelStatus.CONTACTED, Channel.WHATSAPP, 4, new int[]{4}, march1},
                {"Sofía", "Díaz", "sofia@retail.com", "541123456706", "Retail Plus", FunnelStatus.CONTACTED, Channel.EMAIL, 0, new int[]{0}, march20},
                {"Diego", "Sánchez", "diego@logistica.com", "541123456707", "Logística Express", FunnelStatus.CONTACTED, Channel.WHATSAPP, 1, new int[]{1}, march25},

                // IN_NEGOTIATION en marzo: 3 contactos
                {"Valentina", "Pérez", "valentina@health.com", "541123456708", "Health Tech", FunnelStatus.IN_NEGOTIATION, Channel.EMAIL, 2, new int[]{2}, march10},
                {"Nicolás", "Romero", "nico@marketing.com", "541123456709", "Marketing Pro", FunnelStatus.IN_NEGOTIATION, Channel.WHATSAPP, 3, new int[]{3}, march20},
                {"Camila", "Morales", "camila@consulting.com", "541123456710", "Consulting Group", FunnelStatus.IN_NEGOTIATION, Channel.EMAIL, 4, new int[]{4}, march30},

                // PROPOSAL_SENT en marzo: 2 contactos
                {"Pedro", "Ramírez", "pedro@cloud.com", "541123456711", "Cloud Solutions", FunnelStatus.PROPOSAL_SENT, Channel.WHATSAPP, 0, new int[]{0}, march15},
                {"Lucía", "Castillo", "lucia@dev.com", "541123456712", "Dev House", FunnelStatus.PROPOSAL_SENT, Channel.EMAIL, 1, new int[]{1}, march25},

                // CLOSED_WON en marzo: 2 contactos
                {"Mateo", "Ortiz", "mateo@ai.com", "541123456713", "AI Labs", FunnelStatus.CLOSED_WON, Channel.WHATSAPP, 2, new int[]{2}, march5},
                {"Renata", "Silva", "renata@data.com", "541123456714", "Data Corp", FunnelStatus.CLOSED_WON, Channel.EMAIL, 3, new int[]{3}, march18},

                // CLOSED_LOST en marzo: 1 contacto
                {"Facundo", "Núñez", "facundo@blockchain.com", "541123456715", "Blockchain Tech", FunnelStatus.CLOSED_LOST, Channel.WHATSAPP, 4, new int[]{4}, march22},

                // ========== CONTACTOS ABRIL (período actual - para mostrar cambios) ==========
                // NEW_LEAD aumentó: 7 contactos (↑75% desde 4)
                {"Agustina", "Paz", "agustina@biotech.com", "541123456716", "BioTech", FunnelStatus.NEW_LEAD, Channel.EMAIL, 0, new int[]{0}, april1},
                {"Tomás", "Ríos", "tomas@green.com", "541123456717", "Green Energy", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 1, new int[]{1}, april3},
                {"Florencia", "Molina", "flor@media.com", "541123456718", "Media Group", FunnelStatus.NEW_LEAD, Channel.EMAIL, 2, new int[]{2}, april5},
                {"Santiago", "Vega", "santiago@games.com", "541123456719", "Game Studio", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 3, new int[]{3}, april7},
                {"Victoria", "Luna", "victoria@travel.com", "541123456720", "Travel Tech", FunnelStatus.NEW_LEAD, Channel.EMAIL, 4, new int[]{4}, april9},
                {"Gabriel", "Flores", "gabriel@realestate.com", "541123456721", "Real Estate", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 0, new int[]{0}, april11},
                {"Julieta", "Aguirre", "julieta@legal.com", "541123456722", "Legal Tech", FunnelStatus.NEW_LEAD, Channel.EMAIL, 1, new int[]{1}, april13},

                // CONTACTED disminuyó: 2 contactos (↓33% desde 3)
                {"Emiliano", "Correa", "emiliano@construction.com", "541123456723", "Construcción", FunnelStatus.CONTACTED, Channel.WHATSAPP, 2, new int[]{2}, april5},
                {"Mora", "Giménez", "mora@fashion.com", "541123456724", "Fashion Tech", FunnelStatus.CONTACTED, Channel.EMAIL, 3, new int[]{3}, april10},

                // IN_NEGOTIATION estable: 3 contactos (0% cambio)
                {"Bruno", "Rojas", "bruno@logistics.com", "541123456725", "Logistics Pro", FunnelStatus.IN_NEGOTIATION, Channel.WHATSAPP, 4, new int[]{4}, april2},
                {"Clara", "Vidal", "clara@edtech.com", "541123456726", "EdTech", FunnelStatus.IN_NEGOTIATION, Channel.EMAIL, 0, new int[]{0}, april8},
                {"Daniel", "Ponce", "daniel@agrotech.com", "541123456727", "AgroTech", FunnelStatus.IN_NEGOTIATION, Channel.WHATSAPP, 1, new int[]{1}, april12},

                // PROPOSAL_SENT aumentó: 4 contactos (↑100% desde 2)
                {"Elena", "Suárez", "elena@cyber.com", "541123456728", "Cyber Security", FunnelStatus.PROPOSAL_SENT, Channel.EMAIL, 2, new int[]{2}, april4},
                {"Fabián", "Luna", "fabian@robotics.com", "541123456729", "Robotics", FunnelStatus.PROPOSAL_SENT, Channel.WHATSAPP, 3, new int[]{3}, april6},
                {"Gloria", "Paz", "gloria@space.com", "541123456730", "Space Tech", FunnelStatus.PROPOSAL_SENT, Channel.EMAIL, 4, new int[]{4}, april9},
                {"Hugo", "Mora", "hugo@quantum.com", "541123456731", "Quantum Computing", FunnelStatus.PROPOSAL_SENT, Channel.WHATSAPP, 0, new int[]{0}, april11},

                // CLOSED_WON estable: 2 contactos (0% cambio)
                {"Irene", "Castro", "irene@nanotech.com", "541123456732", "NanoTech", FunnelStatus.CLOSED_WON, Channel.EMAIL, 1, new int[]{1}, april7},

                // CLOSED_LOST disminuyó: 0 contactos (↓100% desde 1)
                // (sin contactos CLOSED_LOST en abril - mejora!)
        };

        for (Object[] data : contactData) {
            User owner = salespersons.get((Integer) data[7]);
            LocalDateTime createdAt = (LocalDateTime) data[9];

            Contact contact = Contact.builder()
                    .name((String) data[0])
                    .lastName((String) data[1])
                    .email((String) data[2])
                    .phone((String) data[3])
                    .company((String) data[4])
                    .funnelStatus((FunnelStatus) data[5])
                    .preferredChannel((Channel) data[6])
                    .owner(owner)
                    .createdAt(createdAt)
                    .build();

            int[] tagIndices = (int[]) data[8];
            Set<Tag> contactTags = new HashSet<>();
            for (int idx : tagIndices) {
                if (idx < tags.size()) contactTags.add(tags.get(idx));
            }
            contact.setTags(contactTags);

            contacts.add(contactRepository.save(contact));
            log.info("Contact seeded: {} - {} - creado: {}", contact.getName(), contact.getFunnelStatus(), contact.getCreatedAt());
        }

        return contacts;
    }

    private void seedConversationsAndMessages(List<Contact> contacts, List<User> salespersons, List<Template> templates) {
        Random random = new Random();

        String[] inboundMessages = {"Perfecto, gracias!", "¿Me podés contar más?", "¿Cuánto cuesta?", "Me interesa avanzar."};
        String[] outboundMessages = {"Genial, te cuento.", "Te paso más detalles.", "Podemos coordinar una demo.", "Te envío info ahora."};

        for (Contact contact : contacts) {
            Conversation whatsappConv = Conversation.builder()
                    .contact(contact)
                    .channel(Channel.WHATSAPP)
                    .status(ConversationStatus.OPEN)
                    .assignedTo(contact.getOwner())
                    .lastInteraction(contact.getCreatedAt().plusDays(1))
                    .createdAt(contact.getCreatedAt())
                    .build();
            conversationRepository.save(whatsappConv);

            Conversation emailConv = Conversation.builder()
                    .contact(contact)
                    .channel(Channel.EMAIL)
                    .status(ConversationStatus.OPEN)
                    .assignedTo(contact.getOwner())
                    .lastInteraction(contact.getCreatedAt().plusDays(1))
                    .createdAt(contact.getCreatedAt())
                    .build();
            conversationRepository.save(emailConv);

            Template whatsappTemplate = templates.stream().filter(t -> t.getChannel() == Channel.WHATSAPP).findFirst().orElse(null);

            Message outbound = Message.builder()
                    .conversation(whatsappConv)
                    .direction(MessageDirection.OUTBOUND)
                    .body("Hola " + contact.getName() + "! Soy " + contact.getOwner().getName() + " de CRM.")
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .template(whatsappTemplate)
                    .sender(contact.getOwner())
                    .providerId(generateWhatsAppProviderId())
                    .sentAt(contact.getCreatedAt().plusHours(2))
                    .build();
            messageRepository.save(outbound);

            Message inbound = Message.builder()
                    .conversation(whatsappConv)
                    .direction(MessageDirection.INBOUND)
                    .body("Hola! Gracias por contactarme.")
                    .deliveryStatus(DeliveryStatus.READ)
                    .providerId(generateWhatsAppProviderId())
                    .sentAt(contact.getCreatedAt().plusDays(1))
                    .build();
            messageRepository.save(inbound);

            for (int i = 0; i < 5; i++) {
                boolean isOutbound = i % 2 == 0;
                Message extraMessage = Message.builder()
                        .conversation(whatsappConv)
                        .direction(isOutbound ? MessageDirection.OUTBOUND : MessageDirection.INBOUND)
                        .body(isOutbound ? outboundMessages[random.nextInt(outboundMessages.length)] : inboundMessages[random.nextInt(inboundMessages.length)])
                        .deliveryStatus(isOutbound ? DeliveryStatus.DELIVERED : DeliveryStatus.READ)
                        .template(isOutbound ? whatsappTemplate : null)
                        .sender(isOutbound ? contact.getOwner() : null)
                        .providerId(generateWhatsAppProviderId())
                        .sentAt(contact.getCreatedAt().plusDays(1).plusHours(i * 3))
                        .build();
                messageRepository.save(extraMessage);
            }

            log.info("Messages seeded for contact: {}", contact.getName());
        }
    }

    private void seedTasksWithDates(List<Contact> contacts, List<User> salespersons) {
        Random random = new Random();

        // PERÍODO ANTERIOR (Marzo) - 25 tareas
        for (int i = 0; i < 25; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();
            LocalDateTime dueDate = LocalDateTime.of(2026, 3, 5 + random.nextInt(25), 10, 0);
            TaskStatus status = TaskStatus.COMPLETED;

            Task task = Task.builder()
                    .title("Tarea de marzo")
                    .description("Seguimiento con " + contact.getName())
                    .type(TaskType.CALL)
                    .status(status)
                    .dueDate(dueDate)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(dueDate.minusDays(2))
                    .completedAt(dueDate.plusHours(2))
                    .build();
            taskRepository.save(task);
        }

        // PERÍODO ACTUAL (Abril) - 35 tareas (↑40% aumento)
        for (int i = 0; i < 35; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();
            LocalDateTime dueDate = LocalDateTime.of(2026, 4, 1 + random.nextInt(13), 10, 0);
            TaskStatus status = i < 10 ? TaskStatus.COMPLETED : (i < 25 ? TaskStatus.PENDING : TaskStatus.OVERDUE);

            Task task = Task.builder()
                    .title("Tarea de abril")
                    .description("Seguimiento con " + contact.getName())
                    .type(TaskType.EMAIL)
                    .status(status)
                    .dueDate(dueDate)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(dueDate.minusDays(1))
                    .build();
            if (status == TaskStatus.COMPLETED) {
                task.setCompletedAt(dueDate.plusHours(1));
            }
            taskRepository.save(task);
        }

        // Tareas para hoy (8 tareas)
        for (int i = 0; i < 8; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();
            Task task = Task.builder()
                    .title("Tarea urgente para hoy")
                    .description("Contactar a " + contact.getName())
                    .type(TaskType.CALL)
                    .status(TaskStatus.PENDING)
                    .dueDate(LocalDateTime.now().plusHours(random.nextInt(12)))
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();
            taskRepository.save(task);
        }

        log.info("Tasks seeded: 25 in March, 35 in April, 8 for today");
    }

    private String generateWhatsAppProviderId() {
        return "wamid.HBgM" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    private String generateEmailProviderId() {
        return "<" + System.currentTimeMillis() + "." + new Random().nextInt(100000) + "@smtp-relay.brevo.com>";
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
                .name("Tareas vencidas")
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