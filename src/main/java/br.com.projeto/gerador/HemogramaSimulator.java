package br.com.projeto.gerador;

import com.google.gson.*;
import br.com.projeto.gerador.model.FaixaValores;
import java.util.Map;

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
        double novoValor = faixa.gerarValorSimulado();

        JsonObject valueQuantity = resource.getAsJsonObject("valueQuantity");
        valueQuantity.addProperty("value", Math.round(novoValor * 100.0) / 100.0); // 2 casas decimais

        System.out.println("🔄 " + exameId + ": " + valueQuantity.get("value").getAsString());
    }

    public static String bundleParaJson(JsonObject bundle) {
        return gson.toJson(bundle);
    }
}