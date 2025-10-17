package br.com.projeto.gerador.model;

public record FaixaValores(double valorBase, double variacao) {
    public double gerarValorSimulado() {
        return valorBase + (Math.random() * variacao);
    }
}
