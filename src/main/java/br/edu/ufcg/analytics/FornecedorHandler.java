package br.edu.ufcg.analytics;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.Consumes;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

import com.google.inject.Inject;

/**
 * All Handlers related to Fornecedor.
 */
@Path("/fornecedores")
@Consumes("application/json")
@Produces("application/json")
public class FornecedorHandler {
	private DataSource ds;

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
		if (!parts[0].equals("id") && !parts[0].equals("cnpj") && !parts[0].equals("cpf")) {
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
		case "id":
			f.id = value;
			break;
		case "cnpj":
			f.id = Long.toString(new Random().nextLong());
			f.cnpjCpf = value;
			break;
		case "cpf":
			f.id = Long.toString(new Random().nextLong());
			f.cnpjCpf = value;
			break;
		default:
			Results.noContent();
			break;
		}
		return Results.json(f);
	}

	@GET
	public Result list() {
		String[] names = new String[] { "Super Dragon Ball Z", "Pokemon Go", "Oh My!", "Ghostbusters", "Blablabla" };
		List<Fornecedor> returned = new LinkedList<>();
		for (int i = 0; i < names.length; i++) {
			Fornecedor f = new Fornecedor();
			f.id = UUID.randomUUID().toString();
			f.cnpjCpf = UUID.randomUUID().toString();
			f.nome = names[i];
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
			returned.add(f);
		}
		return Results.json(returned);
	}
}