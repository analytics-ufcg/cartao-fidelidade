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
    public List<Partido> partidos;

    public static class Municipio {
        public String codMunicipio;
        public String nomeMunicipio;
        public double valorEmpenhos;
        public int numEmpenhos;
        public String siglaPartido;

        @Override
        public String toString() {
            return String.format("{%s:\'%s\' \'%s\' %d:%f}",
                    codMunicipio, nomeMunicipio, siglaPartido, numEmpenhos, valorEmpenhos);
        }
    }

    public static class Partido {
        public String sigla;
        public double valorEmpenhos;
        public int numEmpenhos;

        @Override
        public String toString() {
            return String.format("{\'%s\' %d:%f}", sigla, numEmpenhos, valorEmpenhos);
        }
    }
}