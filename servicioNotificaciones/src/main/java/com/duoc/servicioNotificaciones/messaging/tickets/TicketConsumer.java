package com.duoc.servicioNotificaciones.messaging.tickets;

import com.duoc.servicioNotificaciones.messaging.PedidoCreadoEvent;
import com.duoc.servicioNotificaciones.messaging.ProcesadorMensaje;
import com.duoc.servicioNotificaciones.service.ProcesamientoPedidoService;
import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TicketConsumer {

    private final ProcesamientoPedidoService servicio;
    private final ProcesadorMensaje procesador;

    public TicketConsumer(
            ProcesamientoPedidoService servicio,
            ProcesadorMensaje procesador) {

        this.servicio = servicio;
        this.procesador = procesador;
    }

    @RabbitListener(
            queues = "${app.rabbitmq.tickets-queue}",
            ackMode = "MANUAL"
    )
    public void recibir(
            PedidoCreadoEvent evento,
            Message mensaje,
            Channel canal) throws IOException {

        procesador.procesar(
                mensaje,
                canal,
                "TICKETS",
                () -> servicio.generarTicket(evento)
        );
    }
}