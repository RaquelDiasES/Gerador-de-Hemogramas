package br.com.projeto.gerador;

import com.google.gson.*;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GeradorHemogramas {

    private static final int QUANTIDADE = 100;
    // Padrão ajustado para NÃO incluir o offset, conforme a última correção.
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
        Random random = new Random();

        // --- Geração do ID do Bundle ---
        String bundleId = "hemograma-" + UUID.randomUUID().toString();

        // CORREÇÃO ESSENCIAL: Adicionar o 'id' na raiz do Bundle
        bundle.addProperty("id", bundleId);

        // Atualiza o 'identifier/value' (Padrão FHIR)
        if (bundle.has("identifier")) {
            bundle.getAsJsonObject("identifier").addProperty("value", bundleId);
        }

        // --- Geração de Datas e UUIDs ---

        // Usar LocalDateTime, que não inclui fuso, para corresponder ao parser do Projeto 1.
        int diasAtras = random.nextInt(6); // 0 a 5 dias
        LocalDateTime dataColeta = LocalDateTime.now().minusDays(diasAtras);

        String dataColetaStr = dataColeta.format(DATE_FORMATTER);

        // 2. Geração da Data de Nascimento (0 a 5 anos = 1 a 60 meses)
        int idadeSimuladaEmMeses = random.nextInt(60) + 1; // 1 a 60 meses

        LocalDate dataNascimento = dataColeta.toLocalDate().minus(
                Period.ofMonths(idadeSimuladaEmMeses));
        String dataNascimentoStr = dataNascimento.toString();

        // 3. Referências internas que precisam ser substituídas
        final String patientUuidRef = "urn:uuid:PACIENTE-UUID-REF";
        final String patientNewUuid = "urn:uuid:" + UUID.randomUUID().toString();

        JsonArray entries = bundle.getAsJsonArray("entry");

        for (JsonElement entry : entries) {
            JsonObject entryObject = entry.getAsJsonObject();
            JsonObject resource = entryObject.getAsJsonObject("resource");
            String resourceType = resource.get("resourceType").getAsString();

            if (resourceType.equals("Patient")) {
                // ATUALIZAÇÕES NO RECURSO PATIENT

                // 1. Atualizar CPF
                JsonObject identifier = resource.getAsJsonArray("identifier").get(0).getAsJsonObject();
                identifier.addProperty("value", cpf);

                // 2. Atualizar Data de Nascimento (essencial para o cálculo da idade)
                resource.addProperty("birthDate", dataNascimentoStr);

                // 3. Atualizar o fullUrl do Patient com o novo UUID
                entryObject.addProperty("fullUrl", patientNewUuid);

                System.out.println("👶 Paciente: CPF " + cpf + ", Data Nasc: " + dataNascimentoStr + " (" + idadeSimuladaEmMeses + " meses)");


            } else if (resourceType.equals("Observation")) {

                // ATUALIZAÇÕES NOS RECURSOS OBSERVATION

                // 1. Atualizar a referência 'subject' para o novo UUID do Patient
                if (resource.has("subject")) {
                    JsonObject subject = resource.getAsJsonObject("subject");
                    // O template corrigido deve ter a referência placeholder
                    if (subject.has("reference") && subject.get("reference").getAsString().equals(patientUuidRef)) {
                        subject.addProperty("reference", patientNewUuid);
                    }
                }

                // 2. Atualizar data de coleta na Observation (na parte 'contained' Specimen)
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

                // 3. Atualizar effectiveDateTime (hora da coleta da Observation)
                resource.addProperty("effectiveDateTime", dataColetaStr);
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