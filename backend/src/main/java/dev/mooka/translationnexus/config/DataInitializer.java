package dev.mooka.translationnexus.config;

import dev.mooka.translationnexus.domain.entity.AppVersionEntity;
import dev.mooka.translationnexus.domain.entity.LocaleEntity;
import dev.mooka.translationnexus.domain.entity.UserEntity;
import dev.mooka.translationnexus.repository.AppVersionRepository;
import dev.mooka.translationnexus.repository.LocaleRepository;
import dev.mooka.translationnexus.repository.UserRepository;
import dev.mooka.translationnexus.domain.entity.CategoryEntity;
import dev.mooka.translationnexus.domain.entity.PathMappingEntity;
import dev.mooka.translationnexus.repository.CategoryRepository;
import dev.mooka.translationnexus.security.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LocaleRepository localeRepository;
    private final AppVersionRepository appVersionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.database.seeding.enabled:true}")
    private boolean seedingEnabled;

    @Override
    public void run(String... args) {
        if (!seedingEnabled) {
            log.info("Database seeding is disabled by configuration.");
            return;
        }

        if (appVersionRepository.count() == 0) {
            appVersionRepository.save(AppVersionEntity.builder()
                    .version("1.0.0")
                    .active(true)
                    .createdAt(Instant.now())
                    .build());
            log.info("Seeded default active version: 1.0");
        }

        if (localeRepository.count() == 0) {
            localeRepository.saveAll(List.of(
                    new LocaleEntity("pt", "Portuguese"),
                    new LocaleEntity("es", "Spanish"),
                    new LocaleEntity("fr", "French"),
                    new LocaleEntity("de", "German"),
                    new LocaleEntity("ja", "Japanese")));
            log.info("Seeded default locales: pt, es, fr, de, ja");
        }

        if (userRepository.count() == 0) {
            userRepository.save(UserEntity.builder()
                    .username("translator")
                    .passwordHash(passwordEncoder.encode("translator"))
                    .roles(List.of(Roles.TRANSLATOR))
                    .allowedLocales(List.of("pt", "es", "fr", "de", "ja"))
                    .build());

            userRepository.save(UserEntity.builder()
                    .username("reviewer")
                    .passwordHash(passwordEncoder.encode("reviewer"))
                    .roles(List.of(Roles.TRANSLATOR, Roles.REVIEWER))
                    .allowedLocales(List.of())
                    .build());

            userRepository.save(UserEntity.builder()
                    .username("manager")
                    .passwordHash(passwordEncoder.encode("manager"))
                    .roles(List.of(Roles.TRANSLATOR, Roles.REVIEWER, Roles.MANAGER))
                    .allowedLocales(List.of())
                    .build());

            userRepository.save(UserEntity.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin"))
                    .roles(List.of(Roles.TRANSLATOR, Roles.REVIEWER, Roles.ADMIN))
                    .allowedLocales(List.of())
                    .build());

            log.info("Seeded default users: translator (pt/es), reviewer, manager, and admin");
        }
    }
}
