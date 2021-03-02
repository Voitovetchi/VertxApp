package com.voitovetchi.books.httpservers;

import com.voitovetchi.books.repository.JdbcBookRepository;
import com.voitovetchi.books.services.JsonParser;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpServer extends AbstractVerticle {

  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpServer.class);
  private JdbcBookRepository bookRepository;

  @Override
  public void start(Promise<Void> startPromise) {
    ConfigRetrieverOptions options = configureConfigRetrieverOptions();

    ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

    retriever.getConfig(ar -> {
      if (ar.failed()) {
        LOGGER.error("Config failed: ");
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
      bookRepository.getAll()
        .onSuccess(result -> {
          req.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(result.encode());
        })
        .onFailure(req::fail);
      });
  }

  protected abstract void getBookByIsbn(Router books);

  private void addBook(Router books) {
    books.post("/books").handler(req -> {
      bookRepository.add(JsonParser.parseJsonObjectToBook(req.getBodyAsJson()))
        .onComplete(result -> {
          getMessage(req, "message", "Book was successfully added", HttpResponseStatus.CREATED.code());
        })
        .onFailure(req::fail);
    });
  }

  protected abstract void updateBook(Router books);

  protected abstract void deleteBook(Router book);

  private void registerErrorHandler(Router books) {
    books.errorHandler(500, req -> {
      LOGGER.error("Failed: ", req.failure());
      if (req.failure() instanceof NullPointerException) {
        getMessage(req, "error", "Body is not filled", HttpResponseStatus.BAD_REQUEST.code());
      } else {
        getMessage(req, "error", req.failure().getMessage(), HttpResponseStatus.BAD_REQUEST.code());
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
        LOGGER.info("HTTP server started on port 8888");
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

  protected void getBookByIsbnRequest(RoutingContext req, String isbn) {
    bookRepository.getByIsbn(isbn)
      .onSuccess(result -> {
        if (result.isEmpty()) {
          getMessage(req, "error", "There is no book with such isbn", HttpResponseStatus.BAD_REQUEST.code());
        }
        else {
          req.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(result.encode());
        }
      })
      .onFailure(req::fail);
  }

  protected void updateBookRequest(RoutingContext req, String isbn) {
    bookRepository.update(isbn, JsonParser.parseJsonObjectToBook(req.getBodyAsJson()))
      .onSuccess(result -> {
        if (result == null) {
          getMessage(req, "error", "There is no book with such isbn", HttpResponseStatus.BAD_REQUEST.code());
        } else {
          getMessage(req, "message", "Book was successfully updated", HttpResponseStatus.ACCEPTED.code());
        }
      })
      .onFailure(req::fail);
  }

  protected void deleteBookRequest(RoutingContext req, String isbn) {
    bookRepository.delete(isbn)
      .onSuccess(result -> {
        if (result == null) {
          getMessage(req, "error", "There is no book with such isbn", HttpResponseStatus.BAD_REQUEST.code());
        } else {
          getMessage(req, "message", "Book was successfully deleted", HttpResponseStatus.ACCEPTED.code());
        }
      })
      .onFailure(req::fail);
  }
}
