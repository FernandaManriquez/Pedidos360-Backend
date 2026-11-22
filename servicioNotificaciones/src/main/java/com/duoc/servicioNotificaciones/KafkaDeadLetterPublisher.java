package com.duoc.servicioNotificaciones;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaDeadLetterPublisher implements DisposableBean {

    private final DefaultKafkaProducerFactory<String, Object> factory;
    private final KafkaTemplate<String, Object> template;

    public KafkaDeadLetterPublisher(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrap) {

        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        propiedades.put(ProducerConfig.ACKS_CONFIG, "all");
        propiedades.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        propiedades.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        propiedades.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        propiedades.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000);
        propiedades.put("spring.json.add.type.headers", false);

        Map<Class<?>, Serializer<?>> serializers = new LinkedHashMap<>();
        serializers.put(byte[].class, new ByteArraySerializer());
        serializers.put(Object.class, new JacksonJsonSerializer<>());

        factory = new DefaultKafkaProducerFactory<>(
                propiedades,
                new StringSerializer(),
                new DelegatingByTypeSerializer(serializers, true));

        template = new KafkaTemplate<>(factory);
    }

    public KafkaTemplate<String, Object> template() {
        return template;
    }

    @Override
    public void destroy() {
        template.destroy();
        factory.destroy();
    }
}
