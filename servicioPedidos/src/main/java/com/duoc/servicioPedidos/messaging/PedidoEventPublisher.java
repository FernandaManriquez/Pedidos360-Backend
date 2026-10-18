package com.duoc.servicioPedidos.messaging;

import com.duoc.servicioPedidos.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PedidoEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public PedidoEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.exchange}") String exchange,
            @Value("${app.rabbitmq.pedido-creado-routing-key}")
            String routingKey) {

        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publicarPedidoCreado(Pedido pedido) {

        PedidoCreadoEvent evento = new PedidoCreadoEvent(
                UUID.randomUUID().toString(),
                pedido.getId(),
                pedido.getUsuario(),
                pedido.getProductoId(),
                pedido.getCantidad(),
                pedido.getTotal(),
                pedido.getEstado(),
                pedido.getFecha()
        );

        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                evento
        );

        log.info(
                "Publicacion enviada: eventId={}, pedidoId={}",
                evento.eventId(),
                evento.pedidoId()
        );
    }
}