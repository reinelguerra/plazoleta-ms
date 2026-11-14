package com.plazoleta.plazoleta.infrastructure.output.pin.adapter;

import com.plazoleta.plazoleta.domain.spi.GeneradorPinPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** PIN de 6 dígitos con SecureRandom: no se puede adivinar a partir de los anteriores. */
@Component
public class SecureRandomPinAdapter implements GeneradorPinPort {

    private static final int LIMITE = 1_000_000;

    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String generar() {
        return String.format("%06d", aleatorio.nextInt(LIMITE));
    }
}