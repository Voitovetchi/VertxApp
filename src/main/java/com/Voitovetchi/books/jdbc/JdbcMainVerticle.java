package com.Voitovetchi.books.jdbc;

import com.Voitovetchi.books.domain.Book;
import com.Voitovetchi.books.repo.InMemoryBookStore;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class JdbcMainVerticle extends AbstractVerticle {

  public static final Logger LOG = LoggerFactory.getLogger(JdbcMainVerticle.class);
  private InMemoryBookStore store = new InMemoryBookStore();
  private JdbcBookRepository bookRepository;

  @Override
  public void start(Promise<Void> startPromise) {
    bookRepository = new JdbcBookRepository(vertx);

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
        LOG.info("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void getAll(Router books) {
    books.get("/books").handler(req -> {
      bookRepository.getAll().setHandler(ar -> {
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
      bookRepository.getByIsbn(isbn).setHandler(ar -> {
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
      final JsonObject requestBody = req.getBodyAsJson();
      System.out.println("Request body: " + requestBody);
      bookRepository.add(requestBody.mapTo(Book.class)).setHandler(ar -> {
        if (ar.failed()) {
          req.fail(ar.cause());
          return;
        } else {
          req.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(new JsonObject().put("message", "The book was successfully added").encode());
        }
      });
    });
  }

  private void updateBook(Router books) {
    books.put("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");
      final JsonObject requestBody = req.getBodyAsJson();
      System.out.println("Request body: " + requestBody);
      bookRepository.update(isbn, requestBody.mapTo(Book.class))
        .setHandler(ar -> {
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

  private void deleteBook(Router books) {
    books.delete("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");
      bookRepository.delete(isbn).setHandler(ar -> {
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
      LOG.error("Failed ", event.failure());
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
