package it.unilix.automod.utils;

import java.util.regex.Pattern;

public class ApiKeyValidator {
    private static final String API_KEY_REGEX = "AIza[0-9A-Za-z-_]{35}";
    private static final Pattern API_KEY_PATTERN = Pattern.compile(API_KEY_REGEX);

    /**
     * Validates the format of the given Google API key.
     *
     * @param apiKey The API key to validate.
     * @return True if the API key format is valid, false otherwise.
     */
    public static boolean isFormatValid(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }

        return API_KEY_PATTERN.matcher(apiKey).matches();
    }
}
