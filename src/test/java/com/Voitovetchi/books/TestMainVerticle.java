package com.Voitovetchi.books;

import com.Voitovetchi.books.domain.Book;
import com.Voitovetchi.books.jdbc.JdbcMainVerticle;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.ext.unit.Async;
import io.vertx.reactivex.ext.unit.TestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.ServerSocket;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestMainVerticle {

  private Vertx vertx;
  private int port;
  final private Book testBook = new Book(11111111, "testTitle", "testAuthor", "2000-01-01");

  @BeforeEach
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

  @AfterEach
  public void tearDown(VertxTestContext context) {
    vertx.close(context.succeeding(value -> context.checkpoint().flag()));
  }

  @Test
  @Order(1)
  void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }

  @Test
  @Order(2)
  public void testMyApplication(VertxTestContext context) {
    vertx.createHttpClient().getNow(port, "localhost", "/", response -> {
      response.handler(body -> {
        context.completeNow();
      });
    });
  }

  @Test
  @Order(3)
  public void testGetAll(VertxTestContext context) {
    vertx.createHttpClient().getNow(port, "localhost", "/books", response -> context.verify(() -> {
      Assertions.assertEquals(200, response.statusCode());
      Assertions.assertTrue(response.headers().get(HttpHeaders.CONTENT_TYPE).contains(HttpHeaderValues.APPLICATION_JSON));
      response.handler(body -> {
        context.completeNow();
      });
    }));
  }

  @Test
  @Order(4)
  public void testAdd(VertxTestContext context) {
    final String json = Json.encodePrettily(testBook);
    final String length = Integer.toString(json.length());
    vertx.createHttpClient().post(port, "localhost", "/books")
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
      .putHeader(HttpHeaders.CONTENT_LENGTH, length)
      .handler(response -> context.verify(() -> {
        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertTrue(response.headers().get(HttpHeaders.CONTENT_TYPE).contains(HttpHeaderValues.APPLICATION_JSON));
        response.handler(body -> context.verify(() -> {
          Assertions.assertTrue(body.toString().contains("The book was successfully added"));
          context.completeNow();
        }));
      }))
      .write(json)
      .end();
  }

  @Test
  @Order(5)
  public void testGetByIsbn(VertxTestContext context) {
    vertx.createHttpClient().getNow(port, "localhost", "/books/" + testBook.getIsbn(), response -> context.verify(() -> {
      Assertions.assertEquals(200, response.statusCode());
      Assertions.assertTrue(response.headers().get(HttpHeaders.CONTENT_TYPE).contains(HttpHeaderValues.APPLICATION_JSON));
      response.handler(body -> {
        String bodyString = body.toString();
        Assertions.assertTrue(bodyString.contains(Long.toString(testBook.getIsbn())));
        Assertions.assertTrue(bodyString.contains(testBook.getTitle()));
        Assertions.assertTrue(bodyString.contains(testBook.getAuthor()));
        Assertions.assertTrue(bodyString.contains(testBook.getPubdate()));
        context.completeNow();
      });
    }));
  }

  @Test
  @Order(6)
  public void testUpdate(VertxTestContext context) {
    final Book book = new Book(11111111, "UPDtestTitle", "UPDtestAuthor", "2000-01-30");
    final String json = Json.encodePrettily(book);
    final String length = Integer.toString(json.length());
    vertx.createHttpClient().put(port, "localhost", "/books/" + testBook.getIsbn())
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
      .putHeader(HttpHeaders.CONTENT_LENGTH, length)
      .handler(response -> context.verify(() -> {
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertTrue(response.headers().get(HttpHeaders.CONTENT_TYPE).contains(HttpHeaderValues.APPLICATION_JSON));
        response.handler(body -> context.verify(() -> {
          Assertions.assertTrue(body.toString().contains("The book was successfully updated"));
          context.completeNow();
        }));
      }))
      .write(json)
      .end();
  }

  @Test
  @Order(7)
  public void testDelete(VertxTestContext context) {
    vertx.createHttpClient().delete(port, "localhost", "/books/" + testBook.getIsbn(), response -> context.verify(() -> {
      Assertions.assertEquals(200, response.statusCode());
      Assertions.assertTrue(response.headers().get(HttpHeaders.CONTENT_TYPE).contains(HttpHeaderValues.APPLICATION_JSON));
      response.handler(body -> context.verify(() -> {
        Assertions.assertTrue(body.toString().contains("The book was successfully deleted"));
        context.completeNow();
      }));
    }))
    .end();
  }
}
