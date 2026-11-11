package com.duoc.kafkalab;

import java.math.BigDecimal;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
public class PipelineTopology {
    private static final Logger log = LoggerFactory.getLogger(PipelineTopology.class);
    private static final BigDecimal MINIMO = BigDecimal.TEN;
    private static final BigDecimal UMBRAL_ALERTA = BigDecimal.valueOf(10_000);

    @Bean
    public KStream<String, Transaccion> banco(StreamsBuilder builder) {
        JsonSerde<Transaccion> entrada = new JsonSerde<>(Transaccion.class).noTypeInfo();
        JsonSerde<AlertaFraude> salida = new JsonSerde<>(AlertaFraude.class).noTypeInfo();

        KStream<String, Transaccion> transacciones = builder.stream(
                "banco-transacciones", Consumed.with(Serdes.String(), entrada));

        transacciones
                .peek((key, tx) -> log.info("ENTRADA key={} transaccion={}", key, tx))
                .filter((key, tx) -> tx != null && tx.monto() != null
                        && tx.monto().compareTo(MINIMO) > 0)
                .peek((key, tx) -> log.info("PROCESADA key={} monto={}", key, tx.monto()))
                .filter((key, tx) -> tx.monto().compareTo(UMBRAL_ALERTA) > 0)
                .mapValues(tx -> new AlertaFraude(
                        tx.usuario(),
                        tx.monto(),
                        "Transaccion inusualmente alta en " + tx.origen(),
                        "ALTA",
                        tx.timestamp()))
                .peek((key, alerta) -> log.info("ALERTA key={} monto={}", key, alerta.monto()))
                .to("banco-alertas", Produced.with(Serdes.String(), salida));

        return transacciones;
    }
}
