package com.nextdevv.automod.utils;

public class StringUtils {
    public static String[] chunked(String str, int size) {
        int numChunks = (int) Math.ceil((double) str.length() / size);
        String[] chunks = new String[numChunks];
        for (int i = 0; i < numChunks; i++) {
            chunks[i] = str.substring(i * size, Math.min((i + 1) * size, str.length()));
        }
        return chunks;
    }

    public static String[] splitOrEmpty(String str, String regex) {
        return str.isEmpty() ? new String[0] : str.split(regex);
    }

    public static String getSplitOrEmpty(String str, String regex, int index) {
        String[] split = splitOrEmpty(str, regex);
        return index < split.length ? split[index] : "";
    }

    public static String censor(String line, String word, String s) {
        String[] censorChars = s.split("");
        for(int i = word.length(); i > 0; i--) {
            String randomChar = censorChars[(int) (Math.random() * censorChars.length)];
            line = line.replaceFirst(word, randomChar.repeat(i));
        }
        return line;
    }
}

