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
                new Seed("Henry Clark", "henry@crm.com", "Sales008!", false, LocalDateTime.of(2026, 1, 5, 16, 0)),
                new Seed("Ivy Adams", "ivy@crm.com", "Sales009!", true, LocalDateTime.of(2026, 4, 1, 10, 0)),
                new Seed("Jack Turner", "jack@crm.com", "Sales010!", true, LocalDateTime.of(2026, 4, 5, 11, 30))
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
        List<Template> templates = new ArrayList<>();

        // Crear MUCHAS plantillas en meses anteriores para tener números grandes
        Random random = new Random();

        // Plantillas de meses anteriores (para tener total alto: ~289)
        for (int i = 1; i <= 280; i++) {
            LocalDateTime pastDate = LocalDateTime.of(2026, 1 + random.nextInt(2), 1 + random.nextInt(28), 10, 0);
            Template t = Template.builder()
                    .name("Plantilla histórica " + i)
                    .channel(i % 2 == 0 ? Channel.WHATSAPP : Channel.EMAIL)
                    .body("Contenido de plantilla " + i)
                    .variables("{}")
                    .createdBy(admin)
                    .createdAt(pastDate)
                    .build();
            templates.add(t);
        }

        // Plantillas de marzo (período anterior) - 9 plantillas
        LocalDateTime march1 = LocalDateTime.of(2026, 3, 1, 10, 0);
        LocalDateTime march10 = LocalDateTime.of(2026, 3, 10, 14, 0);
        LocalDateTime march20 = LocalDateTime.of(2026, 3, 20, 9, 0);

        templates.add(createTemplate(admin, "Email bienvenida marzo", Channel.EMAIL, march1));
        templates.add(createTemplate(admin, "WhatsApp primer contacto", Channel.WHATSAPP, march10));
        templates.add(createTemplate(admin, "Email propuesta comercial", Channel.EMAIL, march20));

        // Plantillas de abril (período actual) - 9 plantillas (para mostrar +9)
        LocalDateTime april1 = LocalDateTime.of(2026, 4, 1, 8, 0);
        LocalDateTime april5 = LocalDateTime.of(2026, 4, 5, 13, 0);
        LocalDateTime april8 = LocalDateTime.of(2026, 4, 8, 11, 0);
        LocalDateTime april10 = LocalDateTime.of(2026, 4, 10, 10, 0);
        LocalDateTime april12 = LocalDateTime.of(2026, 4, 12, 16, 0);
        LocalDateTime april14 = LocalDateTime.of(2026, 4, 14, 9, 0);

        templates.add(createTemplate(admin, "Email newsletter abril", Channel.EMAIL, april1));
        templates.add(createTemplate(admin, "WhatsApp recordatorio", Channel.WHATSAPP, april5));
        templates.add(createTemplate(admin, "Email seguimiento", Channel.EMAIL, april8));
        templates.add(createTemplate(admin, "WhatsApp felicitaciones", Channel.WHATSAPP, april10));
        templates.add(createTemplate(admin, "Email encuesta", Channel.EMAIL, april12));
        templates.add(createTemplate(admin, "WhatsApp pago", Channel.WHATSAPP, april14));

        // Plantillas de hoy (4 plantillas)
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 4; i++) {
            templates.add(createTemplate(admin, "Plantilla de hoy " + i,
                    i % 2 == 0 ? Channel.WHATSAPP : Channel.EMAIL, now));
        }

        // Guardar todas las plantillas
        List<Template> savedTemplates = new ArrayList<>();
        for (Template template : templates) {
            if (!templateRepository.existsByNameAndCreatedByRole(template.getName(), Role.ADMIN)) {
                savedTemplates.add(templateRepository.save(template));
            }
        }

        log.info("Templates seeded: total={}, thisMonth={}, today={}",
                savedTemplates.size(), 9, 4);

        return savedTemplates;
    }

    private Template createTemplate(User admin, String name, Channel channel, LocalDateTime createdAt) {
        return Template.builder()
                .name(name)
                .channel(channel)
                .body("Contenido de " + name)
                .variables("{}")
                .createdBy(admin)
                .createdAt(createdAt)
                .build();
    }

    private List<Contact> seedContactsWithDates(List<User> salespersons, List<Tag> tags) {
        List<Contact> contacts = new ArrayList<>();

        // FECHAS REALISTAS - Período base (Enero-Febrero)
        LocalDateTime jan1 = LocalDateTime.of(2026, 1, 5, 10, 0);
        LocalDateTime jan15 = LocalDateTime.of(2026, 1, 15, 14, 0);
        LocalDateTime feb1 = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime feb15 = LocalDateTime.of(2026, 2, 15, 11, 0);
        LocalDateTime feb28 = LocalDateTime.of(2026, 2, 28, 16, 0);

// FECHAS REALISTAS - Período anterior (Marzo)
        LocalDateTime mar5 = LocalDateTime.of(2026, 3, 5, 10, 0);
        LocalDateTime mar12 = LocalDateTime.of(2026, 3, 12, 14, 0);
        LocalDateTime mar18 = LocalDateTime.of(2026, 3, 18, 9, 30);
        LocalDateTime mar22 = LocalDateTime.of(2026, 3, 22, 11, 0);
        LocalDateTime mar28 = LocalDateTime.of(2026, 3, 28, 15, 0);

// FECHAS REALISTAS - Período actual (Abril)
        LocalDateTime apr3 = LocalDateTime.of(2026, 4, 3, 10, 0);
        LocalDateTime apr5 = LocalDateTime.of(2026, 4, 5, 14, 0);
        LocalDateTime apr6 = LocalDateTime.of(2026, 4, 6, 11, 0);
        LocalDateTime apr7 = LocalDateTime.of(2026, 4, 7, 14, 0);
        LocalDateTime apr8 = LocalDateTime.of(2026, 4, 8, 9, 0);
        LocalDateTime apr9 = LocalDateTime.of(2026, 4, 9, 16, 0);
        LocalDateTime apr10 = LocalDateTime.of(2026, 4, 10, 9, 0);
        LocalDateTime apr11 = LocalDateTime.of(2026, 4, 11, 15, 0);
        LocalDateTime apr12 = LocalDateTime.of(2026, 4, 12, 11, 0);
        LocalDateTime apr14 = LocalDateTime.of(2026, 4, 14, 13, 0);

        Object[][] contactData = {
                // ========== CONTACTOS ENERO-FEBRERO (base) ==========
                {"Carlos", "Rodríguez", "carlos@techcorp.com", "541123456701", "TechCorp", FunnelStatus.CLOSED_WON, Channel.WHATSAPP, 0, new int[]{0}, jan1},
                {"Ana", "Martínez", "ana@ecomstore.com", "541123456702", "EcomStore", FunnelStatus.CLOSED_WON, Channel.EMAIL, 1, new int[]{1}, jan15},
                {"Martín", "González", "martin@fintech.io", "541123456703", "Fintech IO", FunnelStatus.CLOSED_LOST, Channel.WHATSAPP, 2, new int[]{2}, feb1},
                {"Laura", "Fernández", "laura@startup.com", "541123456704", "StartupX", FunnelStatus.CLOSED_LOST, Channel.EMAIL, 3, new int[]{3}, feb15},
                {"Javier", "López", "javier@saas.com", "541123456705", "SaaS Solutions", FunnelStatus.PROPOSAL_SENT, Channel.WHATSAPP, 4, new int[]{4}, feb28},

                // ========== CONTACTOS MARZO (período anterior - 15 contactos) ==========
                // NEW_LEAD: 4
                {"Pedro", "Ramírez", "pedro@cloud.com", "541123456711", "Cloud Solutions", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 0, new int[]{0}, mar5},
                {"Lucía", "Castillo", "lucia@dev.com", "541123456712", "Dev House", FunnelStatus.NEW_LEAD, Channel.EMAIL, 1, new int[]{1}, mar12},
                {"Mateo", "Ortiz", "mateo@ai.com", "541123456713", "AI Labs", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 2, new int[]{2}, mar18},
                {"Renata", "Silva", "renata@data.com", "541123456714", "Data Corp", FunnelStatus.NEW_LEAD, Channel.EMAIL, 3, new int[]{3}, mar28},

                // CONTACTED: 3
                {"Diego", "Sánchez", "diego@logistica.com", "541123456707", "Logística Express", FunnelStatus.CONTACTED, Channel.WHATSAPP, 1, new int[]{1}, mar5},
                {"Valentina", "Pérez", "valentina@health.com", "541123456708", "Health Tech", FunnelStatus.CONTACTED, Channel.EMAIL, 2, new int[]{2}, mar18},
                {"Nicolás", "Romero", "nico@marketing.com", "541123456709", "Marketing Pro", FunnelStatus.CONTACTED, Channel.WHATSAPP, 3, new int[]{3}, mar28},

                // IN_NEGOTIATION: 3
                {"Sofía", "Díaz", "sofia@retail.com", "541123456706", "Retail Plus", FunnelStatus.IN_NEGOTIATION, Channel.EMAIL, 0, new int[]{0}, mar12},
                {"Camila", "Morales", "camila@consulting.com", "541123456710", "Consulting Group", FunnelStatus.IN_NEGOTIATION, Channel.EMAIL, 4, new int[]{4}, mar22},
                {"Bruno", "Rojas", "bruno@logistics.com", "541123456725", "Logistics Pro", FunnelStatus.IN_NEGOTIATION, Channel.WHATSAPP, 4, new int[]{4}, mar28},

                // PROPOSAL_SENT: 3
                {"Elena", "Suárez", "elena@cyber.com", "541123456728", "Cyber Security", FunnelStatus.PROPOSAL_SENT, Channel.EMAIL, 2, new int[]{2}, mar5},
                {"Fabián", "Luna", "fabian@robotics.com", "541123456729", "Robotics", FunnelStatus.PROPOSAL_SENT, Channel.WHATSAPP, 3, new int[]{3}, mar18},
                {"Gloria", "Paz", "gloria@space.com", "541123456730", "Space Tech", FunnelStatus.PROPOSAL_SENT, Channel.EMAIL, 4, new int[]{4}, mar28},

                // CLOSED_WON: 1
                {"Irene", "Castro", "irene@nanotech.com", "541123456732", "NanoTech", FunnelStatus.CLOSED_WON, Channel.EMAIL, 1, new int[]{1}, mar22},

                // CLOSED_LOST: 1
                {"Facundo", "Núñez", "facundo@blockchain.com", "541123456715", "Blockchain Tech", FunnelStatus.CLOSED_LOST, Channel.WHATSAPP, 4, new int[]{4}, mar12},

                // ========== CONTACTOS ABRIL (período actual - 20 contactos) ==========
                // NEW_LEAD: 8 (↑100% desde 4)
                {"Agustina", "Paz", "agustina@biotech.com", "541123456716", "BioTech", FunnelStatus.NEW_LEAD, Channel.EMAIL, 0, new int[]{0}, apr3},
                {"Tomás", "Ríos", "tomas@green.com", "541123456717", "Green Energy", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 1, new int[]{1}, apr3},
                {"Florencia", "Molina", "flor@media.com", "541123456718", "Media Group", FunnelStatus.NEW_LEAD, Channel.EMAIL, 2, new int[]{2}, apr7},
                {"Santiago", "Vega", "santiago@games.com", "541123456719", "Game Studio", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 3, new int[]{3}, apr7},
                {"Victoria", "Luna", "victoria@travel.com", "541123456720", "Travel Tech", FunnelStatus.NEW_LEAD, Channel.EMAIL, 4, new int[]{4}, apr10},
                {"Gabriel", "Flores", "gabriel@realestate.com", "541123456721", "Real Estate", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 0, new int[]{0}, apr10},
                {"Julieta", "Aguirre", "julieta@legal.com", "541123456722", "Legal Tech", FunnelStatus.NEW_LEAD, Channel.EMAIL, 1, new int[]{1}, apr12},
                {"Hugo", "Mora", "hugo@quantum.com", "541123456731", "Quantum Computing", FunnelStatus.NEW_LEAD, Channel.WHATSAPP, 0, new int[]{0}, apr14},

                // CONTACTED: 4 (↑33% desde 3)
                {"Emiliano", "Correa", "emiliano@construction.com", "541123456723", "Construcción", FunnelStatus.CONTACTED, Channel.WHATSAPP, 2, new int[]{2}, apr3},
                {"Mora", "Giménez", "mora@fashion.com", "541123456724", "Fashion Tech", FunnelStatus.CONTACTED, Channel.EMAIL, 3, new int[]{3}, apr7},
                {"Clara", "Vidal", "clara@edtech.com", "541123456726", "EdTech", FunnelStatus.CONTACTED, Channel.EMAIL, 0, new int[]{0}, apr10},
                {"Daniel", "Ponce", "daniel@agrotech.com", "541123456727", "AgroTech", FunnelStatus.CONTACTED, Channel.WHATSAPP, 1, new int[]{1}, apr14},

                // IN_NEGOTIATION: 4 (↑33% desde 3)
                {"Lucas", "Miranda", "lucas@cleantech.com", "541123456733", "CleanTech", FunnelStatus.IN_NEGOTIATION, Channel.WHATSAPP, 2, new int[]{2}, apr5},
                {"Paula", "Ramos", "paula@insurtech.com", "541123456734", "InsurTech", FunnelStatus.IN_NEGOTIATION, Channel.EMAIL, 3, new int[]{3}, apr8},
                {"Ricardo", "Vega", "ricardo@proptech.com", "541123456735", "PropTech", FunnelStatus.IN_NEGOTIATION, Channel.WHATSAPP, 4, new int[]{4}, apr11},
                {"Silvia", "Méndez", "silvia@foodtech.com", "541123456736", "FoodTech", FunnelStatus.IN_NEGOTIATION, Channel.EMAIL, 0, new int[]{0}, apr14},

                // PROPOSAL_SENT: 4 (↑33% desde 3)
                {"Oscar", "Ponce", "oscar@adtech.com", "541123456737", "AdTech", FunnelStatus.PROPOSAL_SENT, Channel.WHATSAPP, 1, new int[]{1}, apr6},
                {"Nora", "Luna", "nora@martech.com", "541123456738", "MarTech", FunnelStatus.PROPOSAL_SENT, Channel.EMAIL, 2, new int[]{2}, apr9},

                // CLOSED_WON: 2 (↑100% desde 1)
                {"Mario", "Gil", "mario@legaltech.com", "541123456739", "LegalTech", FunnelStatus.CLOSED_WON, Channel.WHATSAPP, 3, new int[]{3}, apr12},
                {"Olga", "Paz", "olga@edtech.com", "541123456740", "EduTech", FunnelStatus.CLOSED_WON, Channel.EMAIL, 4, new int[]{4}, apr14},

                // CLOSED_LOST: 0 (↓100% desde 1 - mejora)
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
        }

        log.info("Contacts seeded: {} total", contacts.size());
        return contacts;
    }

    private void seedConversationsAndMessages(List<Contact> contacts, List<User> salespersons, List<Template> templates) {
        Random random = new Random();

        String[] inboundMessages = {
                "Perfecto, gracias por la info!",
                "¿Me podés contar más sobre los precios?",
                "¿Cuánto cuesta la implementación?",
                "Me interesa avanzar con la propuesta",
                "¿Tienen disponible una demo esta semana?",
                "Lo reviso con mi equipo y te confirmo",
                "Buenísimo! Me parece excelente",
                "¿Cómo seguimos con el contrato?",
                "¿Hay algún descuento por pago anual?",
                "Gracias por toda la información"
        };

        String[] outboundMessages = {
                "Genial, te cuento los detalles del plan",
                "Te paso la propuesta comercial completa",
                "Podemos coordinar una demo para mañana",
                "Te envío el contrato para revisar",
                "Trabajamos con empresas similares a la tuya",
                "Quedo atento a tu respuesta",
                "Te explico cómo funciona la integración",
                "Avancemos con la firma del contrato",
                "Te paso el link de pago",
                "Gracias por tu interés en nuestros servicios"
        };

        int totalMessages = 0;

        for (Contact contact : contacts) {
            // WhatsApp conversation
            Conversation whatsappConv = Conversation.builder()
                    .contact(contact)
                    .channel(Channel.WHATSAPP)
                    .status(ConversationStatus.OPEN)
                    .assignedTo(contact.getOwner())
                    .lastInteraction(contact.getCreatedAt().plusDays(1))
                    .createdAt(contact.getCreatedAt())
                    .build();
            conversationRepository.save(whatsappConv);

            // Email conversation
            Conversation emailConv = Conversation.builder()
                    .contact(contact)
                    .channel(Channel.EMAIL)
                    .status(ConversationStatus.OPEN)
                    .assignedTo(contact.getOwner())
                    .lastInteraction(contact.getCreatedAt().plusDays(1))
                    .createdAt(contact.getCreatedAt())
                    .build();
            conversationRepository.save(emailConv);

            Template whatsappTemplate = templates.stream()
                    .filter(t -> t.getChannel() == Channel.WHATSAPP)
                    .findFirst()
                    .orElse(null);

            // First outbound message
            Message outbound = Message.builder()
                    .conversation(whatsappConv)
                    .direction(MessageDirection.OUTBOUND)
                    .body("Hola " + contact.getName() + "! Soy " + contact.getOwner().getName() + " de CRM Cross-Industry. ¿Cómo estás? Me contacto para conocer más sobre " + contact.getCompany() + ".")
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .template(whatsappTemplate)
                    .sender(contact.getOwner())
                    .providerId(generateWhatsAppProviderId())
                    .sentAt(contact.getCreatedAt().plusHours(2))
                    .build();
            messageRepository.save(outbound);
            totalMessages++;

            // First inbound message
            Message inbound = Message.builder()
                    .conversation(whatsappConv)
                    .direction(MessageDirection.INBOUND)
                    .body("Hola! Gracias por contactarte. Me interesa saber más sobre sus servicios para " + contact.getCompany() + ".")
                    .deliveryStatus(DeliveryStatus.READ)
                    .providerId(generateWhatsAppProviderId())
                    .sentAt(contact.getCreatedAt().plusDays(1))
                    .build();
            messageRepository.save(inbound);
            totalMessages++;

            // Additional messages (3-8 por contacto)
            int additionalCount = 3 + random.nextInt(6);
            for (int i = 0; i < additionalCount; i++) {
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
                        .sentAt(contact.getCreatedAt().plusDays(1).plusHours(i * 3))
                        .build();
                messageRepository.save(extraMessage);
                totalMessages++;
            }

            // Email message (1 por contacto)
            Template emailTemplate = templates.stream()
                    .filter(t -> t.getChannel() == Channel.EMAIL)
                    .findFirst()
                    .orElse(null);

            Message emailOutbound = Message.builder()
                    .conversation(emailConv)
                    .direction(MessageDirection.OUTBOUND)
                    .body("Hola " + contact.getName() + ",\n\nTe escribo para presentarte nuestras soluciones para " + contact.getCompany() + ". Quedo atento a tu respuesta.\n\nSaludos,\n" + contact.getOwner().getName())
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .template(emailTemplate)
                    .sender(contact.getOwner())
                    .providerId(generateEmailProviderId())
                    .sentAt(contact.getCreatedAt().plusDays(2))
                    .build();
            messageRepository.save(emailOutbound);
            totalMessages++;
        }

        log.info("Messages seeded: {} total messages", totalMessages);
    }

    private void seedTasksWithDates(List<Contact> contacts, List<User> salespersons) {
        Random random = new Random();

        int totalTasks = 0;

        // Tareas completadas en Enero-Febrero (base)
        for (int i = 0; i < 15; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();
            LocalDateTime dueDate = LocalDateTime.of(2026, 1 + random.nextInt(2), 5 + random.nextInt(20), 10, 0);

            Task task = Task.builder()
                    .title("Seguimiento inicial")
                    .description("Contactar a " + contact.getName() + " de " + contact.getCompany())
                    .type(TaskType.CALL)
                    .status(TaskStatus.COMPLETED)
                    .dueDate(dueDate)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(dueDate.minusDays(2))
                    .completedAt(dueDate.plusHours(2))
                    .build();
            taskRepository.save(task);
            totalTasks++;
        }

        // Tareas en Marzo (período anterior)
        for (int i = 0; i < 25; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();
            LocalDateTime dueDate = LocalDateTime.of(2026, 3, 5 + random.nextInt(25), 10, 0);
            TaskStatus status = i < 15 ? TaskStatus.COMPLETED : (i < 20 ? TaskStatus.PENDING : TaskStatus.OVERDUE);

            Task task = Task.builder()
                    .title(i < 15 ? "Reunión de seguimiento" : (i < 20 ? "Enviar propuesta" : "Contacto pendiente"))
                    .description("Seguimiento con " + contact.getName())
                    .type(i % 2 == 0 ? TaskType.CALL : TaskType.EMAIL)
                    .status(status)
                    .dueDate(dueDate)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(dueDate.minusDays(random.nextInt(5)))
                    .build();
            if (status == TaskStatus.COMPLETED) {
                task.setCompletedAt(dueDate.plusHours(random.nextInt(24)));
            }
            taskRepository.save(task);
            totalTasks++;
        }

        // Tareas en Abril (período actual - mayor actividad)
        for (int i = 0; i < 40; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();
            LocalDateTime dueDate = LocalDateTime.of(2026, 4, 1 + random.nextInt(14), 10, 0);
            TaskStatus status;
            if (i < 18) {
                status = TaskStatus.COMPLETED;
            } else if (i < 30) {
                status = TaskStatus.PENDING;
            } else {
                status = TaskStatus.OVERDUE;
            }

            Task task = Task.builder()
                    .title(i < 18 ? "Cierre de venta" : (i < 30 ? "Demo programada" : "Llamada de seguimiento"))
                    .description("Gestión con " + contact.getName() + " - " + contact.getCompany())
                    .type(i % 2 == 0 ? TaskType.MEETING : TaskType.CALL)
                    .status(status)
                    .dueDate(dueDate)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(dueDate.minusDays(random.nextInt(3)))
                    .build();
            if (status == TaskStatus.COMPLETED) {
                task.setCompletedAt(dueDate.plusHours(random.nextInt(12)));
            }
            taskRepository.save(task);
            totalTasks++;
        }

        // Tareas para hoy (8 tareas)
        for (int i = 0; i < 8; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();
            Task task = Task.builder()
                    .title("Tarea prioritaria del día")
                    .description("Contactar urgentemente a " + contact.getName() + " para cerrar acuerdo")
                    .type(TaskType.CALL)
                    .status(TaskStatus.PENDING)
                    .dueDate(LocalDateTime.now().plusHours(random.nextInt(12)))
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(LocalDateTime.now().minusDays(1))
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