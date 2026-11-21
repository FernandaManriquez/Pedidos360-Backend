package com.duoc.servicioNotificaciones;

import com.duoc.servicioNotificaciones.PedidoKafkaConsumer.AlertaPedido;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class AlertaKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(AlertaKafkaConsumer.class);

    private final AlertaAuditoriaService servicio;

    public AlertaKafkaConsumer(AlertaAuditoriaService servicio) {
        this.servicio = servicio;
    }

    @KafkaListener(
            id = "alertas-auditoria-listener",
            topics = "pedidos360.alertas",
            groupId = "pedidos360-auditoria-ea3",
            properties = "spring.json.value.default.type="
                    + "com.duoc.servicioNotificaciones."
                    + "PedidoKafkaConsumer$AlertaPedido")
    public void recibir(ConsumerRecord<String, AlertaPedido> registro) {

        servicio.validar(registro.key(), registro.value());

        AlertaPedido alerta = registro.value();

        log.info(
                "Alerta recibida: eventId={}, pedidoId={}, total={}, "
                        + "severidad={}, particion={}, offset={}",
                alerta.eventId(),
                alerta.pedidoId(),
                alerta.total(),
                alerta.severidad(),
                registro.partition(),
                registro.offset());
    }
}