package br.com.projeto.model;

public record FaixaValores(double valorBase, double variacao) {
    public double gerarValorSimulado() {
        return valorBase + (Math.random() * variacao);
    }
}
