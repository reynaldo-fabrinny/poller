package se.kry.codetest;

import io.vertx.core.Vertx;

/**
 * Starts the application
 */
public class Start {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new MainVerticle());
    }
}
