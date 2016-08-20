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
			stmt.executeUpdate("CREATE TABLE LICITACOES AS SELECT * FROM CSVREAD('public/db/licitacao_empresa_partido.csv');");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
