package com.sensedia.cashback.infra.verticle;

import com.sensedia.cashback.domain.PointsEarned;
import com.sensedia.cashback.domain.RegisterCashback;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author claudioed on 09/08/18.
 * Project cashback
 */
public class NotifyTransactionVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotifyTransactionVerticle.class);

  @Override
  public void start() {
    final String kafkaHost = Optional.of(System.getenv("KAFKA_HOST"))
        .orElseThrow(() -> new RuntimeException("KAFKA_HOST not configured properly"));
    Map<String, String> config = new HashMap<>();
    config.put("bootstrap.servers", kafkaHost);
    config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    config.put("acks", "1");
    final KafkaProducer<String, String> producer = KafkaProducer.create(vertx, config);
    vertx.eventBus().<String>consumer("notify.credit.card", handler -> {
      final RegisterCashback registerCashback = Json
          .decodeValue(handler.body(), RegisterCashback.class);
      LOGGER.info("Register Cashback");
      final PointsEarned pointsEarned = PointsEarned.builder().id(registerCashback.getId())
          .pointsEarned(registerCashback.getPointsEarned())
          .storeName(registerCashback.getStoreName()).userEmail(registerCashback.getUserEmail())
          .userId(registerCashback.getUserId()).build();
      KafkaProducerRecord<String, String> record = KafkaProducerRecord
          .create("points-earned", Json.encode(pointsEarned));
      producer.write(record, callback -> {
        LOGGER.info("KAFKA " + callback.succeeded());
      });
    });

  }

}