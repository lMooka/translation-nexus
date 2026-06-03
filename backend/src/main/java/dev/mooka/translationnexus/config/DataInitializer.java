package dev.mooka.translationnexus.config;

import dev.mooka.translationnexus.domain.AppVersion;
import dev.mooka.translationnexus.domain.Locale;
import dev.mooka.translationnexus.domain.User;
import dev.mooka.translationnexus.repository.AppVersionRepository;
import dev.mooka.translationnexus.repository.LocaleRepository;
import dev.mooka.translationnexus.repository.UserRepository;
import dev.mooka.translationnexus.domain.Category;
import dev.mooka.translationnexus.domain.PathMapping;
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
            appVersionRepository.save(AppVersion.builder()
                    .version("1.0")
                    .active(true)
                    .createdAt(Instant.now())
                    .build());
            log.info("Seeded default active version: 1.0");
        }

        if (categoryRepository.count() == 0) {
            categoryRepository.save(Category.builder()
                    .name("General")
                    .pathMappings(List.of(new PathMapping("*", "general.csv")))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            categoryRepository.save(Category.builder()
                    .name("Gameplay")
                    .pathMappings(List.of(
                            new PathMapping("quest.*.desc", "quest_descriptions.csv"),
                            new PathMapping("gameplay.*", "gameplay.csv")
                    ))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            categoryRepository.save(Category.builder()
                    .name("UI")
                    .pathMappings(List.of(
                            new PathMapping("button.*", "buttons.csv"),
                            new PathMapping("ui.*", "ui.csv")
                    ))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            categoryRepository.save(Category.builder()
                    .name("ITEM")
                    .pathMappings(List.of(new PathMapping("item.*", "items.csv")))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            categoryRepository.save(Category.builder()
                    .name("SKILL")
                    .pathMappings(List.of(new PathMapping("skill.*", "skills.csv")))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            categoryRepository.save(Category.builder()
                    .name("MONSTER")
                    .pathMappings(List.of(new PathMapping("monster.*", "monsters.csv")))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            log.info("Seeded default categories with path mappings: General, Gameplay, UI, ITEM, SKILL, MONSTER");
        }

        if (localeRepository.count() == 0) {
            localeRepository.saveAll(List.of(
                    new Locale("pt", "Portuguese"),
                    new Locale("es", "Spanish"),
                    new Locale("fr", "French"),
                    new Locale("de", "German"),
                    new Locale("ja", "Japanese")
            ));
            log.info("Seeded default locales: pt, es, fr, de, ja");
        }

        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .username("translator")
                    .passwordHash(passwordEncoder.encode("translator123"))
                    .roles(List.of(Roles.TRANSLATOR))
                    .allowedLocales(List.of("pt", "es"))
                    .build());

            userRepository.save(User.builder()
                    .username("reviewer")
                    .passwordHash(passwordEncoder.encode("reviewer123"))
                    .roles(List.of(Roles.TRANSLATOR, Roles.REVIEWER))
                    .allowedLocales(List.of())
                    .build());

            userRepository.save(User.builder()
                    .username("manager")
                    .passwordHash(passwordEncoder.encode("manager123"))
                    .roles(List.of(Roles.TRANSLATOR, Roles.REVIEWER, Roles.MANAGER))
                    .allowedLocales(List.of())
                    .build());

            userRepository.save(User.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .roles(List.of(Roles.TRANSLATOR, Roles.REVIEWER, Roles.ADMIN))
                    .allowedLocales(List.of())
                    .build());

            log.info("Seeded default users: translator (pt/es), reviewer, manager, and admin");
        }
    }
}
