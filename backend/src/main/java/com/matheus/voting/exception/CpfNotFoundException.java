package com.matheus.voting.exception;

public class CpfNotFoundException extends RuntimeException {

    public CpfNotFoundException(String message) {
        super(message);
    }
}
