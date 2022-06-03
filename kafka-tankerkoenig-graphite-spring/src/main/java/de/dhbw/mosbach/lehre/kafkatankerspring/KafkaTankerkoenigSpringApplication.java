package de.dhbw.mosbach.lehre.kafkatankerspring;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.codahale.metrics.graphite.Graphite;
import de.dhbw.mosbach.lehre.kafkatankerspring.data.TankerkoenigData;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Slf4j
@SpringBootApplication
public class KafkaTankerkoenigSpringApplication {
    private static boolean RESET_DATA = false;
    private static final int PARTITION_COUNT = 10;
    private static final String KAFKA_TOPIC = "tankerkoenig";

    public static void main(String[] args) {
        SpringApplication.run(KafkaTankerkoenigSpringApplication.class, args);
    }

    @Bean
    public CommandLineRunner queryKafkaWeather() {
        return (args) -> {
            ExecutorService executorService = Executors.newFixedThreadPool(Math.min(PARTITION_COUNT, Runtime.getRuntime().availableProcessors()));

            for (int i = 0 ; i < PARTITION_COUNT ; i++) {
                int finalIdx = i;
                executorService.submit(() -> readPartition(finalIdx));
            }
        };
    }

    private static void readPartition(int partitionIdx) {
        try {
            Graphite graphiteCon = connectToGraphite();
            try (graphiteCon) {
                try (Consumer<String, TankerkoenigData> consumer = new KafkaConsumer<>(getConsumerSettings())) {
                    configureConsumer(consumer, KAFKA_TOPIC, partitionIdx);

                    List<TankerkoenigData> aggregatedData = new ArrayList<>();
                    long nextTimestampCap = -1;

                    while (true) {
                        final ConsumerRecords<String, TankerkoenigData> records = consumer.poll(Duration.of(60, ChronoUnit.SECONDS));
                        if (records.isEmpty()) {
                            try {
                                Thread.sleep(10_000);
                            } catch (InterruptedException e) {
                                log.error("Interrupted", e);
                            }
                        }

                        for (ConsumerRecord<String, TankerkoenigData> record : records) {
                            if (record.timestamp() >= nextTimestampCap) {
                                if (!aggregatedData.isEmpty()) {
                                    double dieselAggregated = aggregatedData
                                            .stream()
                                            .filter(Objects::nonNull)
                                            .mapToDouble(TankerkoenigData::getPDiesel)
                                            .filter(price -> price >= 0.0)
                                            .average()
                                            .orElse(0.0);
                                    double e5Aggregated = aggregatedData
                                            .stream()
                                            .filter(Objects::nonNull)
                                            .mapToDouble(TankerkoenigData::getPE5)
                                            .filter(price -> price >= 0.0)
                                            .average()
                                            .orElse(0.0);
                                    double e10Aggregated = aggregatedData
                                            .stream()
                                            .filter(Objects::nonNull)
                                            .mapToDouble(TankerkoenigData::getPE10)
                                            .filter(price -> price >= 0.0)
                                            .average()
                                            .orElse(0.0);

                                    graphiteCon.send("inf19b.sljk.tanker." + partitionIdx + ".diesel", dieselAggregated + "", nextTimestampCap / 1000);
                                    graphiteCon.send("inf19b.sljk.tanker." + partitionIdx + ".e5", e5Aggregated + "", nextTimestampCap / 1000);
                                    graphiteCon.send("inf19b.sljk.tanker." + partitionIdx + ".e10", e10Aggregated + "", nextTimestampCap / 1000);
                                    graphiteCon.flush();
                                }

                                aggregatedData.clear();

                                ZoneOffset cet = ZoneOffset.of("+02");
                                LocalDateTime nextTimestamp = LocalDateTime.ofEpochSecond(record.timestamp() / 1000, 0, cet);
                                nextTimestamp = nextTimestamp.withMinute(0).withSecond(0).plus(1, ChronoUnit.HOURS);
                                nextTimestampCap = nextTimestamp.toEpochSecond(cet) * 1000;
                            }

                            aggregatedData.add(record.value());
                        }

                        consumer.commitAsync();
                    }
                }
            } catch (IOException ex) {
                log.error("Graphite error", ex);
            }
        } catch (Exception ex) {
            log.error("Exception occured", ex);
        }
    }

    private static void configureConsumer(Consumer<?, ?> consumer, String topic, int partition) {
        TopicPartition partitionObj = new TopicPartition(topic, partition);
        consumer.assign(Collections.singletonList(partitionObj));
        if (RESET_DATA) {
            consumer.seekToBeginning(Collections.singletonList(partitionObj));
        }
    }

    private static Graphite connectToGraphite() {
        try {
            Graphite graphite = new Graphite("10.50.15.52", 2003);
            graphite.connect();
            if (!graphite.isConnected()) {
                throw new Exception("Graphite is not connected after connecting.");
            }
            return graphite;
        } catch (Exception ex) {
            log.error("Could not connect to Graphite", ex);
            throw new RuntimeException(ex);
        }
    }

    private static Properties getConsumerSettings() {
        final Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.50.15.52:9092");
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "group_sljk_tankerkoenig");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Ignores corrupt data
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class.getName());
        consumerProperties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        consumerProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TankerkoenigData.class.getName());
        consumerProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "de.dhbw.mosbach.lehre.kafkatankerspring");
        return consumerProperties;
    }
}
