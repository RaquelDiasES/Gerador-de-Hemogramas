package br.com.projeto.gerador;

import java.util.Map;

public class Config {
    // Configurações via environment variables com fallback
    public static final String FHIR_URL = System.getenv()
            .getOrDefault("FHIR_SERVER_URL", "http://localhost:8080/fhir/Bundle");

    public static final String BUNDLE_PATH = System.getenv()
            .getOrDefault("BUNDLE_TEMPLATE_PATH", "src/main/resources/Bundle-hemograma-completo.json");

    public static final int HTTP_TIMEOUT = Integer.parseInt(
            System.getenv().getOrDefault("HTTP_TIMEOUT_SECONDS", "30"));

    // Configurações dos exames (base + variação)
    public static final Map<String, FaixaValores> EXAMES = Map.of(
            "hemoglobina", new FaixaValores(13.0, 4.0),   // 13.0 ± 4.0
            "hematocrito", new FaixaValores(38.0, 7.0),   // 38.0 ± 7.0
            "leucocitos", new FaixaValores(4.0, 6.0)      // 4.0 ± 6.0
    );
}