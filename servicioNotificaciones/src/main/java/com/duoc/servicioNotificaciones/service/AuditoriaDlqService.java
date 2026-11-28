package com.duoc.servicioNotificaciones.service;

import com.rabbitmq.client.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditoriaDlqService {

    private static final Logger log =
            LoggerFactory.getLogger(AuditoriaDlqService.class);

    private final RabbitTemplate rabbit;
    private final Path directorio;
    private final Map<String, String> colas;

    public AuditoriaDlqService(
            RabbitTemplate rabbit,
            @Value("${app.storage.directory:./output}") String directorio,
            @Value("${app.rabbitmq.tickets-dlq}") String tickets,
            @Value("${app.rabbitmq.notificaciones-dlq}") String notificaciones,
            @Value("${app.rabbitmq.auditoria-dlq:pedidos360.auditoria.dlq}")
            String auditoria) {

        this.rabbit = rabbit;
        this.directorio = Path.of(directorio).resolve("dlq");
        this.colas = Map.of(
                "tickets", tickets,
                "notificaciones", notificaciones,
                "auditoria", auditoria);
    }

    public Map<String, Object> auditar(String tipo) {
        String cola = colas.get(tipo);
        if (cola == null) {
            throw new IllegalArgumentException("Tipo de DLQ no permitido");
        }

        return rabbit.execute(canal -> {
            GetResponse mensaje = canal.basicGet(cola, false);
            if (mensaje == null) {
                return null;
            }

            long tag = mensaje.getEnvelope().getDeliveryTag();
            Path archivo;
            String contenido = new String(
                    mensaje.getBody(), StandardCharsets.UTF_8);

            try {
                Path carpeta = directorio.resolve(tipo);
                Files.createDirectories(carpeta);
                archivo = carpeta.resolve(UUID.randomUUID() + ".txt");

                String registro =
                        "fecha=" + Instant.now() + "\n"
                        + "cola=" + cola + "\n"
                        + "exchange=" + mensaje.getEnvelope().getExchange() + "\n"
                        + "routingKey=" + mensaje.getEnvelope().getRoutingKey() + "\n"
                        + "headers=" + mensaje.getProps().getHeaders() + "\n"
                        + "payloadBase64="
                        + Base64.getEncoder().encodeToString(mensaje.getBody())
                        + "\ncontenidoUTF8=\n" + contenido + "\n";

                Files.writeString(
                        archivo, registro, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE);

            } catch (IOException | RuntimeException error) {
                try {
                    canal.basicNack(tag, false, true);
                } catch (IOException errorNack) {
                    error.addSuppressed(errorNack);
                }
                log.error("No se pudo archivar la DLQ: cola={}", cola, error);
                throw error;
            }

            canal.basicAck(tag, false);

            String resumen = contenido.replace('\r', ' ').replace('\n', ' ');
            if (resumen.length() > 2000) {
                resumen = resumen.substring(0, 2000) + " [ver archivo completo]";
            }

            log.warn("DLQ registrada: cola={}, archivo={}, mensaje={}",
                    cola, archivo, resumen);

            return Map.<String, Object>of(
                    "cola", cola,
                    "archivo", archivo.toAbsolutePath().toString(),
                    "registrado", true);
        });
    }
}