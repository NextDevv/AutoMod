package com.nextdevv.automod.api;

import com.nextdevv.automod.configs.Settings;
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

    public PerspectiveAPI(@NotNull String apiKey, @NotNull Settings settings) {
        this.apiKey = apiKey;
        this.settings = settings;
        censorChars = this.settings.getCensorCharacters().split("");
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
                                        "TOXICITY": {}
                                    },
                                    "spanAnnotations": true
                                }
                              """.replace("%s", text.replace("\"", "\\\""))
                ))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> {
                    JsonObject object = JsonString.fromString(responseBody);
                    JsonObject spanScores = object.get("attributeScores").get("TOXICITY").get("spanScores");
                    List<Character> chars = new ArrayList<>(text.chars().mapToObj(e -> (char) e).toList());
                    boolean toxic = false;

                    if(settings.isDebug()) {
                        System.out.println("[DEBUG] Response: " + object);
                        System.out.println("[DEBUG] Span scores: " + spanScores);
                        if(settings.isVerbose()) {
                            System.out.println("[DEBUG/VERBOSE] Text: " + text);
                            System.out.println("[DEBUG/VERBOSE] Chars: " + chars);
                        }
                    }

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
                            if(settings.isDebug()) {
                                System.out.println("[DEBUG] Toxic span: " + text.substring(begin, end));
                            }
                        }
                    }
                    return new Pair<>(chars.stream().map(String::valueOf).reduce("", String::concat), toxic);
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
