package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import se.kry.codetest.utils.Constants;

/**
 * Database Layer responsible for all the interactions with the Database.
 */
public class DBConnector {

    private final String DB_PATH = "poller.db";
    private final SQLClient client;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new Db connector.
     *
     * @param vertx the vertx
     */
    public DBConnector(Vertx vertx) {
        JsonObject config = new JsonObject()
                .put("url", "jdbc:sqlite:" + DB_PATH)
                .put("driver_class", "org.sqlite.JDBC")
                .put("max_pool_size", 30);
        client = JDBCClient.createShared(vertx, config);
    }

    /**
     * Gets a list with all the services and it status.
     *
     * @return the services urls and its status.
     */
    public Future<ResultSet> getServices() {
        Future<ResultSet> queryResultFuture = Future.future();

        client.query("SELECT " + Constants.URL + ", " +
                Constants.STATUS_RESPONSE + ", " +
                Constants.NAME + ", " +
                Constants.CREATION_DATE + ", " +
                Constants.ID + " FROM " + Constants.DATA_BASE_NAME, response -> {
            if (response.succeeded()) {
                queryResultFuture.complete(response.result());
            } else
                queryResultFuture.fail(response.cause());
        });
        return queryResultFuture;
    }

    /**
     * Adds a new Service to the Database.
     *
     * @param service the service
     */
    public void addService(JsonObject service) {
        client.update("INSERT INTO " + Constants.DATA_BASE_NAME + " (" +
                        Constants.NAME + "," +
                        Constants.URL + "," +
                        Constants.CREATION_DATE + ") VALUES ('" +
                        service.getString(Constants.NAME) + "', '" +
                        service.getString(Constants.URL) + "', '" +
                        service.getString(Constants.CREATION_DATE) + "');",
                ar -> {
                    if (ar.failed()) {
                        logger.error("Was not possible to insert the service. " + ar.cause());
                    }
                });
    }

    /**
     * Deletes a service by its ID.
     *
     * @param id the id
     */
    public void deleteService(String id) {
        client.update("DELETE FROM " + Constants.DATA_BASE_NAME + " WHERE id = '" + id + "'",
                ar -> {
                    if (ar.failed()) {
                        logger.error("Was not possible to delete the service with the following id: " + id + " " +
                                ar.cause());
                    }
                });
    }

    /**
     * Updates the Response Status from the Specified Service.
     *
     * @param id              the id
     * @param status_response the status response
     */
    public void updateServiceStatusResponse(String id, int status_response) {
        client.update("UPDATE " + Constants.DATA_BASE_NAME + " SET " + Constants.STATUS_RESPONSE + " = " + status_response +
                        " WHERE " + Constants.ID + " = " + id,
                ar -> {
                    if (ar.failed()) {
                        logger.error("Was not possible to update the status response from the service " +
                                "with the following id: " + id + " " +
                                ar.cause());
                    }
                });
    }

    /**
     * Query future.
     *
     * @param query the query
     * @return the future
     */
    public Future<ResultSet> query(String query) {
        return query(query, new JsonArray());
    }

    /**
     * Query future.
     *
     * @param query  the query
     * @param params the params
     * @return the future
     */
    public Future<ResultSet> query(String query, JsonArray params) {
        if (query == null || query.isEmpty()) {
            return Future.failedFuture("Query is null or empty");
        }
        if (!query.endsWith(";")) {
            query = query + ";";
        }

        Future<ResultSet> queryResultFuture = Future.future();

        client.queryWithParams(query, params, result -> {
            if (result.failed()) {
                queryResultFuture.fail(result.cause());
            } else {
                queryResultFuture.complete(result.result());
            }
        });
        return queryResultFuture;
    }
}
