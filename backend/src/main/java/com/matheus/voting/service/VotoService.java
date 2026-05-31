package com.matheus.voting.service;

import com.matheus.voting.client.CpfValidationClient;
import com.matheus.voting.client.CpfValidationStatus;
import com.matheus.voting.dto.VotoDTO;
import com.matheus.voting.dto.VotoRequestDTO;
import com.matheus.voting.entity.Pauta;
import com.matheus.voting.entity.Voto;
import com.matheus.voting.exception.AssociateUnableToVoteException;
import com.matheus.voting.repository.PautaRepository;
import com.matheus.voting.repository.VotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VotoService {

    @Autowired
    private VotoRepository votoRepository;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private PautaService pautaService;

    @Autowired
    private CpfValidationClient cpfValidationClient;

    @Transactional
    public VotoDTO criar(VotoDTO dto) {
        return criar(dto.agendaId(), new VotoRequestDTO(dto.associateId(), dto.cpf(), dto.vote()));
    }

    @Transactional
    public VotoDTO criar(Long agendaId, VotoRequestDTO dto) {
        Pauta pauta = pautaRepository.findById(agendaId)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada com ID: " + agendaId));

        if (!pautaService.estaAberta(agendaId)) {
            throw new RuntimeException("A pauta não está mais aberta para votação");
        }

        if (votoRepository.findByAssociateIdAndPautaId(dto.associateId(), agendaId).isPresent()) {
            throw new RuntimeException("Associado já votou nesta pauta");
        }

        String cpf = normalizarCpf(dto.cpf());
        CpfValidationStatus cpfStatus = cpfValidationClient.validate(cpf);
        if (cpfStatus == CpfValidationStatus.UNABLE_TO_VOTE) {
            throw new AssociateUnableToVoteException("Associado não está habilitado para votar");
        }

        Voto voto = new Voto();
        voto.setAssociateId(dto.associateId());
        voto.setCpf(cpf);
        voto.setPauta(pauta);
        voto.setVoto(converterVoto(dto.vote()));

        Voto votoSalvo = votoRepository.save(voto);
        return converterParaDTO(votoSalvo);
    }

    @Transactional(readOnly = true)
    public VotoDTO obterPorId(Long id) {
        Voto voto = votoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voto não encontrado com ID: " + id));
        return converterParaDTO(voto);
    }

    @Transactional(readOnly = true)
    public List<VotoDTO> listarPorPauta(Long pautaId) {
        return votoRepository.findByPautaId(pautaId).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VotoDTO> listar() {
        return votoRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletar(Long id) {
        if (!votoRepository.existsById(id)) {
            throw new RuntimeException("Voto não encontrado com ID: " + id);
        }
        votoRepository.deleteById(id);
    }

    private VotoDTO converterParaDTO(Voto voto) {
        return new VotoDTO(
                voto.getId(),
                voto.getAssociateId(),
                voto.getCpf(),
                voto.getPauta().getId(),
                voto.getVoto().name()
        );
    }

    private String normalizarCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    private Voto.VotoEnum converterVoto(String voto) {
        if ("yes".equalsIgnoreCase(voto.trim()) || "sim".equalsIgnoreCase(voto.trim())) {
            return Voto.VotoEnum.YES;
        }

        if ("no".equalsIgnoreCase(voto.trim())) {
            return Voto.VotoEnum.NO;
        }

        if ("sim".equalsIgnoreCase(voto.trim())) {
            return Voto.VotoEnum.YES;
        }

        String votoNormalizado = voto.trim().toLowerCase();
        if ("nao".equals(votoNormalizado) || "não".equals(votoNormalizado)) {
            return Voto.VotoEnum.NO;
        }

        throw new IllegalArgumentException("Voto deve ser 'YES' ou 'NO'");
    }
}
