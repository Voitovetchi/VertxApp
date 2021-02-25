package com.Voitovetchi.books.httpServers;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class JdbcPathParamVerticle extends AbstractHttpServer {

  @Override
  protected void getBookByIsbn(Router books) {
    books.get("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");
      getBookByIsbnRequest(req, isbn);
    });
  }

  @Override
  protected void updateBook(Router books) {
    books.put("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");
      updateBookRequest(req, isbn);
    });
  }


  @Override
  protected void deleteBook(Router books) {
    books.delete("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");
      deleteBookRequest(req, isbn);
    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new JdbcPathParamVerticle());
  }

}
