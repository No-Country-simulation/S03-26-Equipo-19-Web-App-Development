package com.crm.app.config;

import com.crm.app.model.User;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedSalespersons();
    }

    private void seedAdmin() {
        if (repository.existsByEmail("admin@crm.com")) return;

        User admin = User.builder()
                .name("Administrator")
                .email("admin@crm.com")
                .passwordHash(encoder.encode("Admin1234!"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        repository.save(admin);
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
            if (repository.existsByEmail(s.email())) return;

            User user = User.builder()
                    .name(s.name())
                    .email(s.email())
                    .passwordHash(encoder.encode(s.password()))
                    .role(Role.SALESPERSON)
                    .active(true)
                    .build();

            repository.save(user);
            log.info("Salesperson seeded: {} / {}", s.email(), s.password());
        });
    }
}