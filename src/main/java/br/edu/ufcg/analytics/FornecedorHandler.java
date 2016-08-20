package br.edu.ufcg.analytics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.Consumes;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * All Handlers related to Fornecedor.
 */
@Path("/fornecedores")
@Consumes("application/json")
@Produces("application/json")
public class FornecedorHandler {
	private DataSource ds;
	private static final String CNPJCPF = "NU_CPFCNPJ";
	private static final String PARTIDO = "SIGLA_PARTIDO";
	private static final String QTD_LICITACOES = "QTD_LICITACOES";
	private static final String ANO_INICIAL = "ANO_INICIAL";
	private static final String ANO_FINAL = "ANO_FINAL";

	private static final String REPLACE_CNPJS_CPFS = "{TO_REPLACE_CNPJS_CPFS}";
	private static final String SELECT_TOP = "SELECT NU_CPFCNPJ AS QTD_LICITACOES, MIN(DT_ANO) AS ANO_INICIAL, MAX(DT_ANO) AS ANO_FINAL "
			+ "FROM LICITACOES "
			+ "WHERE NU_CPFCNPJ <> 'NA' GROUP BY NU_CPFCNPJ ORDER BY COUNT(NU_LICITACAO)  DESC  "
			+ "LIMIT 50";
	private static final String LIST_SQL = ""
			+ "SELECT NU_CPFCNPJ, SIGLA_PARTIDO, COUNT(NU_LICITACAO) AS QTD_LICITACOES "
			+ "FROM LICITACOES "
			+ "WHERE NU_CPFCNPJ IN " + REPLACE_CNPJS_CPFS
			+ "GROUP BY NU_CPFCNPJ,SIGLA_PARTIDO;";

	@Inject
	public FornecedorHandler(DataSource ds) {
		this.ds = ds;
	}

	@Path("/:q")
	@GET
	public Result get(String q) {
		String[] parts = q.split(":");
		if (parts.length != 2) {
			return Results.noContent();
		}
		if (!parts[0].equals("cnpj") && !parts[0].equals("cpf")) {
			return Results.noContent();
		}
		String type = parts[0];
		String value = parts[1];

		// Creating a dummy Fornecedor.
		// TODO: Remove and fetch data from database.
		Fornecedor f = new Fornecedor();
		f.nome = "Super Dragon Ball Z";
		f.atividadeEconomica = "Topa Tudo";
		f.anoInicial = 2008;
		f.anoFinal = 2016;
		f.qtdLicitacoes = (new Random()).nextInt(1000);
		f.valorTotal = (new Random()).nextDouble();
		List<Fidelidade> fidelidade = new LinkedList<>();
		fidelidade.add(new Fidelidade("PMDB", (new Random()).nextInt(1000), (new Random()).nextInt(1000),
				(new Random()).nextDouble()));
		fidelidade.add(new Fidelidade("PT", (new Random()).nextInt(1000), (new Random()).nextInt(1000),
				(new Random()).nextDouble()));
		fidelidade.add(new Fidelidade("PSDB", (new Random()).nextInt(1000), (new Random()).nextInt(1000),
				(new Random()).nextDouble()));
		f.fidelidade = fidelidade;
		switch (type) {
		case "cnpj":
			f.cnpjCpf = value;
			break;
		case "cpf":
			f.cnpjCpf = value;
			break;
		default:
			Results.noContent();
			break;
		}
		return Results.json(f);
	}

	@GET
	public Result list() throws SQLException {		
		Map<String, Fornecedor> results = Maps.newHashMap();
		try (Connection conn = this.ds.getConnection();
				 PreparedStatement stmt = conn.prepareStatement(SELECT_TOP);
				 ResultSet rs = stmt.executeQuery()) {
			while(rs.next()) {
				String cnpjCpf = rs.getString(CNPJCPF);
				Fornecedor fornecedor = results.get(cnpjCpf);
				if (fornecedor == null) {
					fornecedor = new Fornecedor();
					fornecedor.cnpjCpf = cnpjCpf;
					fornecedor.anoInicial = rs.getInt(ANO_INICIAL);
					fornecedor.anoFinal = rs.getInt(ANO_FINAL);

				}
				results.put(cnpjCpf, fornecedor);
			}
		}
		
		try (Connection conn = this.ds.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(LIST_SQL.replace(REPLACE_CNPJS_CPFS, "(" + String.join(",", results.keySet()) + ")"));
			 ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				String cnpjCpf = rs.getString(CNPJCPF);
				Fornecedor fornecedor = results.get(cnpjCpf);
				if (fornecedor.fidelidade == null) {
					fornecedor.fidelidade = Lists.newLinkedList();
				}
				fornecedor.fidelidade.add(
						new Fidelidade(
								rs.getString(PARTIDO),
								rs.getInt(QTD_LICITACOES),
								rs.getInt(QTD_LICITACOES),
								0));  // TODO(danielfireman): get real value when we know what to get.
			}
		}
		return Results.json(results.entrySet());
	}
}