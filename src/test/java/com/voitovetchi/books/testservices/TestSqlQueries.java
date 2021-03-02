package com.voitovetchi.books.testservices;

import com.voitovetchi.books.domain.Author;
import com.voitovetchi.books.domain.Book;
import com.voitovetchi.books.services.SqlQueries;
import io.vertx.core.json.JsonArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSqlQueries {
  @Test
  public void testGetInsertStatement() {
    final String expected = "INSERT ALL " +
                      "INTO book (isbn, title, pubdate) VALUES (?, ?, ?) " +
                      "INTO book_author (isbn, idnp) VALUES (?, ?) " +
                      "SELECT * FROM dual";

    Assertions.assertTrue(SqlQueries.getInsertStatement(1).equals(expected));
  }

  @Test
  public void testGetParamsForAddBookQuery() {
    final Author author = new Author(1111111111111L, "testName", "testSurname", "2000-01-01");
    final Book book = new Book(1111111111, "testTitle", "2000-01-01");
    book.getAuthors().add(author);

    final JsonArray expected = new JsonArray()
                                .add(Long.parseLong("1111111111"))
                                .add("testTitle")
                                .add("2000-01-01")
                                .add(Long.parseLong("1111111111"))
                                .add(Long.parseLong("1111111111111"));
    final JsonArray result = SqlQueries.getParamsForAddBookQuery(book);

    for (int i = 0; i < expected.size(); i++) {
      Assertions.assertEquals(expected.getValue(i), result.getValue(i));
    }
  }
}
