package com.nextdevv.automod.utils;

public class ListUtils {
    public static String toString(String[] list, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            builder.append(list[i]);
            if (i != list.length - 1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    public static String random(String[] list) {
        return list[(int) (Math.random() * list.length)];
    }

    public static String[] replace(String[] censored, String line, String censor) {
        for (int i = 0; i < censored.length; i++) {
            if (censored[i].equals(line)) {
                censored[i] = censor;
            }
        }
        return censored;
    }

    public static boolean equals(String[] a, String[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i])) {
                return false;
            }
        }
        return true;
    }

    public static String[] subArray(String[] array, int start, int end) {
        String[] subArray = new String[end - start];
        for (int i = start; i < end; i++) {
            subArray[i - start] = array[i];
        }
        return subArray;
    }
}
