package com.duoc.servicioPedidos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitPublisherConfig {
    private static final Logger log =
            LoggerFactory.getLogger(RabbitPublisherConfig.class);

    @Bean
    SmartInitializingSingleton configurarConfirmaciones(RabbitTemplate template) {
        return () -> {
            template.setMandatory(true);
            template.setConfirmCallback((correlation, ack, cause) -> {
                if (ack) {
                    log.info("RabbitMQ confirmo la publicacion (publisher ACK)");
                } else {
                    log.error("RabbitMQ no confirmo la publicacion: {}", cause);
                }
            });
            template.setReturnsCallback(returned ->
                log.error(
                    "Mensaje sin ruta: exchange={}, routingKey={}, codigo={}, motivo={}",
                    returned.getExchange(), returned.getRoutingKey(),
                    returned.getReplyCode(), returned.getReplyText()
                )
            );
        };
    }
}
