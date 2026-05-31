package be.angularpadelclub.Security;

import be.angularpadelclub.Service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractBearerToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String matricule = jwtService.extractMatricule(token);
            String role = jwtService.extractRole(token);
            Integer adminId = jwtService.extractAdminId(token);
            Integer siteId = jwtService.extractSiteId(token);

            if (matricule != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                AdminPrincipal principal = new AdminPrincipal(
                        adminId,
                        matricule,
                        role,
                        siteId
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        return authorizationHeader.substring(7);
    }
}