package com.Voitovetchi.books.repository;

import com.Voitovetchi.books.domain.Book;
import com.Voitovetchi.books.services.JsonParser;
import com.Voitovetchi.books.services.SqlQueries;
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

  private final SQLClient sql;

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

    sql.query(SqlQueries.GET_ALL_BOOKS, ar -> {
      if (ar.failed()) {
        getAllPromise.fail(ar.cause());
      }
      else {
        final List<JsonObject> rows = ar.result().getRows();
        getAllPromise.complete(JsonParser.parseToBooksWithAuthorsJsonArray(rows));
      }
    });

    return getAllPromise.future();
  }

  public Future<JsonArray> getByIsbn(String isbn) {
    final Promise<JsonArray> getByIsbnPromise = Promise.promise();
    final JsonArray params = new JsonArray().add(Long.parseLong(isbn));

    sql.queryWithParams(SqlQueries.GET_BOOK_BY_ISBN, params, ar -> {
      if (ar.failed()) {
        getByIsbnPromise.fail(ar.cause());
      }
      else if (ar.result().getRows().size() == 0){
        getByIsbnPromise.complete(new JsonArray());
      }
      else {
        final List<JsonObject> rows = ar.result().getRows();
        getByIsbnPromise.complete(JsonParser.parseToBooksWithAuthorsJsonArray(rows));
      }
    });

    return getByIsbnPromise.future();
  }

  public Future<Void> add(Book book) {
    final Promise<Void> addBook = Promise.promise();
    final JsonArray params = SqlQueries.getParamsForAddBookQuery(book);

    sql.queryWithParams(SqlQueries.getInsertStatement(book.getAuthors().size()), params, ar -> {
      if (ar.failed()) {
        addBook.fail(ar.cause());
      }
      else {
        addBook.complete();
      }
    });

    return addBook.future();
  }

  public Future<String> update(String isbn, Book book) {
    final Promise<String> update = Promise.promise();
    final JsonArray params = new JsonArray()
      .add(book.getTitle())
      .add(book.getPubdate())
      .add(Integer.parseInt(isbn));

    sql.updateWithParams(SqlQueries.UPDATE_BOOK, params, ar -> {
      if (ar.failed()) {
        update.fail(ar.cause());
      }
      else if (ar.result().getUpdated() == 0) {
        update.complete();
      }
      else {
        update.complete(isbn);
      }
    });

    return update.future();
  }

  public Future<String> delete(String isbn) {
    final Promise<String> delete = Promise.promise();
    final JsonArray params = new JsonArray().add(Long.parseLong(isbn));

    sql.updateWithParams(SqlQueries.DELETE_BOOK, params, ar -> {
      if (ar.failed()) {
        delete.fail(ar.cause());
      }
      else if (ar.result().getUpdated() == 0) {
        delete.complete();
      }
      else {
        delete.complete(isbn);
      }
    });

    return delete.future();
  }
}
