package com.matheus.voting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "pauta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pauta {

    public enum StatusPauta {
        FECHADA,
        ABERTA,
        ENCERRADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da pauta é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Column
    private String descricao;

    @NotNull(message = "Tempo aberto é obrigatório")
    @Positive(message = "Tempo aberto deve ser um valor positivo")
    @Column(nullable = false)
    private Integer tempoAbertoPorMinutos;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column
    private LocalDateTime dataEncerramento;

    @Enumerated(EnumType.STRING)
    @Column
    private StatusPauta status = StatusPauta.FECHADA;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = StatusPauta.FECHADA;
        }
    }
}
