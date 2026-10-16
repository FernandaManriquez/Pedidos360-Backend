package com.duoc.servicioNotificaciones.messaging;

import com.rabbitmq.client.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProcesadorMensaje {

    private static final Logger log =
            LoggerFactory.getLogger(ProcesadorMensaje.class);

    private final int maxIntentos;

    public ProcesadorMensaje(
            @Value("${app.processing.max-attempts}") int maxIntentos) {

        if (maxIntentos < 1) {
            throw new IllegalArgumentException(
                    "app.processing.max-attempts debe ser mayor que cero"
            );
        }

        this.maxIntentos = maxIntentos;
    }

    public void procesar(
            Message mensaje,
            Channel canal,
            String dominio,
            Tarea tarea) throws IOException {

        long deliveryTag = mensaje.getMessageProperties()
                .getDeliveryTag();

        for (int intento = 1; intento <= maxIntentos; intento++) {

            try {
                tarea.ejecutar();

            } catch (IllegalArgumentException exception) {

                // Los datos invalidos no mejoran al repetir la operacion.
                log.error(
                        "{}: mensaje invalido, rechazo hacia DLQ. tag={}",
                        dominio,
                        deliveryTag,
                        exception
                );

                canal.basicReject(deliveryTag, false);
                return;

            } catch (IOException exception) {

                // Reintentos locales y acotados para errores de archivos.
                if (intento < maxIntentos) {
                    log.warn(
                            "{}: error de E/S, intento {}/{}. tag={}",
                            dominio,
                            intento,
                            maxIntentos,
                            deliveryTag,
                            exception
                    );

                    continue;
                }

                log.error(
                        "{}: intentos agotados, NACK hacia DLQ. tag={}",
                        dominio,
                        deliveryTag,
                        exception
                );

                canal.basicNack(deliveryTag, false, false);
                return;

            } catch (RuntimeException exception) {

                log.error(
                        "{}: error inesperado, NACK hacia DLQ. tag={}",
                        dominio,
                        deliveryTag,
                        exception
                );

                canal.basicNack(deliveryTag, false, false);
                return;
            }

            // Se confirma solamente despues de terminar la tarea.
            // Un error del ACK se propaga, sin repetir aqui el negocio.
            canal.basicAck(deliveryTag, false);

            log.info(
                    "{}: procesamiento terminado, ACK enviado. tag={}",
                    dominio,
                    deliveryTag
            );

            return;
        }
    }

    @FunctionalInterface
    public interface Tarea {

        void ejecutar() throws IOException;
    }
}