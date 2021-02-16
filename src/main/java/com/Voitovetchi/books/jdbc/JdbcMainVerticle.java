package com.Voitovetchi.books.jdbc;

import com.Voitovetchi.books.domain.Book;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class JdbcMainVerticle extends AbstractVerticle {

  private JdbcBookRepository bookRepository;

  @Override
  public void start(Promise<Void> startPromise) {
    String url = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
    String driver = "oracle.jdbc.driver.OracleDriver";
    String user = "books_admin";
    String password = "password";

    bookRepository = new JdbcBookRepository(vertx, url, driver, user, password);

    Router books = Router.router(vertx);
    books.route().handler(BodyHandler.create());
    books.route("/*").handler(StaticHandler.create());

    getAll(books);

    getBookByIsbn(books);

    createBook(books);

    updateBook(books);

    deleteBook(books);

    registerErrorHandler(books);

    vertx.createHttpServer().requestHandler(books).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void getAll(Router books) {

    books.get("/books").handler(req -> {
      bookRepository.getAll().onComplete(ar -> {
        if (ar.failed()) {
          req.fail(ar.cause());
          return;
        } else {
          req.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(ar.result().encode());
        }
      });
    });
  }

  private void getBookByIsbn(Router books) {
    books.get("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");

      bookRepository.getByIsbn(isbn).onComplete(ar -> {
        if (ar.failed()) {
          req.fail(ar.cause());
          return;
        } else if (ar.result().isEmpty()) {
          req.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(new JsonObject().put("error", "There is no book with such isbn").encode());
        }
        else {
          req.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(ar.result().encode());
        }
      });
    });
  }

  private void createBook(Router books) {
    books.post("/books").handler(req -> {
      final JsonObject requestBody = getJsonObject(req);

      bookRepository.add(requestBody.mapTo(Book.class)).onComplete(ar -> {
        if (ar.failed()) {
          req.fail(ar.cause());
          return;
        } else {
          req.response()
            .setStatusCode(HttpResponseStatus.CREATED.code())
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(new JsonObject().put("message", "The book was successfully added").encode());
        }
      });
    });
  }

  private void updateBook(Router books) {
    books.put("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");
      final JsonObject requestBody = getJsonObject(req);

      bookRepository.update(isbn, requestBody.mapTo(Book.class))
        .onComplete(ar -> {
          if (ar.failed()) {
            req.fail(ar.cause());
            return;
          }
          if (ar.result() == null) {
            req.response()
              .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
              .end(new JsonObject().put("error", "There is no book with such isbn").encode());
          } else {
            req.response()
              .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
              .end(new JsonObject().put("message", "The book was successfully updated").encode());
          }
        });

    });
  }

  private JsonObject getJsonObject(io.vertx.ext.web.RoutingContext req) {
    final JsonObject requestBody = new JsonObject()
      .put("isbn", req.getBodyAsJson().getLong("ISBN"))
      .put("title", req.getBodyAsJson().getString("TITLE"))
      .put("author", req.getBodyAsJson().getString("AUTHOR"))
      .put("pubdate", req.getBodyAsJson().getString("PUBDATE"));
    return requestBody;
  }

  private void deleteBook(Router books) {
    books.delete("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");

      bookRepository.delete(isbn).onComplete(ar -> {
        if (ar.failed()) {
          req.fail(ar.cause());
          return;
        }
        if (ar.result() == null) {
          req.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(new JsonObject().put("error", "There is no book with such isbn").encode());
        } else {
          req.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(new JsonObject().put("message", "The book was successfully deleted").encode());
        }
      });
    });
  }

  private void registerErrorHandler(Router books) {
    books.errorHandler(500, event -> {
      System.err.println("Failed " + event.failure());
      if (event.failure() instanceof IllegalArgumentException) {
        event.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
          .end(new JsonObject().put("error", event.failure().getMessage()).encode());
      }
    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new JdbcMainVerticle());
  }
}
