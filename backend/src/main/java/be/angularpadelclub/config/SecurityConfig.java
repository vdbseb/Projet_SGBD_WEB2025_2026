package be.angularpadelclub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                /*
                 * API REST appelée par Angular.
                 * On désactive CSRF car on ne fonctionne pas avec une session serveur/cookie.
                 */
                .csrf(AbstractHttpConfigurer::disable)

                /*
                 * On garde la config CORS déclarée dans WebConfig.
                 */
                .cors(Customizer.withDefaults())

                /*
                 * Étape temporaire :
                 * Spring Security est installé, mais aucune route n'est encore protégée.
                 * On verrouillera les routes admin après avoir validé le login JWT.
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .anyRequest().permitAll()
                )

                /*
                 * On désactive les mécanismes par défaut inutiles pour une API REST Angular.
                 */
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}