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

        List<Template> templates = seedTemplates(admin, salespersons);

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

    private List<Template> seedTemplates(User admin, List<User> salespersons) {
        List<Template> templates = new ArrayList<>();

        // ==================== PLANTILLAS ADMIN (GLOBALES) ====================

        // 1. Email y Whatsapp - Bienvenida para nuevos leads
        templates.add(createTemplate(admin, "Email de bienvenida - Lead", Channel.EMAIL,
                "✅ Bienvenido a nuestro ecosistema",
                """
                Hola {{name}},
                
                Gracias por contactarte con nosotros. Hemos recibido tu consulta y será derivada a nuestro equipo de ventas.
                
                En las próximas horas, un asesor se comunicará contigo.
                
                Mientras tanto, puedes responder este correo si tenés alguna pregunta.
                
                Saludos cordiales,
                Equipo de Ventas
                """,
                "{\"name\":\"string\"}"));

        templates.add(createTemplate(admin, "Bienvenida automática - WhatsApp", Channel.WHATSAPP,
                null,
                """
                👋 Hola {{name}}! Gracias por contactarte con nosotros.
                
                Soy el asistente virtual de CRM Cross-Industry. 🚀
                
                Te informo que tu consulta ha sido recibida y será derivada a uno de nuestros asesores comerciales en breve.
                
                Mientras tanto, ¿podrías contarnos un poco más sobre lo que necesitas? Así podemos ayudarte mejor.
                
                📌 *Importante:* Un agente te responderá a la brevedad.
                
                ¡Gracias por tu paciencia!
                """,
                "{\"name\":\"string\"}"));

        // 2. WhatsApp - Primer contacto con lead activo
        templates.add(createTemplate(admin, "WhatsApp - Primer contacto", Channel.WHATSAPP,
                null,
                """
                👋 Hola {{name}}! Soy {{salesperson}}, asesor de la empresa.
                
                Recibimos tu consulta y queremos ayudarte.
                
                ¿Podrías contarnos un poco más sobre lo que necesitas?
                
                ¡Quedo atento a tu respuesta!
                """,
                "{\"name\":\"string\",\"salesperson\":\"string\"}"));

        // 3. Email - Seguimiento post-reunión
        templates.add(createTemplate(admin, "Email - Seguimiento post-reunión", Channel.EMAIL,
                "📌 Seguimiento de nuestra reunión",
                """
                Hola {{name}},
                
                Fue un placer reunirnos. Como quedamos, te envío la propuesta en archivo adjunto.
                
                Quedo atento a tus comentarios para avanzar con los siguientes pasos.
                
                Saludos,
                {{salesperson}}
                """,
                "{\"name\":\"string\",\"salesperson\":\"string\"}"));

        // 4. WhatsApp - Recordatorio de seguimiento
        templates.add(createTemplate(admin, "WhatsApp - Recordatorio de seguimiento", Channel.WHATSAPP,
                null,
                """
                📅 Hola {{name}}! Te recuerdo que tenemos pendiente el seguimiento de tu caso.
                
                ¿Cómo vamos con lo que hablamos? ¿Necesitas algo más?
                
                ¡Quedo atento!
                """,
                "{\"name\":\"string\"}"));

        // 5. Email - Propuesta comercial
        templates.add(createTemplate(admin, "Email - Propuesta comercial", Channel.EMAIL,
                "📊 Propuesta comercial para {{company}}",
                """
                Hola {{name}},
                
                Adjunto encontrarás la propuesta comercial para {{company}}.
                
                Quedo atento a tu confirmación para coordinar los próximos pasos.
                
                Saludos,
                {{salesperson}}
                """,
                "{\"name\":\"string\",\"company\":\"string\",\"salesperson\":\"string\"}"));

        // 6. WhatsApp - Cliente en seguimiento (post-venta)
        templates.add(createTemplate(admin, "WhatsApp - Cliente en seguimiento", Channel.WHATSAPP,
                null,
                """
                🎯 Hola {{name}}! Pasamos para saber cómo vas con nuestra solución.
                
                ¿Has tenido alguna dificultad? ¿Necesitas asistencia?
                
                Estamos aquí para ayudarte.
                """,
                "{\"name\":\"string\"}"));

        // 7. Email - Newsletter mensual
        templates.add(createTemplate(admin, "Email - Newsletter mensual", Channel.EMAIL,
                "📰 Novedades del mes - {{month}}",
                """
                Hola {{name}},
                
                Este mes te traemos:
                • Nueva integración con WhatsApp
                • Reportes avanzados
                • Plantillas dinámicas
                
                ¿Querés conocer más? Agendá una demo con nosotros.
                
                Saludos,
                Equipo CRM
                """,
                "{\"name\":\"string\",\"month\":\"string\"}"));

        // 8. WhatsApp - Cierre positivo de venta
        templates.add(createTemplate(admin, "WhatsApp - Felicitaciones cierre", Channel.WHATSAPP,
                null,
                """
                🎉 Excelente {{name}}! 🎉
                
                ¡Bienvenido oficialmente a la familia!
                
                En las próximas horas recibirás tus credenciales de acceso y la guía de primeros pasos.
                
                ¡Éxitos en esta nueva etapa!
                """,
                "{\"name\":\"string\"}"));

        // 9. Email - Encuesta de satisfacción
        templates.add(createTemplate(admin, "Email - Encuesta de satisfacción", Channel.EMAIL,
                "⭐ ¿Cómo calificas tu experiencia?",
                """
                Hola {{name}},
                
                Valoramos mucho tu opinión. ¿Podrías tomarte 2 minutos para responder nuestra encuesta?
                
                🔗 {{survey_link}}
                
                ¡Gracias por ayudarnos a mejorar!
                
                Saludos,
                Equipo CRM
                """,
                "{\"name\":\"string\",\"survey_link\":\"string\"}"));

        // 10. WhatsApp - Lead frío (reactivación)
        templates.add(createTemplate(admin, "WhatsApp - Reactivación de lead", Channel.WHATSAPP,
                null,
                """
                🔄 Hola {{name}}! Hace tiempo que no hablamos.
                
                Queremos saber si aún te interesa nuestra solución o si necesitas algo de nosotros.
                
                ¡Estamos a tu disposición!
                """,
                "{\"name\":\"string\"}"));

        // WhatsApp - Bienvenida y propósito
        templates.add(createTemplate(admin, "WhatsApp - Bienvenida y propósito", Channel.WHATSAPP,
                null,
                """
                🎉 Hola {{name}}! Bienvenido a CRM Cross-Industry.
                
                Soy {{salesperson}}, tu asesor comercial.
                
                ¿Podemos coordinar una breve charla?
                
                ¡Quedo atento!
                """,
                "{\"name\":\"string\",\"salesperson\":\"string\"}"));

// Email - Bienvenida y propósito
        templates.add(createTemplate(admin, "Email - Bienvenida y propósito", Channel.EMAIL,
                "🎯 Te contactamos para ayudarte a crecer",
                """
                Hola {{name}},
                
                Nos comunicamos porque creemos que podemos ayudarte a potenciar tus resultados.
                
                Soy {{salesperson}}, tu asesor comercial.
                
                ¿Podemos coordinar una breve reunión?
                
                ¡Esperamos tu respuesta!
                
                Saludos,
                {{salesperson}}
                """,
                "{\"name\":\"string\",\"salesperson\":\"string\"}"));

        // ==================== PLANTILLAS DE VENDEDORES (PERSONALES) ====================

        // Vendedor 1 (Alice) - 3 plantillas personales
        User alice = salespersons.stream().filter(u -> u.getEmail().equals("alice@crm.com")).findFirst().orElse(null);
        if (alice != null) {
            templates.add(createTemplate(alice, "Alice - Demo agendada", Channel.EMAIL,
                    "Demo CRM - {{date}}",
                    "Hola {{name}}, agendamos la demo para el {{date}} a las {{time}}. Te espero.",
                    "{\"name\":\"string\",\"date\":\"string\",\"time\":\"string\"}"));
            templates.add(createTemplate(alice, "Alice - Propuesta personalizada", Channel.WHATSAPP, null,
                    "Hola {{name}}, te envié la propuesta personalizada a tu correo. ¿La recibiste?",
                    "{\"name\":\"string\"}"));
            templates.add(createTemplate(alice, "Alice - Recordatorio de cierre", Channel.EMAIL,
                    "Recordatorio - Oferta vigente",
                    "Hola {{name}}, la oferta que te comenté vence en {{days}} días. ¿Avanzamos?",
                    "{\"name\":\"string\",\"days\":\"string\"}"));
        }

        // Vendedor 2 (Bob) - 3 plantillas personales
        User bob = salespersons.stream().filter(u -> u.getEmail().equals("bob@crm.com")).findFirst().orElse(null);
        if (bob != null) {
            templates.add(createTemplate(bob, "Bob - Contacto inicial rápido", Channel.WHATSAPP, null,
                    "👋 Hola {{name}}, soy Bob de Ventas. ¿Conectamos para una breve charla?",
                    "{\"name\":\"string\"}"));
            templates.add(createTemplate(bob, "Bob - Envío de materiales", Channel.EMAIL,
                    "Materiales solicitados",
                    "Hola {{name}}, adjunto los materiales que solicitaste. Quedo atento a tus comentarios.",
                    "{\"name\":\"string\"}"));
            templates.add(createTemplate(bob, "Bob - Llamada pendiente", Channel.WHATSAPP, null,
                    "📞 Hola {{name}}, te llamo para coordinar la llamada pendiente. ¿Cuándo te queda bien?",
                    "{\"name\":\"string\"}"));
        }

        // Vendedor 3 (Carol) - 3 plantillas personales
        User carol = salespersons.stream().filter(u -> u.getEmail().equals("carol@crm.com")).findFirst().orElse(null);
        if (carol != null) {
            templates.add(createTemplate(carol, "Carol - Newsletter personal", Channel.EMAIL,
                    "Info para {{company}}",
                    "Hola {{name}}, encontré información que puede interesarle a {{company}}. ¿Te la comparto?",
                    "{\"name\":\"string\",\"company\":\"string\"}"));
            templates.add(createTemplate(carol, "Carol - Consulta rápida", Channel.WHATSAPP, null,
                    "Hola {{name}}, ¿recibiste mi mail? Quedo atento a tu respuesta.",
                    "{\"name\":\"string\"}"));
            templates.add(createTemplate(carol, "Carol - Seguimiento de propuesta", Channel.EMAIL,
                    "Seguimiento de propuesta",
                    "Hola {{name}}, paso a consultar si tuviste tiempo de revisar la propuesta que te envié.",
                    "{\"name\":\"string\"}"));
        }

        // Guardar todas las plantillas
        List<Template> savedTemplates = new ArrayList<>();
        for (Template template : templates) {
            if (!templateRepository.existsByNameAndCreatedByRole(template.getName(), template.getCreatedBy().getRole())) {
                savedTemplates.add(templateRepository.save(template));
                log.info("Template seeded: '{}' by {}", template.getName(), template.getCreatedBy().getEmail());
            }
        }

        log.info("Templates seeded: {} total (10 admin + 9 salespersons)", savedTemplates.size());
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
                .createdAt(LocalDateTime.now())
                .build();
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
                        .sentAt(contact.getCreatedAt().plusDays(1).plusHours(i * 3L))
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
        LocalDateTime now = LocalDateTime.of(2026, 4, 15, 10, 0);

        int totalTasks = 0;

        // ================= COMPLETED (pasadas y coherentes)
        for (int i = 0; i < 20; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();

            LocalDateTime dueDate = now.minusDays(5 + random.nextInt(10));
            LocalDateTime completedAt = dueDate.plusHours(1 + random.nextInt(5));

            Task task = Task.builder()
                    .title("Seguimiento completado")
                    .description("Tarea ya realizada con " + contact.getName())
                    .type(TaskType.CALL)
                    .status(TaskStatus.COMPLETED)
                    .dueDate(dueDate)
                    .completedAt(completedAt)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(dueDate.minusDays(2))
                    .build();

            taskRepository.save(task);
            totalTasks++;
        }

        // ================= PENDING (futuras)
        for (int i = 0; i < 25; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();

            LocalDateTime dueDate = now.plusDays(1 + random.nextInt(10));

            Task task = Task.builder()
                    .title("Tarea pendiente")
                    .description("Pendiente con " + contact.getName())
                    .type(TaskType.EMAIL)
                    .status(TaskStatus.PENDING)
                    .dueDate(dueDate)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(now.minusDays(random.nextInt(3)))
                    .build();

            taskRepository.save(task);
            totalTasks++;
        }

        // ================= OVERDUE (pasadas sin completar)
        for (int i = 0; i < 15; i++) {
            Contact contact = contacts.get(random.nextInt(contacts.size()));
            User assignedTo = contact.getOwner();

            LocalDateTime dueDate = now.minusDays(1 + random.nextInt(7));

            Task task = Task.builder()
                    .title("Tarea vencida")
                    .description("No se contactó a tiempo a " + contact.getName())
                    .type(TaskType.CALL)
                    .status(TaskStatus.OVERDUE)
                    .dueDate(dueDate)
                    .contact(contact)
                    .assignedTo(assignedTo)
                    .createdAt(dueDate.minusDays(2))
                    .build();

            taskRepository.save(task);
            totalTasks++;
        }

        log.info("Tasks seeded (FIXED): {} total", totalTasks);
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