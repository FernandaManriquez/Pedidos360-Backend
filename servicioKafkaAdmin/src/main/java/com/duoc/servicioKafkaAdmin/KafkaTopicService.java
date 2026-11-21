package com.duoc.servicioKafkaAdmin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KafkaTopicService {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaTopicService.class);

    private final Admin admin;

    public KafkaTopicService(Admin admin) {
        this.admin = admin;
    }

    public List<String> listar()
            throws InterruptedException, ExecutionException,
            TimeoutException {

        return admin.listTopics()
                .names()
                .get(10, TimeUnit.SECONDS)
                .stream()
                .filter(nombre -> nombre.startsWith("pedidos360."))
                .sorted()
                .toList();
    }

    public Map<String, Object> consultar(String nombre)
            throws InterruptedException, ExecutionException,
            TimeoutException {

        validarNombre(nombre);

        var descripcion = admin.describeTopics(List.of(nombre))
                .allTopicNames()
                .get(10, TimeUnit.SECONDS)
                .get(nombre);

        var recurso = new ConfigResource(
                ConfigResource.Type.TOPIC, nombre);

        var configuracion = admin.describeConfigs(List.of(recurso))
                .all()
                .get(10, TimeUnit.SECONDS)
                .get(recurso);

        var particiones = descripcion.partitions().stream()
                .map(particion -> Map.<String, Object>of(
                        "particion", particion.partition(),
                        "lider", particion.leader() == null
                                ? -1 : particion.leader().id(),
                        "replicas", particion.replicas().stream()
                                .map(nodo -> nodo.id()).toList(),
                        "isr", particion.isr().stream()
                                .map(nodo -> nodo.id()).toList()))
                .toList();

        return Map.of(
                "nombre", nombre,
                "particiones", particiones,
                "retencionMs",
                configuracion.get("retention.ms").value(),
                "politicaLimpieza",
                configuracion.get("cleanup.policy").value(),
                "minInSyncReplicas",
                configuracion.get("min.insync.replicas").value());
    }

    public void crear(CrearTopicRequest solicitud)
            throws InterruptedException, ExecutionException,
            TimeoutException {

        validarNombre(solicitud.nombre());

        var topic = new NewTopic(
                solicitud.nombre(),
                solicitud.particiones(),
                solicitud.replicas().shortValue());

        topic.configs(Map.of(
                "retention.ms", solicitud.retencionMs().toString(),
                "cleanup.policy", solicitud.politicaLimpieza(),
                "min.insync.replicas",
                Integer.toString(Math.min(2, solicitud.replicas()))));

        admin.createTopics(List.of(topic))
                .all()
                .get(10, TimeUnit.SECONDS);

        log.info(
                "Topic creado: nombre={}, particiones={}, replicas={}, "
                        + "retencionMs={}, politicaLimpieza={}",
                solicitud.nombre(),
                solicitud.particiones(),
                solicitud.replicas(),
                solicitud.retencionMs(),
                solicitud.politicaLimpieza());
    }

    public void eliminar(String nombre)
            throws InterruptedException, ExecutionException,
            TimeoutException {

        validarNombre(nombre);

        admin.deleteTopics(List.of(nombre))
                .all()
                .get(10, TimeUnit.SECONDS);

        log.info("Eliminacion de topic confirmada: nombre={}", nombre);
    }

    private void validarNombre(String nombre) {
        if (nombre == null
                || nombre.length() > 249
                || !nombre.matches("pedidos360\\.[a-zA-Z0-9._-]+")) {

            throw new IllegalArgumentException(
                    "El nombre debe comenzar con pedidos360. "
                            + "y contener solo letras, numeros, puntos, "
                            + "guiones o guiones bajos.");
        }
    }
}