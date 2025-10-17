package br.com.projeto.gerador;

import java.net.http.*;
import java.net.URI;
import java.time.Duration;

public class FhirClient {

    public static HttpResponse<String> enviarBundle(JsonObject bundle) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(Config.HTTP_TIMEOUT))
                    .version(HttpClient.Version.HTTP_2)
                    .build();

            String jsonBody = HemogramaSimulator.bundleParaJson(bundle);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Config.FHIR_URL))
                    .header("Content-Type", "application/fhir+json")
                    .header("User-Agent", "GeradorHemogramas/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(Config.HTTP_TIMEOUT))
                    .build();

            System.out.println("📤 Enviando para: " + Config.FHIR_URL);
            return client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar para FHIR: " + e.getMessage(), e);
        }
    }

    public static boolean isSucesso(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}