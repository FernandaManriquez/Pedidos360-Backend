package com.duoc.servicioPedidos.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class PedidoKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoKafkaPublisher.class);
    private static final String TOPIC = "pedidos360.pedido-creado";

    private final KafkaTemplate<String, PedidoCreadoEvent> kafkaTemplate;

    public PedidoKafkaPublisher(KafkaTemplate<String, PedidoCreadoEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(PedidoCreadoEvent evento) {
        String clave = evento.pedidoId().toString();

        try {
            kafkaTemplate.send(TOPIC, clave, evento).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.error("Fallo al publicar en Kafka: topic={}, eventId={}, pedidoId={}",
                            TOPIC, evento.eventId(), evento.pedidoId(), error);
                    return;
                }

                var metadata = resultado.getRecordMetadata();
                log.info("Kafka confirmado: topic={}, eventId={}, pedidoId={}, particion={}, offset={}",
                        metadata.topic(), evento.eventId(), evento.pedidoId(),
                        metadata.partition(), metadata.offset());
            });
        } catch (RuntimeException error) {
            log.error("No se pudo iniciar el envio a Kafka: topic={}, eventId={}, pedidoId={}",
                    TOPIC, evento.eventId(), evento.pedidoId(), error);
        }
    }
}