package com.plazoleta.plazoleta.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationFilterTest {

    private static final String SECRETO = "clave-solo-para-pruebas-0123456789-abcdefghij";

    private final JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(new JwtTokenParser(SECRETO));

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private String tokenValido() {
        return Jwts.builder()
                .claim("id", 7L)
                .claim("rol", "PROPIETARIO")
                .claim("correo", "ana@mail.com")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    @Test
    void conTokenValido_autenticaConElRolYContinuaLaCadena() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenValido());
        MockFilterChain cadena = new MockFilterChain();

        filtro.doFilter(request, new MockHttpServletResponse(), cadena);

        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(autenticacion);
        assertTrue(autenticacion.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROPIETARIO")));
        assertEquals(7L, ((UsuarioAutenticado) autenticacion.getPrincipal()).id());
        assertNotNull(cadena.getRequest());
    }

    @Test
    void sinCabecera_noAutenticaPeroContinuaLaCadena() throws Exception {
        MockFilterChain cadena = new MockFilterChain();

        filtro.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(cadena.getRequest());
    }

    @Test
    void conTokenInvalido_noAutenticaPeroContinuaLaCadena() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-falso");
        MockFilterChain cadena = new MockFilterChain();

        filtro.doFilter(request, new MockHttpServletResponse(), cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(cadena.getRequest());
    }
}