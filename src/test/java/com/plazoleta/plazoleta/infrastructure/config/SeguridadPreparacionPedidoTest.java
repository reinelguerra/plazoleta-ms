package com.plazoleta.plazoleta.infrastructure.config;

import com.plazoleta.plazoleta.domain.api.PedidoServicePort;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Quién puede tomar un pedido (HU 13): solo el EMPLEADO. */
@SpringBootTest(properties = "jwt.secret=clave-solo-para-pruebas-0123456789-abcdefghij")
@AutoConfigureMockMvc
class SeguridadPreparacionPedidoTest {

    private static final String SECRETO = "clave-solo-para-pruebas-0123456789-abcdefghij";
    private static final String URL = "/api/v1/pedidos/40/preparacion";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoServicePort pedidoServicePort;

    @BeforeEach
    void preparar() {
        Pedido pedido = new Pedido();
        pedido.setId(40L);
        pedido.setEstado(EstadoPedido.EN_PREPARACION);
        pedido.setPlatos(List.of());
        when(pedidoServicePort.iniciarPreparacion(anyLong(), anyLong())).thenReturn(pedido);
    }

    private String bearer(RolUsuario rol) {
        String token = Jwts.builder()
                .claim("id", 12L)
                .claim("rol", rol.name())
                .claim("correo", "test@mail.com")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
        return "Bearer " + token;
    }

    @Test
    void sinToken_devuelve401() throws Exception {
        mockMvc.perform(patch(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void empleado_puedeTomarElPedido() throws Exception {
        mockMvc.perform(patch(URL).header("Authorization", bearer(RolUsuario.EMPLEADO)))
                .andExpect(status().isOk());
    }

    @Test
    void cliente_noPuede() throws Exception {
        mockMvc.perform(patch(URL).header("Authorization", bearer(RolUsuario.CLIENTE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void propietario_noPuede() throws Exception {
        mockMvc.perform(patch(URL).header("Authorization", bearer(RolUsuario.PROPIETARIO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void administrador_noPuede() throws Exception {
        mockMvc.perform(patch(URL).header("Authorization", bearer(RolUsuario.ADMINISTRADOR)))
                .andExpect(status().isForbidden());
    }
}