package com.duoc.servicioNotificaciones.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitAuditoriaConfig {

    @Bean
    public Declarables auditoriaPedidoDeclarables(
            @Value("${app.rabbitmq.exchange}") String exchange,
            @Value("${app.rabbitmq.pedido-creado-routing-key}") String routingKey,
            @Value("${app.rabbitmq.dead-letter-exchange}") String dlx) {

        DirectExchange origen = new DirectExchange(exchange, true, false);
        DirectExchange errores = new DirectExchange(dlx, true, false);

        TopicExchange topic = new TopicExchange(
                "pedidos360.auditoria.eventos", true, false);

        Queue auditoria = QueueBuilder.durable("pedidos360.auditoria")
                .deadLetterExchange(dlx)
                .deadLetterRoutingKey("auditoria.dead")
                .build();

        Queue dlq = QueueBuilder.durable("pedidos360.auditoria.dlq")
                .build();

        return new Declarables(
                topic,
                auditoria,
                dlq,
                BindingBuilder.bind(topic).to(origen).with(routingKey),
                BindingBuilder.bind(auditoria).to(topic).with("pedido.*"),
                BindingBuilder.bind(dlq).to(errores).with("auditoria.dead")
        );
    }
}
