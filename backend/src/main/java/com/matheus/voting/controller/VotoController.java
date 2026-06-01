package com.matheus.voting.controller;

import com.matheus.voting.dto.VotoDTO;
import com.matheus.voting.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votos")
@Tag(name = "Votos", description = "Consulta e endpoints legados de votos.")
public class VotoController {

    @Autowired
    private VotoService votoService;

    @PostMapping
    @Operation(summary = "Registra voto legado", description = "Recebe voto com agendaId no corpo da requisição.")
    public ResponseEntity<VotoDTO> criar(@Valid @RequestBody VotoDTO dto) {
        return receberVoto(dto);
    }

    @PostMapping("/receber")
    @Operation(summary = "Recebe voto legado", description = "Atalho antigo para registrar voto com agendaId no corpo.")
    public ResponseEntity<VotoDTO> receber(@Valid @RequestBody VotoDTO dto) {
        return receberVoto(dto);
    }

    private ResponseEntity<VotoDTO> receberVoto(VotoDTO dto) {
        VotoDTO votoDTO = votoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(votoDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um voto", description = "Retorna um voto pelo ID.")
    public ResponseEntity<VotoDTO> obterPorId(@PathVariable Long id) {
        VotoDTO votoDTO = votoService.obterPorId(id);
        return ResponseEntity.ok(votoDTO);
    }

    @GetMapping
    @Operation(summary = "Lista votos", description = "Retorna todos os votos registrados.")
    public ResponseEntity<List<VotoDTO>> listar() {
        List<VotoDTO> votos = votoService.listar();
        return ResponseEntity.ok(votos);
    }

    @GetMapping("/pauta/{pautaId}")
    @Operation(summary = "Lista votos da pauta", description = "Retorna os votos registrados em uma pauta.")
    public ResponseEntity<List<VotoDTO>> listarPorPauta(@PathVariable Long pautaId) {
        List<VotoDTO> votos = votoService.listarPorPauta(pautaId);
        return ResponseEntity.ok(votos);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um voto", description = "Apaga um voto pelo ID.")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        votoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
