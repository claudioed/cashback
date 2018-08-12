package com.sensedia.cashback.infra.verticle;

import com.sensedia.cashback.infra.exception.DatabaseAutoCommitException;
import com.sensedia.cashback.infra.exception.DatabaseConnectionException;
import com.sensedia.cashback.infra.proto.RegisterCashbackRequest;
import com.sensedia.cashback.infra.proto.RegisterCashbackResponse;
import com.sensedia.cashback.infra.proto.RegisterCashbackTransactionGrpc;
import io.grpc.stub.StreamObserver;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * @author claudioed on 09/08/18.
 * Project cashback
 */
public class CashbackVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(CashbackVerticle.class);

  private static final String INSERT_CASHBACK_EVENT = "INSERT INTO cashback (id, type, user_id, user_email, store, order_id, order_total) values (?,?,?,?,?,?,?)";

  @Override
  public void start() throws IOException {

    final String dbHost = Optional.of(System.getenv("DB_HOST"))
        .orElseThrow(() -> new RuntimeException("DB_HOST not configured properly"));
    final String dbUser = Optional.of(System.getenv("DB_USER"))
        .orElseThrow(() -> new RuntimeException("DB_USER not configured properly"));
    final String dbPass = Optional.of(System.getenv("DB_PASS"))
        .orElseThrow(() -> new RuntimeException("DB_PASS not configured properly"));

    JsonObject postgreSQLClientConfig = new JsonObject().put("host", dbHost);
    SQLClient postgreSQLClient = PostgreSQLClient.createShared(vertx, postgreSQLClientConfig);

    RegisterCashbackTransactionGrpc.RegisterCashbackTransactionImplBase service = new RegisterCashbackTransactionGrpc.RegisterCashbackTransactionImplBase(){
      @Override
      public void register(RegisterCashbackRequest request,StreamObserver<RegisterCashbackResponse> response) {

        postgreSQLClient.getConnection(res ->{
          if(res.succeeded()){
            final SQLConnection connection = res.result();
            connection.setAutoCommit(true,resConnection ->{
              if(resConnection.succeeded()){
                LOGGER.info("Creating the new cashback register");
                final String cashbackId = UUID.randomUUID().toString();
                JsonArray params = new JsonArray().add(cashbackId).add(request.getType()).add(request.getUser().getId())
                    .add(request.getUser().getEmail()).add(request.getStore().getName())
                    .add(request.getOrder().getId()).add(request.getOrder().getTotal());
                connection.updateWithParams(INSERT_CASHBACK_EVENT,params,updateResultAsyncResult -> {
                  LOGGER.info("Cashback register created successfully.. ID: " + cashbackId);
                  response.onNext(RegisterCashbackResponse.newBuilder().setId(cashbackId).build());
                });
              }else{
                LOGGER.error("Error to configure auto-commit in postgresql driver");
                response.onError(new DatabaseAutoCommitException());
              }
            });
          }else{
            LOGGER.error("Error to get postgresql connection");
            response.onError(new DatabaseConnectionException());
          }
        });
      }
    };

    VertxServer rpcServer = VertxServerBuilder
        .forAddress(vertx, "localhost", 8080)
        .addService(service)
        .build();

    rpcServer.start();

  }
}