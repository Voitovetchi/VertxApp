package com.Voitovetchi.books.jdbc;

import com.Voitovetchi.books.services.JsonParser;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class JdbcHeaderParamVerticle extends AbstractVerticle {

  private JdbcBookRepository bookRepository;

  @Override
  public void start(Promise<Void> startPromise) {
    ConfigRetrieverOptions options = configureConfigRetrieverOptions();

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

        JsonObject httpServerConfig = config.getJsonObject("httpServer");

        Router books = setBookRouter();

        createHttpServer(startPromise, httpServerConfig, books);
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
    books.get("/books/getByIsbn").handler(req -> {
      final String isbn = req.request().getHeader("isbn");

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

  private void addBook(Router books) {
    books.post("/books").handler(req -> {
      bookRepository.add(JsonParser.parseJsonObjectToBook(req.getBodyAsJson()))
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
    books.put("/books/updateByIsbn").handler(req -> {
      final String isbn = req.request().getHeader("isbn");

      bookRepository.update(isbn, JsonParser.parseJsonObjectToBook(req.getBodyAsJson()))
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
    books.delete("/books/deleteByIsbn").handler(req -> {
      final String isbn = req.request().getHeader("isbn");

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
        getMessage(event, "error", "Body is not filled", HttpResponseStatus.BAD_REQUEST.code());
      } else {
        getMessage(event, "error", event.failure().getMessage(), HttpResponseStatus.BAD_REQUEST.code());
      }
    });
  }

  private ConfigRetrieverOptions configureConfigRetrieverOptions() {
    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("file")
      .setConfig(new JsonObject().put("path", "config.json"));

    return new ConfigRetrieverOptions().addStore(fileStore);
  }

  private Router setBookRouter() {
    Router books = Router.router(vertx);
    books.route().handler(BodyHandler.create());

    getAll(books);
    getBookByIsbn(books);
    addBook(books);
    updateBook(books);
    deleteBook(books);
    registerErrorHandler(books);

    return books;
  }

  private void createHttpServer(Promise<Void> startPromise, JsonObject httpServerConfig, Router books) {
    vertx.createHttpServer().requestHandler(books).listen(httpServerConfig.getInteger("port"), http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void getMessage(RoutingContext req, String key, String message, int statusCode) {
    req.response()
      .setStatusCode(statusCode)
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
      .end(new JsonObject().put(key, message).encode());
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new JdbcHeaderParamVerticle());
  }
}
