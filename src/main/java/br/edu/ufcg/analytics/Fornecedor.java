package br.edu.ufcg.analytics;

import java.util.List;

/**
 * "Fornecedor" entity data holder.
 */
public class Fornecedor {
    public String cpfCnpj;
    public String nome;
    public int totalNumEmpenhos;
    public double totalValorEmpenhos;
    public String codMunicipio;

    public List<Municipio> municipios;
    public static class Municipio {
        public String codMunicipio;
        public double valorEmpenhos;
        public int numEmpenhos;
        public String siglaPartido;
    }

    public List<Partido> partidos;
    public static class Partido {
        public String sigla;
        public double valorEmpenhos;
        public int numEmpenhos;
    }
}