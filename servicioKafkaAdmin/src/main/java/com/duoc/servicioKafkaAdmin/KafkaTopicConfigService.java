package com.duoc.servicioKafkaAdmin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KafkaTopicConfigService {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaTopicConfigService.class);

    private final Admin admin;
    private final KafkaTopicService topics;

    public KafkaTopicConfigService(Admin admin, KafkaTopicService topics) {
        this.admin = admin;
        this.topics = topics;
    }

    public Map<String, Object> actualizar(
            String nombre,
            ActualizarTopicConfigRequest solicitud)
            throws InterruptedException, ExecutionException, TimeoutException {

        if (solicitud.retencionMs() == null
                && solicitud.politicaLimpieza() == null) {
            throw new IllegalArgumentException(
                    "Indica retencionMs o politicaLimpieza.");
        }

        // Reutiliza la validacion del nombre y comprueba que el topic existe.
        topics.consultar(nombre);

        List<AlterConfigOp> operaciones = new ArrayList<>();

        if (solicitud.retencionMs() != null) {
            operaciones.add(new AlterConfigOp(
                    new ConfigEntry(
                            "retention.ms",
                            solicitud.retencionMs().toString()),
                    AlterConfigOp.OpType.SET));
        }

        if (solicitud.politicaLimpieza() != null) {
            operaciones.add(new AlterConfigOp(
                    new ConfigEntry(
                            "cleanup.policy",
                            solicitud.politicaLimpieza()),
                    AlterConfigOp.OpType.SET));
        }

        ConfigResource recurso = new ConfigResource(
                ConfigResource.Type.TOPIC, nombre);

        admin.incrementalAlterConfigs(Map.of(recurso, operaciones))
                .all()
                .get(10, TimeUnit.SECONDS);

        log.info(
                "Configuracion actualizada: topic={}, retencionMs={}, politicaLimpieza={}",
                nombre, solicitud.retencionMs(), solicitud.politicaLimpieza());

        return topics.consultar(nombre);
    }
}
