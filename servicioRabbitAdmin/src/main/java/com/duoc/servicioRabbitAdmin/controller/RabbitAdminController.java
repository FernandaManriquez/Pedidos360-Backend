package com.duoc.servicioRabbitAdmin.controller;

import com.duoc.servicioRabbitAdmin.dto.*;
import com.duoc.servicioRabbitAdmin.service.RabbitAdminService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/rabbit")
public class RabbitAdminController {

    private final RabbitAdminService servicio;

    public RabbitAdminController(RabbitAdminService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/queues")
    public ResponseEntity<Void> crearCola(
            @Valid @RequestBody QueueRequest datos) {

        servicio.crearCola(datos);
        return ResponseEntity.created(
                URI.create("/api/rabbit/queues/" + datos.name())
        ).build();
    }

    @GetMapping("/queues/{name}")
    public Map<String, Object> consultarCola(
            @PathVariable String name) {

        return servicio.consultarCola(name);
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> eliminarCola(
            @PathVariable String name) {

        servicio.eliminarCola(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Void> crearExchange(
            @Valid @RequestBody ExchangeRequest datos) {

        servicio.crearExchange(datos);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> eliminarExchange(
            @PathVariable String name) {

        servicio.eliminarExchange(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<Void> crearBinding(
            @Valid @RequestBody BindingRequest datos) {

        servicio.crearBinding(datos);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(
            @Valid @RequestBody BindingRequest datos) {

        servicio.eliminarBinding(datos);
        return ResponseEntity.noContent().build();
    }
}
