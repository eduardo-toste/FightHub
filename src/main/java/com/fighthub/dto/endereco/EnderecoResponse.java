package com.fighthub.dto.endereco;

import com.fighthub.model.Endereco;

public record EnderecoResponse(

        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado

) {

    public static EnderecoResponse fromEntity(Endereco endereco) {
        return new EnderecoResponse(
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado()
        );
    }

}

