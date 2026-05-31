package com.matheus.voting.controller;

import com.matheus.voting.dto.VotoDTO;
import com.matheus.voting.service.VotoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votos")
public class VotoController {

    @Autowired
    private VotoService votoService;

    @PostMapping
    public ResponseEntity<VotoDTO> criar(@Valid @RequestBody VotoDTO dto) {
        return receberVoto(dto);
    }

    @PostMapping("/receber")
    public ResponseEntity<VotoDTO> receber(@Valid @RequestBody VotoDTO dto) {
        return receberVoto(dto);
    }

    private ResponseEntity<VotoDTO> receberVoto(VotoDTO dto) {
        VotoDTO votoDTO = votoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(votoDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VotoDTO> obterPorId(@PathVariable Long id) {
        VotoDTO votoDTO = votoService.obterPorId(id);
        return ResponseEntity.ok(votoDTO);
    }

    @GetMapping
    public ResponseEntity<List<VotoDTO>> listar() {
        List<VotoDTO> votos = votoService.listar();
        return ResponseEntity.ok(votos);
    }

    @GetMapping("/pauta/{pautaId}")
    public ResponseEntity<List<VotoDTO>> listarPorPauta(@PathVariable Long pautaId) {
        List<VotoDTO> votos = votoService.listarPorPauta(pautaId);
        return ResponseEntity.ok(votos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        votoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
