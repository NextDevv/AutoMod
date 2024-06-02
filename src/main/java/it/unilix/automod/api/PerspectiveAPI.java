package it.unilix.automod.api;

import it.unilix.automod.configs.Settings;
import it.unilix.json.JsonObject;
import it.unilix.json.JsonString;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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

    public PerspectiveAPI(@NotNull String apiKey, @NotNull Settings settings) {
        this.apiKey = apiKey;
        this.settings = settings;
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

                    System.out.println("Response: " + object);
                    System.out.println("Span scores: " + spanScores);

                    for (int i = 0; i < spanScores.size(); i++) {
                        JsonObject spanScore = spanScores.get(i);
                        double score = Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("score").get("value").get())));
                        if(score > settings.getThreshold()) {
                            toxic = true;

                            int begin = (int) Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("begin").get())));
                            int end = (int) Double.parseDouble(String.valueOf(Objects.requireNonNull(spanScore.get("end").get())));
                            IntStream.range(begin, end).forEach(j -> {
                                if(chars.get(j) != ' ' && chars.get(j) != '\n' && chars.get(j) != '\t' && chars.get(j) != '\r')
                                    chars.set(j, '*');
                            });
                            System.out.println("Toxic span: " + text.substring(begin, end));
                        }
                    }
                    return new Pair<>(chars.stream().map(String::valueOf).reduce("", String::concat), toxic);
                });
    }
}
