package br.com.projeto.gerador;

import com.google.gson.*;
import br.com.projeto.gerador.model.FaixaValores;

import java.util.Random;

public class HemogramaSimulator {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static JsonObject simularValores(JsonObject bundle) {
        JsonArray entries = bundle.getAsJsonArray("entry");

        for (JsonElement entryElement : entries) {
            JsonObject entry = entryElement.getAsJsonObject();
            JsonObject resource = entry.getAsJsonObject("resource");

            String resourceId = resource.get("id").getAsString();

            if (Config.EXAMES.containsKey(resourceId)) {
                simularExame(resource, resourceId);
            }
        }

        return bundle;
    }

    private static void simularExame(JsonObject resource, String exameId) {
        FaixaValores faixa = Config.EXAMES.get(exameId);

        Random random = new Random();
        int tipoAnemia = random.nextInt(3);
        double novoValor;

        switch (tipoAnemia) {
            case 0:
                novoValor = 10.0 + random.nextDouble() * 2.0;
                break;
            case 1:
                novoValor = 8.0 + random.nextDouble() * 2.0;
                break;
            case 2:
                novoValor = 5.0 + random.nextDouble() * 3.0;
                break;
            default:
                novoValor = faixa.gerarValorSimulado();
        }

        JsonObject valueQuantity = resource.getAsJsonObject("valueQuantity");
        valueQuantity.addProperty("value", Math.round(novoValor * 100.0) / 100.0);

        resource.addProperty("anemiaClassification", tipoAnemia == 0 ? "leve" : tipoAnemia == 1 ? "moderado" : "grave");

        System.out.println("🔄 " + exameId + ": " + valueQuantity.get("value").getAsString() +
                " (" + (tipoAnemia == 0 ? "leve" : tipoAnemia == 1 ? "moderado" : "grave") + ")");
    }

    public static String bundleParaJson(JsonObject bundle) {
        return gson.toJson(bundle);
    }
}