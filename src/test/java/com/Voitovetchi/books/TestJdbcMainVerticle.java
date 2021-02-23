package com.Voitovetchi.books;

import com.Voitovetchi.books.jdbc.JdbcBookRepository;
import com.Voitovetchi.books.jdbc.JdbcMainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
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
public class TestJdbcMainVerticle {

  private Vertx vertx;
  private int port;
  final JsonObject testAuhtor = new JsonObject()
    .put("IDNP", 4102305607524L);
  final JsonObject testBook = new JsonObject()
    .put("ISBN", 1111111111)
    .put("TITLE", "testTitle")
    .put("PUBDATE", "2000-01-01")
    .put("AUTHORS", new JsonArray().add(testAuhtor));
  final JsonObject updatedTestBook = new JsonObject()
    .put("ISBN", 1111111111)
    .put("TITLE", "UPDtestTitle")
    .put("PUBDATE", "2001-01-01")
    .put("AUTHORS", new JsonArray());

  @BeforeAll
  public void setUp(VertxTestContext context) throws IOException {
    vertx = Vertx.vertx();
    ServerSocket socket = new ServerSocket(8888);
    port = socket.getLocalPort();
    socket.close();
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(new JsonObject().put("http.port", port)
      );
    vertx.deployVerticle(JdbcMainVerticle.class.getName(), options, context.succeeding(value -> context.checkpoint().flag()));
  }

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
  @Order(3)
  @Timeout(5000)
  public void testDbConnection(VertxTestContext context) {
    String url = "jdbc:postgresql://127.0.0.1/books";
    String driver = "org.postgresql.Driver";
    String user = "postgres";
    String password = "secret";
    JdbcBookRepository bookRepository = new JdbcBookRepository(vertx, url, driver, user, password);
    Assertions.assertNotEquals(null, bookRepository.getSql());
    context.completeNow();
  }

  @Test
  @Order(4)
  @Timeout(5000)
  public void testGetAll(VertxTestContext context) {
    vertx.createHttpClient().request(HttpMethod.GET, port, "localhost", "/books")
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
        Assertions.assertTrue(error.toString().contains("\"error\" :"));
      }));
  }

  @Test
  @Order(5)
  @Timeout(5000)
  public void testAdd(VertxTestContext context) {
    vertx.createHttpClient().request(HttpMethod.POST, port, "localhost", "/books")
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

  @Test
  @Order(6)
  @Timeout(5000)
  public void testGetByIsbn(VertxTestContext context) {
    vertx.createHttpClient().request(HttpMethod.GET, port, "localhost", "/books/" + testBook.getLong("ISBN"))
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onSuccess(buffer -> context.verify(() -> {
        Assertions.assertEquals(1, buffer.toJsonArray().size());
        Assertions.assertTrue(buffer.toString().contains(testBook.getLong("ISBN").toString()));
        context.completeNow();
      }))
      .onFailure(error -> context.verify(() -> {
        Assertions.assertFalse(error.toString().isEmpty());
        Assertions.assertTrue(error.toString().contains("\"error\" :"));
      }));
  }

  @Test
  @Order(7)
  @Timeout(5000)
  public void testUpdate(VertxTestContext context) {
    vertx.createHttpClient().request(HttpMethod.PUT, port, "localhost", "/books/" + testBook.getLong("ISBN"))
      .compose(req -> req.send(updatedTestBook.toString()).compose(HttpClientResponse::body))
      .onSuccess(buffer -> context.verify(() -> {
        Assertions.assertTrue(buffer.toString().contains("Book was successfully updated"));
        context.completeNow();
      }))
      .onFailure(error -> context.verify(() -> {
        Assertions.assertFalse(error.toString().isEmpty());
        Assertions.assertTrue(error.toString().contains("\"error\" :"));
      }));
  }

  @Test
  @Order(8)
  @Timeout(5000)
  public void testDelete(VertxTestContext context) {
    vertx.createHttpClient().request(HttpMethod.DELETE, port, "localhost", "/books/" + testBook.getLong("ISBN"))
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(buffer -> context.verify(() -> {
        Assertions.assertTrue(buffer.toString().contains("Book was successfully deleted"));
        context.completeNow();
      }))
      .onFailure(error -> context.verify(() -> {
        Assertions.assertFalse(error.toString().isEmpty());
        Assertions.assertTrue(error.toString().contains("\"error\" :"));
      }));
  }
}
