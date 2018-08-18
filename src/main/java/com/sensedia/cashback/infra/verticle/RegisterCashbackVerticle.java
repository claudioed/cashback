package com.sensedia.cashback.infra.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import java.util.Optional;
import java.util.UUID;

/**
 * @author claudioed on 09/08/18.
 * Project cashback
 */
public class RegisterCashbackVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterCashbackVerticle.class);

  private static final String INSERT_CASHBACK_EVENT = "INSERT INTO cashback (id, type, user_id, user_email, store, order_id, order_total) values (?,?,?,?,?,?,?)";

  @Override
  public void start() {

    final String dbHost = Optional.of(System.getenv("DB_HOST"))
        .orElseThrow(() -> new RuntimeException("DB_HOST not configured properly"));
    final String dbUser = Optional.of(System.getenv("DB_USER"))
        .orElseThrow(() -> new RuntimeException("DB_USER not configured properly"));
    final String dbPass = Optional.of(System.getenv("DB_PASS"))
        .orElseThrow(() -> new RuntimeException("DB_PASS not configured properly"));
    final String database = Optional.of(System.getenv("DB_DATABASE"))
        .orElseThrow(() -> new RuntimeException("DB_DATABASE not configured properly"));

    JsonObject postgreSQLClientConfig = new JsonObject().put("host", dbHost).put("username", dbUser)
        .put("password", dbPass).put("charset", "utf8").put("port", 5432)
        .put("database", database);

    SQLClient postgreSQLClient = PostgreSQLClient.createNonShared(vertx, postgreSQLClientConfig);
    vertx.eventBus().<JsonArray>consumer("register.cashback", handler -> {
      final JsonArray params = handler.body();
      postgreSQLClient.getConnection(res -> {
        if (res.succeeded()) {
          final SQLConnection connection = res.result();
          connection.setAutoCommit(true, resConnection -> {
            if (resConnection.succeeded()) {
              LOGGER.info("Creating the new cashback register");
              final String cashbackId = UUID.randomUUID().toString();
              connection
                  .updateWithParams(INSERT_CASHBACK_EVENT, params, updateResultAsyncResult -> {
                    LOGGER.info("Cashback register created successfully.. ID: " + cashbackId);

                    handler.reply(new JsonObject().put("id", cashbackId));

                  });
            } else {
              LOGGER.error("Error to configure auto-commit in postgresql driver");
              handler.fail(5001, "Error to configure auto-commit in postgresql driver");
            }
          });
        } else {
          LOGGER.error("Error to get postgresql connection");
          handler.fail(5002, "Error to get postgresql connection");
        }
      });

    });
  }

}