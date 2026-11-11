package com.duoc.kafkalab;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LabPublisher {
    private final KafkaTemplate<String, String> kafka;

    public LabPublisher(KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
    }

    public Publication send(String topic, String key, String value) {
        try {
            RecordMetadata metadata = kafka.send(topic, key, value)
                    .get(10, TimeUnit.SECONDS)
                    .getRecordMetadata();

            return new Publication(metadata.topic(), metadata.partition(), metadata.offset(), key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Envio interrumpido", e);
        } catch (ExecutionException | TimeoutException
                 | org.springframework.kafka.KafkaException
                 | org.apache.kafka.common.KafkaException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Kafka no confirmo el envio", e);
        }
    }

    public record Publication(String topic, int partition, long offset, String key) {}
}
