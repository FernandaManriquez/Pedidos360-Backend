package com.duoc.servicioKafkaAdmin;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validar(
            MethodArgumentNotValidException error) {

        String detalle = error.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(campo -> campo.getField() + ": "
                        + campo.getDefaultMessage())
                .sorted()
                .collect(java.util.stream.Collectors.joining("; "));

        return respuesta(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(
            HttpMessageNotReadableException error) {

        return respuesta(
                HttpStatus.BAD_REQUEST,
                "El JSON es invalido o contiene tipos incorrectos.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(
            IllegalArgumentException error) {

        return respuesta(HttpStatus.BAD_REQUEST, error.getMessage());
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<Map<String, String>> tiempoAgotado(
            TimeoutException error) {

        log.warn("Tiempo de espera agotado al administrar Kafka", error);

        return respuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Kafka no respondio dentro del tiempo permitido. "
                        + "Consulta el estado antes de repetir la operacion.");
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<Map<String, String>> interrumpido(
            InterruptedException error) {

        Thread.currentThread().interrupt();

        return respuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                "La operacion fue interrumpida.");
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<Map<String, String>> falloKafka(
            ExecutionException error) {

        Throwable causa = error.getCause();

        log.warn("Fallo en una operacion administrativa de Kafka", error);

        if (causa instanceof TopicExistsException) {
            return respuesta(
                    HttpStatus.CONFLICT,
                    "Ya existe un topic con ese nombre.");
        }

        if (causa instanceof UnknownTopicOrPartitionException) {
            return respuesta(
                    HttpStatus.NOT_FOUND,
                    "El topic no existe o sus metadatos aun no estan disponibles.");
        }

        if (causa instanceof InvalidReplicationFactorException
                || causa instanceof InvalidConfigurationException
                || causa instanceof InvalidTopicException) {

            return respuesta(
                    HttpStatus.BAD_REQUEST,
                    "La configuracion del topic no es valida para el cluster.");
        }

        if (causa instanceof org.apache.kafka.common.errors.TimeoutException) {
            return respuesta(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Kafka no respondio a tiempo. Consulta el estado "
                            + "antes de repetir la operacion.");
        }

        return respuesta(
                HttpStatus.BAD_GATEWAY,
                "Kafka no pudo completar la operacion. Revisa el log del servicio.");
    }

    private ResponseEntity<Map<String, String>> respuesta(
            HttpStatus estado, String mensaje) {

        return ResponseEntity.status(estado)
                .body(Map.of("error", mensaje));
    }
}