package com.duoc.servicioRabbitAdmin.exception;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    private ResponseEntity<Map<String, Object>> respuesta(
            HttpStatus estado, String mensaje, Map<String, String> errores) {

        return ResponseEntity.status(estado).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", estado.value(),
                "message", mensaje,
                "errors", errores
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(
            MethodArgumentNotValidException exception) {

        Map<String, String> errores = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errores.putIfAbsent(
                        error.getField(),
                        Objects.requireNonNullElse(
                                error.getDefaultMessage(), "Valor invalido"
                        )
                )
        );

        return respuesta(
                HttpStatus.BAD_REQUEST, "Datos invalidos", errores
        );
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(
            RecursoNoEncontradoException exception) {

        return respuesta(
                HttpStatus.NOT_FOUND, exception.getMessage(), Map.of()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> argumento(
            IllegalArgumentException exception) {

        return respuesta(
                HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(
            HttpMessageNotReadableException exception) {

        return respuesta(
                HttpStatus.BAD_REQUEST,
                "JSON invalido o tipos de datos incorrectos",
                Map.of()
        );
    }

    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<Map<String, Object>> conexion(
            AmqpConnectException exception) {

        log.error("No se pudo conectar con RabbitMQ", exception);
        return respuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                "RabbitMQ no esta disponible",
                Map.of()
        );
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> rabbit(
            AmqpException exception) {

        log.error("Fallo de administracion de RabbitMQ", exception);

        for (Throwable causa = exception;
                causa != null; causa = causa.getCause()) {

            if (causa instanceof ShutdownSignalException signal
                    && signal.getReason() instanceof AMQP.Channel.Close cierre) {

                if (cierre.getReplyCode() == 404) {
                    return respuesta(
                            HttpStatus.NOT_FOUND,
                            "No existe uno de los recursos indicados",
                            Map.of()
                    );
                }

                if (cierre.getReplyCode() == 405
                        || cierre.getReplyCode() == 406) {
                    return respuesta(
                            HttpStatus.CONFLICT,
                            "Configuracion incompatible o recurso en uso",
                            Map.of()
                    );
                }
            }
        }

        return respuesta(
                HttpStatus.BAD_GATEWAY,
                "No se pudo completar la operacion en RabbitMQ",
                Map.of()
        );
    }
}
