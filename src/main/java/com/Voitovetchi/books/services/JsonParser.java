package com.Voitovetchi.books.services;

import com.Voitovetchi.books.domain.Author;
import com.Voitovetchi.books.domain.Book;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {
  public static Book parseJsonObjectToBook(JsonObject body) {
    final Book book = new Book(body.getLong("ISBN"), body.getString("TITLE"), body.getString("PUBDATE"));
    final JsonArray authors = body.getJsonArray("AUTHORS");

    for(int i = 0; i < authors.size(); i++) {
      final Author author = new Author(authors.getJsonObject(i).getLong("IDNP"));
      book.getAuthors().add(author);
    }

    return book;
  }

  public static JsonArray parseToBooksWithAuthorsJsonArray(List<JsonObject> books) {
    final JsonArray booksWithAuthors = new JsonArray();
    final List<Long> added = new ArrayList<>();

    for (JsonObject book : books) {
      final JsonArray authors = new JsonArray();
      final Long currentBookIsbn = book.getLong("ISBN");

      for (JsonObject otherBook : books) {
        if (otherBook.getLong("ISBN").equals(currentBookIsbn)
          && !added.contains(currentBookIsbn)
        ) {
          JsonObject author = new JsonObject()
            .put("IDNP", otherBook.getLong("IDNP"))
            .put("NAME", otherBook.getString("NAME"))
            .put("SURNAME", otherBook.getString("SURNAME"))
            .put("BIRTHDATE", otherBook.getString("BIRTHDATE"));
          authors.add(author);
        }
      }

      if (!added.contains(currentBookIsbn)) {
        final JsonObject bookWithAuthor = new JsonObject()
          .put("ISBN", currentBookIsbn)
          .put("TITLE", book.getString("TITLE"))
          .put("PUBDATE", book.getString("PUBDATE"))
          .put("AUTHORS", authors);

        booksWithAuthors.add(bookWithAuthor);
        added.add(currentBookIsbn);
      }
    }

    return booksWithAuthors;
  }
}
