package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import se.kry.codetest.utils.Constants;
import se.kry.codetest.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An extension from {@link AbstractVerticle}
 */
public class MainVerticle extends AbstractVerticle {

    private DBConnector connector;
    private final BackgroundPoller poller = new BackgroundPoller();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start(Promise<Void> startPromise) {
        connector = new DBConnector(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        vertx.setPeriodic(Constants.REFRESH_DEFAULT_TIME, timerId -> {
                    poller.pollServices(connector.getServices(), vertx).onComplete(ar -> {
                        if (ar.succeeded()) {
                            List<JsonObject> jsonObjects = parseToJson(ar.result().getResults());
                            vertx.eventBus().publish(Constants.PAGE_UPDATED, jsonObjects.toString());
                        }
                    });
                }
        );
        setRoutes(router);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(Constants.DEFAULT_PORT, ar -> {
                    if (ar.succeeded()) {
                        logger.info("KRY code test service started");
                        startPromise.complete();
                    } else {
                        logger.error("KRY code test service could not be started ", ar.cause());
                        startPromise.fail(ar.cause());
                    }
                });
    }

    private List<JsonObject> parseToJson(List<JsonArray> results) {
        List<JsonObject> jsonObjects = new ArrayList<>();
        for (JsonArray result : results) {
            JsonObject json = new JsonObject();
            json.put(Constants.URL, result.getValue(0));
            json.put(Constants.STATUS_RESPONSE, result.getValue(1));
            json.put(Constants.NAME, result.getValue(2));
            json.put(Constants.CREATION_DATE, result.getValue(3));
            json.put(Constants.ID, result.getValue(4));
            json.put(Constants.USER_COOKIE_ID, result.getValue(5));
            jsonObjects.add(json);
        }
        return jsonObjects;
    }

    private void setRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());

        setListServicesRouter(router);
        setAddServiceRouter(router);
        setDeleteServiceRouter(router);
        setUpdateServiceRouter(router);
        setEventBusRouter(router);
    }

    private void setEventBusRouter(Router router) {
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        SockJSBridgeOptions options = new SockJSBridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("app.markdown"))
                .addOutboundPermitted(new PermittedOptions().setAddress("page.updated"));
        sockJSHandler.bridge(options);
        router.route("/eventbus/*").handler(sockJSHandler);
    }

    private void setListServicesRouter(Router router) {
        router.get("/service").handler(req -> {

            Cookie cookie = req.getCookie(Constants.COOKIE_ID);
            connector.getServices(cookie.getValue()).onComplete(ar -> {
                if (ar.succeeded()) {
                    List<JsonArray> results = ar.result().getResults();
                    if (!results.isEmpty()) {
                        List<JsonObject> jsonServices = ar.result().getResults().stream()
                                .map(service ->
                                        new JsonObject()
                                                .put(Constants.URL, service.getValue(0))
                                                .put(Constants.STATUS_RESPONSE, service.getValue(1))
                                                .put(Constants.NAME, service.getValue(2))
                                                .put(Constants.CREATION_DATE, service.getValue(3))
                                                .put(Constants.ID, service.getValue(4))
                                                .put(Constants.USER_COOKIE_ID, service.getValue(5)))
                                .collect(Collectors.toList());

                        req.response()
                                .setStatusCode(200)
                                .putHeader("content-type", "application/json")
                                .end(new JsonArray(jsonServices).encode());
                    }
                } else {
                    req.fail(500);
                }
            });
        });
    }

    private void setAddServiceRouter(Router router) {
        router.post("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();

            if (!Utils.isValidUrl(jsonBody.getString(Constants.URL))) {
                logger.error("Invalid URL");
                req.fail(401);
                return;
            }
            Cookie cookie = req.getCookie(Constants.COOKIE_ID);
            String value;
            if (cookie == null || cookie.getValue() == null) {
                value = Utils.generateUniqueUserCookieId();
                req.addCookie(Cookie.cookie(Constants.COOKIE_ID, value));
            } else {
                value = cookie.getValue();
            }

            jsonBody.put(Constants.USER_COOKIE_ID, value);
            jsonBody.put(Constants.CREATION_DATE, Utils.getNow());
            connector.addService(jsonBody);
            req.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "text/plain")
                    .end("CREATED");
        });
    }

    private void setDeleteServiceRouter(Router router) {
        router.delete("/remove/:id").handler(req -> {
            if (!req.request().getParam("id").isEmpty()) {
                String id = req.request().getParam("id");
                connector.deleteService(id);
                req.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "text/plain")
                        .end("OK");
            } else {
                logger.error("Invalid Service ID.");
                req.fail(404);
            }
        });
    }

    private void setUpdateServiceRouter(Router router) {
        router.put("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();

            try {
                int serviceId = Integer.parseInt(jsonBody.getString(Constants.ID));
                if (!Utils.isValidUrl(jsonBody.getString(Constants.URL))) {
                    logger.error("Not allowed.");
                    req.fail(405);
                    return;
                }

                connector.updateService(serviceId, jsonBody);
                req.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "text/plain")
                        .end(new JsonArray(Collections.singletonList(jsonBody)).encode());
            } catch (ClassCastException ex) {
                logger.error("Invalid Service ID.");
                req.fail(500);
            } catch (NumberFormatException e) {
                logger.error("Invalid Service ID.");
                req.fail(404);
            }
        });
    }
}
