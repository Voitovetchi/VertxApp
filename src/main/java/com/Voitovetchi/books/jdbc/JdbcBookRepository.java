package com.Voitovetchi.books.jdbc;

import com.Voitovetchi.books.domain.Book;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;

import java.util.List;

public class JdbcBookRepository {
  private SQLClient sql;
  public JdbcBookRepository(final Vertx vertx) {
    final JsonObject config = new JsonObject();
    config.put("url", "jdbc:postgresql://127.0.0.1/books");
    config.put("driver_class", "org.postgresql.Driver");
    config.put("user", "postgres");
    config.put("password", "secret");
    sql = JDBCClient.createShared(vertx, config);

  }

  public Future<JsonArray> getAll() {
    Future<JsonArray> getAllFuture = Future.future();
    sql.query("SELECT * FROM book", ar -> {
      if (ar.failed()) {
        getAllFuture.fail(ar.cause());
        return;
      } else {
        final List<JsonObject> rows = ar.result().getRows();
        final JsonArray result = new JsonArray();
        rows.forEach(result::add);
        getAllFuture.complete(result);
      }
    });
    return getAllFuture;
  }

  public Future<Void> add(Book book) {
    final Future<Void> added = Future.future();
    final JsonArray params = new JsonArray()
      .add(book.getIsbn())
      .add(book.getTitle())
      .add(book.getAuthor())
      .add(book.getPubdate());
    sql.updateWithParams("INSERT INTO book VALUES (?, ?, ?, ?)", params, ar -> {
      if (ar.failed()) {
        added.fail(ar.cause());
        return;
      }
      else if (ar.result().getUpdated() != 1) {
        added.fail(new IllegalStateException("Wrong update count on insert" + ar.cause()));
        return;
      } else {
        added.complete();
      }
    });
    return added;
  }

  public Future<String> delete(String isbn) {
    final Future<String> deleted = Future.future();
    final JsonArray params = new JsonArray().add(Integer.parseInt(isbn));
    System.out.println(params);
    sql.updateWithParams("DELETE FROM book WHERE isbn=?", params, ar -> {
      if (ar.failed()) {
        deleted.fail(ar.cause());
        return;
      }
      if (ar.result().getUpdated() == 0) {
        deleted.complete();
        return;
      }
      deleted.complete();
    });
    return deleted;
  }
}
