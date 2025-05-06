package de.hhu.exambyte.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

        @Bean
        public SecurityFilterChain configure(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(configurer -> configurer
                                                .requestMatchers("/", "/login", "/error").permitAll() // Öffentliche
                                                                                                      // Seiten
                                                .requestMatchers("/student/**").hasRole("STUDENT") // Geschützte Seiten
                                                .anyRequest().authenticated() // Alle anderen Anfragen
                                )
                                .oauth2Login(config -> config.userInfoEndpoint(
                                                info -> info.userService(new AppUserService()) // Benutzer-Dienst
                                )
                                                .defaultSuccessUrl("/student/dashboard", true) // Weiterleitung nach
                                                                                               // erfolgreichem
                                                // Login
                                                .failureUrl("/login?error=true") // Weiterleitung bei Fehler
                                );
                return http.build();
        }
}