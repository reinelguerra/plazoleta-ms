package com.plazoleta.plazoleta.domain.validator;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.model.Restaurante;

import java.util.regex.Pattern;


public final class RestauranteValidator {

    
    private static final Pattern SOLO_DIGITOS = Pattern.compile("\\d+");

    private static final Pattern TELEFONO = Pattern.compile("^\\+?\\d{1,12}$");

    private RestauranteValidator() {
    }

    public static void validar(Restaurante restaurante) {
        if (restaurante == null) {
            throw new DomainException("El restaurante es obligatorio");
        }
        validarNombre(restaurante.getNombre());
        validarNit(restaurante.getNit());
        validarDireccion(restaurante.getDireccion());
        validarTelefono(restaurante.getTelefono());
        validarUrlLogo(restaurante.getUrlLogo());
        validarPropietario(restaurante.getIdPropietario());
    }

    private static void validarNombre(String nombre) {
        if (esVacio(nombre)) {
            throw new DomainException("El nombre del restaurante es obligatorio");
        }
        // Puede contener números, pero no estar formado únicamente por números.
        if (SOLO_DIGITOS.matcher(nombre.trim()).matches()) {
            throw new DomainException("El nombre del restaurante no puede contener solo números");
        }
    }

    private static void validarNit(String nit) {
        if (esVacio(nit)) {
            throw new DomainException("El NIT es obligatorio");
        }
        if (!SOLO_DIGITOS.matcher(nit).matches()) {
            throw new DomainException("El NIT debe ser numérico");
        }
    }

    private static void validarDireccion(String direccion) {
        if (esVacio(direccion)) {
            throw new DomainException("La dirección es obligatoria");
        }
    }

    private static void validarTelefono(String telefono) {
        if (esVacio(telefono)) {
            throw new DomainException("El teléfono es obligatorio");
        }
        if (!TELEFONO.matcher(telefono).matches()) {
            throw new DomainException(
                "El teléfono debe tener máximo 13 caracteres, solo números y opcionalmente el símbolo +");
        }
    }

    private static void validarUrlLogo(String urlLogo) {
        if (esVacio(urlLogo)) {
            throw new DomainException("La URL del logo es obligatoria");
        }
    }

    private static void validarPropietario(Long idPropietario) {
        if (idPropietario == null) {
            throw new DomainException("El id del propietario es obligatorio");
        }
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}