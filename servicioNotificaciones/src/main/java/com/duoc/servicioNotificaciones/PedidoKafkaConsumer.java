package com.duoc.servicioNotificaciones;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class PedidoKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoKafkaConsumer.class);

    private static final String TOPIC_ALERTAS = "pedidos360.alertas";

    private final KafkaTemplate<String, AlertaPedido> kafkaTemplate;
    private final PedidoAlertaService servicio;

    public PedidoKafkaConsumer(
            KafkaTemplate<String, AlertaPedido> kafkaTemplate,
            PedidoAlertaService servicio) {

        this.kafkaTemplate = kafkaTemplate;
        this.servicio = servicio;
    }

    @KafkaListener(
            id = "pedidos-alertas-listener",
            topics = "pedidos360.pedido-creado",
            groupId = "pedidos360-alertas-ea3")
    public void procesar(ConsumerRecord<String, PedidoCreado> registro)
            throws InterruptedException, ExecutionException {

        var resultadoEvaluacion =
                servicio.evaluar(registro.key(), registro.value());

        PedidoCreado pedido = registro.value();

        log.info(
                "Pedido validado: eventId={}, pedidoId={}, total={}, "
                        + "particion={}, offset={}",
                pedido.eventId(), pedido.pedidoId(), pedido.total(),
                registro.partition(), registro.offset());

        if (resultadoEvaluacion.isEmpty()) {
            log.info(
                    "Pedido sin alerta: eventId={}, pedidoId={}, total={}, "
                            + "particion={}, offset={}",
                    pedido.eventId(), pedido.pedidoId(), pedido.total(),
                    registro.partition(), registro.offset());
            return;
        }

        AlertaPedido alerta = resultadoEvaluacion.get();

        try {
            var resultado = kafkaTemplate
                    .send(TOPIC_ALERTAS, registro.key(), alerta)
                    .get();

            var metadata = resultado.getRecordMetadata();

            log.info(
                    "Alerta confirmada: eventId={}, pedidoId={}, "
                            + "particion={}, offset={}",
                    alerta.eventId(), alerta.pedidoId(),
                    metadata.partition(), metadata.offset());

        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw error;
        }
    }

    public record PedidoCreado(
            String eventId,
            Long pedidoId,
            String usuario,
            Long productoId,
            Integer cantidad,
            Integer total,
            String estado,
            LocalDateTime fecha) {
    }

    public record AlertaPedido(
            String eventId,
            Long pedidoId,
            Integer total,
            String severidad) {
    }
}