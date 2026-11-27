package ch.sponsorplatz.home;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class OeffentlicheTestSecurityConfig {

    @Bean
    @Order(0)
    SecurityFilterChain oeffentlicheTestseite(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/__test/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .build();
    }
}
