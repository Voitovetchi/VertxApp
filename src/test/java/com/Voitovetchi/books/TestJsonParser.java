package com.Voitovetchi.books;

import com.Voitovetchi.books.domain.Author;
import com.Voitovetchi.books.domain.Book;
import com.Voitovetchi.books.services.JsonParser;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestJsonParser {
  @Test
  public void testParseJsonObjectToBook() {
    JsonObject author = new JsonObject()
      .put("IDNP", 1111111111111L)
      .put("NAME", "testName")
      .put("SURNAME", "testSurname")
      .put("BIRTHDATE", "2000-01-01");
    JsonObject bookJsonObject = new JsonObject()
      .put("ISBN", 1111111111)
      .put("TITLE", "testTitle")
      .put("PUBDATE", "2000-01-01")
      .put("AUTHORS", new JsonArray().add(author));

    Book expected = new Book(1111111111, "testTitle", "2000-01-01");
    expected.getAuthors().add(new Author(1111111111111L, "testName", "testSurname", "2000-01-01"));

    Book result = JsonParser.parseJsonObjectToBook(bookJsonObject);

    Assertions.assertEquals(expected.getIsbn(), result.getIsbn());
    Assertions.assertTrue(expected.getTitle().equals(result.getTitle()));
    Assertions.assertTrue(expected.getPubdate().equals(result.getPubdate()));
    Assertions.assertEquals(expected.getAuthors().size(), result.getAuthors().size());
    Assertions.assertEquals(expected.getAuthors().get(0).getIdnp(), result.getAuthors().get(0).getIdnp());
    Assertions.assertTrue(expected.getAuthors().get(0).getName().equals(result.getAuthors().get(0).getName()));
    Assertions.assertTrue(expected.getAuthors().get(0).getSurname().equals(result.getAuthors().get(0).getSurname()));
    Assertions.assertTrue(expected.getAuthors().get(0).getBirthdate().equals(result.getAuthors().get(0).getBirthdate()));
  }
}
