package dev.mooka.translationnexus.config;

import dev.mooka.translationnexus.security.JwtAuthFilter;
import dev.mooka.translationnexus.security.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // ADMIN-only
                        .requestMatchers("/api/users/**").hasRole(Roles.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/translations/keys").hasRole(Roles.MANAGER)
                        .requestMatchers(HttpMethod.PUT, "/api/translations/*/priority").hasRole(Roles.MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/translations/**").hasRole(Roles.MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/versions").hasRole(Roles.MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasRole(Roles.MANAGER)
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole(Roles.MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole(Roles.MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/locales").hasRole(Roles.MANAGER)
                        .requestMatchers(HttpMethod.PUT, "/api/locales/**").hasRole(Roles.MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/locales/**").hasRole(Roles.MANAGER)
                        .requestMatchers("/api/export", "/api/export/**").hasRole(Roles.MANAGER)
                        // REVIEWER-only
                        .requestMatchers(HttpMethod.POST, "/api/translations/import").hasRole(Roles.REVIEWER)
                        .requestMatchers(HttpMethod.GET, "/api/translations/pending").hasRole(Roles.REVIEWER)
                        .requestMatchers(HttpMethod.POST, "/api/translations/*/*/approve").hasRole(Roles.REVIEWER)
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
