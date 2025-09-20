package com.fighthub.model;

import com.fighthub.dto.endereco.EnderecoRequest;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Endereco {

    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

    public String formatado() {
        return String.format("%s, %s - %s/%s", logradouro, numero, cidade, estado);
    }

    public void patchUpdate(EnderecoRequest request) {
        if (request.cep() != null) { this.cep = request.cep(); }
        if (request.logradouro() != null) { this.logradouro = request.logradouro(); }
        if (request.numero() != null) { this.numero = request.numero(); }
        if (request.complemento() != null) { this.complemento = request.complemento(); }
        if (request.bairro() != null) { this.bairro = request.bairro(); }
        if (request.cidade() != null) { this.cidade = request.cidade(); }
        if (request.estado() != null) { this.estado = request.estado(); }
    }
}
