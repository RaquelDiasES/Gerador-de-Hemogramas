package br.com.projeto.gerador.model;

import br.com.projeto.gerador.Config;

public record FaixaValores(double valorBase, double variacao) {

    public double gerarValorSimulado() {
        // Gera valor aleatório
        double valor = valorBase + (Math.random() * variacao * 2) - variacao;

        if (Math.random() < Config.PROBABILIDADE_ANEMIA) {
            valor = valor * 0.7;
        }

        return Math.max(0, valor); // Não negativo negativo
    }
}