package de.dhbw.mosbach.lehre.kafkaweatherspring;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

import de.dhbw.mosbach.lehre.kafkaweatherspring.data.WeatherData;
import lombok.val;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@SpringBootApplication
public class KafkaWeatherSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaWeatherSpringApplication.class, args);
    }

    @Bean
    public CommandLineRunner queryKafkaWeather() {
        return (args) -> {
            final Properties consumerProperties = new Properties();
            consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.50.15.52:9092");
            consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "group_sljk");
            consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            // Ignores corrupt data
            consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class.getName());
            consumerProperties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
            consumerProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, WeatherData.class.getName());
            consumerProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "de.dhbw.mosbach.lehre.kafkaweatherspring");

            final Consumer<String, WeatherData> consumer = new KafkaConsumer<>(consumerProperties);
            consumer.subscribe(Collections.singletonList("weather"));
            val records = consumer.poll(Duration.of(10, ChronoUnit.SECONDS));
            records.forEach(rec -> System.out.println(rec.value()));

            consumer.close();
        };
    }
}
