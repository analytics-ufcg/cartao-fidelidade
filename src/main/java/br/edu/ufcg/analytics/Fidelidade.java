package br.edu.ufcg.analytics;

/**
 * Data holder for the "Fidelidade" bar.
 */
public class Fidelidade {
	public String nomePartido;
	public int indice;
	public int qtdLicitacoes;
	public double valorTotal;
	public Fidelidade(String nomePartido, int indice, int qtdLicitacoes, double valorTotal) {
		this.nomePartido = nomePartido;
		this.indice = indice;
		this.qtdLicitacoes = qtdLicitacoes;
		this.valorTotal = valorTotal;
	}
}
