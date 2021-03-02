package com.voitovetchi.books.httpServers;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class JdbcPathParamVerticle extends AbstractHttpServer {

  private static final String URL = "/books/:isbn";
  public static final String PARAM = "isbn";

  @Override
  protected void getBookByIsbn(Router books) {
    books.get(URL).handler(req -> {
      final String isbn = req.pathParam(PARAM);
      getBookByIsbnRequest(req, isbn);
    });
  }

  @Override
  protected void updateBook(Router books) {
    books.put(URL).handler(req -> {
      final String isbn = req.pathParam(PARAM);
      updateBookRequest(req, isbn);
    });
  }


  @Override
  protected void deleteBook(Router books) {
    books.delete(URL).handler(req -> {
      final String isbn = req.pathParam(PARAM);
      deleteBookRequest(req, isbn);
    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new JdbcPathParamVerticle());
  }

}
