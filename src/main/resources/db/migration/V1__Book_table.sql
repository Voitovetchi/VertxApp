CREATE TABLE book (
  ISBN numeric PRIMARY KEY,
  TITLE varchar(100) NOT NULL,
  AUTHOR varchar(100) NOT NULL,
  PUBDATE date NOT NULL
);