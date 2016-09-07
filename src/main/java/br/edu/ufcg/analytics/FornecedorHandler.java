package br.edu.ufcg.analytics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.Consumes;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import ca.krasnay.sqlbuilder.SelectBuilder;

/**
 * All Handlers related to Fornecedor.
 */
@Consumes("application/json")
@Produces("application/json")
public class FornecedorHandler {
	private DataSource ds;
	private static final String TBL_EMPENHOS_POR_MUNICIO = "EMPENHOS_POR_MUNICIPIO";
	private static final String CPF_CNPJ = "NU_CPFCNPJ";
	private static final String NOME_FORNECEDOR = "NOME_FORNECEDOR";
	private static final String COD_MUNICIPIO = "COD_MUNICIPIO";
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

	@Inject
	public FornecedorHandler(DataSource ds) {
		this.ds = ds;
	}

	@Path("fornecedores/:state/:id/:year/:rankingFunction")
	@GET
	public Result getPorEstado(String state, String id, int rankingFunction, int year) throws SQLException {
		String sql = "";
		switch (rankingFunction) {
		case RANKING_FUNCTION_QTD_EMPENHOS:
			sql = new SelectBuilder()
				.column(CPF_CNPJ, true /* group by */)
				.column(NOME_FORNECEDOR, true /* group by */)
				.column(COD_MUNICIPIO, true /* group by */)
				.column(SIGLA_PARTIDO, true /* group by */)
				.column(COD_MUNICIPIO_FORNECEDOR, true /* group by */)
				.column("SUM(" + QTD_EMPENHOS + ") AS VALOR")
				.column("SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS)
				.column("SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS)
				.where(CPF_CNPJ + " = '" + id + "' AND " + ANO_MANDATO + " = " + year)
				.from(TBL_EMPENHOS_POR_MUNICIO)
				.toString();
			break;
		case RANKING_FUNCTION_VALOR_EMPENHOS:
			sql = new SelectBuilder()
				.column(CPF_CNPJ, true /* group by */)
				.column(NOME_FORNECEDOR, true /* group by */)
				.column(COD_MUNICIPIO, true /* group by */)
				.column(SIGLA_PARTIDO, true /* group by */)
				.column(COD_MUNICIPIO_FORNECEDOR, true /* group by */)
				.column("SUM(" + VALOR_EMPENHOS + ") AS VALOR")
				.column("SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS)
				.column("SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS)
				.from(TBL_EMPENHOS_POR_MUNICIO)
				.where(CPF_CNPJ + " = '" + id + "' AND " + ANO_MANDATO + " = " + year)
				.toString();
			break;

		default:
			throw new IllegalArgumentException("Invalid ranking function: " + rankingFunction);
		}
		Fornecedor fornecedor = null;
		List<Fidelidade> fidelidades = Lists.newLinkedList();
		Map<String, Double> partidos = Maps.newHashMap();
		try (Connection conn = this.ds.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			int numEmpenhos = 0;
			double valorEmpenhos = 0.0;
			while(rs.next()) {
				if (fornecedor == null) {
					fornecedor = new Fornecedor();
					fornecedor.cpfCnpj = rs.getString(CPF_CNPJ);
					fornecedor.nome = rs.getString(NOME_FORNECEDOR);
				}
				numEmpenhos += rs.getInt(TOTAL_EMPENHOS);
				valorEmpenhos += rs.getDouble(TOTAL_VALOR_EMPENHOS);
				String partido = rs.getString(SIGLA_PARTIDO);
				double valor = rs.getDouble("VALOR");

				Fidelidade fidelidade = new Fidelidade();
				fidelidade.municipio = rs.getString(COD_MUNICIPIO);
				fidelidade.valor = valor;
				fidelidade.siglaPartido = partido;
				fidelidades.add(fidelidade);

				Double valorPorPartido = partidos.containsKey(partido) ? partidos.get(partido) : 0.0;
				partidos.put(partido, valorPorPartido + valor);
			}
			if (fornecedor != null) {
				fornecedor.fidelidade = fidelidades;
				fornecedor.numEmpenhos = numEmpenhos;
				fornecedor.valorEmpenhos = valorEmpenhos;
				fornecedor.resumoPartidos = partidos.entrySet()
						.stream()
						.sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
						.collect(Collectors.toList());
			}
		}
		return Results.json(fornecedor);
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
				.where(COD_MUNICIPIO + " = " + codMunicipio)
				.orderBy(TOTAL_EMPENHOS, false /* descending */)
				.toString() +
				" LIMIT " + LIMIT;
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
			.where(COD_MUNICIPIO + " = " + codMunicipio)
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

	private List<Fornecedor> listFornecedoresFromQuery(String sql) throws SQLException {
		List<Fornecedor> results = Lists.newArrayListWithCapacity(LIMIT);
		try (Connection conn = this.ds.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			while(rs.next()) {
				Fornecedor fornecedor = new Fornecedor();
				fornecedor.cpfCnpj = rs.getString(CPF_CNPJ);
				fornecedor.nome = rs.getString(NOME_FORNECEDOR);
				fornecedor.numEmpenhos = rs.getInt(TOTAL_EMPENHOS);
				fornecedor.valorEmpenhos = rs.getDouble(TOTAL_VALOR_EMPENHOS);
				fornecedor.codMunicipio = rs.getString(COD_MUNICIPIO_FORNECEDOR);
				results.add(fornecedor);
			}
		}
		return results;
	}
}
