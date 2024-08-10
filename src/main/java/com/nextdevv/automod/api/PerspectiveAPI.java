package com.nextdevv.automod.api;

import com.nextdevv.automod.configs.Settings;
import com.nextdevv.automod.enums.Attribute;
import com.nextdevv.automod.utils.Debug;
import com.nextdevv.automod.utils.Pair;
import com.nextdevv.automod.utils.ListUtils;
import it.unilix.json.JsonObject;
import it.unilix.json.JsonString;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.time.Duration.*;

public class PerspectiveAPI {
    private final String apiKey;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(ofSeconds(5))
            .build();
    private final Settings settings;
    private final String[] censorChars;
    private final Attribute[] attributes;
    StringBuilder attributesString = new StringBuilder();

    public PerspectiveAPI(@NotNull String apiKey, @NotNull Settings settings) {
        this.apiKey = apiKey;
        this.settings = settings;
        censorChars = this.settings.getCensorCharacters().split("");
        attributes = this.settings.getAttributes();

        for (Attribute attribute : this.attributes) {
            attributesString.append("\"").append(attribute.name()).append("\": {},");
        }
        attributesString.deleteCharAt(attributesString.length() - 1);
    }

    public CompletableFuture<Pair<String, Boolean>> censorAsync(String text, Attribute attribute) throws URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        """
                                {
                                    "comment": {
                                        "text": "%s"
                                    },
                                    "requestedAttributes": {
                                        "%s": {}
                                    },
                                    "spanAnnotations": true
                                }
                              """.replace("%s", text
                                        .replace("\"", "\\\""))
                                 .replace("%s", attribute.name())
                ))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> {
                    boolean toxic = false;

                    JsonObject object = JsonString.fromString(responseBody);
                    JsonObject spanScores = object.get("attributeScores").get(attribute.name()).get("spanScores");
                    List<Character> chars = new ArrayList<>(text.chars().mapToObj(e -> (char) e).toList());

                    Debug.log("Attribute: " + attribute.name());
                    Debug.log("Response: " + object);
                    Debug.verbose("Text: " + text);
                    Debug.verbose("Chars: " + chars);
                    Debug.verbose("Span scores: " + spanScores);

                    for (int i = 0; i < spanScores.size(); i++) {
                        JsonObject spanScore = spanScores.get(i);
                        double score = Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("score").get("value").get())));
                        if (score > settings.getThreshold()) {
                            toxic = true;

                            int begin = (int) Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("begin").get())));
                            int end = (int) Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("end").get())));
                            IntStream.range(begin, end).forEach(j -> {
                                String randomChar = ListUtils.random(censorChars);
                                if (chars.get(j) != ' ' && chars.get(j) != '\n' && chars.get(j) != '\t' && chars.get(j) != '\r')
                                    chars.set(j, randomChar.charAt(0));
                            });

                            Debug.log("Toxic span: " + text.substring(begin, end));
                        }
                    }

                    return new Pair<>(chars.stream().map(String::valueOf).reduce("", String::concat), toxic);
                });
    }

    public CompletableFuture<Pair<String, Boolean>> censorAsync(String text) throws URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        """
                                {
                                    "comment": {
                                        "text": "%s"
                                    },
                                    "requestedAttributes": {
                                        %att
                                    },
                                    "spanAnnotations": true
                                }
                              """.replace("%s", text.replace("\"", "\\\""))
                                 .replace("%att", attributesString.toString())
                ))
                .build();

        Debug.log("Request: " + request);
        Debug.verbose("Request Body: " + """
                {
                    "comment": {
                        "text": "%s"
                    },
                    "requestedAttributes": {
                        %att
                    },
                    "spanAnnotations": true
                }
                """.replace("%s", text.replace("\"", "\\\""))
                             .replace("%att", attributesString.toString()));

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> {
                    boolean toxic = false;

                    JsonObject object = JsonString.fromString(responseBody);

                    for(Attribute attribute : attributes) {
                        JsonObject spanScores = object.get("attributeScores").get(attribute.name()).get("spanScores");
                        List<Character> chars = new ArrayList<>(text.chars().mapToObj(e -> (char) e).toList());

                        Debug.log("Attribute: " + attribute.name());
                        Debug.log("Response: " + object);
                        Debug.verbose("Text: " + text);
                        Debug.verbose("Chars: " + chars);
                        Debug.verbose("Span scores: " + spanScores);

                        for (int i = 0; i < spanScores.size(); i++) {
                            JsonObject spanScore = spanScores.get(i);
                            double score = Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("score").get("value").get())));
                            if(score > settings.getThreshold()) {
                                toxic = true;

                                int begin = (int) Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("begin").get())));
                                int end = (int) Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("end").get())));
                                IntStream.range(begin, end).forEach(j -> {
                                    String randomChar = ListUtils.random(censorChars);
                                    if(chars.get(j) != ' ' && chars.get(j) != '\n' && chars.get(j) != '\t' && chars.get(j) != '\r')
                                        chars.set(j, randomChar.charAt(0));
                                });

                                Debug.log("Toxic span: " + text.substring(begin, end));
                            }
                        }

                        if(toxic) return new Pair<>(chars.stream().map(String::valueOf).reduce("", String::concat), toxic);
                    }

                    return new Pair<>(text, false);
                });
    }

    public boolean isConnected() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        """
                                {
                                    "comment": {
                                        "text": "test"
                                    },
                                    "requestedAttributes": {
                                        "TOXICITY": {}
                                    },
                                    "spanAnnotations": true
                                }
                              """
                ))
                .build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
