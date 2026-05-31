package com.matheus.voting.dto;

import java.time.LocalDateTime;

public record PautaInfoDTO(
        Long id,
        String nome,
        String descricao,
        Integer tempoAbertoPorMinutos,
        LocalDateTime dataCriacao,
        LocalDateTime dataEncerramento,
        String status,
        boolean aberta,
        long totalVotos,
        long votosSim,
        long votosNao
) {}
