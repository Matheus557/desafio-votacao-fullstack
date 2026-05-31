package com.matheus.voting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VotoRequestDTO(
        @NotNull(message = "ID do associado é obrigatório")
        Long associateId,
        @NotBlank(message = "CPF é obrigatório")
        String cpf,
        @NotBlank(message = "Voto é obrigatório")
        String vote
) {}
