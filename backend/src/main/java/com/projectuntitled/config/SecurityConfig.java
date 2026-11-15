package com.projectuntitled.config;

import com.projectuntitled.auth.AuthPaths;
import com.projectuntitled.auth.web.CookieBearerTokenResolver;
import com.projectuntitled.auth.web.CsrfHeaderFilter;
import com.projectuntitled.auth.web.SecurityErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless API security. Roles come from the JWT "role" claim (CREATOR or MEMBER). That a member's
 * token belongs to the community in the URL is checked separately by web.TenantInterceptor.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http, CookieBearerTokenResolver tokenResolver,
            SecurityErrorHandler errorHandler) throws Exception {
        http
                // Replaced by CsrfHeaderFilter (custom-header pattern); see docs/ARCHITECTURE.md
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(AuthPaths.PUBLIC + "/**").permitAll()
                        .requestMatchers(AuthPaths.CREATOR_AUTH + "/**").permitAll()
                        .requestMatchers(AuthPaths.MEMBER + "/*/auth/**").permitAll()
                        .requestMatchers(AuthPaths.CREATOR + "/**").hasRole("CREATOR")
                        .requestMatchers(AuthPaths.MEMBER + "/**").hasRole("MEMBER")
                        // New endpoints must live under one of the roots above.
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(tokenResolver)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .addFilterBefore(new CsrfHeaderFilter(errorHandler.exceptionResolver()),
                        BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
