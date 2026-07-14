package tech.provokedynamic.gymcrmworkload.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.provokedynamic.gymcrmworkload.security.JwtValidationService;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class BearerAuthFilter extends OncePerRequestFilter {

    private final JwtValidationService jwtValidationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtValidationService.isValid(token)) {
                var auth = new UsernamePasswordAuthenticationToken(token, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}
