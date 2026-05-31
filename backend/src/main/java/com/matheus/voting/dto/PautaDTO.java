package com.matheus.voting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PautaDTO(
        Long id,
        @NotBlank(message = "Nome da pauta é obrigatório")
        String nome,
        @NotBlank(message = "Descrição da pauta é obrigatória")
        String descricao,
        @NotNull(message = "Tempo aberto é obrigatório")
        @Positive(message = "Tempo aberto deve ser um valor positivo")
        Integer tempoAbertoPorMinutos
) {}
