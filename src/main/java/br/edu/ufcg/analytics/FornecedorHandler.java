package br.edu.ufcg.analytics;

import ca.krasnay.sqlbuilder.SelectBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.jooby.Err;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Consumes;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * All Handlers related to Fornecedor.
 */
@Consumes("application/json")
@Produces("application/json")
public class FornecedorHandler {
    private static final String TBL_EMPENHOS_POR_MUNICIO = "EMPENHOS_POR_MUNICIPIO";
    private static final String TBL_MUNICIPIOS = "MUNICIPIOS";
    private static final String CPF_CNPJ = "NU_CPFCNPJ";
    private static final String NOME_FORNECEDOR = "NOME_FORNECEDOR";
    private static final String COD_MUNICIPIO = "COD_MUNICIPIO";
    private static final String NOME_MUNICIPIO = "NOME_MUNICIPIO";
    private static final String ANO_MANDATO = "ANO_ELEICAO";
    private static final String QTD_EMPENHOS = "QT_EMPENHOS";
    private static final String VALOR_EMPENHOS = "VL_EMPENHOS";
    private static final String SIGLA_PARTIDO = "SIGLA_PARTIDO";
    private static final String COD_MUNICIPIO_FORNECEDOR = "CODIGO_MUNICIPIO_FORNECEDOR";
    private static final String TOTAL_EMPENHOS = "TOTAL_EMPENHOS";
    private static final String TOTAL_VALOR_EMPENHOS = "TOTAL_VALOR_EMPENHOS";
    private static final int LIMIT = 10;
    private static final int RANKING_FUNCTION_QTD_EMPENHOS = 1;
    private static final int RANKING_FUNCTION_VALOR_EMPENHOS = 2;
    private static final Set<Integer> VALID_RANKING_FUNCTIONS = ImmutableSet.of(
            RANKING_FUNCTION_QTD_EMPENHOS, RANKING_FUNCTION_VALOR_EMPENHOS);
    private DataSource ds;

    @Inject
    public FornecedorHandler(DataSource ds) {
        this.ds = ds;
    }

    @Path("fornecedores/:state/:id/:year/:rankingFunction")
    @GET
    public Result getPorEstado(String state, String id, int rankingFunction, int year) throws SQLException {
        if (!VALID_RANKING_FUNCTIONS.contains(rankingFunction)) {
            throw new Err(Status.BAD_REQUEST, String.format("Invalid ranking function: %d", rankingFunction));
        }
        String sql = new SelectBuilder()
                .column(CPF_CNPJ, true /* group by */)
                .column(NOME_FORNECEDOR, true /* group by */)
                .column("epm." + COD_MUNICIPIO, true /* group by */)
                .column(SIGLA_PARTIDO, true /* group by */)
                .column(COD_MUNICIPIO_FORNECEDOR, true /* group by */)
                .column("m." + NOME_MUNICIPIO, false /* group by */)
                .column("SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS)
                .column("SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS)
                .where(CPF_CNPJ + " = '" + id + "' AND " + ANO_MANDATO + " = " + year)
                .from(TBL_EMPENHOS_POR_MUNICIO + " as epm")
                .join(TBL_MUNICIPIOS + " as m ON epm." + COD_MUNICIPIO + " = m." + COD_MUNICIPIO)
                .toString();
        Fornecedor fornecedor = null;
        Map<String, Fornecedor.Partido> partidos = Maps.newHashMap();
        Map<String, Fornecedor.Municipio> municipios = Maps.newHashMap();
        try (Connection conn = this.ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int totalNumEmpenhos = 0;
            double totalValorEmpenhos = 0.0;
            while (rs.next()) {
                if (fornecedor == null) {
                    fornecedor = new Fornecedor();
                    fornecedor.cpfCnpj = rs.getString(CPF_CNPJ);
                    fornecedor.nome = rs.getString(NOME_FORNECEDOR);
                }
                int numEmpenhos = rs.getInt(TOTAL_EMPENHOS);
                double valorEmpenhos = rs.getDouble(TOTAL_VALOR_EMPENHOS);
                totalNumEmpenhos += numEmpenhos;
                totalValorEmpenhos += valorEmpenhos;

                // Informação sobre partidos relacionados a um certo fornecedor.
                String siglaPartido = rs.getString(SIGLA_PARTIDO);
                Fornecedor.Partido partido = partidos.get(siglaPartido);
                if (partido == null) {
                    partido = new Fornecedor.Partido();
                    partido.sigla = siglaPartido;
                    partidos.put(siglaPartido, partido);
                }
                partido.valorEmpenhos += valorEmpenhos;
                partido.numEmpenhos += numEmpenhos;

                // Informações sobre municípios ligados a um certo fornecedor.
                String codMunicipio = rs.getString(COD_MUNICIPIO);
                Fornecedor.Municipio municipio = municipios.get(codMunicipio);
                if (municipio == null) {
                    municipio = new Fornecedor.Municipio();
                    municipio.codMunicipio = codMunicipio;
                    municipio.siglaPartido = siglaPartido;
                    municipio.nomeMunicipio = rs.getString(NOME_MUNICIPIO);
                    municipios.put(codMunicipio, municipio);
                }
                municipio.valorEmpenhos += valorEmpenhos;
                municipio.numEmpenhos += numEmpenhos;
            }
            if (fornecedor != null) {
                fornecedor.totalNumEmpenhos = totalNumEmpenhos;
                fornecedor.totalValorEmpenhos = totalValorEmpenhos;
                switch (rankingFunction) {
                    case RANKING_FUNCTION_QTD_EMPENHOS:
                        fornecedor.partidos = partidos.values().stream()
                                .sorted((p1, p2) -> Integer.compare(p2.numEmpenhos, p1.numEmpenhos))
                                .collect(Collectors.toList());
                        fornecedor.municipios = municipios.values().stream()
                                .sorted((m1, m2) -> Integer.compare(m2.numEmpenhos, m1.numEmpenhos))
                                .collect(Collectors.toList());
                        break;
                    case RANKING_FUNCTION_VALOR_EMPENHOS:
                        fornecedor.partidos = partidos.values().stream()
                                .sorted((p1, p2) -> Double.compare(p2.valorEmpenhos, p1.valorEmpenhos))
                                .collect(Collectors.toList());
                        fornecedor.municipios = municipios.values().stream()
                                .sorted((m1, m2) -> Double.compare(m2.valorEmpenhos, m1.valorEmpenhos))
                                .collect(Collectors.toList());
                        break;
                }
            }
        }
        return Results.json(fornecedor);
    }

    @Path("ranked/fornecedores/:year/:rankingFunction")
    @GET
    public Result list(int year, int rankingFunction) throws SQLException {
        String sql = "";
        switch (rankingFunction) {
            case RANKING_FUNCTION_QTD_EMPENHOS:
                sql = new SelectBuilder()
                        .column(CPF_CNPJ, true /* group by */)
                        .column(NOME_FORNECEDOR, true /* group by */)
                        .column(COD_MUNICIPIO_FORNECEDOR, true /* group by */)
                        .column("SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS)
                        .column("SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS)
                        .from(TBL_EMPENHOS_POR_MUNICIO)
                        .where(ANO_MANDATO + " = " + year)
                        .orderBy(TOTAL_EMPENHOS, false /* descending */)
                        .toString() +
                        " LIMIT " + LIMIT;
                break;
            case RANKING_FUNCTION_VALOR_EMPENHOS:
                sql = new SelectBuilder()
                        .column(CPF_CNPJ, true /* group by */)
                        .column(NOME_FORNECEDOR, true /* group by */)
                        .column(COD_MUNICIPIO_FORNECEDOR, true /* group by */)
                        .column("SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS)
                        .column("SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS)
                        .from(TBL_EMPENHOS_POR_MUNICIO)
                        .where(ANO_MANDATO + " = " + year)
                        .orderBy(TOTAL_VALOR_EMPENHOS, false /* descending */)
                        .toString() +
                        " LIMIT " + LIMIT;

                break;
            default:
                throw new IllegalArgumentException("Invalid ranking function: " + rankingFunction);
        }
        List<Fornecedor> results = Lists.newArrayListWithCapacity(LIMIT);
        try (Connection conn = this.ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Fornecedor fornecedor = new Fornecedor();
                fornecedor.cpfCnpj = rs.getString(CPF_CNPJ);
                fornecedor.nome = rs.getString(NOME_FORNECEDOR);
                fornecedor.totalNumEmpenhos = rs.getInt(TOTAL_EMPENHOS);
                fornecedor.totalValorEmpenhos = rs.getDouble(TOTAL_VALOR_EMPENHOS);
                fornecedor.codMunicipio = rs.getString(COD_MUNICIPIO_FORNECEDOR);
                results.add(fornecedor);
            }
        }
        return Results.json(results);
    }

    @Path("ranked/fornecedores/:year/:rankingFunction")
    @GET
    public Result listFornecedores(int year, int rankingFunction) throws SQLException {
        String sql = "";
        switch (rankingFunction) {
            case RANKING_FUNCTION_QTD_EMPENHOS:
                sql = new SelectBuilder()
                        .column(CPF_CNPJ, true /* group by */)
                        .column("MIN(" + NOME_FORNECEDOR + ") AS " + NOME_FORNECEDOR)
                        .column("MIN(" + COD_MUNICIPIO_FORNECEDOR + ") AS " + COD_MUNICIPIO_FORNECEDOR)
                        .column("SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS)
                        .column("SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS)
                        .from(TBL_EMPENHOS_POR_MUNICIO)
                        .where(ANO_MANDATO + " = " + year)
                        .orderBy(TOTAL_EMPENHOS, false /* descending */)
                        .toString() +
                        " LIMIT " + LIMIT;
                break;
            case RANKING_FUNCTION_VALOR_EMPENHOS:
                sql = new SelectBuilder()
                        .column(CPF_CNPJ, true /* group by */)
                        .column("MIN(" + NOME_FORNECEDOR + ") AS " + NOME_FORNECEDOR)
                        .column("MIN(" + COD_MUNICIPIO_FORNECEDOR + ") AS " + COD_MUNICIPIO_FORNECEDOR)
                        .column("SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS)
                        .column("SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS)
                        .from(TBL_EMPENHOS_POR_MUNICIO)
                        .where(ANO_MANDATO + " = " + year)
                        .orderBy(TOTAL_VALOR_EMPENHOS, false /* descending */)
                        .toString() +
                        " LIMIT " + LIMIT;

                break;
            default:
                throw new IllegalArgumentException("Invalid ranking function: " + rankingFunction);
        }
        List<Fornecedor> results = listFornecedoresFromQuery(sql);
        return Results.json(results);
    }

    @Path("ranked/fornecedores/:year/:rankingFunction/:codMunicipio")
    @GET
    public Result listFornecedoresMunicipio(int year, int rankingFunction, String codMunicipio) throws SQLException {
        String sql = "";
        switch (rankingFunction) {
            case RANKING_FUNCTION_QTD_EMPENHOS:
                sql = new SelectBuilder()
                        .column(CPF_CNPJ, true /* group by */)
                        .column(COD_MUNICIPIO, true /* group by */)
                        .column("MIN(" + NOME_FORNECEDOR + ") AS " + NOME_FORNECEDOR)
                        .column("MIN(" + COD_MUNICIPIO_FORNECEDOR + ") AS " + COD_MUNICIPIO_FORNECEDOR)
                        .column("SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS)
                        .column("SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS)
                        .from(TBL_EMPENHOS_POR_MUNICIO)
                        .where(ANO_MANDATO + " = " + year)
                        .where(COD_MUNICIPIO + " = '" + codMunicipio + "'")
                        .orderBy(TOTAL_EMPENHOS, false /* descending */)
                        .toString();
                break;
            case RANKING_FUNCTION_VALOR_EMPENHOS:
                sql = new SelectBuilder()
                        .column(CPF_CNPJ, true /* group by */)
                        .column(COD_MUNICIPIO, true /* group by */)
                        .column("MIN(" + NOME_FORNECEDOR + ") AS " + NOME_FORNECEDOR)
                        .column("MIN(" + COD_MUNICIPIO_FORNECEDOR + ") AS " + COD_MUNICIPIO_FORNECEDOR)
                        .column("SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS)
                        .column("SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS)
                        .from(TBL_EMPENHOS_POR_MUNICIO)
                        .where(ANO_MANDATO + " = " + year)
                        .where(COD_MUNICIPIO + " = '" + codMunicipio + "'")
                        .orderBy(TOTAL_VALOR_EMPENHOS, false /* descending */)
                        .toString();

                break;
            default:
                throw new IllegalArgumentException("Invalid ranking function: " + rankingFunction);
        }
        List<Fornecedor> results = listFornecedoresFromQuery(sql);
        return Results.json(results);
    }

    private List<Fornecedor> listFornecedoresFromQuery(String sql) throws SQLException {
        List<Fornecedor> results = Lists.newArrayListWithCapacity(LIMIT);
        try (Connection conn = this.ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Fornecedor fornecedor = new Fornecedor();
                fornecedor.cpfCnpj = rs.getString(CPF_CNPJ);
                fornecedor.nome = rs.getString(NOME_FORNECEDOR);
                fornecedor.totalNumEmpenhos = rs.getInt(TOTAL_EMPENHOS);
                fornecedor.totalValorEmpenhos = rs.getDouble(TOTAL_VALOR_EMPENHOS);
                fornecedor.codMunicipio = rs.getString(COD_MUNICIPIO_FORNECEDOR);
                results.add(fornecedor);
            }
        }
        return results;
    }
}
