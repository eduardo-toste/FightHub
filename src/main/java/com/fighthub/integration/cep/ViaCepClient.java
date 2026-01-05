package com.fighthub.integration.cep;

import com.fighthub.exception.IntegrationExternalException;
import com.fighthub.integration.cep.dto.ViaCepResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ViaCepClient {

    private final WebClient viaCepWebClient;

    public ViaCepResponse buscarEnderecoPorCep(String cepSomenteDigitos) {
        return viaCepWebClient.get()
                .uri("/ws/{cep}/json/", cepSomenteDigitos)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(
                                        new IntegrationExternalException("Erro ao consultar ViaCEP: " + resp.statusCode())
                                ))
                )
                .bodyToMono(ViaCepResponse.class)
                .block();
    }

}
