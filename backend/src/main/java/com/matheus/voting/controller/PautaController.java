package com.matheus.voting.controller;

import com.matheus.voting.dto.AbrirPautaDTO;
import com.matheus.voting.dto.PautaDTO;
import com.matheus.voting.dto.PautaInfoDTO;
import com.matheus.voting.dto.VotoDTO;
import com.matheus.voting.dto.VotoRequestDTO;
import com.matheus.voting.service.PautaService;
import com.matheus.voting.service.VotoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pautas")
public class PautaController {

    @Autowired
    private PautaService pautaService;

    @Autowired
    private VotoService votoService;

    @PostMapping
    public ResponseEntity<PautaDTO> criar(@Valid @RequestBody PautaDTO dto) {
        return cadastrarPauta(dto);
    }

    @PostMapping("/cadastro")
    public ResponseEntity<PautaDTO> cadastrar(@Valid @RequestBody PautaDTO dto) {
        return cadastrarPauta(dto);
    }

    private ResponseEntity<PautaDTO> cadastrarPauta(PautaDTO dto) {
        PautaDTO pautaDTO = pautaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pautaDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PautaDTO> obterPorId(@PathVariable Long id) {
        PautaDTO pautaDTO = pautaService.obterPorId(id);
        return ResponseEntity.ok(pautaDTO);
    }

    @GetMapping
    public ResponseEntity<List<PautaDTO>> listar() {
        List<PautaDTO> pautas = pautaService.listar();
        return ResponseEntity.ok(pautas);
    }

    @GetMapping("/informacoes")
    public ResponseEntity<List<PautaInfoDTO>> listarComInformacoes() {
        List<PautaInfoDTO> pautas = pautaService.listarComInformacoes();
        return ResponseEntity.ok(pautas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PautaDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PautaDTO dto) {
        PautaDTO pautaDTO = pautaService.atualizar(id, dto);
        return ResponseEntity.ok(pautaDTO);
    }

    @PatchMapping("/{id}/abrir")
    public ResponseEntity<PautaInfoDTO> abrir(@PathVariable Long id, @Valid @RequestBody AbrirPautaDTO dto) {
        PautaInfoDTO pautaDTO = pautaService.abrir(id, dto);
        return ResponseEntity.ok(pautaDTO);
    }

    @PostMapping("/{agendaId}/votos")
    public ResponseEntity<VotoDTO> receberVoto(
            @PathVariable Long agendaId,
            @Valid @RequestBody VotoRequestDTO dto
    ) {
        VotoDTO votoDTO = votoService.criar(agendaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(votoDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pautaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/aberta")
    public ResponseEntity<Boolean> verificarSeEstaAberta(@PathVariable Long id) {
        boolean estaAberta = pautaService.estaAberta(id);
        return ResponseEntity.ok(estaAberta);
    }
}
