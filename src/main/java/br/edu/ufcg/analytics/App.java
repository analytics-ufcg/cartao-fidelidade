package br.edu.ufcg.analytics;

import org.jooby.Jooby;

/**
 * @author jooby generator
 */
public class App extends Jooby {

  {
    get("/", () -> "Hello World!");
  }

  public static void main(final String[] args) throws Throwable {
    run(App::new, args);
  }

}
