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

import static java.time.Duration.ofSeconds;

public class PerspectiveAPI {
    private final String apiKey;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(ofSeconds(5))
            .build();
    private final Settings settings;
    private final String[] censorChars;
    private final Attribute[] attributes;
    private final String attributesString;

    public PerspectiveAPI(@NotNull String apiKey, @NotNull Settings settings) {
        this.apiKey = apiKey;
        this.settings = settings;
        this.censorChars = settings.getCensorCharacters().split("");
        this.attributes = settings.getAttributes();
        this.attributesString = buildAttributesString(attributes);
    }

    private String buildAttributesString(Attribute[] attributes) {
        StringBuilder sb = new StringBuilder();
        for (Attribute attribute : attributes) {
            sb.append("\"").append(attribute.name()).append("\": {},");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public CompletableFuture<Pair<String, Boolean>> censorAsync(String text, Attribute attribute) throws URISyntaxException {
        HttpRequest request = buildRequest(text, attribute.name());
        return processResponse(text, request, attribute);
    }

    public CompletableFuture<Pair<String, Boolean>> censorAsync(String text) throws URISyntaxException {
        HttpRequest request = buildRequest(text, attributesString);
        return processResponse(text, request, attributes);
    }

    private HttpRequest buildRequest(String text, String attributes) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        String.format("""
                                {
                                    "comment": {
                                        "text": "%s"
                                    },
                                    "requestedAttributes": {
                                        %s
                                    },
                                    "spanAnnotations": true
                                }
                              """, text.replace("\"", "\\\""), attributes)
                ))
                .build();
    }

    private CompletableFuture<Pair<String, Boolean>> processResponse(String text, HttpRequest request, Attribute... attributes) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> {
                    boolean toxic = false;
                    JsonObject object = JsonString.fromString(responseBody);
                    List<Character> chars = new ArrayList<>(text.chars().mapToObj(e -> (char) e).toList());

                    for (Attribute attribute : attributes) {
                        JsonObject spanScores = object.get("attributeScores").get(attribute.name()).get("spanScores");
                        toxic = processSpanScores(text, chars, spanScores);
                        if (toxic) break;
                    }

                    return new Pair<>(chars.stream().map(String::valueOf).reduce("", String::concat), toxic);
                });
    }

    private boolean processSpanScores(String text, List<Character> chars, JsonObject spanScores) {
        boolean toxic = false;
        for (int i = 0; i < spanScores.size(); i++) {
            JsonObject spanScore = spanScores.get(i);
            double score = Double.parseDouble(String.valueOf(spanScore.get("score").get("value").get()));
            if (score > settings.getThreshold()) {
                toxic = true;
                int begin = (int) Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("begin").get())));
                int end = (int) Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("begin").get())));
                IntStream.range(begin, end).forEach(j -> {
                    if (!Character.isWhitespace(chars.get(j))) {
                        chars.set(j, ListUtils.random(censorChars).charAt(0));
                    }
                });
                Debug.log("Toxic span: " + text.substring(begin, end));
            }
        }
        return toxic;
    }

    public boolean isConnected() {
        HttpRequest request = buildTestRequest();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private HttpRequest buildTestRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                            "comment": {
                                "text": "test"
                            },
                            "requestedAttributes": {
                                "TOXICITY": {}
                            },
                            "spanAnnotations": true
                        }
                      """))
                .build();
    }
}