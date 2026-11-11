package com.plazoleta.plazoleta.infrastructure.config;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Quién puede listar los platos de un restaurante (HU 10): solo el CLIENTE. */
@SpringBootTest(properties = "jwt.secret=clave-solo-para-pruebas-0123456789-abcdefghij")
@AutoConfigureMockMvc
class SeguridadListadoPlatosTest {

    private static final String SECRETO = "clave-solo-para-pruebas-0123456789-abcdefghij";
    private static final String URL = "/api/v1/platos/restaurante/1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestauranteServicePort restauranteServicePort;

    @MockitoBean
    private PlatoServicePort platoServicePort;

    @BeforeEach
    void preparar() {
        when(platoServicePort.listarPlatos(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(new PaginaRespuesta<>(List.of(), 0, 10, 0, 0));
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
    void cliente_puedeListar() throws Exception {
        mockMvc.perform(get(URL).header("Authorization", bearer(RolUsuario.CLIENTE)))
                .andExpect(status().isOk());
    }

    @Test
    void propietario_noPuedeListar() throws Exception {
        mockMvc.perform(get(URL).header("Authorization", bearer(RolUsuario.PROPIETARIO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void empleado_noPuedeListar() throws Exception {
        mockMvc.perform(get(URL).header("Authorization", bearer(RolUsuario.EMPLEADO)))
                .andExpect(status().isForbidden());
    }
}