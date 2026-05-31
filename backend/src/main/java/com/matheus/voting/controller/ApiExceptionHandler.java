package com.matheus.voting.controller;

import com.matheus.voting.exception.AssociateUnableToVoteException;
import com.matheus.voting.exception.CpfNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, RuntimeException.class})
    public ResponseEntity<String> tratarRegraDeNegocio(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(CpfNotFoundException.class)
    public ResponseEntity<String> tratarCpfInvalido(CpfNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(AssociateUnableToVoteException.class)
    public ResponseEntity<String> tratarAssociadoNaoHabilitado(AssociateUnableToVoteException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }
}
