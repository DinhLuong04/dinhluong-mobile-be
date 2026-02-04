package com.dinhluong.dlmstore.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {
    public static String unAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toLowerCase().replace("đ", "d");
    }
    public static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}