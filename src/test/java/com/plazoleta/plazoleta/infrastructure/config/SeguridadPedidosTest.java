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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Quién puede realizar un pedido (HU 11): solo el CLIENTE. */
@SpringBootTest(properties = "jwt.secret=clave-solo-para-pruebas-0123456789-abcdefghij")
@AutoConfigureMockMvc
class SeguridadPedidosTest {

    private static final String SECRETO = "clave-solo-para-pruebas-0123456789-abcdefghij";
    private static final String URL = "/api/v1/pedidos";
    private static final String JSON = """
            { "idRestaurante": 1, "platos": [ { "idPlato": 10, "cantidad": 2 } ] }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoServicePort pedidoServicePort;

    @BeforeEach
    void preparar() {
        when(pedidoServicePort.realizarPedido(any(Pedido.class))).thenAnswer(invocacion -> {
            Pedido pedido = invocacion.getArgument(0);
            pedido.setId(1L);
            pedido.setEstado(EstadoPedido.PENDIENTE);
            pedido.setFecha(LocalDateTime.now());
            return pedido;
        });
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

    private ResultActions enviar(String autorizacion) throws Exception {
        var peticion = post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON);
        if (autorizacion != null) {
            peticion.header("Authorization", autorizacion);
        }
        return mockMvc.perform(peticion);
    }

    @Test
    void sinToken_devuelve401() throws Exception {
        enviar(null).andExpect(status().isUnauthorized());
    }

    @Test
    void cliente_puedeRealizarPedido() throws Exception {
        enviar(bearer(RolUsuario.CLIENTE)).andExpect(status().isCreated());
    }

    @Test
    void propietario_noPuede() throws Exception {
        enviar(bearer(RolUsuario.PROPIETARIO)).andExpect(status().isForbidden());
    }

    @Test
    void empleado_noPuede() throws Exception {
        enviar(bearer(RolUsuario.EMPLEADO)).andExpect(status().isForbidden());
    }

    @Test
    void administrador_noPuede() throws Exception {
        enviar(bearer(RolUsuario.ADMINISTRADOR)).andExpect(status().isForbidden());
    }
}