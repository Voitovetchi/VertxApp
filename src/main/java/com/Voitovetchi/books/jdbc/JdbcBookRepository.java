package com.Voitovetchi.books.jdbc;

import com.Voitovetchi.books.domain.Book;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;

import java.util.List;

public class JdbcBookRepository {
  private static final String GET_ALL_BOOKS_INFO = "select b.isbn, title, TO_CHAR(pubdate, 'dd-mm-yyyy') as pubdate, a.idnp, name, surname, TO_CHAR(birthdate, 'dd-mm-yyyy') as birthdate from BOOKS_ADMIN.book b " +
    "inner join BOOKS_ADMIN.book_author ba " +
    "on b.isbn = ba.isbn " +
    "inner join BOOKS_ADMIN.author a " +
    "on ba.idnp = a.idnp";
  private static final String GET_BOOK_BY_ISBN = "select b.isbn, title, TO_CHAR(pubdate, 'dd-mm-yyyy') as pubdate, a.idnp, name, surname, TO_CHAR(birthdate, 'dd-mm-yyyy') as birthdate from BOOKS_ADMIN.book b " +
    "inner join BOOKS_ADMIN.book_author ba " +
    "on b.isbn = ba.isbn " +
    "inner join BOOKS_ADMIN.author a " +
    "on ba.idnp = a.idnp " +
    "where b.isbn=?";
  private static final String INSERT_BOOK = "INSERT INTO book VALUES (?, ?, ?, ?)";
  private static final String UPDATE_BOOK = "UPDATE book SET title=?, author=?, pubdate=? WHERE isbn=?";
  private static final String DELETE_BOOK = "DELETE FROM book WHERE isbn=?";
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

    sql.query(GET_ALL_BOOKS_INFO, ar -> {
      if (ar.failed()) {
        getAllPromise.fail(ar.cause());
      }
      else {
        final List<JsonObject> rows = ar.result().getRows();
        final JsonArray result = new JsonArray();
        rows.forEach(result::add);
        getAllPromise.complete(result);
      }
    });

    return getAllPromise.future();
  }

  public Future<JsonObject> getByIsbn(String isbn) {
    final Promise<JsonObject> getByIsbnPromise = Promise.promise();
    final JsonArray params = new JsonArray().add(Long.parseLong(isbn));

    sql.queryWithParams(GET_BOOK_BY_ISBN, params, ar -> {
      if (ar.failed()) {
        getByIsbnPromise.fail(ar.cause());
      }
      else if (ar.result().getRows().size() == 0){
        getByIsbnPromise.complete(new JsonObject());
      }
      else {
        final List<JsonObject> rows = ar.result().getRows();
        getByIsbnPromise.complete(getBookWithAuthors(rows));
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

    sql.updateWithParams(INSERT_BOOK, params, ar -> {
      if (ar.failed()) {
        add.fail(ar.cause());
      }
      else {
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

    sql.updateWithParams(UPDATE_BOOK, params, ar -> {
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
    final JsonArray params = new JsonArray().add(Integer.parseInt(isbn));

    sql.updateWithParams(DELETE_BOOK, params, ar -> {
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

  private JsonObject getBookWithAuthors(List<JsonObject> rows) {
    //final JsonArray result = new JsonArray();
    JsonArray authors = new JsonArray();

    for (JsonObject row : rows) {
      JsonObject author = new JsonObject()
        .put("IDNP", row.getLong("IDNP"))
        .put("NAME", row.getString("NAME"))
        .put("SURNAME", row.getString("SURNAME"))
        .put("BIRTHDATE", row.getString("BIRTHDATE"));
      authors.add(author);
    }

    return new JsonObject()
      .put("ISBN", rows.get(0).getLong("ISBN"))
      .put("TITLE", rows.get(0).getString("TITLE"))
      .put("PUBDATE", rows.get(0).getString("PUBDATE"))
      .put("AUTHORS", authors);
  }

}
