package com.sensedia.cashback.infra.verticle;

import com.sensedia.cashback.domain.RegisterCashback;
import com.sensedia.cashback.infra.exception.DatabaseConnectionException;
import com.sensedia.cashback.infra.proto.RegisterCashbackRequest;
import com.sensedia.cashback.infra.proto.RegisterCashbackResponse;
import com.sensedia.cashback.infra.proto.RegisterCashbackTransactionGrpc;
import io.grpc.stub.StreamObserver;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import java.io.IOException;
import java.util.UUID;
import lombok.val;

/**
 * @author claudioed on 09/08/18.
 * Project cashback
 */
public class CashbackVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(CashbackVerticle.class);

  @Override
  public void start() throws IOException {

    RegisterCashbackTransactionGrpc.RegisterCashbackTransactionImplBase service = new RegisterCashbackTransactionGrpc.RegisterCashbackTransactionImplBase(){
      @Override
      public void register(RegisterCashbackRequest request,StreamObserver<RegisterCashbackResponse> response) {
        final String cashbackId = UUID.randomUUID().toString();

        val requestData = RegisterCashback.builder().id(cashbackId).type(request.getType()).userId(request.getUser().getId())
            .userEmail(request.getUser().getEmail()).storeName(request.getStore().getName()).orderId(request.getOrder().getId())
            .orderTotal(request.getOrder().getTotal()).pointsEarned(request.getPointsEarned()).build();

        vertx.eventBus().<String>send("register.cashback", Json.encode(requestData),res ->{
          if(res.succeeded()){
            response.onNext(RegisterCashbackResponse.newBuilder().setId(cashbackId).build());
          }else{
            response.onError(new DatabaseConnectionException());
          }
        });
      }
    };

    VertxServer rpcServer = VertxServerBuilder
        .forAddress(vertx, "localhost", 8080)
        .addService(service)
        .build();

    vertx.deployVerticle(new RegisterCashbackVerticle());

    rpcServer.start();

  }
}