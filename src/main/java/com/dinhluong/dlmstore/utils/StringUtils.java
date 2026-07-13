package com.dinhluong.dlmstore.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

    public static String unAccent(String s) {

        if (s == null) return "";

        String temp = Normalizer.normalize(
                s,
                Normalizer.Form.NFD
        );

        Pattern pattern =
                Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        return pattern.matcher(temp)
                .replaceAll("")
                .replace("đ", "d")
                .toLowerCase();
    }

    public static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static String toSlug(String text) {

        if (!hasText(text)) {
            return "";
        }

        return unAccent(text)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}