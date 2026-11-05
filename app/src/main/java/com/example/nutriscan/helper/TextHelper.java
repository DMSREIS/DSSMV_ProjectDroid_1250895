package com.example.nutriscan.helper;

import java.text.Normalizer;
import java.util.Locale;

public class TextHelper {

    // "  Arroz   com ATUM " -> "arroz com atum" (sem maiúsculas, acentos nem espaços a mais)
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String s = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}