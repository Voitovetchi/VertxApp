package com.Voitovetchi.books.jdbc;

import com.Voitovetchi.books.domain.Book;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import lombok.Getter;

import java.util.List;

@Getter
public class JdbcBookRepository {
  private SQLClient sql;

  public JdbcBookRepository(final Vertx vertx, String url, String driver, String user, String password) {
    final JsonObject config = new JsonObject();
    config.put("url", url);
    config.put("driver_class", driver);
    config.put("user", user);
    config.put("password", password);
    sql = JDBCClient.createShared(vertx, config);

  }

  public Future<JsonArray> getAll() {
    final Promise<JsonArray> getAllPromise = Promise.promise();

    sql.query("SELECT * FROM book", ar -> {
      if (ar.failed()) {
        getAllPromise.fail(ar.cause());
      } else {
        final List<JsonObject> rows = ar.result().getRows();
        final JsonArray result = new JsonArray();
        rows.forEach(result::add);
        getAllPromise.complete(result);
      }
    });

    return getAllPromise.future();
  }

  public Future<JsonArray> getByIsbn(String isbn) {
    final Promise<JsonArray> getByIsbnPromise = Promise.promise();
    final JsonArray params = new JsonArray().add(Integer.parseInt(isbn));

    sql.queryWithParams("SELECT * FROM book WHERE isbn=?", params,  ar -> {
      if (ar.failed()) {
        getByIsbnPromise.fail(ar.cause());
        return;
      } else {
        final List<JsonObject> rows = ar.result().getRows();
        final JsonArray result = new JsonArray();
        rows.forEach(result::add);
        getByIsbnPromise.complete(result);
      }
    });

    return getByIsbnPromise.future();
  }

  public Future<Void> add(Book book) {
    final Promise<Void> add = Promise.promise();
    final JsonArray params = new JsonArray()
      .add(book.getIsbn())
      .add(book.getTitle())
      .add(book.getAuthor())
      .add(book.getPubdate());

    sql.updateWithParams("INSERT INTO book VALUES (?, ?, ?, ?)", params, ar -> {
      if (ar.failed()) {
        add.fail(ar.cause());
        return;
      }
      else if (ar.result().getUpdated() != 1) {
        add.fail(new IllegalStateException("Wrong update count on insert" + ar.cause()));
        return;
      } else {
        add.complete();
      }
    });

    return add.future();
  }

  public Future<String> update(String isbn, Book book) {
    final Promise<String> update = Promise.promise();
    final JsonArray params = new JsonArray()
      .add(book.getTitle())
      .add(book.getAuthor())
      .add(book.getPubdate())
      .add(Integer.parseInt(isbn));

    sql.updateWithParams("UPDATE book SET title=?, author=?, pubdate=? WHERE isbn=?", params, ar -> {
      if (ar.failed()) {
        update.fail(ar.cause());
        return;
      }
      if (ar.result().getUpdated() == 0) {
        update.complete();
        return;
      } else {
        update.complete(isbn);
      }
    });

    return update.future();
  }

  public Future<String> delete(String isbn) {
    final Promise<String> delete = Promise.promise();
    final JsonArray params = new JsonArray().add(Integer.parseInt(isbn));

    sql.updateWithParams("DELETE FROM book WHERE isbn=?", params, ar -> {
      if (ar.failed()) {
        delete.fail(ar.cause());
        return;
      }
      if (ar.result().getUpdated() == 0) {
        delete.complete();
        return;
      } else {
        delete.complete(isbn);
      }
    });

    return delete.future();
  }


}
