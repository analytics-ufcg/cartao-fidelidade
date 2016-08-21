package br.edu.ufcg.analytics;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.jooby.Env;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DatabaseLoader {
	private DataSource ds;
	private ExecutorService executor;
	private Env env;

	@Inject
	public DatabaseLoader(DataSource ds, ExecutorService executor, Env env) {
		this.ds = ds;
		this.executor = executor;
		this.env = env;
	}

	@PostConstruct
	public void start() {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
					conn.setAutoCommit(true);
					switch (env.name()) {
					case "prod":
						stmt.executeUpdate(loadProd());
						break;
					default:
						stmt.executeUpdate(loadDev());
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}

		});
	}

	private String loadProd() {

		return "DROP TABLE IF EXISTS EMPENHOS_POR_MUNICIPIO; " + 
				"CREATE TABLE EMPENHOS_POR_MUNICIPIO (" +
				"nu_CPFCNPJ VARCHAR NOT NULL," +
				"nome_fornecedor VARCHAR NOT NULL," +
				"cod_municipio VARCHAR NOT NULL," +
				"ano_eleicao INT NOT NULL,"+
				"qt_Empenhos INT NOT NULL," +
				"vl_Empenhos FLOAT NOT NULL," +
				"sigla_partido VARCHAR NOT NULL); " +
				"\\copy EMPENHOS_POR_MUNICIPIO FROM '" + new File("public/db/empenhos_por_municipio.csv").getAbsolutePath() + "' DELIMITER ',' CSV HEADER;"
				;
	}

	private String loadDev() {
		return "CREATE TABLE EMPENHOS_POR_MUNICIPIO (" +
				"nu_CPFCNPJ VARCHAR NOT NULL," +
				"nome_fornecedor VARCHAR NOT NULL," +
				"cod_municipio VARCHAR NOT NULL," +
				"ano_eleicao INT NOT NULL,"+
				"qt_Empenhos INT NOT NULL," +
				"vl_Empenhos FLOAT NOT NULL," +
				"sigla_partido VARCHAR NOT NULL) " +
				"AS SELECT * FROM CSVREAD('public/db/empenhos_por_municipio.csv');";
	}
}
