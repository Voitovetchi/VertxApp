package com.Voitovetchi.books.httpServers;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class JdbcHeaderParamVerticle extends AbstractHttpServer {

  @Override
  protected void getBookByIsbn(Router books) {
    books.get("/books/getByIsbn").handler(req -> {
      final String isbn = req.request().getHeader("isbn");
      getBookByIsbnRequest(req, isbn);
    });
  }

  @Override
  protected void updateBook(Router books) {
    books.put("/books/updateByIsbn").handler(req -> {
      final String isbn = req.request().getHeader("isbn");
      updateBookRequest(req, isbn);
    });
  }

  @Override
  protected void deleteBook(Router books) {
    books.delete("/books/deleteByIsbn").handler(req -> {
      final String isbn = req.request().getHeader("isbn");
      deleteBookRequest(req, isbn);
    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new JdbcHeaderParamVerticle());
  }
}
