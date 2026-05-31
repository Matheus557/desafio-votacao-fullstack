package com.matheus.voting.service;

import com.matheus.voting.dto.VotoDTO;
import com.matheus.voting.entity.Pauta;
import com.matheus.voting.entity.Voto;
import com.matheus.voting.repository.PautaRepository;
import com.matheus.voting.repository.VotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private PautaService pautaService;

    @InjectMocks
    private VotoService votoService;

    @Test
    void deveReceberVotoSimComCpf() {
        VotoDTO request = new VotoDTO(null, "123.456.789-00", 2L, "Sim");
        Pauta pauta = criarPauta(request.pautaId());

        when(pautaService.estaAberta(request.pautaId())).thenReturn(true);
        when(votoRepository.findByCpfAndPautaId("12345678900", request.pautaId())).thenReturn(Optional.empty());
        when(pautaRepository.findById(request.pautaId())).thenReturn(Optional.of(pauta));
        when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> {
            Voto voto = invocation.getArgument(0);
            voto.setId(3L);
            return voto;
        });

        VotoDTO response = votoService.criar(request);

        assertAll(
                () -> assertEquals(3L, response.id()),
                () -> assertEquals("12345678900", response.cpf()),
                () -> assertEquals(request.pautaId(), response.pautaId()),
                () -> assertEquals("SIM", response.voto())
        );

        ArgumentCaptor<Voto> votoCaptor = ArgumentCaptor.forClass(Voto.class);
        verify(votoRepository).save(votoCaptor.capture());
        assertAll(
                () -> assertEquals("12345678900", votoCaptor.getValue().getCpf()),
                () -> assertEquals(Voto.VotoEnum.SIM, votoCaptor.getValue().getVoto())
        );
    }

    @Test
    void deveReceberVotoNaoComAcento() {
        VotoDTO request = new VotoDTO(null, "12345678900", 2L, "Não");
        Pauta pauta = criarPauta(request.pautaId());

        when(pautaService.estaAberta(request.pautaId())).thenReturn(true);
        when(votoRepository.findByCpfAndPautaId(request.cpf(), request.pautaId())).thenReturn(Optional.empty());
        when(pautaRepository.findById(request.pautaId())).thenReturn(Optional.of(pauta));
        when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> {
            Voto voto = invocation.getArgument(0);
            voto.setId(3L);
            return voto;
        });

        VotoDTO response = votoService.criar(request);

        assertEquals("NAO", response.voto());
    }

    @Test
    void deveBloquearCpfQueJaVotouNaPauta() {
        VotoDTO request = new VotoDTO(null, "12345678900", 2L, "Sim");

        when(pautaService.estaAberta(request.pautaId())).thenReturn(true);
        when(votoRepository.findByCpfAndPautaId(request.cpf(), request.pautaId()))
                .thenReturn(Optional.of(new Voto()));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> votoService.criar(request)
        );

        assertEquals("CPF já votou nesta pauta", exception.getMessage());
        verify(pautaRepository, never()).findById(request.pautaId());
        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveBloquearVotoQuandoPautaEstiverFechada() {
        VotoDTO request = new VotoDTO(null, "12345678900", 2L, "Sim");

        when(pautaService.estaAberta(request.pautaId())).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> votoService.criar(request)
        );

        assertEquals("A pauta não está mais aberta para votação", exception.getMessage());
        verify(votoRepository, never()).findByCpfAndPautaId(request.cpf(), request.pautaId());
        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveBloquearVotoDiferenteDeSimOuNao() {
        VotoDTO request = new VotoDTO(null, "12345678900", 2L, "Talvez");
        Pauta pauta = criarPauta(request.pautaId());

        when(pautaService.estaAberta(request.pautaId())).thenReturn(true);
        when(votoRepository.findByCpfAndPautaId(request.cpf(), request.pautaId())).thenReturn(Optional.empty());
        when(pautaRepository.findById(request.pautaId())).thenReturn(Optional.of(pauta));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> votoService.criar(request)
        );

        assertEquals("Voto deve ser 'Sim' ou 'Não'", exception.getMessage());
        verify(votoRepository, never()).save(any(Voto.class));
    }

    private Pauta criarPauta(Long id) {
        Pauta pauta = new Pauta();
        pauta.setId(id);
        return pauta;
    }
}
