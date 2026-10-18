package com.duoc.servicioRabbitAdmin.service;

import com.duoc.servicioRabbitAdmin.dto.*;
import com.duoc.servicioRabbitAdmin.exception.RecursoNoEncontradoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;

@Service
public class RabbitAdminService {

    private static final Logger log =
            LoggerFactory.getLogger(RabbitAdminService.class);

    private final RabbitAdmin admin;

    public RabbitAdminService(RabbitAdmin admin) {
        this.admin = admin;
    }

    public void crearCola(QueueRequest datos) {
        admin.declareQueue(new Queue(
                datos.name(),
                datos.durable(),
                false,
                datos.autoDelete()
        ));
        log.info("Cola declarada: {}", datos.name());
    }

    public Map<String, Object> consultarCola(String nombre) {
        validarNombre(nombre);
        Properties datos = admin.getQueueProperties(nombre);

        if (datos == null) {
            throw new RecursoNoEncontradoException(
                    "No existe la cola: " + nombre
            );
        }

        return Map.of(
                "name", nombre,
                "messages", datos.get(RabbitAdmin.QUEUE_MESSAGE_COUNT),
                "consumers", datos.get(RabbitAdmin.QUEUE_CONSUMER_COUNT)
        );
    }

    public void eliminarCola(String nombre) {
        consultarCola(nombre);

        // Solo elimina colas vacias y sin consumidores.
        admin.deleteQueue(nombre, true, true);

        log.info("Cola eliminada: {}", nombre);
    }

    public void crearExchange(ExchangeRequest datos) {
        Exchange exchange = switch (datos.type()) {
            case "direct" -> new DirectExchange(
                    datos.name(), datos.durable(), datos.autoDelete()
            );
            case "topic" -> new TopicExchange(
                    datos.name(), datos.durable(), datos.autoDelete()
            );
            case "fanout" -> new FanoutExchange(
                    datos.name(), datos.durable(), datos.autoDelete()
            );
            default -> throw new IllegalArgumentException(
                    "Tipo de exchange no admitido"
            );
        };

        admin.declareExchange(exchange);
        log.info("Exchange declarado: {}", datos.name());
    }

    public void eliminarExchange(String nombre) {
        validarNombre(nombre);
        admin.deleteExchange(nombre);
        log.info("Exchange eliminado: {}", nombre);
    }

    public void crearBinding(BindingRequest datos) {
        consultarCola(datos.queue());
        admin.declareBinding(construirBinding(datos));
        log.info("Binding declarado: exchange={}, cola={}, clave={}",
                datos.exchange(), datos.queue(), datos.routingKey());
    }

    public void eliminarBinding(BindingRequest datos) {
        admin.removeBinding(construirBinding(datos));
        log.info("Binding eliminado: exchange={}, cola={}, clave={}",
                datos.exchange(), datos.queue(), datos.routingKey());
    }

    private Binding construirBinding(BindingRequest datos) {
        return new Binding(
                datos.queue(),
                Binding.DestinationType.QUEUE,
                datos.exchange(),
                datos.routingKey(),
                null
        );
    }

    private void validarNombre(String nombre) {
        if (nombre == null
                || nombre.length() > 255
                || !nombre.matches(RabbitValidation.NAME_PATTERN)) {

            throw new IllegalArgumentException(
                    "Nombre de recurso invalido o reservado"
            );
        }
    }
}
