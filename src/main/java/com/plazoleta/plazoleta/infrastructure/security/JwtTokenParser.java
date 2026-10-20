package com.plazoleta.plazoleta.infrastructure.security;

import com.plazoleta.plazoleta.domain.model.RolUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


public class JwtTokenParser {

    private static final int LONGITUD_MINIMA_CLAVE = 32;

    private final SecretKey clave;

    public JwtTokenParser(String secreto) {
        byte[] bytes = secreto == null ? new byte[0] : secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < LONGITUD_MINIMA_CLAVE) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos " + LONGITUD_MINIMA_CLAVE + " caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
    }

    /** Devuelve vacío si el token es inválido, está vencido, o le faltan datos. */
    public Optional<UsuarioAutenticado> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(token).getPayload();

            Number id = claims.get("id", Number.class);
            String rol = claims.get("rol", String.class);
            String correo = claims.get("correo", String.class);
            if (id == null || rol == null) {
                return Optional.empty();
            }
            return Optional.of(new UsuarioAutenticado(id.longValue(), correo, RolUsuario.valueOf(rol)));
        } catch (JwtException | IllegalArgumentException e) {
            // firma inválida, token vencido, mal formado, o rol desconocido
            return Optional.empty();
        }
    }
}