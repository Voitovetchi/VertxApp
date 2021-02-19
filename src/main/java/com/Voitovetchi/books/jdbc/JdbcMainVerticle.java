package com.Voitovetchi.books.jdbc;

import com.Voitovetchi.books.domain.Author;
import com.Voitovetchi.books.domain.Book;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
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
        } else if (ar.result().isEmpty()) {
          getMessage(req, "error", "There is no book with such isbn", HttpResponseStatus.BAD_REQUEST.code());
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
      Book book = getBookFromJsonObject(req.getBodyAsJson());
      bookRepository.add(book)
        .onComplete(ar -> {
          if (ar.failed()) {
            req.fail(ar.cause());
          } else {
            getMessage(req, "message", "Book was successfully added", HttpResponseStatus.CREATED.code());
          }
        });
    });
  }

  private void updateBook(Router books) {
    books.put("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");

      bookRepository.update(isbn, getBookFromJsonObject(req.getBodyAsJson()))
        .onComplete(ar -> {
          if (ar.failed()) {
            req.fail(ar.cause());
          } else if (ar.result() == null) {
            getMessage(req, "error", "There is no book with such isbn", HttpResponseStatus.BAD_REQUEST.code());
          } else {
            getMessage(req, "message", "Book was successfully updated", HttpResponseStatus.ACCEPTED.code());
          }
        });

    });
  }

  private void deleteBook(Router books) {
    books.delete("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");

      bookRepository.delete(isbn).onComplete(ar -> {
        if (ar.failed()) {
          req.fail(ar.cause());
        } else if (ar.result() == null) {
          getMessage(req, "error", "There is no book with such isbn", HttpResponseStatus.BAD_REQUEST.code());
        } else {
          getMessage(req, "message", "Book was successfully deleted", HttpResponseStatus.ACCEPTED.code());
        }
      });
    });
  }

  private void registerErrorHandler(Router books) {
    books.errorHandler(500, event -> {
      if (event.failure() instanceof NullPointerException) {
        getMessage(event, "error", "Body is empty", HttpResponseStatus.BAD_REQUEST.code());
      } else {
        getMessage(event, "error", event.failure().getMessage(), HttpResponseStatus.BAD_REQUEST.code());
      }
    });
  }

  private Book getBookFromJsonObject(JsonObject body) {

    final Book book = new Book(body.getLong("ISBN"), body.getString("TITLE"), body.getString("PUBDATE"));

    final JsonArray authors = body.getJsonArray("AUTHORS");

    for(int i = 0; i < authors.size(); i++) {
      final Author author = new Author(
        authors.getJsonObject(i).getLong("IDNP"),
        authors.getJsonObject(i).getString("NAME"),
        authors.getJsonObject(i).getString("SURNAME"),
        authors.getJsonObject(i).getString("BIRTHDATE")
        );

      book.getAuthors().add(author);
    }

    return book;
  }

  private void getMessage(RoutingContext req, String key, String message, int statusCode) {
    req.response()
      .setStatusCode(statusCode)
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
      .end(new JsonObject().put(key, message).encode());
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new JdbcMainVerticle());
  }
}
