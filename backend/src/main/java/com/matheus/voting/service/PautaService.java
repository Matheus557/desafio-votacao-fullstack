package com.matheus.voting.service;

import com.matheus.voting.dto.AbrirPautaDTO;
import com.matheus.voting.dto.PautaDTO;
import com.matheus.voting.dto.PautaInfoDTO;
import com.matheus.voting.entity.Pauta;
import com.matheus.voting.entity.Pauta.StatusPauta;
import com.matheus.voting.entity.Voto;
import com.matheus.voting.repository.PautaRepository;
import com.matheus.voting.repository.VotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PautaService {

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private VotoRepository votoRepository;

    @Transactional
    public PautaDTO criar(PautaDTO dto) {
        Pauta pauta = new Pauta();
        pauta.setNome(dto.nome());
        pauta.setDescricao(dto.descricao());
        pauta.setTempoAbertoPorMinutos(dto.tempoAbertoPorMinutos());
        pauta.setStatus(StatusPauta.FECHADA);

        Pauta pautaSalva = pautaRepository.save(pauta);
        return converterParaDTO(pautaSalva);
    }

    @Transactional(readOnly = true)
    public PautaDTO obterPorId(Long id) {
        Pauta pauta = pautaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada com ID: " + id));
        return converterParaDTO(pauta);
    }

    @Transactional(readOnly = true)
    public List<PautaDTO> listar() {
        return pautaRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PautaInfoDTO> listarComInformacoes() {
        return pautaRepository.findAll().stream()
                .map(this::converterParaInfoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PautaDTO atualizar(Long id, PautaDTO dto) {
        Pauta pauta = pautaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada com ID: " + id));

        pauta.setNome(dto.nome());
        pauta.setDescricao(dto.descricao());
        pauta.setTempoAbertoPorMinutos(dto.tempoAbertoPorMinutos());

        Pauta pautaAtualizada = pautaRepository.save(pauta);
        return converterParaDTO(pautaAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        if (!pautaRepository.existsById(id)) {
            throw new RuntimeException("Pauta não encontrada com ID: " + id);
        }
        pautaRepository.deleteById(id);
    }

    @Transactional
    public PautaInfoDTO abrir(Long id, AbrirPautaDTO dto) {
        Pauta pauta = pautaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada com ID: " + id));

        StatusPauta statusAtual = resolverStatusAtual(pauta);
        if (statusAtual == StatusPauta.ABERTA) {
            throw new RuntimeException("A pauta já está aberta para votação");
        }

        if (statusAtual == StatusPauta.ENCERRADA || pauta.getDataEncerramento() != null) {
            pauta.setStatus(StatusPauta.ENCERRADA);
            pautaRepository.save(pauta);
            throw new RuntimeException("Tempo da votação encerrado");
        }

        pauta.setTempoAbertoPorMinutos(dto.tempoAbertoPorMinutos());
        pauta.setDataEncerramento(LocalDateTime.now().plusMinutes(dto.tempoAbertoPorMinutos()));
        pauta.setStatus(StatusPauta.ABERTA);

        Pauta pautaAtualizada = pautaRepository.save(pauta);
        return converterParaInfoDTO(pautaAtualizada);
    }

    @Transactional
    public boolean estaAberta(Long id) {
        Pauta pauta = pautaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada com ID: " + id));
        StatusPauta statusAtual = resolverStatusAtual(pauta);
        if (statusAtual == StatusPauta.ENCERRADA && pauta.getStatus() != StatusPauta.ENCERRADA) {
            pauta.setStatus(StatusPauta.ENCERRADA);
            pautaRepository.save(pauta);
        }
        return statusAtual == StatusPauta.ABERTA;
    }

    private PautaDTO converterParaDTO(Pauta pauta) {
        return new PautaDTO(pauta.getId(), pauta.getNome(), pauta.getDescricao(), pauta.getTempoAbertoPorMinutos());
    }

    private PautaInfoDTO converterParaInfoDTO(Pauta pauta) {
        long votosSim = votoRepository.countByPautaIdAndVoto(pauta.getId(), Voto.VotoEnum.SIM);
        long votosNao = votoRepository.countByPautaIdAndVoto(pauta.getId(), Voto.VotoEnum.NAO);
        long totalVotos = votoRepository.countByPautaId(pauta.getId());
        StatusPauta statusAtual = resolverStatusAtual(pauta);

        return new PautaInfoDTO(
                pauta.getId(),
                pauta.getNome(),
                pauta.getDescricao(),
                pauta.getTempoAbertoPorMinutos(),
                pauta.getDataCriacao(),
                pauta.getDataEncerramento(),
                statusAtual.name(),
                statusAtual == StatusPauta.ABERTA,
                totalVotos,
                votosSim,
                votosNao
        );
    }

    private StatusPauta resolverStatusAtual(Pauta pauta) {
        StatusPauta status = pauta.getStatus() == null ? StatusPauta.FECHADA : pauta.getStatus();

        if (status == StatusPauta.ABERTA
                && pauta.getDataEncerramento() != null
                && !LocalDateTime.now().isBefore(pauta.getDataEncerramento())) {
            return StatusPauta.ENCERRADA;
        }

        if (status == StatusPauta.FECHADA && pauta.getDataEncerramento() != null) {
            return StatusPauta.ENCERRADA;
        }

        return status;
    }
}
