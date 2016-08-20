package br.edu.ufcg.analytics;

import javax.sql.DataSource;

import org.jooby.Jooby;
import org.jooby.jdbc.Jdbc;
import org.jooby.json.Jackson;

/**
 * Main entry point.
 */
public class App extends Jooby {

  // Routes.
  {
	// Making JSON no-brainer.
	use(new Jackson());

	// Frontend assets.
	assets("/", "index.html");
	assets("/robots.txt", "robots.txt");
	assets("/favicon.ico", "favicon.ico");
	assets("/404.html", "404.html");
	assets("/bower_components/**");
	assets("/images/**");
	assets("/scripts/**");
	assets("/styles/**");
	
	// Database
	use(new Jdbc("db"));

	// API routes.
	use(FornecedorHandler.class);
	
	lifeCycle(DatabaseLoader.class);
  }

  public static void main(final String[] args) throws Throwable {
    run(App::new, args);
  }
}
