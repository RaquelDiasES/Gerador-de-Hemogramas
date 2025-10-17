package br.com.projeto.gerador;

public class Main {
    public static void main(String[] args) {
        System.out.println("🔬 Gerador de Hemogramas FHIR");
        System.out.println("==============================");
        System.out.println("Configurações:");
        System.out.println("  - FHIR Server: " + Config.FHIR_URL);
        System.out.println("  - Template: " + Config.BUNDLE_PATH);
        System.out.println("  - Timeout: " + Config.HTTP_TIMEOUT + "s");
        System.out.println("==============================\n");

        EnvioHemogramaBundle.main(args);
    }
}