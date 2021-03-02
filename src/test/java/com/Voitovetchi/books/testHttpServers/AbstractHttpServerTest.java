package com.Voitovetchi.books.testHttpServers;

import com.voitovetchi.books.repository.JdbcBookRepository;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.ServerSocket;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
public abstract class AbstractHttpServerTest {

  protected Vertx vertx;
  protected HttpClient httpClient;
  protected JdbcBookRepository bookRepository;
  protected int port;
  protected final JsonObject testAuthor = new JsonObject()
    .put("IDNP", 7856941235468L);
  protected final JsonObject testBook = new JsonObject()
    .put("ISBN", 1111111111)
    .put("TITLE", "testTitle")
    .put("PUBDATE", "2000-01-01")
    .put("AUTHORS", new JsonArray().add(testAuthor));
  protected final JsonObject updatedTestBook = new JsonObject()
    .put("ISBN", 1111111111)
    .put("TITLE", "UPDtestTitle")
    .put("PUBDATE", "2001-01-01")
    .put("AUTHORS", new JsonArray());

  public abstract void setUp(VertxTestContext context) throws IOException;

  @AfterAll
  public void tearDown(VertxTestContext context) {
    vertx.close(context.succeeding(value -> context.checkpoint().flag()));
  }

  @Test
  @Order(1)
  void verticle_deployed(VertxTestContext testContext) {
    testContext.completeNow();
  }

  @Test
  @Order(2)
  @Timeout(5000)
  public void testDbConnection(VertxTestContext context) {
    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("file")
      .setConfig(new JsonObject().put("path", "config.json"));

    ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);

    ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

    retriever.getConfig(ar -> {
      if (ar.failed()) {
        System.out.println("Config failed");
      } else {
        JsonObject config = ar.result();

        JsonObject databaseConfig = config.getJsonObject("database");
        bookRepository = new JdbcBookRepository(vertx,
          databaseConfig.getString("url"),
          databaseConfig.getString("driver"),
          databaseConfig.getString("user"),
          databaseConfig.getString("password"));

        Assertions.assertNotEquals(null, bookRepository.getSql());
        context.completeNow();
      }
    });
  }

  @Test
  @Order(3)
  @Timeout(5000)
  public void testGetAll(VertxTestContext context) {
    httpClient.request(HttpMethod.GET, port, "localhost", "/books")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onSuccess(buffer -> context.verify(() -> {
        Assertions.assertTrue(buffer.toString().contains("ISBN"));
        Assertions.assertTrue(buffer.toString().contains("TITLE"));
        Assertions.assertTrue(buffer.toString().contains("PUBDATE"));
        Assertions.assertTrue(buffer.toString().contains("AUTHOR"));
        context.completeNow();
      }))
      .onFailure(error -> context.verify(() -> {
        Assertions.assertFalse(error.toString().isEmpty());
        Assertions.assertTrue(error.toString().contains("\"error\":"));
      }));
  }

  @Test
  @Order(4)
  @Timeout(5000)
  public void testAdd(VertxTestContext context) {
    httpClient.request(HttpMethod.POST, port, "localhost", "/books")
      .compose(req -> req.send(testBook.toString()).compose(HttpClientResponse::body))
      .onSuccess(buffer -> context.verify(() -> {
        Assertions.assertTrue(buffer.toString().contains("Book was successfully added"));
        context.completeNow();
      }))
      .onFailure(error -> context.verify(() -> {
        Assertions.assertFalse(error.toString().isEmpty());
        Assertions.assertTrue(error.toString().contains("\"error\" :"));
      }));
  }

  public abstract void testGetByIsbn(VertxTestContext context);

  public void testGetByIsbnRequest(Future<Buffer> request, VertxTestContext context) {
    request
      .onSuccess(buffer -> context.verify(() -> {
        Assertions.assertEquals(1, buffer.toJsonArray().size());
        Assertions.assertTrue(buffer.toString().contains(testBook.getLong("ISBN").toString()));
        context.completeNow();
      }))
      .onFailure(error -> context.verify(() -> {
        Assertions.assertFalse(error.toString().isEmpty());
        Assertions.assertTrue(error.toString().contains("\"error\":"));
      }));
  }

  public abstract void testUpdate(VertxTestContext context);

  public void testUpdateRequest(Future<Buffer> request, VertxTestContext context) {
    request
      .onSuccess(buffer -> context.verify(() -> {
        Assertions.assertTrue(buffer.toString().contains("Book was successfully updated"));
        context.completeNow();
      }))
      .onFailure(error -> context.verify(() -> {
        Assertions.assertFalse(error.toString().isEmpty());
        Assertions.assertTrue(error.toString().contains("\"error\":"));
      }));
  }

  public abstract void testDelete(VertxTestContext context);

  public void testDeleteRequest(Future<Buffer> request, VertxTestContext context) {
    request
      .onSuccess(buffer -> context.verify(() -> {
        Assertions.assertTrue(buffer.toString().contains("Book was successfully deleted"));
        context.completeNow();
      }))
      .onFailure(error -> context.verify(() -> {
        Assertions.assertFalse(error.toString().isEmpty());
        Assertions.assertTrue(error.toString().contains("\"error\" :"));
      }));
  }

  protected DeploymentOptions getDeploymentOptions() throws IOException {
    vertx = Vertx.vertx();
    httpClient = vertx.createHttpClient();
    ServerSocket socket = new ServerSocket(8888);
    port = socket.getLocalPort();
    socket.close();
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(new JsonObject().put("http.port", port));
    return options;
  }
}
