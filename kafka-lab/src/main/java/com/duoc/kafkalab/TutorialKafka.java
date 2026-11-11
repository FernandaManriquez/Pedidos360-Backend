package com.duoc.kafkalab;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka")
public class TutorialKafka {
    private static final Logger log = LoggerFactory.getLogger(TutorialKafka.class);
    private static final String TOPIC = "tutorial-particiones";

    private final LabPublisher publisher;

    public TutorialKafka(LabPublisher publisher) {
        this.publisher = publisher;
    }

    @GetMapping("/health")
    public String health() {
        return "API Kafka operativa";
    }

    @PostMapping("/send")
    public LabPublisher.Publication send(
            @RequestParam String message,
            @RequestParam(required = false) String key) {
        return publisher.send(TOPIC, key, message);
    }

    @KafkaListener(topics = TOPIC)
    public void receive(ConsumerRecord<String, String> record) {
        log.info("RECIBIDO topic={} particion={} offset={} key={} value={}",
                record.topic(), record.partition(), record.offset(),
                record.key(), record.value());
    }
}
