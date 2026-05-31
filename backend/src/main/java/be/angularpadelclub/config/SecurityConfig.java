package be.angularpadelclub.config;

import be.angularpadelclub.Security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /*
                 * Réponses HTTP propres :
                 * - 401 si aucun JWT valide n'est fourni.
                 * - 403 si le JWT existe mais n'a pas le bon rôle.
                 */
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentification admin requise."
                                )
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Accès refusé pour cet administrateur."
                                )
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        /*
                         * Login admin public : sans ça, impossible de récupérer un token.
                         */
                        .requestMatchers("/api/auth/admin/login").permitAll()

                        /*
                         * Swagger public pour la démo.
                         */
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        /*
                         * Première vraie protection JWT.
                         */
                        .requestMatchers("/api/administrateurs/**").hasAnyRole("GLOBAL", "SITE")

                        /*
                         * Temporaire : le reste reste ouvert tant que le frontend
                         * ne sait pas encore envoyer le JWT.
                         */
                        .requestMatchers("/api/**").permitAll()

                        .anyRequest().permitAll()
                )

                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}