package com.duoc.servicioNotificaciones.messaging;

import java.time.LocalDateTime;

public record PedidoCreadoEvent(
        String eventId,
        Long pedidoId,
        String usuario,
        Long productoId,
        Integer cantidad,
        Integer total,
        String estado,
        LocalDateTime fecha
) {
}