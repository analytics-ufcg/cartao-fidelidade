package br.edu.ufcg.analytics;

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
					case "dev":
						stmt.executeUpdate(
								"CREATE TABLE EMPENHOS_POR_MUNICIPIO (" +
								"nu_CPFCNPJ VARCHAR NOT NULL," +
								"nome_fornecedor VARCHAR NOT NULL," +
								"codigo_municipio_fornecedor VARCHAR NOT NULL," +
								"cod_municipio VARCHAR NOT NULL," +
								"ano_eleicao INT NOT NULL,"+
								"qt_Empenhos INT NOT NULL," +
								"vl_Empenhos FLOAT NOT NULL," +
								"sigla_partido VARCHAR NOT NULL) " +
								"AS SELECT * FROM CSVREAD('dados/empenhos_por_municipio.csv');");
						stmt.executeUpdate(
								"CREATE TABLE MUNICIPIOS (" +
								"COD_MUNICIPIO INT NOT NULL CONSTRAINT cod_pk PRIMARY KEY," +
								"NOME_MUNICIPIO VARCHAR NOT NULL)" +
								"AS SELECT * FROM CSVREAD('dados/dados_municipios.csv');");
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}

		});
	}
}
