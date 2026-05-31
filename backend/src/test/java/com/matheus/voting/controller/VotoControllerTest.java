package com.matheus.voting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.voting.dto.VotoDTO;
import com.matheus.voting.service.VotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VotoController.class)
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VotoService votoService;

    @Test
    void deveReceberVotoComCpf() throws Exception {
        VotoDTO request = new VotoDTO(null, "12345678900", 2L, "Sim");
        VotoDTO response = new VotoDTO(3L, request.cpf(), request.pautaId(), "SIM");

        when(votoService.criar(any(VotoDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/votos/receber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.pautaId").value(2L))
                .andExpect(jsonPath("$.voto").value("SIM"));

        verify(votoService).criar(any(VotoDTO.class));
    }

    @Test
    void deveRetornarBadRequestQuandoVotoEstiverInvalido() throws Exception {
        VotoDTO request = new VotoDTO(null, "", null, "");

        mockMvc.perform(post("/api/votos/receber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(votoService);
    }
}
