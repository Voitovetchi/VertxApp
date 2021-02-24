package com.Voitovetchi.books.testServices;

import com.Voitovetchi.books.domain.Author;
import com.Voitovetchi.books.domain.Book;
import com.Voitovetchi.books.services.JsonParser;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

public class TestJsonParser {
  @Test
  public void testParseJsonObjectToBook() {
    JsonObject author = new JsonObject()
      .put("IDNP", 1111111111111L);
    JsonObject bookJsonObject = new JsonObject()
      .put("ISBN", 1111111111)
      .put("TITLE", "testTitle")
      .put("PUBDATE", "2000-01-01")
      .put("AUTHORS", new JsonArray().add(author));

    Book expected = new Book(1111111111, "testTitle", "2000-01-01");
    expected.getAuthors().add(new Author(1111111111111L));

    Book result = JsonParser.parseJsonObjectToBook(bookJsonObject);

    Assertions.assertEquals(expected.getIsbn(), result.getIsbn());
    Assertions.assertTrue(expected.getTitle().equals(result.getTitle()));
    Assertions.assertTrue(expected.getPubdate().equals(result.getPubdate()));
    Assertions.assertEquals(expected.getAuthors().size(), result.getAuthors().size());
    Assertions.assertEquals(expected.getAuthors().get(0).getIdnp(), result.getAuthors().get(0).getIdnp());
  }

  @Test
  public void testParseToBooksWithAuthorsJsonArray() {
    List<JsonObject> input = new ArrayList<>();

    for (int i = 0; i < 2; i++) {
      input.add(new JsonObject().put("ISBN", 1111111111)
        .put("TITLE", "testTitle")
        .put("PUBDATE", "01-01-2000")
        .put("IDNP", 1111111111111L + i)
        .put("NAME", "testName" + i)
        .put("SURNAME", "testSurname" + i)
        .put("BIRTHDATE", "01-01-2000"));
    }

    final JsonArray result = JsonParser.parseToBooksWithAuthorsJsonArray(input);

    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(1111111111, result.getJsonObject(0).getLong("ISBN"));
    Assertions.assertTrue(result.getJsonObject(0).getString("TITLE").equals("testTitle"));
    Assertions.assertTrue(result.getJsonObject(0).getString("PUBDATE").equals("01-01-2000"));
    Assertions.assertEquals(2, result.getJsonObject(0).getJsonArray("AUTHORS").size());

    for (int i = 0; i < result.getJsonObject(0).getJsonArray("AUTHORS").size(); i++) {
      final JsonObject author = result.getJsonObject(0).getJsonArray("AUTHORS").getJsonObject(i);

      Assertions.assertEquals(1111111111111L + i, author.getLong("IDNP"));
      Assertions.assertTrue(author.getString("NAME").equals("testName" + i));
      Assertions.assertTrue(author.getString("SURNAME").equals("testSurname" + i));
      Assertions.assertTrue(author.getString("BIRTHDATE").equals("01-01-2000"));
    }
  }
}
