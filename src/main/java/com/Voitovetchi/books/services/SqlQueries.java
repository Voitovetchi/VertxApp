package com.Voitovetchi.books.services;

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
  public static final String UPDATE_BOOK = "UPDATE book SET title=?, author=?, pubdate=? WHERE isbn=?";
  public static final String DELETE_BOOK = "DELETE FROM book WHERE isbn=?";

  public static String getInsertStatement(int authorsNum) {
    StringBuffer insertStatement = new StringBuffer(
      "INSERT ALL " +
      "INTO book (isbn, title, pubdate) VALUES (?, ?, ?) "
    );

    String addAuthor = "INTO author (name, surname, birthdate, idnp) VALUES (?, ?, ?, ?) " +
                        "INTO book_author (isbn, idnp) VALUES (?, ?) ";
    insertStatement.append(addAuthor.repeat(authorsNum));

    insertStatement.append("SELECT * FROM dual");

    return insertStatement.toString();
  }
}
