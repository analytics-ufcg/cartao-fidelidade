package br.edu.ufcg.analytics;

import org.jooby.Jooby;

/**
 * Main entry point.
 */
public class App extends Jooby {

  // Routes.
  {
	// Frontend assets.
	assets("/", "index.html");
	assets("/robots.txt", "robots.txt");
	assets("/favicon.ico", "favicon.ico");
	assets("/404.html", "404.html");
	assets("/bower_components/**");
	assets("/images/**");
	assets("/scripts/**");
	assets("/styles/**");
  }

  public static void main(final String[] args) throws Throwable {
    run(App::new, args);
  }
}