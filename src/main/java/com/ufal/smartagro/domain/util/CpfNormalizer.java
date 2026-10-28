package com.ufal.smartagro.domain.util;

public final class CpfNormalizer {

    private CpfNormalizer() {
    }

    public static String digitsOnly(String cpf) {
        if (cpf == null) {
            return null;
        }
        String digits = cpf.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }
}
