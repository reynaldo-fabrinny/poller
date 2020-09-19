package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.utils.Constants;
import se.kry.codetest.utils.Utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

    private HashMap<String, String> services = new HashMap<>();

    private DBConnector connector;
    private BackgroundPoller poller = new BackgroundPoller();

    @Override
    public void start(Future<Void> startFuture) {
        connector = new DBConnector(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(services));
        setRoutes(router);

        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8081, result -> {
                    if (result.succeeded()) {
                        System.out.println("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private void setRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());
        router.get("/service").handler(req -> {

            connector.getServices().onComplete(result -> {
                if (result.succeeded()) {
                    List<JsonArray> results = result.result().getResults();
                    if (!results.isEmpty()) {
                        List<JsonObject> jsonServices = result.result().getResults().stream()
                                .map(service ->
                                        new JsonObject()
                                                .put(Constants.URL, service.getValue(0))
                                                .put(Constants.STATUS_RESPONSE, service.getValue(1))
                                                .put(Constants.NAME, service.getValue(2))
                                                .put(Constants.CREATION_DATE, service.getValue(3)))
                                .collect(Collectors.toList());

                        req.response()
                                .putHeader("content-type", "application/json")
                                .end(new JsonArray(jsonServices).encode());
                    }
                }
            });
        });

        router.post("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();
            jsonBody.put(Constants.CREATION_DATE, Utils.getNow());
            connector.addService(jsonBody);

            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("OK");
        });
    }

}
