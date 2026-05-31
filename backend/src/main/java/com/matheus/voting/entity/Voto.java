package com.matheus.voting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "voto",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_vote_agenda_associate",
                columnNames = {"associate_id", "pauta_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "associate_id")
    private Long associateId;

    @NotBlank(message = "CPF é obrigatório")
    @Column(nullable = false)
    private String cpf;

    @NotNull(message = "Pauta é obrigatória")
    @ManyToOne
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;

    @NotNull(message = "Voto é obrigatório")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VotoEnum voto;

    @Column(nullable = false)
    private LocalDateTime dataVoto;

    @PrePersist
    protected void onCreate() {
        dataVoto = LocalDateTime.now();
    }

    public enum VotoEnum {
        YES("Sim"),
        NO("Não");

        private final String descricao;

        VotoEnum(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}
