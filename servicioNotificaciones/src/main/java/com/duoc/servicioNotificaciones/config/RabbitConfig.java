package com.duoc.servicioNotificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.pedido-creado-routing-key}")
    private String pedidoCreadoRoutingKey;

    @Value("${app.rabbitmq.notificaciones-queue}")
    private String notificacionesQueueName;

    @Value("${app.rabbitmq.tickets-queue}")
    private String ticketsQueueName;

    @Value("${app.rabbitmq.dead-letter-exchange}")
    private String deadLetterExchangeName;

    @Value("${app.rabbitmq.notificaciones-dlq}")
    private String notificacionesDlqName;

    @Value("${app.rabbitmq.tickets-dlq}")
    private String ticketsDlqName;

    @Value("${app.rabbitmq.notificaciones-dead-routing-key}")
    private String notificacionesDeadRoutingKey;

    @Value("${app.rabbitmq.tickets-dead-routing-key}")
    private String ticketsDeadRoutingKey;

    // Exchange principal para los eventos de pedidos.
    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(exchange, true, false);
    }

    // Exchange para enrutar mensajes rechazados hacia sus DLQ.
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchangeName, true, false);
    }

    // Cola de notificaciones y su ruta para mensajes fallidos.
    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(notificacionesQueueName)
                .deadLetterExchange(deadLetterExchangeName)
                .deadLetterRoutingKey(notificacionesDeadRoutingKey)
                .build();
    }

    // Cola de tickets y su ruta para mensajes fallidos.
    @Bean
    public Queue ticketsQueue() {
        return QueueBuilder.durable(ticketsQueueName)
                .deadLetterExchange(deadLetterExchangeName)
                .deadLetterRoutingKey(ticketsDeadRoutingKey)
                .build();
    }

    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(notificacionesDlqName).build();
    }

    @Bean
    public Queue ticketsDlq() {
        return QueueBuilder.durable(ticketsDlqName).build();
    }

    // pedido.creado se entrega a la cola de notificaciones.
    @Bean
    public Binding notificacionesBinding() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(pedidosExchange())
                .with(pedidoCreadoRoutingKey);
    }

    // El mismo evento tambien se entrega a la cola de tickets.
    @Bean
    public Binding ticketsBinding() {
        return BindingBuilder.bind(ticketsQueue())
                .to(pedidosExchange())
                .with(pedidoCreadoRoutingKey);
    }

    // Mensajes fallidos de notificaciones hacia su DLQ.
    @Bean
    public Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq())
                .to(deadLetterExchange())
                .with(notificacionesDeadRoutingKey);
    }

    // Mensajes fallidos de tickets hacia su DLQ.
    @Bean
    public Binding ticketsDlqBinding() {
        return BindingBuilder.bind(ticketsDlq())
                .to(deadLetterExchange())
                .with(ticketsDeadRoutingKey);
    }
}