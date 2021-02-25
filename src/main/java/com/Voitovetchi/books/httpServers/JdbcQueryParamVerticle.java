package com.Voitovetchi.books.httpServers;

import com.Voitovetchi.books.repository.JdbcBookRepository;
import com.Voitovetchi.books.services.JsonParser;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class JdbcQueryParamVerticle extends AbstractHttpServer {

  @Override
  protected void getBookByIsbn(Router books) {
    books.get("/books/getByIsbn").handler(req -> {
      final String isbn = req.queryParam("isbn").get(0);
      getBookByIsbnRequest(req, isbn);
    });
  }

  @Override
  protected void updateBook(Router books) {
    books.put("/books/updateByIsbn").handler(req -> {
      final String isbn = req.queryParam("isbn").get(0);
      updateBookRequest(req, isbn);
    });
  }

  @Override
  protected void deleteBook(Router books) {
    books.delete("/books/deleteByIsbn").handler(req -> {
      final String isbn = req.queryParam("isbn").get(0);
      deleteBookRequest(req, isbn);
    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new JdbcQueryParamVerticle());
  }
}
