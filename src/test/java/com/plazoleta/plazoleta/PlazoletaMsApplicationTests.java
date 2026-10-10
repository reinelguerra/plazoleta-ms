package com.plazoleta.plazoleta;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=clave-solo-para-pruebas-0123456789-abcdefghij")
class PlazoletaMsApplicationTests {

    @Test
    void contextLoads() {
    }
}
