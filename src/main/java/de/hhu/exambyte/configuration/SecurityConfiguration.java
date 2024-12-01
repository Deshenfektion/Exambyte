package de.hhu.exambyte.configuration;

import de.hhu.exambyte.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
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
                            .anyRequest().authenticated()
                    )
                    .oauth2Login(config ->
                            config.userInfoEndpoint(
                                    info -> info.userService(new AppUserService())
                            ));
            return http.build();
        }
}

