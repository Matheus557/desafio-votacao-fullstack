package com.matheus.voting.service;

import com.matheus.voting.client.CpfValidationClient;
import com.matheus.voting.client.CpfValidationStatus;
import com.matheus.voting.dto.VotoDTO;
import com.matheus.voting.dto.VotoRequestDTO;
import com.matheus.voting.entity.Pauta;
import com.matheus.voting.entity.Voto;
import com.matheus.voting.exception.AssociateUnableToVoteException;
import com.matheus.voting.exception.CpfNotFoundException;
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

    @Mock
    private CpfValidationClient cpfValidationClient;

    @InjectMocks
    private VotoService votoService;

    @Test
    void deveReceberVotoYesComAssociateId() {
        VotoRequestDTO request = new VotoRequestDTO(10L, "123.456.789-00", "YES");
        Pauta pauta = criarPauta(2L);

        when(pautaRepository.findById(2L)).thenReturn(Optional.of(pauta));
        when(pautaService.estaAberta(2L)).thenReturn(true);
        when(votoRepository.findByAssociateIdAndPautaId(request.associateId(), 2L)).thenReturn(Optional.empty());
        when(cpfValidationClient.validate("12345678900")).thenReturn(CpfValidationStatus.ABLE_TO_VOTE);
        when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> {
            Voto voto = invocation.getArgument(0);
            voto.setId(3L);
            return voto;
        });

        VotoDTO response = votoService.criar(2L, request);

        assertAll(
                () -> assertEquals(3L, response.id()),
                () -> assertEquals(10L, response.associateId()),
                () -> assertEquals("12345678900", response.cpf()),
                () -> assertEquals(2L, response.agendaId()),
                () -> assertEquals("YES", response.vote())
        );

        ArgumentCaptor<Voto> votoCaptor = ArgumentCaptor.forClass(Voto.class);
        verify(votoRepository).save(votoCaptor.capture());
        assertAll(
                () -> assertEquals(10L, votoCaptor.getValue().getAssociateId()),
                () -> assertEquals("12345678900", votoCaptor.getValue().getCpf()),
                () -> assertEquals(Voto.VotoEnum.YES, votoCaptor.getValue().getVoto())
        );
    }

    @Test
    void deveReceberVotoNo() {
        VotoRequestDTO request = new VotoRequestDTO(10L, "12345678900", "NO");
        Pauta pauta = criarPauta(2L);

        when(pautaRepository.findById(2L)).thenReturn(Optional.of(pauta));
        when(pautaService.estaAberta(2L)).thenReturn(true);
        when(votoRepository.findByAssociateIdAndPautaId(request.associateId(), 2L)).thenReturn(Optional.empty());
        when(cpfValidationClient.validate(request.cpf())).thenReturn(CpfValidationStatus.ABLE_TO_VOTE);
        when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> {
            Voto voto = invocation.getArgument(0);
            voto.setId(3L);
            return voto;
        });

        VotoDTO response = votoService.criar(2L, request);

        assertEquals("NO", response.vote());
    }

    @Test
    void deveBloquearAssociadoQueJaVotouNaPauta() {
        VotoRequestDTO request = new VotoRequestDTO(10L, "12345678900", "YES");

        when(pautaRepository.findById(2L)).thenReturn(Optional.of(criarPauta(2L)));
        when(pautaService.estaAberta(2L)).thenReturn(true);
        when(votoRepository.findByAssociateIdAndPautaId(request.associateId(), 2L))
                .thenReturn(Optional.of(new Voto()));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> votoService.criar(2L, request)
        );

        assertEquals("Associado já votou nesta pauta", exception.getMessage());
        verify(cpfValidationClient, never()).validate(any());
        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveBloquearVotoQuandoPautaEstiverFechada() {
        VotoRequestDTO request = new VotoRequestDTO(10L, "12345678900", "YES");

        when(pautaRepository.findById(2L)).thenReturn(Optional.of(criarPauta(2L)));
        when(pautaService.estaAberta(2L)).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> votoService.criar(2L, request)
        );

        assertEquals("A pauta não está mais aberta para votação", exception.getMessage());
        verify(votoRepository, never()).findByAssociateIdAndPautaId(request.associateId(), 2L);
        verify(cpfValidationClient, never()).validate(any());
        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveBloquearVotoDiferenteDeSimOuNao() {
        VotoRequestDTO request = new VotoRequestDTO(10L, "12345678900", "Talvez");
        Pauta pauta = criarPauta(2L);

        when(pautaRepository.findById(2L)).thenReturn(Optional.of(pauta));
        when(pautaService.estaAberta(2L)).thenReturn(true);
        when(votoRepository.findByAssociateIdAndPautaId(request.associateId(), 2L)).thenReturn(Optional.empty());
        when(cpfValidationClient.validate(request.cpf())).thenReturn(CpfValidationStatus.ABLE_TO_VOTE);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> votoService.criar(2L, request)
        );

        assertEquals("Voto deve ser 'YES' ou 'NO'", exception.getMessage());
        verify(cpfValidationClient).validate(request.cpf());
        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveRetornarErroQuandoCpfForInvalidoNoClientFake() {
        VotoRequestDTO request = new VotoRequestDTO(10L, "12345678900", "YES");

        when(pautaRepository.findById(2L)).thenReturn(Optional.of(criarPauta(2L)));
        when(pautaService.estaAberta(2L)).thenReturn(true);
        when(votoRepository.findByAssociateIdAndPautaId(request.associateId(), 2L)).thenReturn(Optional.empty());
        when(cpfValidationClient.validate(request.cpf())).thenThrow(new CpfNotFoundException("CPF inválido"));

        CpfNotFoundException exception = assertThrows(
                CpfNotFoundException.class,
                () -> votoService.criar(2L, request)
        );

        assertEquals("CPF inválido", exception.getMessage());
        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveBloquearQuandoCpfValidationClientRetornarUnableToVote() {
        VotoRequestDTO request = new VotoRequestDTO(10L, "12345678900", "YES");

        when(pautaRepository.findById(2L)).thenReturn(Optional.of(criarPauta(2L)));
        when(pautaService.estaAberta(2L)).thenReturn(true);
        when(votoRepository.findByAssociateIdAndPautaId(request.associateId(), 2L)).thenReturn(Optional.empty());
        when(cpfValidationClient.validate(request.cpf())).thenReturn(CpfValidationStatus.UNABLE_TO_VOTE);

        AssociateUnableToVoteException exception = assertThrows(
                AssociateUnableToVoteException.class,
                () -> votoService.criar(2L, request)
        );

        assertEquals("Associado não está habilitado para votar", exception.getMessage());
        verify(votoRepository, never()).save(any(Voto.class));
    }

    private Pauta criarPauta(Long id) {
        Pauta pauta = new Pauta();
        pauta.setId(id);
        return pauta;
    }
}
