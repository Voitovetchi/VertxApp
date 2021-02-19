package com.Voitovetchi.books.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class Author {
  private long idnp;
  private String name;
  private String surname;
  private String birthdate;
}
