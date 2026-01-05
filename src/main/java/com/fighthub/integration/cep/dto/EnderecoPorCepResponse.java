package com.fighthub.integration.cep.dto;

public record EnderecoPorCepResponse(

        String cep,
        String logradouro,
        String bairro,
        String cidade,
        String uf

) {
}
