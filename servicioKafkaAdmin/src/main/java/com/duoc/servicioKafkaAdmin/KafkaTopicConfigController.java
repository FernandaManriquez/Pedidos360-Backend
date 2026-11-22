package com.duoc.servicioKafkaAdmin;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka/topics")
public class KafkaTopicConfigController {

    private final KafkaTopicConfigService servicio;

    public KafkaTopicConfigController(KafkaTopicConfigService servicio) {
        this.servicio = servicio;
    }

    @PatchMapping("/{nombre}/config")
    public Map<String, Object> actualizar(
            @PathVariable String nombre,
            @Valid @RequestBody ActualizarTopicConfigRequest solicitud)
            throws InterruptedException, ExecutionException, TimeoutException {

        return servicio.actualizar(nombre, solicitud);
    }
}
