package com.matheus.voting.controller;

import com.matheus.voting.dto.AbrirPautaDTO;
import com.matheus.voting.dto.PautaDTO;
import com.matheus.voting.dto.PautaInfoDTO;
import com.matheus.voting.dto.VotoDTO;
import com.matheus.voting.dto.VotoRequestDTO;
import com.matheus.voting.service.PautaService;
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
@RequestMapping("/api/pautas")
@Tag(name = "Pautas", description = "Cadastro, abertura e consulta das pautas.")
public class PautaController {

    @Autowired
    private PautaService pautaService;

    @Autowired
    private VotoService votoService;

    @PostMapping
    @Operation(summary = "Cria uma pauta", description = "Cadastra uma pauta fechada, ainda sem votação aberta.")
    public ResponseEntity<PautaDTO> criar(@Valid @RequestBody PautaDTO dto) {
        return cadastrarPauta(dto);
    }

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastra uma pauta", description = "Cadastra uma nova pauta com título, descrição e tempo padrão.")
    public ResponseEntity<PautaDTO> cadastrar(@Valid @RequestBody PautaDTO dto) {
        return cadastrarPauta(dto);
    }

    private ResponseEntity<PautaDTO> cadastrarPauta(PautaDTO dto) {
        PautaDTO pautaDTO = pautaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pautaDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma pauta", description = "Retorna os dados básicos de uma pauta pelo ID.")
    public ResponseEntity<PautaDTO> obterPorId(@PathVariable Long id) {
        PautaDTO pautaDTO = pautaService.obterPorId(id);
        return ResponseEntity.ok(pautaDTO);
    }

    @GetMapping
    @Operation(summary = "Lista pautas", description = "Retorna todas as pautas cadastradas.")
    public ResponseEntity<List<PautaDTO>> listar() {
        List<PautaDTO> pautas = pautaService.listar();
        return ResponseEntity.ok(pautas);
    }

    @GetMapping("/informacoes")
    @Operation(summary = "Lista pautas com votos", description = "Mostra status, prazo e total de votos de cada pauta.")
    public ResponseEntity<List<PautaInfoDTO>> listarComInformacoes() {
        List<PautaInfoDTO> pautas = pautaService.listarComInformacoes();
        return ResponseEntity.ok(pautas);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma pauta", description = "Altera título, descrição e tempo padrão da pauta.")
    public ResponseEntity<PautaDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PautaDTO dto) {
        PautaDTO pautaDTO = pautaService.atualizar(id, dto);
        return ResponseEntity.ok(pautaDTO);
    }

    @PatchMapping("/{id}/abrir")
    @Operation(summary = "Abre uma pauta", description = "Inicia a votação e começa a contar o tempo informado.")
    public ResponseEntity<PautaInfoDTO> abrir(@PathVariable Long id, @Valid @RequestBody AbrirPautaDTO dto) {
        PautaInfoDTO pautaDTO = pautaService.abrir(id, dto);
        return ResponseEntity.ok(pautaDTO);
    }

    @PostMapping("/{agendaId}/votos")
    @Operation(summary = "Registra um voto", description = "Recebe o voto de um associado em uma pauta aberta.")
    public ResponseEntity<VotoDTO> receberVoto(
            @PathVariable Long agendaId,
            @Valid @RequestBody VotoRequestDTO dto
    ) {
        VotoDTO votoDTO = votoService.criar(agendaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(votoDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma pauta", description = "Apaga uma pauta pelo ID.")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pautaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/aberta")
    @Operation(summary = "Verifica se está aberta", description = "Informa se a pauta ainda aceita votos.")
    public ResponseEntity<Boolean> verificarSeEstaAberta(@PathVariable Long id) {
        boolean estaAberta = pautaService.estaAberta(id);
        return ResponseEntity.ok(estaAberta);
    }
}
