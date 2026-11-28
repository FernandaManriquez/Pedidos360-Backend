package com.duoc.servicioNotificaciones.controller;

import com.duoc.servicioNotificaciones.service.AuditoriaDlqService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/rabbit/dlq")
public class AuditoriaDlqController {

    private final AuditoriaDlqService servicio;

    public AuditoriaDlqController(AuditoriaDlqService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/{tipo}/auditar")
    public ResponseEntity<Map<String, Object>> auditar(
            @PathVariable String tipo) {

        if (!Set.of("tickets", "notificaciones", "auditoria").contains(tipo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Tipo de DLQ no permitido");
        }

        Map<String, Object> resultado = servicio.auditar(tipo);
        if (resultado == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(resultado);
    }
}