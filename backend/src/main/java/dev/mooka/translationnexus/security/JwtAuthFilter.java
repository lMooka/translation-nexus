package dev.mooka.translationnexus.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtTokenProvider.parseToken(token);
                String username = claims.getSubject();
                List<?> roles = claims.get("roles", List.class);
                List<?> allowedLocales = claims.get("allowedLocales", List.class);

                List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
                if (roles != null) {
                    for (Object roleObj : roles) {
                        if (roleObj instanceof String role) {
                            authorities.add(new SimpleGrantedAuthority(Roles.ROLE_PREFIX + role.toUpperCase()));
                        }
                    }
                }
                if (allowedLocales != null) {
                    for (Object localeObj : allowedLocales) {
                        if (localeObj instanceof String locale) {
                            authorities.add(new SimpleGrantedAuthority(Roles.ROLE_TRANSLATOR + "_" + locale.toUpperCase()));
                        }
                    }
                }

                var auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                MDC.put("user", username);
            } catch (Exception ignored) {
                // Invalid or expired token — request will fail authorization
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("user");
        }
    }
}
