package com.duoc.servicioNotificaciones;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Profile("kafka")
public class KafkaConsumerErrorConfig {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaConsumerErrorConfig.class);

    @Bean
    public KafkaAdmin.NewTopics topicosErroresKafka() {
        Map<String, String> configuracion = Map.of(
                "retention.ms", "2419200000",
                "cleanup.policy", "delete",
                "min.insync.replicas", "2");

        return new KafkaAdmin.NewTopics(
                new NewTopic("pedidos360.pedido-creado.DLT", 3, (short) 3)
                        .configs(configuracion),
                new NewTopic("pedidos360.alertas.DLT", 3, (short) 3)
                        .configs(configuracion));
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaDeadLetterPublisher publicador) {

        var recuperador = new DeadLetterPublishingRecoverer(
                publicador.template(),
                (registro, error) -> new TopicPartition(
                        registro.topic() + ".DLT",
                        registro.partition()));

        recuperador.setFailIfSendResultIsError(true);

        var handler = new DefaultErrorHandler(
                (registro, error) -> {
                    recuperador.accept(registro, error);
                    log.error(
                            "Registro enviado a DLT: topic={}, particion={}, "
                                    + "offset={}, clave={}, destino={}",
                            registro.topic(),
                            registro.partition(),
                            registro.offset(),
                            registro.key(),
                            registro.topic() + ".DLT",
                            error);
                },
                new FixedBackOff(1000L, 3L));

        handler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                DeserializationException.class);

        handler.setAckAfterHandle(true);
        handler.setResetStateOnRecoveryFailure(true);

        handler.setRetryListeners((registro, error, intento) ->
                log.warn(
                        "Fallo de consumo: topic={}, particion={}, offset={}, "
                                + "intento={}, tipoError={}",
                        registro.topic(),
                        registro.partition(),
                        registro.offset(),
                        intento,
                        error.getClass().getSimpleName()));

        return handler;
    }
}
