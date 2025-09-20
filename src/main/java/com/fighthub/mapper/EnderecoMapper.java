package com.fighthub.mapper;

import com.fighthub.dto.auth.AtivacaoRequest;
import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.model.Endereco;

public class EnderecoMapper {

    public static Endereco toEntity(EnderecoRequest request) {
        return new Endereco(
                request.cep(),
                request.logradouro(),
                request.numero(),
                request.complemento(),
                request.bairro(),
                request.cidade(),
                request.estado()
        );
    }

    public static Endereco toEntity(AtivacaoRequest request) {
        return new Endereco(
                request.endereco().cep(),
                request.endereco().logradouro(),
                request.endereco().numero(),
                request.endereco().complemento(),
                request.endereco().bairro(),
                request.endereco().cidade(),
                request.endereco().estado()
        );
    }
}
