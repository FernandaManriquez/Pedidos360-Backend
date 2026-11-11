package com.duoc.servicioNotificaciones.messaging.notificaciones;

import com.duoc.servicioNotificaciones.messaging.PedidoCreadoEvent;
import com.duoc.servicioNotificaciones.messaging.ProcesadorMensaje;
import com.duoc.servicioNotificaciones.service.ProcesamientoPedidoService;
import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NotificacionConsumer {

    private final ProcesamientoPedidoService servicio;
    private final ProcesadorMensaje procesador;

    public NotificacionConsumer(
            ProcesamientoPedidoService servicio,
            ProcesadorMensaje procesador) {

        this.servicio = servicio;
        this.procesador = procesador;
    }

    @RabbitListener(
            queues = "${app.rabbitmq.notificaciones-queue}",
            ackMode = "MANUAL"
    )
    public void recibir(
            PedidoCreadoEvent evento,
            Message mensaje,
            Channel canal) throws IOException {

        procesador.procesar(
                mensaje,
                canal,
                "NOTIFICACIONES",
                () -> servicio.registrarNotificacion(evento)
        );
    }
}