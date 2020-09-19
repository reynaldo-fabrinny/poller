package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

/**
 * Migration job used to generate the poller.db and the services table.
 */
public class DBMigration {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DBConnector connector = new DBConnector(vertx);
        connector.query("CREATE TABLE IF NOT EXISTS services (" +
                "id int NOT NULL AUTO_INCREMENT," +
                "url VARCHAR(128) NOT NULL UNIQUE, " +
                "name VARCHAR(128)," +
                "status_response TINYINT(1)," +
                "creation_date DATETIME2(3)," +
                "last_modified DATETIME2(3))").onComplete(done -> {
            if (done.succeeded()) {
                System.out.println("completed db migrations");
            } else {
                done.cause().printStackTrace();
            }
            vertx.close(shutdown -> {
                System.exit(0);
            });
        });
    }
}
