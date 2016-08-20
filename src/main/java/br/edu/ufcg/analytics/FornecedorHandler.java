package br.edu.ufcg.analytics;

import java.util.Random;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.Consumes;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

/**
 * All Handlers related to Fornecedor.
 */
@Path("/fornecedor")
@Consumes("application/json")
@Produces("application/json")
public class FornecedorHandler {
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
		Fornecedor f = new Fornecedor();
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
}
