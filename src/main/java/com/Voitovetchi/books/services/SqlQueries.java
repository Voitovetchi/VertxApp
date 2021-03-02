package com.voitovetchi.books.services;

import com.voitovetchi.books.domain.Author;
import com.voitovetchi.books.domain.Book;
import io.vertx.core.json.JsonArray;
import lombok.Getter;

@Getter
public class SqlQueries {
  public static final String GET_ALL_BOOKS = "SELECT b.isbn, title, TO_CHAR(pubdate, 'dd-mm-yyyy') AS pubdate, a.idnp, name, surname, TO_CHAR(birthdate, 'dd-mm-yyyy') AS birthdate FROM book b " +
                                              "INNER JOIN book_author ba " +
                                              "ON b.isbn = ba.isbn " +
                                              "INNER JOIN author a " +
                                              "ON ba.idnp = a.idnp";

  public static final String GET_BOOK_BY_ISBN = "SELECT b.isbn, title, TO_CHAR(pubdate, 'dd-mm-yyyy') AS pubdate, a.idnp, name, surname, TO_CHAR(birthdate, 'dd-mm-yyyy') AS birthdate FROM book b " +
                                              "INNER JOIN book_author ba " +
                                              "ON b.isbn = ba.isbn " +
                                              "INNER JOIN author a " +
                                              "ON ba.idnp = a.idnp " +
                                              "WHERE b.isbn=?";

  public static final String INSERT_BOOK = "INSERT into book values (?, ?, ?)";
  public static final String UPDATE_BOOK = "UPDATE book SET title=?, pubdate=? WHERE isbn=?";
  public static final String DELETE_BOOK = "DELETE FROM book WHERE isbn=?";
  public static final String DELETE_AUTHOR = "DELETE FROM author WHERE idnp=?";

  public static String getInsertStatement(int authorsNum) {

    String addAuthor = "INTO book_author (isbn, idnp) VALUES (?, ?) ";

    return "INSERT ALL " +
      "INTO book (isbn, title, pubdate) VALUES (?, ?, ?) " + addAuthor.repeat(authorsNum) +
      "SELECT * FROM dual";
  }

  public static JsonArray getParamsForAddBookQuery(Book book) {
    final JsonArray params = new JsonArray()
      .add(book.getIsbn())
      .add(book.getTitle())
      .add(book.getPubdate());

    for (Author author: book.getAuthors()) {
      params
        .add(book.getIsbn())
        .add(author.getIdnp());
    }

    return params;
  }
}
