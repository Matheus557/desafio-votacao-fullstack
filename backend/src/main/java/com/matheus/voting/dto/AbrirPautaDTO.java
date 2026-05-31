package com.matheus.voting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AbrirPautaDTO(
        @NotNull(message = "Tempo aberto é obrigatório")
        @Positive(message = "Tempo aberto deve ser um valor positivo")
        Integer tempoAbertoPorMinutos
) {}
