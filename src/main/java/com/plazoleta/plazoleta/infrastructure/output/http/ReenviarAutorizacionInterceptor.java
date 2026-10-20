package com.plazoleta.plazoleta.infrastructure.output.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;


public class ReenviarAutorizacionInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
            String autorizacion = atributos.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (autorizacion != null) {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, autorizacion);
            }
        }
        return execution.execute(request, body);
    }
}