package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

/**
 * The type Background poller.
 */
public class BackgroundPoller {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
                        try {
                            int serviceId = Integer.parseInt(service.getValue(4).toString());
                            String serviceURL = service.getValue(0).toString();
                            int response = getResponseCodeFromUrl(serviceURL);
                            connector.updateServiceStatusResponse(serviceId, response);
                        } catch (NumberFormatException e) {
                            Future.failedFuture("Invalid Service ID.");
                        }
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
        } catch (UnknownHostException h) {
            logger.error("Host unknown for the given URL. " + url + " " + h);
            code = -1;
        } catch (SocketTimeoutException to) {
            logger.error("Timeout for the given URL. " + url + " " + to);
        } catch (IOException e) {
            logger.error("Could not connect to the given URL. " + url + " " + e);
        }
        return code;
    }
}
