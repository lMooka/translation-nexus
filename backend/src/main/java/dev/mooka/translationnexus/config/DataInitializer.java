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
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Seeds default users and locales on first startup if the collections are empty.
 *
 *  translator / translator123  → TRANSLATOR
 *  reviewer   / reviewer123    → REVIEWER
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LocaleRepository localeRepository;
    private final AppVersionRepository appVersionRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (appVersionRepository.count() == 0) {
            appVersionRepository.save(AppVersionEntity.builder()
                    .version("1.0.0")
                    .active(true)
                    .createdAt(Instant.now())
                    .build());
            log.info("Seeded default active version: 1.0");
        }

        if (categoryRepository.count() == 0) {
            categoryRepository.save(CategoryEntity.builder()
                    .name("General")
                    .pathMappings(List.of(new PathMappingEntity("*", "general.csv")))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Gameplay")
                    .pathMappings(List.of(
                            new PathMappingEntity("quest.*.desc", "quest_descriptions.csv"),
                            new PathMappingEntity("gameplay.*", "gameplay.csv")
                    ))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("UI")
                    .pathMappings(List.of(
                            new PathMappingEntity("button.*", "buttons.csv"),
                            new PathMappingEntity("ui.*", "ui.csv")
                    ))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("ITEM")
                    .pathMappings(List.of(new PathMappingEntity("item.*", "items.csv")))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("SKILL")
                    .pathMappings(List.of(new PathMappingEntity("skill.*", "skills.csv")))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("MONSTER")
                    .pathMappings(List.of(new PathMappingEntity("monster.*", "monsters.csv")))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            log.info("Seeded default categories with path mappings: General, Gameplay, UI, ITEM, SKILL, MONSTER");
        }

        if (localeRepository.count() == 0) {
            localeRepository.saveAll(List.of(
                    new LocaleEntity("pt", "Portuguese"),
                    new LocaleEntity("es", "Spanish"),
                    new LocaleEntity("fr", "French"),
                    new LocaleEntity("de", "German"),
                    new LocaleEntity("ja", "Japanese")
            ));
            log.info("Seeded default locales: pt, es, fr, de, ja");
        }

        if (userRepository.count() == 0) {
            userRepository.save(UserEntity.builder()
                    .username("translator")
                    .passwordHash(passwordEncoder.encode("translator123"))
                    .roles(List.of(Roles.TRANSLATOR))
                    .allowedLocales(List.of("pt", "es"))
                    .build());

            userRepository.save(UserEntity.builder()
                    .username("reviewer")
                    .passwordHash(passwordEncoder.encode("reviewer123"))
                    .roles(List.of(Roles.TRANSLATOR, Roles.REVIEWER))
                    .allowedLocales(List.of())
                    .build());

            userRepository.save(UserEntity.builder()
                    .username("manager")
                    .passwordHash(passwordEncoder.encode("manager123"))
                    .roles(List.of(Roles.TRANSLATOR, Roles.REVIEWER, Roles.MANAGER))
                    .allowedLocales(List.of())
                    .build());

            userRepository.save(UserEntity.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .roles(List.of(Roles.TRANSLATOR, Roles.REVIEWER, Roles.ADMIN))
                    .allowedLocales(List.of())
                    .build());

            log.info("Seeded default users: translator (pt/es), reviewer, manager, and admin");
        }
    }
}
