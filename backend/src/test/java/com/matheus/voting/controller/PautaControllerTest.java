package com.matheus.voting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.voting.dto.AbrirPautaDTO;
import com.matheus.voting.dto.PautaDTO;
import com.matheus.voting.dto.PautaInfoDTO;
import com.matheus.voting.dto.VotoDTO;
import com.matheus.voting.dto.VotoRequestDTO;
import com.matheus.voting.exception.AssociateUnableToVoteException;
import com.matheus.voting.exception.CpfNotFoundException;
import com.matheus.voting.service.PautaService;
import com.matheus.voting.service.VotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PautaController.class)
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PautaService pautaService;

    @MockitoBean
    private VotoService votoService;

    @Test
    void deveCadastrarPauta() throws Exception {
        PautaDTO request = new PautaDTO(null, "Pauta de orcamento", "Descricao da pauta", 30);
        PautaDTO response = new PautaDTO(1L, request.nome(), request.descricao(), request.tempoAbertoPorMinutos());

        when(pautaService.criar(any(PautaDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/pautas/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Pauta de orcamento"))
                .andExpect(jsonPath("$.descricao").value("Descricao da pauta"))
                .andExpect(jsonPath("$.tempoAbertoPorMinutos").value(30));

        verify(pautaService).criar(any(PautaDTO.class));
    }

    @Test
    void deveRetornarBadRequestQuandoCadastroEstiverInvalido() throws Exception {
        PautaDTO request = new PautaDTO(null, "", "", 0);

        mockMvc.perform(post("/api/pautas/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pautaService);
    }

    @Test
    void deveListarPautasComInformacoes() throws Exception {
        LocalDateTime dataCriacao = LocalDateTime.of(2026, 5, 30, 20, 0);
        LocalDateTime dataEncerramento = LocalDateTime.of(2030, 5, 30, 20, 30);
        PautaInfoDTO pauta = new PautaInfoDTO(
                1L,
                "Pauta de orcamento",
                "Descricao da pauta",
                30,
                dataCriacao,
                dataEncerramento,
                "ABERTA",
                true,
                5,
                3,
                2
        );

        when(pautaService.listarComInformacoes()).thenReturn(List.of(pauta));

        mockMvc.perform(get("/api/pautas/informacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("Pauta de orcamento"))
                .andExpect(jsonPath("$[0].descricao").value("Descricao da pauta"))
                .andExpect(jsonPath("$[0].tempoAbertoPorMinutos").value(30))
                .andExpect(jsonPath("$[0].dataCriacao").value("2026-05-30T20:00:00"))
                .andExpect(jsonPath("$[0].dataEncerramento").value("2030-05-30T20:30:00"))
                .andExpect(jsonPath("$[0].status").value("ABERTA"))
                .andExpect(jsonPath("$[0].aberta").value(true))
                .andExpect(jsonPath("$[0].totalVotos").value(5))
                .andExpect(jsonPath("$[0].votosSim").value(3))
                .andExpect(jsonPath("$[0].votosNao").value(2));

        verify(pautaService).listarComInformacoes();
    }

    @Test
    void deveAbrirPautaParaVotacao() throws Exception {
        AbrirPautaDTO request = new AbrirPautaDTO(15);
        PautaInfoDTO response = new PautaInfoDTO(
                1L,
                "Pauta de orcamento",
                "Descricao da pauta",
                15,
                LocalDateTime.of(2026, 5, 30, 20, 0),
                LocalDateTime.of(2030, 5, 30, 20, 15),
                "ABERTA",
                true,
                0,
                0,
                0
        );

        when(pautaService.abrir(any(Long.class), any(AbrirPautaDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/api/pautas/1/abrir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.aberta").value(true))
                .andExpect(jsonPath("$.tempoAbertoPorMinutos").value(15));

        verify(pautaService).abrir(any(Long.class), any(AbrirPautaDTO.class));
    }

    @Test
    void deveReceberVotoDaPautaComAssociateId() throws Exception {
        VotoRequestDTO request = new VotoRequestDTO(10L, "12345678901", "YES");
        VotoDTO response = new VotoDTO(3L, request.associateId(), request.cpf(), 1L, "YES");

        when(votoService.criar(any(Long.class), any(VotoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.associateId").value(10L))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.agendaId").value(1L))
                .andExpect(jsonPath("$.vote").value("YES"));

        verify(votoService).criar(any(Long.class), any(VotoRequestDTO.class));
    }

    @Test
    void deveRetornarNotFoundQuandoCpfForInvalidoNoClientFake() throws Exception {
        VotoRequestDTO request = new VotoRequestDTO(10L, "12345678901", "YES");

        when(votoService.criar(any(Long.class), any(VotoRequestDTO.class)))
                .thenThrow(new CpfNotFoundException("CPF inválido"));

        mockMvc.perform(post("/api/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(votoService).criar(any(Long.class), any(VotoRequestDTO.class));
    }

    @Test
    void deveRetornarForbiddenQuandoAssociadoNaoEstiverHabilitado() throws Exception {
        VotoRequestDTO request = new VotoRequestDTO(10L, "12345678901", "YES");

        when(votoService.criar(any(Long.class), any(VotoRequestDTO.class)))
                .thenThrow(new AssociateUnableToVoteException("Associado não está habilitado para votar"));

        mockMvc.perform(post("/api/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(votoService).criar(any(Long.class), any(VotoRequestDTO.class));
    }
}
