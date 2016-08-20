package br.edu.ufcg.analytics;

import java.util.List;

/**
 * "Fornecedor" entity data holder.
 */
public class Fornecedor {
	public String id;
	public String cnpjCpf;
	public String nome;
	public String atividadeEconomica;
	public int anoInicial;
	public int anoFinal;
	public int qtdLicitacoes;
	public double valorTotal;
	public List<Fidelidade> fidelidade;
}