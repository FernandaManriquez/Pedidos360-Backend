package com.duoc.kafkalab;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class BancoController {
    private final LabPublisher publisher;
    private final ObjectMapper mapper;

    public BancoController(LabPublisher publisher, ObjectMapper mapper) {
        this.publisher = publisher;
        this.mapper = mapper;
    }

    @PostMapping("/api/banco/transaccion")
    public ResponseEntity<LabPublisher.Publication> send(@RequestBody Transaccion input)
            throws JsonProcessingException {
        if (input.usuario() == null || input.usuario().isBlank()
                || input.monto() == null || input.monto().signum() < 0
                || input.origen() == null || input.origen().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos invalidos");
        }

        long timestamp = input.timestamp() == null
                ? System.currentTimeMillis()
                : input.timestamp();

        Transaccion tx = new Transaccion(
                input.usuario(), input.monto(), input.origen(), timestamp);

        return ResponseEntity.accepted().body(
                publisher.send("banco-transacciones", tx.usuario(), mapper.writeValueAsString(tx)));
    }
}
