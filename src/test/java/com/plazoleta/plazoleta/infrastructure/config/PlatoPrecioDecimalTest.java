package com.plazoleta.plazoleta.infrastructure.config;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.model.Plato;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Con la app real (Jackson de Spring Boot): un precio con decimales debe rechazarse, no truncarse. */
@SpringBootTest(properties = "jwt.secret=clave-solo-para-pruebas-0123456789-abcdefghij")
@AutoConfigureMockMvc
class PlatoPrecioDecimalTest {

    private static final String SECRETO = "clave-solo-para-pruebas-0123456789-abcdefghij";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlatoServicePort platoServicePort;

    @BeforeEach
    void preparar() {
        Plato plato = new Plato(1L, "Bandeja", 25000, "d", "u", "c", true, 1L);
        when(platoServicePort.crearPlato(any(Plato.class), anyLong())).thenReturn(plato);
        when(platoServicePort.modificarPlato(anyLong(), any(), any(), anyLong())).thenReturn(plato);
    }

    private String bearerPropietario() {
        String token = Jwts.builder()
                .claim("id", 5L)
                .claim("rol", "PROPIETARIO")
                .claim("correo", "p@mail.com")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
        return "Bearer " + token;
    }

    private String cuerpoCrear(String precio) {
        return "{\"nombre\":\"Bandeja\",\"precio\":" + precio + ",\"descripcion\":\"d\","
                + "\"urlImagen\":\"u\",\"categoria\":\"c\",\"idRestaurante\":1}";
    }

    @Test
    void control_precioEnteroValido_devuelve201() throws Exception {
        mockMvc.perform(post("/api/v1/platos").header("Authorization", bearerPropietario())
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoCrear("25000")))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_precioConDecimales_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/platos").header("Authorization", bearerPropietario())
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoCrear("10.5")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modificar_precioConDecimales_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/v1/platos/1").header("Authorization", bearerPropietario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"precio\":10.5,\"descripcion\":\"algo\"}"))
                .andExpect(status().isBadRequest());
    }
}