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
    final Future<JsonArray> getAllFuture = Future.future();

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

  public Future<JsonArray> getByIsbn(String isbn) {
    final Future<JsonArray> getByIsbnFuture = Future.future();
    final JsonArray params = new JsonArray().add(Integer.parseInt(isbn));

    sql.queryWithParams("SELECT * FROM book WHERE isbn=?", params,  ar -> {
      if (ar.failed()) {
        getByIsbnFuture.fail(ar.cause());
        return;
      } else {
        final List<JsonObject> rows = ar.result().getRows();
        final JsonArray result = new JsonArray();
        rows.forEach(result::add);
        getByIsbnFuture.complete(result);
      }
    });

    return getByIsbnFuture;
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

  public Future<String> update(String isbn, Book book) {
    final Future<String> updated = Future.future();
    final JsonArray params = new JsonArray()
      .add(book.getTitle())
      .add(book.getAuthor())
      .add(book.getPubdate())
      .add(Integer.parseInt(isbn));

    sql.updateWithParams("UPDATE book SET title=?, author=?, pubdate=? WHERE isbn=?", params, ar -> {
      if (ar.failed()) {
        updated.fail(ar.cause());
        return;
      }
      if (ar.result().getUpdated() == 0) {
        updated.complete();
        return;
      } else {
        updated.complete(isbn);
      }
    });

    return updated;
  }

  public Future<String> delete(String isbn) {
    final Future<String> deleted = Future.future();
    final JsonArray params = new JsonArray().add(Integer.parseInt(isbn));

    sql.updateWithParams("DELETE FROM book WHERE isbn=?", params, ar -> {
      if (ar.failed()) {
        deleted.fail(ar.cause());
        return;
      }
      if (ar.result().getUpdated() == 0) {
        deleted.complete();
        return;
      } else {
        deleted.complete(isbn);
      }
    });

    return deleted;
  }


}
