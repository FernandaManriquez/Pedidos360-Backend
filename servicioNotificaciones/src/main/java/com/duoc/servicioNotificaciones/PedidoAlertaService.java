package com.duoc.servicioNotificaciones;

import java.util.Optional;

import com.duoc.servicioNotificaciones.PedidoKafkaConsumer.AlertaPedido;
import com.duoc.servicioNotificaciones.PedidoKafkaConsumer.PedidoCreado;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("kafka")
public class PedidoAlertaService {

    private static final int UMBRAL_ALERTA = 10_000;

    public Optional<AlertaPedido> evaluar(
            String clave, PedidoCreado pedido) {

        validar(clave, pedido);

        if (pedido.total() <= UMBRAL_ALERTA) {
            return Optional.empty();
        }

        return Optional.of(new AlertaPedido(
                pedido.eventId(),
                pedido.pedidoId(),
                pedido.total(),
                "ALTA"));
    }

    private void validar(String clave, PedidoCreado pedido) {
        if (pedido == null
                || pedido.eventId() == null
                || pedido.eventId().isBlank()
                || pedido.pedidoId() == null
                || pedido.pedidoId() <= 0
                || !pedido.pedidoId().toString().equals(clave)
                || pedido.usuario() == null
                || pedido.usuario().isBlank()
                || pedido.productoId() == null
                || pedido.productoId() <= 0
                || pedido.cantidad() == null
                || pedido.cantidad() <= 0
                || pedido.total() == null
                || pedido.total() < 0
                || pedido.estado() == null
                || pedido.estado().isBlank()
                || pedido.fecha() == null) {

            throw new IllegalArgumentException(
                    "Pedido invalido: revisa sus campos obligatorios "
                            + "y la coincidencia entre clave y pedidoId.");
        }
    }
}