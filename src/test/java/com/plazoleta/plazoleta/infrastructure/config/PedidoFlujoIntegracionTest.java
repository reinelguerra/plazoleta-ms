package com.plazoleta.plazoleta.infrastructure.config;

import com.jayway.jsonpath.JsonPath;
import com.plazoleta.plazoleta.domain.model.DatosUsuario;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.spi.TrazabilidadClientPort;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo real de pedidos (HU 11, 12, 13 y 16) con casos de uso, seguridad y base H2 de verdad.
 * Solo se simulan los servicios externos: usuarios-ms y trazabilidad-ms.
 */
@SpringBootTest(properties = "jwt.secret=clave-solo-para-pruebas-0123456789-abcdefghij")
@AutoConfigureMockMvc
class PedidoFlujoIntegracionTest {

    private static final String SECRETO = "clave-solo-para-pruebas-0123456789-abcdefghij";
    private static final String AUTH = "Authorization";
    private static final AtomicLong IDS = new AtomicLong(1000);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioClientPort usuarioClientPort;

    @MockitoBean
    private TrazabilidadClientPort trazabilidadClientPort;

    private record Escenario(long idRestaurante, long idPlato) {
    }

    private String bearer(long id, RolUsuario rol) {
        String token = Jwts.builder()
                .claim("id", id)
                .claim("rol", rol.name())
                .claim("correo", "u" + id + "@mail.com")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
        return "Bearer " + token;
    }

    private long idDe(MvcResult resultado) throws Exception {
        Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    /** Un restaurante nuevo con un plato activo, creados por la API como lo haría el administrador y el dueño. */
    private Escenario crearEscenario() throws Exception {
        long propietario = IDS.incrementAndGet();
        when(usuarioClientPort.obtenerRolPorId(propietario)).thenReturn(Optional.of(RolUsuario.PROPIETARIO));
        long nit = 900_000_000L + IDS.incrementAndGet();

        String restaurante = """
                {"nombre":"Rest %d","nit":"%d","direccion":"Calle 1","telefono":"+573001234567",
                 "urlLogo":"http://logo.png","idPropietario":%d}
                """.formatted(nit, nit, propietario);
        long idRestaurante = idDe(mockMvc.perform(post("/api/v1/restaurantes")
                        .header(AUTH, bearer(1, RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON).content(restaurante))
                .andExpect(status().isCreated()).andReturn());

        String plato = """
                {"nombre":"Plato %d","precio":15000,"descripcion":"Rico","urlImagen":"http://img.png",
                 "categoria":"Fuerte","idRestaurante":%d}
                """.formatted(nit, idRestaurante);
        long idPlato = idDe(mockMvc.perform(post("/api/v1/platos")
                        .header(AUTH, bearer(propietario, RolUsuario.PROPIETARIO))
                        .contentType(MediaType.APPLICATION_JSON).content(plato))
                .andExpect(status().isCreated()).andReturn());

        return new Escenario(idRestaurante, idPlato);
    }

    private void registrarEmpleado(long idEmpleado, long idRestaurante) {
        when(usuarioClientPort.obtenerUsuarioPorId(idEmpleado)).thenReturn(Optional.of(
                new DatosUsuario(idEmpleado, RolUsuario.EMPLEADO, "+573001112233", idRestaurante)));
    }

    private ResultActions pedir(long cliente, Escenario escenario) throws Exception {
        String pedido = """
                {"idRestaurante":%d,"platos":[{"idPlato":%d,"cantidad":2}]}
                """.formatted(escenario.idRestaurante(), escenario.idPlato());
        return mockMvc.perform(post("/api/v1/pedidos").header(AUTH, bearer(cliente, RolUsuario.CLIENTE))
                .contentType(MediaType.APPLICATION_JSON).content(pedido));
    }

    private ResultActions listar(long empleado, String estado) throws Exception {
        return mockMvc.perform(get("/api/v1/pedidos").param("estado", estado)
                .header(AUTH, bearer(empleado, RolUsuario.EMPLEADO)));
    }

    private ResultActions tomar(long empleado, long idPedido) throws Exception {
        return mockMvc.perform(patch("/api/v1/pedidos/{id}/preparacion", idPedido)
                .header(AUTH, bearer(empleado, RolUsuario.EMPLEADO)));
    }

    private ResultActions cancelar(long cliente, long idPedido) throws Exception {
        return mockMvc.perform(patch("/api/v1/pedidos/{id}/cancelacion", idPedido)
                .header(AUTH, bearer(cliente, RolUsuario.CLIENTE)));
    }

    @Test
    void flujoCompleto_pedirListarTomarYNoPoderCancelarEnPreparacion() throws Exception {
        Escenario escenario = crearEscenario();
        long cliente = IDS.incrementAndGet();
        long empleado = IDS.incrementAndGet();
        registrarEmpleado(empleado, escenario.idRestaurante());

        long idPedido = idDe(pedir(cliente, escenario)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn());

        pedir(cliente, escenario)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.CLIENTE_CON_PEDIDO_EN_PROCESO));

        listar(empleado, "PENDIENTE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[*].id", hasItem(Math.toIntExact(idPedido))))
                .andExpect(jsonPath("$.contenido[0].pin").doesNotExist());

        tomar(empleado, idPedido)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"))
                .andExpect(jsonPath("$.idEmpleado").value(empleado));

        listar(empleado, "PENDIENTE")
                .andExpect(jsonPath("$.contenido[*].id", not(hasItem(Math.toIntExact(idPedido)))));
        listar(empleado, "EN_PREPARACION")
                .andExpect(jsonPath("$.contenido[*].id", hasItem(Math.toIntExact(idPedido))));

        tomar(empleado, idPedido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PEDIDO_NO_PENDIENTE));

        cancelar(cliente, idPedido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PEDIDO_NO_CANCELABLE));

        verify(trazabilidadClientPort).registrarCambioEstado(argThat(registro ->
                registro.idPedido().equals(idPedido)
                        && registro.estadoAnterior() == EstadoPedido.PENDIENTE
                        && registro.estadoNuevo() == EstadoPedido.EN_PREPARACION
                        && Long.valueOf(empleado).equals(registro.idEmpleado())));
    }

    @Test
    void cancelacion_soloElDuenoYSoloPendiente_yLiberaAlClienteParaPedirDeNuevo() throws Exception {
        Escenario escenario = crearEscenario();
        long cliente = IDS.incrementAndGet();
        long otroCliente = IDS.incrementAndGet();
        long empleado = IDS.incrementAndGet();
        registrarEmpleado(empleado, escenario.idRestaurante());

        long idPedido = idDe(pedir(cliente, escenario).andExpect(status().isCreated()).andReturn());

        cancelar(otroCliente, idPedido)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PEDIDO_DE_OTRO_CLIENTE));

        cancelar(cliente, idPedido)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        listar(empleado, "CANCELADO")
                .andExpect(jsonPath("$.contenido[*].id", hasItem(Math.toIntExact(idPedido))));
        listar(empleado, "PENDIENTE")
                .andExpect(jsonPath("$.contenido[*].id", not(hasItem(Math.toIntExact(idPedido)))));

        cancelar(cliente, idPedido).andExpect(status().isBadRequest());

        pedir(cliente, escenario).andExpect(status().isCreated());

        verify(trazabilidadClientPort).registrarCambioEstado(argThat(registro ->
                registro.idPedido().equals(idPedido)
                        && registro.estadoAnterior() == EstadoPedido.PENDIENTE
                        && registro.estadoNuevo() == EstadoPedido.CANCELADO));
    }

    @Test
    void empleadoDeOtroRestaurante_noVeNiTomaPedidosAjenos() throws Exception {
        Escenario restaurante1 = crearEscenario();
        Escenario restaurante2 = crearEscenario();
        long cliente = IDS.incrementAndGet();
        long empleadoDelOtro = IDS.incrementAndGet();
        registrarEmpleado(empleadoDelOtro, restaurante2.idRestaurante());

        long idPedido = idDe(pedir(cliente, restaurante1).andExpect(status().isCreated()).andReturn());

        listar(empleadoDelOtro, "PENDIENTE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[*].id", not(hasItem(Math.toIntExact(idPedido)))));

        tomar(empleadoDelOtro, idPedido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PEDIDO_DE_OTRO_RESTAURANTE));
    }

    @Test
    void pedidoInexistente_devuelve404() throws Exception {
        long empleado = IDS.incrementAndGet();
        long cliente = IDS.incrementAndGet();
        registrarEmpleado(empleado, 1L);

        tomar(empleado, 987654321L).andExpect(status().isNotFound());
        cancelar(cliente, 987654321L).andExpect(status().isNotFound());
    }
}