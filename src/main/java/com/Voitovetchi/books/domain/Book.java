package com.Voitovetchi.books.domain;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class Book {

  private long isbn;
  private String title;
  private String author;
  private String pubdate;
}
