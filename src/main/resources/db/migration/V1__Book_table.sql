CREATE TABLE book (
  isbn NUMERIC(10),
  title VARCHAR2(50) NOT NULL,
  author VARCHAR2(50) NOT NULL,
  pubdate DATE NOT NULL,
  PRIMARY KEY (isbn)
);
