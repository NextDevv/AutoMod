package com.nextdevv.automod.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeConverter {

    public static int convertToSeconds(String inputString) {
        Map<String, Integer> timeFactors = getTimeFactors();

        Pattern pattern = Pattern.compile("(\\d+)([a-zA-Z]+)");
        Matcher matcher = pattern.matcher(inputString);

        int totalSeconds = 0;

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            if (timeFactors.containsKey(unit)) {
                totalSeconds += value * timeFactors.get(unit);
            } else {
                throw new IllegalArgumentException("Invalid time unit '" + unit + "' found in input string.");
            }
        }

        return totalSeconds;
    }

    private static @NotNull Map<String, Integer> getTimeFactors() {
        Map<String, Integer> timeFactors = new HashMap<>();
        timeFactors.put("m", 60);              // minute to seconds
        timeFactors.put("d", 86400);           // day to seconds
        timeFactors.put("s", 1);               // second to seconds
        timeFactors.put("y", 31536000);        // year to seconds
        timeFactors.put("w", 604800);          // week to seconds
        timeFactors.put("mo", 2592000);        // month to seconds (30 days)
        timeFactors.put("h", 3600);            // hour to seconds
        return timeFactors;
    }
}