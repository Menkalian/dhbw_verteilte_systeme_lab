package de.dhbw.mosbach.lehre.kafkaproducerspring;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import de.dhbw.mosbach.lehre.kafkaproducerspring.data.ComputerData;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Slf4j
@SpringBootApplication
public class KafkaProducerSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaProducerSpringApplication.class, args);
    }

    @Bean
    public CommandLineRunner produceKafkaData() {
        return (args) -> {
            final Properties producerProperties = new Properties();
            producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.50.15.52:9092");
            producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
            producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
            producerProperties.put(JsonSerializer.TYPE_MAPPINGS, "cpu:de.dhbw.mosbach.lehre.kafkaproducerspring.data.ComputerData");

            try (Producer<Long, ComputerData> producer = new KafkaProducer<>(producerProperties)) {
                for (int i = 0 ; i < 600 ; i++) {
                    long ts = System.currentTimeMillis();
                    producer.send(new ProducerRecord<>("vlvs_inf19b_sljk_cpu", ts, new ComputerData(Runtime.getRuntime().freeMemory())));
                    Thread.sleep(500);
                }
            }
        };
    }

    // Generates random data to fill the JVM-Heap, so we see change in the produced data
    @Bean
    public CommandLineRunner createJvmMemoryTraffic() {
        return args -> {
            for (int i = 0 ; i < 1_000_000 ; i++) {
                List<String> dummyData = new ArrayList<>();
                Random rng = new Random();
                for (int j = 0 ; j < rng.nextInt(6000) ; j++) {
                    dummyData.add(rng.nextBoolean() ? "hallo" : "nop" + rng.nextInt() + rng.nextDouble());
                }
                log.trace(dummyData.toString());
            }
        };
    }
}
