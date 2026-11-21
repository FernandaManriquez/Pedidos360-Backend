package com.duoc.servicioKafkaAdmin;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KafkaGroupService {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaGroupService.class);

    private final Admin admin;

    public KafkaGroupService(Admin admin) {
        this.admin = admin;
    }

    public List<String> listar()
            throws InterruptedException, ExecutionException,
            TimeoutException {

        return admin.listConsumerGroups()
                .all()
                .get(10, TimeUnit.SECONDS)
                .stream()
                .map(grupo -> grupo.groupId())
                .filter(nombre -> nombre.startsWith("pedidos360-"))
                .sorted()
                .toList();
    }

    public Map<String, Object> consultar(String grupo)
            throws InterruptedException, ExecutionException,
            TimeoutException {

        validarGrupo(grupo);

        var descripcion = admin.describeConsumerGroups(List.of(grupo))
                .all()
                .get(10, TimeUnit.SECONDS)
                .get(grupo);

        var miembros = descripcion.members().stream()
                .map(miembro -> Map.<String, Object>of(
                        "consumerId", miembro.consumerId(),
                        "clientId", miembro.clientId(),
                        "host", miembro.host(),
                        "particiones",
                        miembro.assignment().topicPartitions().stream()
                                .sorted(Comparator
                                        .comparing(TopicPartition::topic)
                                        .thenComparingInt(
                                                TopicPartition::partition))
                                .map(particion -> Map.<String, Object>of(
                                        "topic", particion.topic(),
                                        "particion", particion.partition()))
                                .toList()))
                .toList();

        var offsets = admin.listConsumerGroupOffsets(grupo)
                .partitionsToOffsetAndMetadata()
                .get(10, TimeUnit.SECONDS);

        var posiciones = offsets.entrySet().stream()
                .filter(entrada -> entrada.getValue() != null)
                .sorted(Comparator
                        .comparing(
                                (Map.Entry<TopicPartition,
                                        org.apache.kafka.clients.consumer.OffsetAndMetadata>
                                        entrada) -> entrada.getKey().topic())
                        .thenComparingInt(
                                entrada -> entrada.getKey().partition()))
                .map(entrada -> Map.<String, Object>of(
                        "topic", entrada.getKey().topic(),
                        "particion", entrada.getKey().partition(),
                        "offsetConfirmado", entrada.getValue().offset()))
                .toList();

        return Map.of(
                "grupo", grupo,
                "estado", descripcion.groupState().toString(),
                "cantidadMiembros", miembros.size(),
                "miembros", miembros,
                "offsets", posiciones);
    }

    public void eliminar(String grupo)
            throws InterruptedException, ExecutionException,
            TimeoutException {

        validarGrupo(grupo);

        var descripcion = admin.describeConsumerGroups(List.of(grupo))
                .all()
                .get(10, TimeUnit.SECONDS)
                .get(grupo);

        if (!descripcion.members().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El grupo tiene consumidores activos. "
                            + "Detenlos antes de eliminarlo.");
        }

        admin.deleteConsumerGroups(List.of(grupo))
                .all()
                .get(10, TimeUnit.SECONDS);

        log.info("Grupo eliminado: grupo={}", grupo);
    }

    private void validarGrupo(String grupo) {
        if (grupo == null
                || grupo.length() > 255
                || !grupo.matches("pedidos360-[a-zA-Z0-9._-]+")) {

            throw new IllegalArgumentException(
                    "El grupo debe comenzar con pedidos360- "
                            + "y contener solo letras, numeros, puntos, "
                            + "guiones o guiones bajos.");
        }
    }
}