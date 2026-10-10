package com.plazoleta.plazoleta.infrastructure.security;

import com.plazoleta.plazoleta.domain.model.RolUsuario;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenParserTest {

    private static final String SECRETO = "clave-solo-para-pruebas-0123456789-abcdefghij";
    private static final String OTRO_SECRETO = "otra-clave-distinta-0123456789-abcdefghijkl";

    private final JwtTokenParser parser = new JwtTokenParser(SECRETO);

    private SecretKey clave(String secreto) {
        return Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    private Date enUnaHora() {
        return new Date(System.currentTimeMillis() + 3_600_000);
    }

    private String token(String secreto, Date vence, Long id, String rol) {
        JwtBuilder builder = Jwts.builder()
                .claim("correo", "ana@mail.com")
                .expiration(vence)
                .signWith(clave(secreto), Jwts.SIG.HS256);
        if (id != null) {
            builder.claim("id", id);
        }
        if (rol != null) {
            builder.claim("rol", rol);
        }
        return builder.compact();
    }

    @Test
    void tokenValido_devuelveElUsuario() {
        Optional<UsuarioAutenticado> resultado =
                parser.parse(token(SECRETO, enUnaHora(), 7L, "PROPIETARIO"));

        assertTrue(resultado.isPresent());
        assertEquals(7L, resultado.get().id());
        assertEquals("ana@mail.com", resultado.get().correo());
        assertEquals(RolUsuario.PROPIETARIO, resultado.get().rol());
    }

    @Test
    void firmadoConOtraClave_devuelveVacio() {
        assertTrue(parser.parse(token(OTRO_SECRETO, enUnaHora(), 7L, "PROPIETARIO")).isEmpty());
    }

    @Test
    void tokenVencido_devuelveVacio() {
        Date ayer = new Date(System.currentTimeMillis() - 86_400_000);
        assertTrue(parser.parse(token(SECRETO, ayer, 7L, "PROPIETARIO")).isEmpty());
    }

    @Test
    void sinRol_devuelveVacio() {
        assertTrue(parser.parse(token(SECRETO, enUnaHora(), 7L, null)).isEmpty());
    }

    @Test
    void rolDesconocido_devuelveVacio() {
        assertTrue(parser.parse(token(SECRETO, enUnaHora(), 7L, "SUPERADMIN")).isEmpty());
    }

    @Test
    void textoQueNoEsUnToken_devuelveVacio() {
        assertTrue(parser.parse("esto-no-es-un-token").isEmpty());
    }

    @Test
    void claveDemasiadoCorta_lanzaExcepcion() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenParser("corta"));
    }
}