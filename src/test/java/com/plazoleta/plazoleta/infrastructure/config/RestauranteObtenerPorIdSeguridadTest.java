package com.plazoleta.plazoleta.infrastructure.config;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** GET /restaurantes/{id}: quién puede consultarlo y qué devuelve. */
@SpringBootTest(properties = "jwt.secret=clave-solo-para-pruebas-0123456789-abcdefghij")
@AutoConfigureMockMvc
class RestauranteObtenerPorIdSeguridadTest {

    private static final String SECRETO = "clave-solo-para-pruebas-0123456789-abcdefghij";
    private static final String URL = "/api/v1/restaurantes/1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestauranteServicePort restauranteServicePort;

    @BeforeEach
    void preparar() {
        when(restauranteServicePort.obtenerRestaurante(1L)).thenReturn(new Restaurante(
                1L, "Sabor 24", "900123456", "Calle 10 # 5-20", "+573001234567",
                "https://logo.com/s.png", 10L));
        when(restauranteServicePort.obtenerRestaurante(99L)).thenThrow(new RestauranteNoEncontradoException(99L));
    }

    private String bearer(RolUsuario rol) {
        String token = Jwts.builder()
                .claim("id", 7L)
                .claim("rol", rol.name())
                .claim("correo", "test@mail.com")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
        return "Bearer " + token;
    }

    @Test
    void sinToken_devuelve401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void propietario_veElRestauranteConSuIdPropietario() throws Exception {
        mockMvc.perform(get(URL).header("Authorization", bearer(RolUsuario.PROPIETARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Sabor 24"))
                .andExpect(jsonPath("$.idPropietario").value(10));
    }

    @Test
    void administrador_tambienPuede() throws Exception {
        mockMvc.perform(get(URL).header("Authorization", bearer(RolUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void cliente_noPuede() throws Exception {
        mockMvc.perform(get(URL).header("Authorization", bearer(RolUsuario.CLIENTE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void empleado_noPuede() throws Exception {
        mockMvc.perform(get(URL).header("Authorization", bearer(RolUsuario.EMPLEADO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void restauranteInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/v1/restaurantes/99").header("Authorization", bearer(RolUsuario.PROPIETARIO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}