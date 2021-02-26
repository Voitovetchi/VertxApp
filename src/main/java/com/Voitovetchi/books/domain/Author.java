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
  public Author(long idnp) {
    this.idnp = idnp;
  }

  private long idnp;
  private String name;
  private String surname;
  private String birthdate;
}
