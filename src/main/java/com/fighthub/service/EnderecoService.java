package com.fighthub.service;

import com.fighthub.exception.CepInvalidoException;
import com.fighthub.exception.CepNaoEncontradoException;
import com.fighthub.exception.IntegrationExternalException;
import com.fighthub.integration.cep.ViaCepClient;
import com.fighthub.integration.cep.dto.EnderecoPorCepResponse;
import com.fighthub.integration.cep.dto.ViaCepResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final ViaCepClient viaCepClient;

    public EnderecoPorCepResponse buscarEnderecoPorCep(String cep) {
        String cepNormalizado = normalizarCep(cep);

        ViaCepResponse via = viaCepClient.buscarEnderecoPorCep(cepNormalizado);

        if (via == null) {
            throw new IntegrationExternalException("Resposta vazia do ViaCEP");
        }

        if (Boolean.TRUE.equals(via.erro())) {
            throw new CepNaoEncontradoException("CEP não encontrado: " + cepNormalizado);
        }

        return new EnderecoPorCepResponse(
                cepNormalizado,
                via.logradouro(),
                via.bairro(),
                via.localidade(),
                via.uf()
        );
    }

    private String normalizarCep(String cep) {
        if (cep == null) throw new CepInvalidoException("CEP é obrigatório");

        String digits = cep.replaceAll("\\D", "");
        if (!digits.matches("\\d{8}")) {
            throw new CepInvalidoException("CEP inválido. Informe 8 dígitos.");
        }
        return digits;
    }

}
