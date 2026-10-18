package com.duoc.servicioNotificaciones.service;

import com.duoc.servicioNotificaciones.messaging.PedidoCreadoEvent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ProcesamientoPedidoService {

    private final Path directorioSalida;

    public ProcesamientoPedidoService(
            @Value("${app.storage.directory}") String directorioSalida) {

        this.directorioSalida = Path.of(directorioSalida)
                .toAbsolutePath()
                .normalize();
    }

    public void generarTicket(PedidoCreadoEvent evento)
            throws IOException {

        guardarDocumento(
                evento,
                "tickets",
                "TICKET DE PEDIDO"
        );
    }

    public void registrarNotificacion(PedidoCreadoEvent evento)
            throws IOException {

        guardarDocumento(
                evento,
                "notificaciones",
                "NOTIFICACION INTERNA DE PEDIDO CREADO"
        );
    }

    private void guardarDocumento(
            PedidoCreadoEvent evento,
            String subdirectorio,
            String titulo) throws IOException {

        validarEvento(evento);

        // El identificador se normaliza como UUID para formar el archivo.
        String identificador = UUID.fromString(
                evento.eventId()
        ).toString();

        Path carpeta = directorioSalida.resolve(subdirectorio);

        Files.createDirectories(carpeta);

        Path destino = carpeta.resolve(identificador + ".txt");

        String contenido = """
                %s

                Evento: %s
                Pedido: %s
                Usuario: %s
                Producto: %s
                Cantidad: %s
                Total: %s
                Estado: %s
                Fecha del pedido: %s
                """.formatted(
                titulo,
                identificador,
                evento.pedidoId(),
                evento.usuario(),
                evento.productoId(),
                evento.cantidad(),
                evento.total(),
                evento.estado(),
                evento.fecha()
        );

        // Primero se escribe un temporal para evitar un resultado parcial.
        Path temporal = Files.createTempFile(
                carpeta,
                "pedido-",
                ".tmp"
        );

        try {
            Files.writeString(
                    temporal,
                    contenido,
                    StandardCharsets.UTF_8
            );

            try {
                Files.move(
                        temporal,
                        destino,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        temporal,
                        destino,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    private void validarEvento(PedidoCreadoEvent evento) {

        if (evento == null) {
            throw new IllegalArgumentException("El evento es obligatorio");
        }

        if (evento.eventId() == null || evento.eventId().isBlank()) {
            throw new IllegalArgumentException("eventId es obligatorio");
        }

        UUID.fromString(evento.eventId());

        if (evento.pedidoId() == null || evento.pedidoId() <= 0) {
            throw new IllegalArgumentException("pedidoId debe ser positivo");
        }

        if (evento.usuario() == null || evento.usuario().isBlank()) {
            throw new IllegalArgumentException("usuario es obligatorio");
        }

        if (evento.productoId() == null || evento.productoId() <= 0) {
            throw new IllegalArgumentException("productoId debe ser positivo");
        }

        if (evento.cantidad() == null || evento.cantidad() <= 0) {
            throw new IllegalArgumentException("cantidad debe ser positiva");
        }

        if (evento.total() == null || evento.total() < 0) {
            throw new IllegalArgumentException("total no puede ser negativo");
        }

        if (evento.estado() == null || evento.estado().isBlank()) {
            throw new IllegalArgumentException("estado es obligatorio");
        }

        if (evento.fecha() == null) {
            throw new IllegalArgumentException("fecha es obligatoria");
        }
    }
}