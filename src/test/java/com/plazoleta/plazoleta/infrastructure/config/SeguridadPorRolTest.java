package com.plazoleta.plazoleta.infrastructure.config;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.model.Pagina;
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

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la cadena de seguridad REAL: qué rol puede entrar a cada ruta.
 * Los casos de uso son dobles de Mockito: aquí solo se prueba quién pasa y quién no.
 */
@SpringBootTest(properties = "jwt.secret=clave-solo-para-pruebas-0123456789-abcdefghij")
@AutoConfigureMockMvc
class SeguridadPorRolTest {

    private static final String SECRETO = "clave-solo-para-pruebas-0123456789-abcdefghij";

    private static final String RESTAURANTES = "/api/v1/restaurantes";
    private static final String PLATOS = "/api/v1/platos";

    private static final String JSON_RESTAURANTE = """
            { "nombre": "Sabor 24", "nit": "900123456", "direccion": "Calle 10 # 5-20",
              "telefono": "+573001234567", "urlLogo": "https://logo.com/s.png", "idPropietario": 7 }
            """;
    private static final String JSON_PLATO = """
            { "nombre": "Bandeja", "precio": 25000, "descripcion": "Plato tipico",
              "urlImagen": "https://img.com/b.png", "categoria": "Fuerte", "idRestaurante": 1 }
            """;
    private static final String JSON_MODIFICACION = "{ \"precio\": 30000, \"descripcion\": \"Nueva\" }";
    private static final String JSON_ESTADO = "{ \"activo\": false }";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestauranteServicePort restauranteServicePort;

    @MockitoBean
    private PlatoServicePort platoServicePort;

    @BeforeEach
    void preparar() {
        when(restauranteServicePort.listarRestaurantes(anyInt(), anyInt()))
                .thenReturn(new Pagina<>(List.of(), 0, 10, 0, 0));
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

    // ---------- 401: sin identidad ----------

    @Test
    void sinToken_devuelve401ConElFormatoAcordado() throws Exception {
        mockMvc.perform(get(RESTAURANTES))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void conTokenFalso_devuelve401() throws Exception {
        mockMvc.perform(get(RESTAURANTES).header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Restaurantes ----------

    @Test
    void administrador_creaRestaurante() throws Exception {
        mockMvc.perform(post(RESTAURANTES).header("Authorization", bearer(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_RESTAURANTE))
                .andExpect(status().isCreated());
    }

    @Test
    void cliente_noPuedeCrearRestaurante() throws Exception {
        mockMvc.perform(post(RESTAURANTES).header("Authorization", bearer(RolUsuario.CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_RESTAURANTE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void propietario_noPuedeCrearRestaurante() throws Exception {
        mockMvc.perform(post(RESTAURANTES).header("Authorization", bearer(RolUsuario.PROPIETARIO))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_RESTAURANTE))
                .andExpect(status().isForbidden());
    }

    @Test
    void cliente_listaRestaurantes() throws Exception {
        mockMvc.perform(get(RESTAURANTES).header("Authorization", bearer(RolUsuario.CLIENTE)))
                .andExpect(status().isOk());
    }

    @Test
    void administrador_noPuedeListarRestaurantes() throws Exception {
        mockMvc.perform(get(RESTAURANTES).header("Authorization", bearer(RolUsuario.ADMINISTRADOR)))
                .andExpect(status().isForbidden());
    }

    // ---------- Platos ----------

    @Test
    void propietario_creaPlato() throws Exception {
        mockMvc.perform(post(PLATOS).header("Authorization", bearer(RolUsuario.PROPIETARIO))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_PLATO))
                .andExpect(status().isCreated());
    }

    @Test
    void administrador_noPuedeCrearPlato() throws Exception {
        mockMvc.perform(post(PLATOS).header("Authorization", bearer(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_PLATO))
                .andExpect(status().isForbidden());
    }

    @Test
    void propietario_modificaPlato() throws Exception {
        mockMvc.perform(patch(PLATOS + "/1").header("Authorization", bearer(RolUsuario.PROPIETARIO))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_MODIFICACION))
                .andExpect(status().isOk());
    }

    @Test
    void cliente_noPuedeCambiarElEstadoDeUnPlato() throws Exception {
        mockMvc.perform(patch(PLATOS + "/1/estado").header("Authorization", bearer(RolUsuario.CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_ESTADO))
                .andExpect(status().isForbidden());
    }

    @Test
    void empleado_noPuedeModificarPlatos() throws Exception {
        mockMvc.perform(patch(PLATOS + "/1").header("Authorization", bearer(RolUsuario.EMPLEADO))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_MODIFICACION))
                .andExpect(status().isForbidden());
    }
}