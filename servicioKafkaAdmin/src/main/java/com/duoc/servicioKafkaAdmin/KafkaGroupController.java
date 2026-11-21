package com.duoc.servicioKafkaAdmin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka/groups")
public class KafkaGroupController {

    private final KafkaGroupService servicio;

    public KafkaGroupController(KafkaGroupService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<String> listar()
            throws InterruptedException, ExecutionException,
            TimeoutException {

        return servicio.listar();
    }

    @GetMapping("/{grupo}")
    public Map<String, Object> consultar(@PathVariable String grupo)
            throws InterruptedException, ExecutionException,
            TimeoutException {

        return servicio.consultar(grupo);
    }

    @DeleteMapping("/{grupo}")
    public ResponseEntity<Void> eliminar(@PathVariable String grupo)
            throws InterruptedException, ExecutionException,
            TimeoutException {

        servicio.eliminar(grupo);
        return ResponseEntity.noContent().build();
    }
}