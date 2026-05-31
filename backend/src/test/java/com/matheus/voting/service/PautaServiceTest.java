package com.matheus.voting.service;

import com.matheus.voting.dto.PautaDTO;
import com.matheus.voting.dto.PautaInfoDTO;
import com.matheus.voting.dto.AbrirPautaDTO;
import com.matheus.voting.entity.Pauta;
import com.matheus.voting.entity.Pauta.StatusPauta;
import com.matheus.voting.entity.Voto;
import com.matheus.voting.repository.PautaRepository;
import com.matheus.voting.repository.VotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private PautaService pautaService;

    @Test
    void deveCadastrarNovaPauta() {
        PautaDTO request = new PautaDTO(null, "Pauta de orcamento", "Descricao da pauta", 30);

        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> {
            Pauta pauta = invocation.getArgument(0);
            pauta.setId(1L);
            return pauta;
        });

        PautaDTO response = pautaService.criar(request);

        assertAll(
                () -> assertEquals(1L, response.id()),
                () -> assertEquals(request.nome(), response.nome()),
                () -> assertEquals(request.descricao(), response.descricao()),
                () -> assertEquals(request.tempoAbertoPorMinutos(), response.tempoAbertoPorMinutos())
        );

        ArgumentCaptor<Pauta> pautaCaptor = ArgumentCaptor.forClass(Pauta.class);
        verify(pautaRepository).save(pautaCaptor.capture());

        Pauta pautaSalva = pautaCaptor.getValue();
        assertAll(
                () -> assertEquals(request.nome(), pautaSalva.getNome()),
                () -> assertEquals(request.descricao(), pautaSalva.getDescricao()),
                () -> assertEquals(request.tempoAbertoPorMinutos(), pautaSalva.getTempoAbertoPorMinutos()),
                () -> assertEquals(StatusPauta.FECHADA, pautaSalva.getStatus())
        );
        verifyNoInteractions(votoRepository);
    }

    @Test
    void deveListarPautasComInformacoes() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setNome("Pauta de orcamento");
        pauta.setDescricao("Descricao da pauta");
        pauta.setTempoAbertoPorMinutos(30);
        pauta.setDataCriacao(LocalDateTime.of(2026, 5, 30, 20, 0));
        pauta.setDataEncerramento(LocalDateTime.of(2030, 5, 30, 20, 30));
        pauta.setStatus(StatusPauta.ABERTA);

        when(pautaRepository.findAll()).thenReturn(List.of(pauta));
        when(votoRepository.countByPautaId(1L)).thenReturn(5L);
        when(votoRepository.countByPautaIdAndVoto(1L, Voto.VotoEnum.SIM)).thenReturn(3L);
        when(votoRepository.countByPautaIdAndVoto(1L, Voto.VotoEnum.NAO)).thenReturn(2L);

        List<PautaInfoDTO> response = pautaService.listarComInformacoes();

        PautaInfoDTO pautaInfo = response.get(0);
        assertAll(
                () -> assertEquals(1, response.size()),
                () -> assertEquals(1L, pautaInfo.id()),
                () -> assertEquals("Pauta de orcamento", pautaInfo.nome()),
                () -> assertEquals("Descricao da pauta", pautaInfo.descricao()),
                () -> assertEquals(30, pautaInfo.tempoAbertoPorMinutos()),
                () -> assertEquals(pauta.getDataCriacao(), pautaInfo.dataCriacao()),
                () -> assertEquals(pauta.getDataEncerramento(), pautaInfo.dataEncerramento()),
                () -> assertEquals("ABERTA", pautaInfo.status()),
                () -> assertTrue(pautaInfo.aberta()),
                () -> assertEquals(5L, pautaInfo.totalVotos()),
                () -> assertEquals(3L, pautaInfo.votosSim()),
                () -> assertEquals(2L, pautaInfo.votosNao())
        );

        verify(votoRepository).countByPautaId(1L);
        verify(votoRepository).countByPautaIdAndVoto(1L, Voto.VotoEnum.SIM);
        verify(votoRepository).countByPautaIdAndVoto(1L, Voto.VotoEnum.NAO);
    }

    @Test
    void deveAbrirPautaParaVotacao() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setNome("Pauta de orcamento");
        pauta.setDescricao("Descricao da pauta");
        pauta.setTempoAbertoPorMinutos(30);
        pauta.setDataCriacao(LocalDateTime.of(2026, 5, 30, 20, 0));
        pauta.setStatus(StatusPauta.FECHADA);

        when(pautaRepository.findById(1L)).thenReturn(java.util.Optional.of(pauta));
        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PautaInfoDTO response = pautaService.abrir(1L, new AbrirPautaDTO(15));

        assertAll(
                () -> assertEquals(1L, response.id()),
                () -> assertEquals(15, response.tempoAbertoPorMinutos()),
                () -> assertEquals("ABERTA", response.status()),
                () -> assertTrue(response.aberta()),
                () -> assertEquals(0L, response.totalVotos())
        );

        ArgumentCaptor<Pauta> pautaCaptor = ArgumentCaptor.forClass(Pauta.class);
        verify(pautaRepository).save(pautaCaptor.capture());
        assertAll(
                () -> assertEquals(15, pautaCaptor.getValue().getTempoAbertoPorMinutos()),
                () -> assertEquals(StatusPauta.ABERTA, pautaCaptor.getValue().getStatus()),
                () -> assertTrue(pautaCaptor.getValue().getDataEncerramento().isAfter(LocalDateTime.now().minusMinutes(1)))
        );
    }

    @Test
    void deveBloquearReaberturaDePautaEncerrada() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setNome("Pauta de orcamento");
        pauta.setDescricao("Descricao da pauta");
        pauta.setTempoAbertoPorMinutos(30);
        pauta.setDataCriacao(LocalDateTime.of(2026, 5, 30, 20, 0));
        pauta.setDataEncerramento(LocalDateTime.now().minusMinutes(1));
        pauta.setStatus(StatusPauta.ABERTA);

        when(pautaRepository.findById(1L)).thenReturn(java.util.Optional.of(pauta));
        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> pautaService.abrir(1L, new AbrirPautaDTO(10))
        );

        assertEquals("Tempo da votação encerrado", exception.getMessage());
        ArgumentCaptor<Pauta> pautaCaptor = ArgumentCaptor.forClass(Pauta.class);
        verify(pautaRepository).save(pautaCaptor.capture());
        assertEquals(StatusPauta.ENCERRADA, pautaCaptor.getValue().getStatus());
    }

    @Test
    void deveRetornarFechadaQuandoPautaAindaNaoFoiAberta() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setNome("Pauta de orcamento");
        pauta.setDescricao("Descricao da pauta");
        pauta.setTempoAbertoPorMinutos(30);
        pauta.setDataCriacao(LocalDateTime.of(2026, 5, 30, 20, 0));
        pauta.setStatus(StatusPauta.FECHADA);

        when(pautaRepository.findAll()).thenReturn(List.of(pauta));

        PautaInfoDTO pautaInfo = pautaService.listarComInformacoes().get(0);

        assertAll(
                () -> assertEquals("FECHADA", pautaInfo.status()),
                () -> assertFalse(pautaInfo.aberta())
        );
    }
}
