package com.plazoleta.plazoleta.infrastructure.output.pin.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SecureRandomPinAdapterTest {

    @Test
    void generar_devuelveSiempreSeisDigitos() {
        SecureRandomPinAdapter adapter = new SecureRandomPinAdapter();

        for (int i = 0; i < 2000; i++) {
            String pin = adapter.generar();
            assertTrue(pin.matches("^\\d{6}$"), "PIN inválido: " + pin);
        }
    }
}