package com.matheus.voting.repository;

import com.matheus.voting.entity.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {
    Optional<Voto> findByAssociateIdAndPautaId(Long associateId, Long pautaId);
    List<Voto> findByPautaId(Long pautaId);
    long countByPautaId(Long pautaId);
    long countByPautaIdAndVoto(Long pautaId, Voto.VotoEnum voto);
}
