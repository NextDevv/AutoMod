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
}
