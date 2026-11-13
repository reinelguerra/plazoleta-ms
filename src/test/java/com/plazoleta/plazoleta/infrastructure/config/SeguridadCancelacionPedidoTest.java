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

/** Quién puede cancelar (HU 16: solo CLIENTE) y que la regla de HU 13 sigue intacta (solo EMPLEADO). */
@SpringBootTest(properties = "jwt.secret=clave-solo-para-pruebas-0123456789-abcdefghij")
@AutoConfigureMockMvc
class SeguridadCancelacionPedidoTest {

    private static final String SECRETO = "clave-solo-para-pruebas-0123456789-abcdefghij";
    private static final String CANCELAR = "/api/v1/pedidos/40/cancelacion";
    private static final String PREPARAR = "/api/v1/pedidos/40/preparacion";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoServicePort pedidoServicePort;

    @BeforeEach
    void preparar() {
        Pedido pedido = new Pedido();
        pedido.setId(40L);
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setPlatos(List.of());
        when(pedidoServicePort.cancelarPedido(anyLong(), anyLong())).thenReturn(pedido);
        when(pedidoServicePort.iniciarPreparacion(anyLong(), anyLong())).thenReturn(pedido);
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
    void cancelar_sinToken_devuelve401() throws Exception {
        mockMvc.perform(patch(CANCELAR)).andExpect(status().isUnauthorized());
    }

    @Test
    void cancelar_cliente_puede() throws Exception {
        mockMvc.perform(patch(CANCELAR).header("Authorization", bearer(RolUsuario.CLIENTE)))
                .andExpect(status().isOk());
    }

    @Test
    void cancelar_empleado_noPuede() throws Exception {
        mockMvc.perform(patch(CANCELAR).header("Authorization", bearer(RolUsuario.EMPLEADO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelar_propietario_noPuede() throws Exception {
        mockMvc.perform(patch(CANCELAR).header("Authorization", bearer(RolUsuario.PROPIETARIO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelar_administrador_noPuede() throws Exception {
        mockMvc.perform(patch(CANCELAR).header("Authorization", bearer(RolUsuario.ADMINISTRADOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void preparar_siguesiendoSoloDelEmpleado() throws Exception {
        mockMvc.perform(patch(PREPARAR).header("Authorization", bearer(RolUsuario.EMPLEADO)))
                .andExpect(status().isOk());
        mockMvc.perform(patch(PREPARAR).header("Authorization", bearer(RolUsuario.CLIENTE)))
                .andExpect(status().isForbidden());
    }
}