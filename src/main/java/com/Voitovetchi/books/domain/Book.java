package com.Voitovetchi.books.domain;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class Book {

  private long isbn;
  private String title;
  private String author;
  private Date pubDate;
}
