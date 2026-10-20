package com.plazoleta.plazoleta.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtTokenParser jwtTokenParser;

    public JwtAuthenticationFilter(JwtTokenParser jwtTokenParser) {
        this.jwtTokenParser = jwtTokenParser;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            jwtTokenParser.parse(cabecera.substring(PREFIJO.length()).trim()).ifPresent(usuario -> {
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().name())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        filterChain.doFilter(request, response);
    }
}