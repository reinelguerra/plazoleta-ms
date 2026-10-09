package com.plazoleta.plazoleta.infrastructure.exception;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.UsuarioNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce las excepciones a respuestas HTTP con el formato ErrorResponse.
 * Spring elige el manejador más específico: UsuarioNoEncontradoException (404)
 * gana sobre DomainException (400), de la que hereda.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> usuarioNoEncontrado(UsuarioNoEncontradoException e) {
        return construir(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler(RestauranteNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> restauranteNoEncontrado(RestauranteNoEncontradoException e) {
        return construir(HttpStatus.NOT_FOUND, e.getMessage());
       }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> reglaDeNegocio(DomainException e) {
        return construir(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ServicioUsuariosNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> usuariosNoDisponible(ServicioUsuariosNoDisponibleException e) {
        return construir(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException e) {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return construir(HttpStatus.BAD_REQUEST, "El parámetro '" + e.getName() + "' tiene un valor inválido");
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponse(mensaje, status.value()));
    }
}