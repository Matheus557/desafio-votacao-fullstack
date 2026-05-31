package com.matheus.voting.service;

import com.matheus.voting.dto.VotoDTO;
import com.matheus.voting.entity.Pauta;
import com.matheus.voting.entity.Voto;
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

    @Transactional
    public VotoDTO criar(VotoDTO dto) {
        String cpf = normalizarCpf(dto.cpf());

        if (!pautaService.estaAberta(dto.pautaId())) {
            throw new RuntimeException("A pauta não está mais aberta para votação");
        }

        if (votoRepository.findByCpfAndPautaId(cpf, dto.pautaId()).isPresent()) {
            throw new RuntimeException("CPF já votou nesta pauta");
        }

        Pauta pauta = pautaRepository.findById(dto.pautaId())
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada com ID: " + dto.pautaId()));

        Voto voto = new Voto();
        voto.setCpf(cpf);
        voto.setPauta(pauta);
        voto.setVoto(converterVoto(dto.voto()));

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
                voto.getCpf(),
                voto.getPauta().getId(),
                voto.getVoto().name()
        );
    }

    private String normalizarCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    private Voto.VotoEnum converterVoto(String voto) {
        if ("sim".equalsIgnoreCase(voto.trim())) {
            return Voto.VotoEnum.SIM;
        }

        String votoNormalizado = voto.trim().toLowerCase();
        if ("nao".equals(votoNormalizado) || "não".equals(votoNormalizado)) {
            return Voto.VotoEnum.NAO;
        }

        throw new IllegalArgumentException("Voto deve ser 'Sim' ou 'Não'");
    }
}
