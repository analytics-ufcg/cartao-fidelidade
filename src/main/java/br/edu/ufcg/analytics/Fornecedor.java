package br.edu.ufcg.analytics;

import java.util.List;

/**
 * "Fornecedor" entity data holder.
 */
public class Fornecedor {
	public String cpfCnpj;
	public String nome;
	public int numEmpenhos;
	public double valorEmpenhos;
	public List<Fidelidade> fidelidade;
}