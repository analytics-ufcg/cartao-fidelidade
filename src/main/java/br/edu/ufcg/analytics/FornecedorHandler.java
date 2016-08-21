package br.edu.ufcg.analytics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.Consumes;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

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
	private static final String NOME_MUNICIPIO = "NOME_MUNICIPIO";
	private static final String ANO_MANDATO = "ANO_ELEICAO";
	private static final String QTD_EMPENHOS = "QT_EMPENHOS";
	private static final String VALOR_EMPENHOS = "VL_EMPENHOS";
	private static final String SIGLA_PARTIDO = "SIGLA_PARTIDO";
	private static final String TOTAL_EMPENHOS = "TOTAL_EMPENHOS";
	private static final String TOTAL_VALOR_EMPENHOS = "TOTAL_VALOR_EMPENHOS";
	private static final int LIMIT = 10;
	private static final int RANKING_FUNCTION_QTD_EMPENHOS = 1;
	private static final int RANKING_FUNCTION_VALOR_EMPENHOS = 2;

	@Inject
	public FornecedorHandler(DataSource ds) {
		this.ds = ds;
	}

	@Path("fornecedores/:id/:year/:rankingFunction")
	@GET
	public Result get(String id, int rankingFunction, int year) throws SQLException {
		String sql = "";
		switch (rankingFunction) {
		case RANKING_FUNCTION_QTD_EMPENHOS:
			sql = "SELECT " + 
					CPF_CNPJ + "," +
					NOME_FORNECEDOR + "," +
					NOME_MUNICIPIO + "," +
					SIGLA_PARTIDO + "," +
				  	"SUM(" + QTD_EMPENHOS + ") AS VALOR," +
				  	"SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS + "," +
				  	"SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS +
				  " FROM " + TBL_EMPENHOS_POR_MUNICIO +
				  " WHERE " + CPF_CNPJ + " = '" + id + "'" +
				  " GROUP BY " + NOME_MUNICIPIO + "," + SIGLA_PARTIDO;
			break;
		case RANKING_FUNCTION_VALOR_EMPENHOS:
			sql = "SELECT " + 
					CPF_CNPJ + "," +
					NOME_FORNECEDOR + "," +
					NOME_MUNICIPIO + "," +
					SIGLA_PARTIDO + "," +
				  	"SUM(" + VALOR_EMPENHOS + ") AS VALOR," +
				  	"SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS + "," +
				  	"SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS +
				  " FROM " + TBL_EMPENHOS_POR_MUNICIO +
				  " WHERE " + CPF_CNPJ + " = '" + id + "'" +
				  " GROUP BY " + NOME_MUNICIPIO + "," + SIGLA_PARTIDO;
			break;

		default:
			throw new IllegalArgumentException("Invalid ranking function: " + rankingFunction);
		}
		Fornecedor fornecedor = null;
		List<Fidelidade> fidelidades = Lists.newLinkedList();
		try (Connection conn = this.ds.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			while(rs.next()) {
				if (fornecedor == null) {
					fornecedor = new Fornecedor();
					fornecedor.cpfCnpj = rs.getString(CPF_CNPJ);
					fornecedor.nome = rs.getString(NOME_FORNECEDOR);
					fornecedor.numEmpenhos = rs.getInt(TOTAL_EMPENHOS);	
					fornecedor.valorEmpenhos = rs.getDouble(TOTAL_VALOR_EMPENHOS);
				}
				Fidelidade fidelidade = new Fidelidade();
				fidelidade.municipio = rs.getString(NOME_MUNICIPIO).toLowerCase().replace(" ", "");
				fidelidade.valor = rs.getDouble("VALOR");
				fidelidade.siglaPartido = rs.getString(SIGLA_PARTIDO);
				fidelidades.add(fidelidade);
			}
			if (fornecedor != null) {
				fornecedor.fidelidade = fidelidades;
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
			sql = "SELECT " + 
					CPF_CNPJ + "," +
					NOME_FORNECEDOR + "," +
				  	"SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS + "," +
				  	"SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS +
				  " FROM " + TBL_EMPENHOS_POR_MUNICIO +
				  " WHERE " + ANO_MANDATO + " = " + year +
				  " GROUP BY " + CPF_CNPJ + "," + NOME_FORNECEDOR +
				  " ORDER BY " + TOTAL_EMPENHOS + " DESC" +
				  " LIMIT " + LIMIT;
			break;
		case RANKING_FUNCTION_VALOR_EMPENHOS:
			sql = "SELECT " + 
					CPF_CNPJ + "," +
					NOME_FORNECEDOR + "," +
				  	"SUM(" + QTD_EMPENHOS + ") AS " + TOTAL_EMPENHOS + "," +
				  	"SUM(" + VALOR_EMPENHOS + ") AS " + TOTAL_VALOR_EMPENHOS +
				  " FROM " + TBL_EMPENHOS_POR_MUNICIO +
				  " WHERE " + ANO_MANDATO + " = " + year +
				  " GROUP BY " + CPF_CNPJ + "," + NOME_FORNECEDOR +
				  " ORDER BY " + TOTAL_VALOR_EMPENHOS + " DESC" +
				  " LIMIT " + LIMIT;
			break;
		default:
			throw new IllegalArgumentException("Invalid ranking function: " + rankingFunction);
		}
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
				results.add(fornecedor);
			}
		}
		return Results.json(results);
	}
}