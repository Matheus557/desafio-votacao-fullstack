package com.matheus.voting.client;

import com.matheus.voting.exception.CpfNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CpfValidationClient {

    private final Random random = new Random();

    public CpfValidationStatus validate(String cpf) {
        String normalizedCpf = cpf == null ? "" : cpf.replaceAll("\\D", "");

        if (normalizedCpf.length() != 11) {
            throw new CpfNotFoundException("CPF inválido");
        }

        int result = random.nextInt(3);
        if (result == 0) {
            throw new CpfNotFoundException("CPF inválido");
        }

        return result == 1
                ? CpfValidationStatus.ABLE_TO_VOTE
                : CpfValidationStatus.UNABLE_TO_VOTE;
    }
}
