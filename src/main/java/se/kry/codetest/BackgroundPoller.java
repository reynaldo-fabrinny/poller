package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * The type Background poller.
 */
public class BackgroundPoller {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Poll services future.
     *
     * @param connector the connector
     * @return the future
     */
    public Future<List<String>> pollServices(DBConnector connector) {
        connector.getServices().onComplete(ar -> {
            if (ar.succeeded()) {
                List<JsonArray> results = ar.result().getResults();
                if (!results.isEmpty()) {
                    ar.result().getResults().forEach(service -> {
                        String serviceURL = service.getValue(0).toString();
                        String serviceId = service.getValue(4).toString();

                        int response = getResponseCodeFromUrl(serviceURL);
                        connector.updateServiceStatusResponse(serviceId, response);
                    });
                }
            } else {
                logger.error("Could not poll the services.");
            }
        });
        return Future.succeededFuture();
    }

    private int getResponseCodeFromUrl(String url) {
        int code = 0;
        try {
            URL serviceUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) serviceUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.connect();

            code = connection.getResponseCode();
            connection.disconnect();
        } catch (IOException e) {
            logger.error("Could not connect to the given URL. " + e);
        }
        return code;
    }
}
