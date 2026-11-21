package com.duoc.servicioKafkaAdmin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka/topics")
public class KafkaTopicController {

    private final KafkaTopicService servicio;

    public KafkaTopicController(KafkaTopicService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<String> listar()
            throws InterruptedException, ExecutionException,
            TimeoutException {

        return servicio.listar();
    }

    @GetMapping("/{nombre}")
    public Map<String, Object> consultar(@PathVariable String nombre)
            throws InterruptedException, ExecutionException,
            TimeoutException {

        return servicio.consultar(nombre);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> crear(
            @Valid @RequestBody CrearTopicRequest solicitud)
            throws InterruptedException, ExecutionException,
            TimeoutException {

        servicio.crear(solicitud);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "mensaje", "Topic creado",
                        "nombre", solicitud.nombre()));
    }

    @DeleteMapping("/{nombre}")
    public ResponseEntity<Void> eliminar(@PathVariable String nombre)
            throws InterruptedException, ExecutionException,
            TimeoutException {

        servicio.eliminar(nombre);
        return ResponseEntity.noContent().build();
    }
}