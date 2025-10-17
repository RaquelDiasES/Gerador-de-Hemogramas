package br.com.projeto.gerador;

import com.google.gson.*;
import java.nio.file.*;
import java.io.IOException;

public class EnvioHemogramaBundle {

    public static void main(String[] args) {
        System.out.println("🔬 Iniciando Gerador de Hemogramas...");

        try {
            // 1. Carregar bundle template
            JsonObject bundle = carregarBundle(Config.BUNDLE_PATH);
            System.out.println("✅ Bundle carregado: " + Config.BUNDLE_PATH);

            // 2. Simular valores dos exames
            JsonObject bundleSimulado = HemogramaSimulator.simularValores(bundle);
            System.out.println("✅ Valores simulados com sucesso");

            // 3. Enviar para servidor FHIR
            HttpResponse<String> response = FhirClient.enviarBundle(bundleSimulado);

            // 4. Processar resposta
            System.out.println("📊 Status: " + response.statusCode());

            if (FhirClient.isSucesso(response.statusCode())) {
                System.out.println("🎉 Bundle enviado com sucesso!");
                System.out.println("Resposta:\n" + response.body());
            } else {
                System.out.println("⚠️  Resposta inesperada do servidor");
                System.out.println("Detalhes: " + response.body());
            }

        } catch (IOException e) {
            System.err.println("❌ Erro ao ler arquivo: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static JsonObject carregarBundle(String caminho) throws IOException {
        try {
            String json = Files.readString(Paths.get(caminho));
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new IOException("JSON inválido no arquivo: " + caminho, e);
        }
    }
}