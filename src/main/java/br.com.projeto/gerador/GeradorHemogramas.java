package br.com.projeto.gerador;

import com.google.gson.*;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GeradorHemogramas {

    private static final int QUANTIDADE = 100;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("🔬 Gerador de 100 Hemogramas FHIR");
        System.out.println("=====================================");
        System.out.println("📊 Quantidade: " + QUANTIDADE);
        System.out.println("🌐 Servidor: " + Config.FHIR_URL);
        System.out.println("=====================================\n");

        try {
            JsonObject templateBundle = carregarBundle(Config.BUNDLE_PATH);

            long startTime = System.currentTimeMillis();
            int sucessos = 0;
            int falhas = 0;

            // Gerar CPFs únicos
            List<String> cpfs = gerarCPFsUnicos(QUANTIDADE);

            for (int i = 0; i < QUANTIDADE; i++) {
                try {
                    // Clonar template
                    JsonObject bundle = templateBundle.deepCopy();

                    // Personalizar
                    String cpf = cpfs.get(i);
                    personalizarBundle(bundle, cpf, i);

                    // Simular valores
                    JsonObject bundleSimulado = HemogramaSimulator.simularValores(bundle);

                    // Enviar
                    HttpResponse<String> response = FhirClient.enviarBundle(bundleSimulado);

                    if (FhirClient.isSucesso(response.statusCode())) {
                        sucessos++;
                        System.out.println("✅ " + (i + 1) + "/" + QUANTIDADE + " - CPF: " + cpf);
                    } else {
                        falhas++;
                        System.out.println("❌ " + (i + 1) + "/" + QUANTIDADE + " - Status: " + response.statusCode());
                    }

                    // Pequeno delay a cada 10 para não sobrecarregar
                    if ((i + 1) % 10 == 0) {
                        Thread.sleep(200);
                    }

                } catch (Exception e) {
                    falhas++;
                    System.err.println("❌ Erro no hemograma " + (i + 1) + ": " + e.getMessage());
                }
            }

            long endTime = System.currentTimeMillis();
            double tempoSeg = (endTime - startTime) / 1000.0;

            // Estatísticas finais
            System.out.println("\n" + "=".repeat(50));
            System.out.println("📊 RESULTADO FINAL");
            System.out.println("=".repeat(50));
            System.out.println("✅ Sucessos: " + sucessos + " (" + String.format("%.1f%%", 100.0 * sucessos / QUANTIDADE) + ")");
            System.out.println("❌ Falhas: " + falhas);
            System.out.println("⏱️  Tempo: " + String.format("%.2f", tempoSeg) + "s");
            System.out.println("🚀 Taxa: " + String.format("%.2f", QUANTIDADE / tempoSeg) + " hemogramas/s");
            System.out.println("=".repeat(50));

        } catch (Exception e) {
            System.err.println("❌ Erro fatal: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static List<String> gerarCPFsUnicos(int quantidade) {
        Set<String> cpfs = new HashSet<>();
        Random random = new Random();

        while (cpfs.size() < quantidade) {
            // Gerar CPF fictício (11 dígitos)
            long numero = 10000000000L + (long)(random.nextDouble() * 90000000000L);
            cpfs.add(String.valueOf(numero));
        }

        return new ArrayList<>(cpfs);
    }

    private static void personalizarBundle(JsonObject bundle, String cpf, int index) {
        // Atualizar ID do bundle
        String bundleId = "hemograma-" + UUID.randomUUID().toString();
        if (bundle.has("identifier")) {
            bundle.getAsJsonObject("identifier").addProperty("value", bundleId);
        }

        // Variar data de coleta (últimas 72h)
        Random random = new Random();
        int horasAtras = random.nextInt(72);
        LocalDateTime dataColeta = LocalDateTime.now().minusHours(horasAtras);
        String dataColetaStr = dataColeta.format(DATE_FORMATTER) + "-03:00";

        // Atualizar todas as observations
        JsonArray entries = bundle.getAsJsonArray("entry");
        for (JsonElement entry : entries) {
            JsonObject resource = entry.getAsJsonObject().getAsJsonObject("resource");

            if (resource.get("resourceType").getAsString().equals("Observation")) {
                // Atualizar CPF
                if (resource.has("subject")) {
                    JsonObject subject = resource.getAsJsonObject("subject");
                    if (subject.has("identifier")) {
                        subject.getAsJsonObject("identifier").addProperty("value", cpf);
                    }
                }

                // Atualizar data de coleta
                if (resource.has("contained")) {
                    JsonArray contained = resource.getAsJsonArray("contained");
                    for (JsonElement cont : contained) {
                        JsonObject specimen = cont.getAsJsonObject();
                        if (specimen.has("collection")) {
                            specimen.getAsJsonObject("collection")
                                    .addProperty("collectedDateTime", dataColetaStr);
                        }
                    }
                }
            }
        }
    }

    private static JsonObject carregarBundle(String caminho) throws IOException {
        try {
            String json = Files.readString(Paths.get(caminho));
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new IOException("JSON inválido: " + caminho, e);
        }
    }
}