package be.angularpadelclub.config;

import be.angularpadelclub.Security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                         * Auth admin.
                         */
                        .requestMatchers("/api/auth/admin/login").permitAll()

                        /*
                         * Swagger public pour la démo.
                         */
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        /*
                         * Routes explicitement utilisées côté membre.
                         * Elles doivent rester accessibles sans JWT admin.
                         */
                        .requestMatchers(HttpMethod.GET, "/api/penalites/member/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/paiements/member/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/paiements").permitAll()

                        /*
                         * Paiements côté membre/mock.
                         */
                        .requestMatchers(HttpMethod.POST, "/api/paiements/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/paiements/*/confirmer").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/paiements/*/refuser").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/paiements/*/annuler").permitAll()

                        /*
                         * Administrateurs : jamais public.
                         */
                        .requestMatchers("/api/administrateurs/**").hasAnyRole("GLOBAL", "SITE")

                        /*
                         * Gestion membres côté admin.
                         * Attention : /api/members/{matricule} reste public pour le login membre.
                         */
                        .requestMatchers("/api/members/admin/**").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers("/api/members/next-matricule").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers(HttpMethod.POST, "/api/members").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers(HttpMethod.PATCH, "/api/members/*/active").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/members/**").hasAnyRole("GLOBAL", "SITE")

                        /*
                         * Sites : lecture publique, écriture admin global.
                         */
                        .requestMatchers(HttpMethod.POST, "/api/sites/**").hasRole("GLOBAL")
                        .requestMatchers(HttpMethod.PUT, "/api/sites/**").hasRole("GLOBAL")
                        .requestMatchers(HttpMethod.DELETE, "/api/sites/**").hasRole("GLOBAL")

                        /*
                         * Terrains : lecture publique, écriture admin.
                         */
                        .requestMatchers(HttpMethod.POST, "/api/courts/**").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers(HttpMethod.PUT, "/api/courts/**").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers(HttpMethod.PATCH, "/api/courts/**").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/courts/**").hasAnyRole("GLOBAL", "SITE")

                        /*
                         * Horaires : lecture publique, écriture admin.
                         */
                        .requestMatchers(HttpMethod.POST, "/api/horaires-sites/**").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers(HttpMethod.PUT, "/api/horaires-sites/**").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/horaires-sites/**").hasAnyRole("GLOBAL", "SITE")

                        /*
                         * Jours de fermeture : lecture publique, écriture admin.
                         */
                        .requestMatchers(HttpMethod.POST, "/api/jours-fermeture/**").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers(HttpMethod.PUT, "/api/jours-fermeture/**").hasAnyRole("GLOBAL", "SITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/jours-fermeture/**").hasAnyRole("GLOBAL", "SITE")

                        /*
                         * Paiements admin sensibles.
                         * On garde le remboursement protégé.
                         */
                        .requestMatchers(HttpMethod.PATCH, "/api/paiements/*/rembourser").hasAnyRole("GLOBAL", "SITE")

                        /*
                         * Pénalités admin.
                         * Les routes /api/penalites/member/** sont déjà autorisées plus haut.
                         */
                        .requestMatchers("/api/penalites/**").hasAnyRole("GLOBAL", "SITE")

                        /*
                         * Le reste reste accessible :
                         * login membre par matricule, matches publics, réservations membre,
                         * lecture sites/courts/horaires/fermetures.
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