package br.edu.ufcg.analytics;

import java.sql.Connection;
import java.sql.Statement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DatabaseLoader {
	private DataSource ds;

	@Inject
	public DatabaseLoader(DataSource ds) {
		this.ds = ds;
	}

	@PostConstruct
	public void start() {
		try (Connection conn = this.ds.getConnection(); Statement stmt = conn.createStatement()) {
			conn.setAutoCommit(true);
			stmt.executeUpdate("CREATE TABLE EMPENHOS_POR_MUNICIPIO (" +
					"nu_CPFCNPJ VARCHAR NOT NULL," +
					"nome_fornecedor VARCHAR NOT NULL," +
					"nome_municipio VARCHAR NOT NULL," +
					"ano_eleicao INT NOT NULL,"+
					"qt_Empenhos INT NOT NULL," +
					"vl_Empenhos FLOAT NOT NULL," +
					"nome_candidato VARCHAR NOT NULL," +
					"sigla_partido VARCHAR NOT NULL) " +
			        "AS SELECT * FROM CSVREAD('public/db/empenhos_por_municipio.csv');");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
