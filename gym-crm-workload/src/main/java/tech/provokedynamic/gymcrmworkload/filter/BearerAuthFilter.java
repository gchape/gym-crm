package tech.provokedynamic.gymcrmworkload.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrmworkload.security.JwtValidationService;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
public class BearerAuthFilter extends HttpFilter {

    private final JwtValidationService jwtValidationService;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ") || !jwtValidationService.isValid(header.substring(7))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid bearer token");
            return;
        }

        chain.doFilter(request, response);
    }
}
