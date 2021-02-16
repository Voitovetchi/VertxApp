package com.Voitovetchi.books.examples;

import com.Voitovetchi.books.jdbc.JdbcMainVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;

public class SqlInteractionExample extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    String url = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
    String driver = "oracle.jdbc.driver.OracleDriver";
    String user = "books_admin";
    String password = "password";

    final JsonObject config = new JsonObject();
    config.put("url", url);
    config.put("driver_class", driver);
    config.put("user", user);
    config.put("password", password);

    SQLClient client = JDBCClient.createShared(vertx, config);

    client.query("select * from book where isbn = 1", res -> {
      if (res.succeeded()) {
        // Get the result set
        System.out.println("success");
        ResultSet resultSet = res.result();
        System.out.println(resultSet.getNumRows());
      } else {
        System.out.println("error");
      }

    });

    startPromise.complete();
    /*JDBCPool pool = JDBCPool.pool(vertx, config);

    pool
      .query("SELECT * FROM test")
      .execute()
      .onFailure(e -> {
        System.out.println(e.getMessage());
      })
      .onSuccess(rows -> {
        System.out.println(rows.size());
      });

    startPromise.complete();
  }*/
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new SqlInteractionExample());
  }
}
