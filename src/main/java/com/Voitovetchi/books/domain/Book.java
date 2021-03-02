package com.voitovetchi.books.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class Book {
  public Book(long isbn, String title, String pubdate) {
    this.isbn = isbn;
    this.title = title;
    this.pubdate = pubdate;
    this.authors = new ArrayList<>();
  }

  private long isbn;
  private String title;
  private String pubdate;
  private List<Author> authors;
}
