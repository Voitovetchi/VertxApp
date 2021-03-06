CREATE TABLE BOOK (
  ISBN NUMERIC(10),
  TITLE VARCHAR2(50) NOT NULL,
  PUBDATE DATE NOT NULL,
  CONSTRAINT BOOK_PK PRIMARY KEY (ISBN)
);

CREATE TABLE AUTHOR (
  IDNP NUMERIC(13) NOT NULL,
  NAME VARCHAR2(50) NOT NULL,
  SURNAME VARCHAR2(50) NOT NULL,
  BIRTHDATE DATE NOT NULL,
  CONSTRAINT AUTHOR_PK PRIMARY KEY (IDNP)
);

CREATE TABLE BOOK_AUTHOR (
	ISBN NUMERIC(10) NOT NULL,
	IDNP NUMERIC(13) NOT NULL
);

ALTER TABLE BOOK_AUTHOR ADD CONSTRAINT BOOK_AUTHOR_ISBN FOREIGN KEY (ISBN)
	  REFERENCES BOOK (ISBN) ON DELETE CASCADE ENABLE;
ALTER TABLE BOOK_AUTHOR ADD CONSTRAINT BOOK_AUTHOR_IDNP FOREIGN KEY (IDNP)
	  REFERENCES AUTHOR (IDNP) ON DELETE CASCADE ENABLE;


INSERT ALL
    INTO book (isbn, title, pubdate) VALUES (7845692132, 'PEACE AND WAR', DATE '2000-01-01')
    INTO book (isbn, title, pubdate) VALUES (4236598715, 'GOOD OMENS', DATE '2010-11-21')
    INTO book (isbn, title, pubdate) VALUES (3256303329, 'ANNA CORENINA', DATE '2000-02-02')
    INTO book (isbn, title, pubdate) VALUES (4632519856, 'HEADS YOU LOSE', DATE '1999-08-25')
SELECT * FROM dual;

INSERT ALL
    INTO author (idnp, name, surname, birthdate) VALUES (7856941235468, 'LEV', 'TOLSTOI', DATE '1828-09-09')
    INTO author (idnp, name, surname, birthdate) VALUES (5632149568723, 'NEIL', 'GAIMAN', DATE '1960-10-11')
    INTO author (idnp, name, surname, birthdate) VALUES (2356149856325, 'LISA', 'LUTZ', DATE '1970-03-13')
    INTO author (idnp, name, surname, birthdate) VALUES (4236598213656, 'TERRY', 'PRATCHETT', DATE '1948-04-26')
    INTO author (idnp, name, surname, birthdate) VALUES (5632878941265, 'DAVID', 'HAYWORD', DATE '1950-08-30')
SELECT * FROM dual;

INSERT ALL
    INTO book_author (isbn, idnp) VALUES (7845692132, 7856941235468)
    INTO book_author (isbn, idnp) VALUES (4236598715, 5632149568723)
    INTO book_author (isbn, idnp) VALUES (4236598715, 4236598213656)
    INTO book_author (isbn, idnp) VALUES (3256303329, 7856941235468)
    INTO book_author (isbn, idnp) VALUES (4632519856, 2356149856325)
    INTO book_author (isbn, idnp) VALUES (4632519856, 5632878941265)
SELECT * FROM dual;
