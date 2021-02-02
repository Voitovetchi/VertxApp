package com.Voitovetchi.books.repo;

import com.Voitovetchi.books.domain.Book;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InMemoryBookStore {
  private Map<Long, Book> books = new HashMap<>();

  public InMemoryBookStore() {
    books.put(1l, new Book(1l, "book1", "author1", new Date(80, 0, 1)));
    books.put(2l, new Book(2l, "book2", "author2", new Date(80, 0, 2)));
  }

  public JsonArray getAll() {
    JsonArray all = new JsonArray();
    books.values().forEach(book -> {
      all.add(JsonObject.mapFrom(book));
    });
    return all;
  }

  public Book getByIsbn(String isbn) {
    Long key = Long.parseLong(isbn);
    return books.get(key);
  }

  public void add(Book entry) {
    books.put(entry.getIsbn(), entry);
  }

  public Book update(String isbn, Book entry) {
    Long key = Long.parseLong(isbn);

    if (key != entry.getIsbn()) {
      throw new IllegalArgumentException("isbn does not match");
    } else {
      books.put(key, entry);
    }

    return entry;
  }

  public Book delete(String isbn) {
    Long key = Long.parseLong(isbn);
    return books.remove(key);
  }
}
