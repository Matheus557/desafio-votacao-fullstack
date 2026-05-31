package com.matheus.voting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VotoDTO(
        Long id,
        @NotBlank(message = "CPF é obrigatório")
        String cpf,
        @NotNull(message = "ID da pauta é obrigatório")
        Long pautaId,
        @NotBlank(message = "Voto é obrigatório")
        String voto
) {}
