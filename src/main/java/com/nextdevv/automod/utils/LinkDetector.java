package com.nextdevv.automod.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkDetector {
    private static final String URL_REGEX =
            "(?i)\\b((?:https?|ftp|file)://|www\\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]|\\b[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b|\\b\\d{1,3}(\\.\\d{1,3}){3}\\b|\\b([a-fA-F0-9:]+:+)+[a-fA-F0-9]+\\b";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
    private static final Long TIMEOUT_MS = 1000L;

    public static List<String> detect(String text) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<String>> future = executor.submit(() -> {
            List<String> urls = new ArrayList<>();
            if (text == null || text.isEmpty()) {
                return urls;
            }

            Matcher matcher = URL_PATTERN.matcher(text);
            while (matcher.find()) {
                String url = matcher.group();
                urls.add(url);
            }
            return urls;
        });

        try {
            return future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.err.println("[AutoMod] URL detection timed out.");
            return Collections.emptyList();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("[AutoMod] URL detection interrupted: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            executor.shutdown();
        }
    }

    public static String censor(String message, String censorChars) {
        List<String> urls = detect(message);
        String[] chars = censorChars.split("");
        StringBuilder censor = new StringBuilder();
        for(String url : urls) {
            for (int i = 0; i < url.length(); i++) {
                censor.append(ListUtils.random(chars));
            }
            message = message.replace(url, censor.toString());
            censor.setLength(0);
        }

        return message;
    }

    public static String censor(String message) {
        return censor(message, "*");
    }
}