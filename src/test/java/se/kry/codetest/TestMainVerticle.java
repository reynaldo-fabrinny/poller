package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.kry.codetest.utils.Constants;
import se.kry.codetest.utils.Utils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Test main verticle.
 */
@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

    /**
     * Deploy verticle.
     *
     * @param vertx       the vertx
     * @param testContext the test context
     */
    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    /**
     * Start http server.
     *
     * @param vertx       the vertx
     * @param testContext the test context
     */
    @Test
    @DisplayName("Start a web server on localhost responding to path /service on the default port " + Constants.DEFAULT_PORT)
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void start_http_server(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .get(Constants.DEFAULT_PORT, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Tests the isValidUrl method from the Utils.")
    void validateURLTest() {
        // Valid URLs
        assertTrue(Utils.isValidUrl("https://www.kry.se/"));
        assertTrue(Utils.isValidUrl("https://www.youtube.com/"));
        assertTrue(Utils.isValidUrl("https://thisisrosa.com/"));
        assertTrue(Utils.isValidUrl("https://stackoverflow.com/"));
        assertTrue(Utils.isValidUrl("https://www.google.com.br/"));

        //Invalid URLs
        assertFalse(Utils.isValidUrl("kry.se"));
        assertFalse(Utils.isValidUrl("sdgf"));
        assertFalse(Utils.isValidUrl(""));
        assertFalse(Utils.isValidUrl("HTT:/google.com"));
        assertFalse(Utils.isValidUrl("bbc.com"));
        assertFalse(Utils.isValidUrl("httsp://google.com"));
    }

    @Test
    @DisplayName("Test add new valid Service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void addServiceTest(Vertx vertx, VertxTestContext testContext) {
        JsonObject newService = new JsonObject().
                put(Constants.URL, "https://soundcloud.com/")
                .put(Constants.NAME, "Sound Cloud");

        WebClient.create(vertx)
                .post(Constants.DEFAULT_PORT, "::1", "/service")
                .sendJsonObject(newService, response -> testContext.verify(() -> {
                    assertEquals(201, response.result().statusCode());
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Test Add Invalid url valid Service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void addInvalidUrlServiceTest(Vertx vertx, VertxTestContext testContext) {
        JsonObject invalidURLService = new JsonObject().
                put(Constants.URL, "www.soundcloud.com/")
                .put(Constants.NAME, "Sound Cloud");

        WebClient.create(vertx)
                .post(Constants.DEFAULT_PORT, "::1", "/service")
                .sendJsonObject(invalidURLService, response -> testContext.verify(() -> {
                    assertEquals(401, response.result().statusCode());
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("test Service without url")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void addServiceWithoutUrlTest(Vertx vertx, VertxTestContext testContext) {
        JsonObject noUrlService = new JsonObject()
                .put(Constants.NAME, "Sound Cloud");

        WebClient.create(vertx)
                .post(Constants.DEFAULT_PORT, "::1", "/service")
                .sendJsonObject(noUrlService, response -> testContext.verify(() -> {
                    assertEquals(401, response.result().statusCode());
                    testContext.completeNow();
                }));
    }

    // TODO Test service visibility by cookie


    @Test
    @DisplayName("Test get Services")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void getServicesTest(Vertx vertx, VertxTestContext testContext) {
       /* WebClient.create(vertx)
                .get(Constants.DEFAULT_PORT, "::1", "/service")
                .send(req -> testContext.verify(() -> {
                    assertEquals(200, req.result().statusCode());
                    JsonArray services = req.result().bodyAsJsonArray();
                    JsonObject service = services.getJsonObject(0);

                    assertNotNull(service.getString(Constants.URL));
                    assertNotNull(service.getString(Constants.CREATION_DATE));
                    testContext.completeNow();
                }));*/
    }

    @Test
    @DisplayName("Test remove existing service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void removeExistingService(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .delete(Constants.DEFAULT_PORT, "::1", "/remove/" + 1)
                .send(response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Test remove with missing service ID parameter")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void removeNonExistentService(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .delete(Constants.DEFAULT_PORT, "::1", "/remove/")
                .send(response -> testContext.verify(() -> {
                    assertEquals(404, response.result().statusCode());
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Test update service ")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void updateService(Vertx vertx, VertxTestContext testContext) {

        JsonObject newService = new JsonObject().
                put(Constants.URL, "https://soundcloud.com/")
                .put(Constants.NAME, "Sound Cloud");

        WebClient.create(vertx)
                .post(Constants.DEFAULT_PORT, "::1", "/service")
                .sendJsonObject(newService, response -> testContext.verify(() -> {

                    JsonObject upService = new JsonObject()
                            .put(Constants.ID, "1")
                            .put(Constants.URL, "https://soundcloud.com/discover")
                            .put(Constants.NAME, "Sound Cloud UPDATED");

                    WebClient.create(vertx)
                            .put(Constants.DEFAULT_PORT, "::1", "/service")
                            .sendJsonObject(upService, r -> testContext.verify(() -> {

                                JsonArray bodyAsJsonArray = r.result().bodyAsJsonArray();
                                JsonObject service = bodyAsJsonArray.getJsonObject(0);
                                assertEquals("https://soundcloud.com/discover", service.getString(Constants.URL));
                                assertEquals("Sound Cloud UPDATED", service.getString(Constants.NAME));

                                assertEquals(200, r.result().statusCode());
                                testContext.completeNow();
                            }));

                }));

    }

    @Test
    @DisplayName("Test update service with Invalid URL")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void updateServiceInvalidUrl(Vertx vertx, VertxTestContext testContext) {
        JsonObject newService = new JsonObject().
                put(Constants.URL, "https://soundcloud.com/")
                .put(Constants.NAME, "Sound Cloud");

        WebClient.create(vertx)
                .post(Constants.DEFAULT_PORT, "::1", "/service")
                .sendJsonObject(newService, response -> testContext.verify(() -> {

                    JsonObject upService = new JsonObject()
                            .put(Constants.ID, "1")
                            .put(Constants.URL, "www.soundcloud.com/discover")
                            .put(Constants.NAME, "Sound Cloud UPDATED");

                    WebClient.create(vertx)
                            .put(Constants.DEFAULT_PORT, "::1", "/service")
                            .sendJsonObject(upService, r -> testContext.verify(() -> {
                                assertEquals(405, r.result().statusCode());
                                testContext.completeNow();
                            }));

                }));

    }

}
