package it.unilix.automod.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkDetector {
    private static final String URL_REGEX =
            "(?i)\\b((?:https?|ftp|file)://|www\\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]|\\b[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b|\\b\\d{1,3}(\\.\\d{1,3}){3}\\b|\\b([a-fA-F0-9:]+:+)+[a-fA-F0-9]+\\b";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    public static List<String> detect(String text) {
        List<String> urls = new ArrayList<>();
        if(text == null || text.isEmpty()) {
            return urls;
        }

        Matcher matcher = URL_PATTERN.matcher(text);
        while(matcher.find()) {
            String url = matcher.group();
            urls.add(url);
        }

        return urls;
    }

    public static String censor(String message) {
        List<String> urls = detect(message);
        for(String url : urls) {
            message = message.replace(url, "*".repeat(url.length()));
        }

        return message;
    }
}