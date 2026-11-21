package com.duoc.servicioNotificaciones;

import com.duoc.servicioNotificaciones.PedidoKafkaConsumer.AlertaPedido;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("kafka")
public class AlertaAuditoriaService {

    public void validar(String clave, AlertaPedido alerta) {
        if (alerta == null
                || alerta.eventId() == null
                || alerta.eventId().isBlank()
                || alerta.pedidoId() == null
                || alerta.pedidoId() <= 0
                || !alerta.pedidoId().toString().equals(clave)
                || alerta.total() == null
                || alerta.total() <= 10_000
                || !"ALTA".equals(alerta.severidad())) {

            throw new IllegalArgumentException(
                    "Alerta invalida: debe corresponder a un pedido "
                            + "con total mayor a 10000, severidad ALTA "
                            + "y clave igual al pedidoId.");
        }
    }
}