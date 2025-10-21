package br.com.projeto.gerador;

import br.com.projeto.gerador.model.FaixaValores;

import java.util.Map;

public class Config {
    // URL do receptor FHIR
    public static final String FHIR_URL = System.getenv()
            .getOrDefault("FHIR_SERVER_URL", "http://localhost:8080/fhir/Bundle");

    public static final String BUNDLE_PATH = System.getenv()
            .getOrDefault("BUNDLE_TEMPLATE_PATH", "src/main/resources/Bundle-hemograma-completo.json");

    public static final int HTTP_TIMEOUT = Integer.parseInt(
            System.getenv().getOrDefault("HTTP_TIMEOUT_SECONDS", "30"));

    // Valores de referências pediátricas
    public static final Map<String, FaixaValores> EXAMES = Map.of(
            "hemoglobina", new FaixaValores(11.5, 1.5),
            "hematocrito", new FaixaValores(34.0, 4.0),
            "leucocitos", new FaixaValores(8.0, 4.0)
    );

    public static final double PROBABILIDADE_ANEMIA = 0.15; // 15% de chance de anemia
}