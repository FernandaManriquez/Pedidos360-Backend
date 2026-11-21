package com.duoc.servicioNotificaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Profile("kafka")
public class KafkaConsumerErrorConfig {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaConsumerErrorConfig.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaListenerEndpointRegistry registry) {

        var handler = new DefaultErrorHandler(
                (registro, error) -> {
                    String listenerId = switch (registro.topic()) {
                        case "pedidos360.pedido-creado" ->
                                "pedidos-alertas-listener";
                        case "pedidos360.alertas" ->
                                "alertas-auditoria-listener";
                        default -> throw new KafkaException(
                                "Topic sin consumidor identificado: "
                                        + registro.topic(),
                                error);
                    };

                    log.error(
                            "Procesamiento detenido: listener={}, topic={}, "
                                    + "clave={}, particion={}, offset={}. "
                                    + "El registro requiere revision.",
                            listenerId,
                            registro.topic(),
                            registro.key(),
                            registro.partition(),
                            registro.offset(),
                            error);

                    var container =
                            registry.getListenerContainer(listenerId);

                    if (container != null) {
                        container.getContainerProperties()
                                .setStopImmediate(true);

                        container.stop(() -> log.warn(
                                "Consumidor detenido: listener={}",
                                listenerId));
                    }

                    throw new KafkaException(
                            "Registro no recuperado; no avanzar su offset.",
                            error);
                },
                new FixedBackOff(1000L, 3L));

        handler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                DeserializationException.class);

        handler.setAckAfterHandle(false);
        handler.setResetStateOnRecoveryFailure(false);
        handler.setResetStateOnExceptionChange(false);

        handler.setRetryListeners((registro, error, intento) ->
                log.warn(
                        "Fallo de consumo: topic={}, clave={}, "
                                + "particion={}, offset={}, intento={}, "
                                + "tipoError={}",
                        registro.topic(),
                        registro.key(),
                        registro.partition(),
                        registro.offset(),
                        intento,
                        error.getClass().getSimpleName()));

        return handler;
    }
}