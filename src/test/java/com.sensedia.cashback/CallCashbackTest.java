package com.sensedia.cashback;

import com.sensedia.cashback.infra.proto.Order;
import com.sensedia.cashback.infra.proto.RegisterCashbackRequest;
import com.sensedia.cashback.infra.proto.RegisterCashbackTransactionGrpc;
import com.sensedia.cashback.infra.proto.RegisterCashbackTransactionGrpc.RegisterCashbackTransactionVertxStub;
import com.sensedia.cashback.infra.proto.Store;
import com.sensedia.cashback.infra.proto.User;
import io.grpc.ManagedChannel;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxChannelBuilder;
import org.junit.Test;

/**
 * @author claudioed on 18/08/18.
 * Project cashback
 */
public class CallCashbackTest {

  @Test
  public void simpleCall() throws InterruptedException {

    final Vertx vertx = Vertx.vertx();
    ManagedChannel channel = VertxChannelBuilder
        .forAddress(vertx, "localhost", 8080)
        .usePlaintext(true)
        .build();

    final RegisterCashbackTransactionVertxStub stub = RegisterCashbackTransactionGrpc
        .newVertxStub(channel);

    final RegisterCashbackRequest cashbackRequest = RegisterCashbackRequest.newBuilder()
        .setOrder(Order.newBuilder().setId("AB").setTotal(10D).build()).setUser(
            User.newBuilder().setEmail("joe@joe.com").setId("joe").build())
        .setStore(Store.newBuilder().setName("ABC").build()).setType("register").build();

    stub.register(cashbackRequest,ar ->{
      if (ar.succeeded()){
        System.out.println("Got the server response: " + ar.result().getId());
      } else {
        System.out.println("Coult not reach server " + ar.cause().getMessage());
      }
    });

    Thread.sleep(5000);

  }

}
