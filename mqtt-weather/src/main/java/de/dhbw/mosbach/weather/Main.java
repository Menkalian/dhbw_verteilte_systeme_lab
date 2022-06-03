package de.dhbw.mosbach.weather;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import de.dhbw.mosbach.weather.pojo.WeatherData;

public class Main {
    private static JsonMapper json = new JsonMapper();
    private static List<Mqtt5Client> connectedClients = new LinkedList<>();

    public static void main(String[] args) {

        Scanner reader = new Scanner(System.in);
        System.out.println("Wetterabfrage gestartet. Geben sie \"quit\" ein um die Anwendung zu beenden.");
        startReadingWeather("mosbach");
        startReadingWeather("mergentheim");
        startReadingWeather("stuttgart");

        while (!reader.nextLine().equals("quit")) {
            System.out.println("Ungültige Eingabe! Geben sie \"quit\" ein um die Anwendung zu beenden.");
        }

        for (Mqtt5Client connectedClient : connectedClients) {
            try {
                connectedClient.toAsync().disconnect().get();
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        }

        System.exit(0);
    }

    private static void startReadingWeather(String city) {
        Mqtt5AsyncClient client = Mqtt5Client.builder()
                                             .serverHost("10.50.12.150")
                                             .buildAsync();
        try {
            client.connect().get();
            client.subscribe(
                    Mqtt5Subscribe.builder()
                                  .topicFilter("/weather/" + city)
                                  .build(),
                    pubMsg -> {
                        try {
                            WeatherData weatherData = json.readValue(pubMsg.getPayloadAsBytes(), WeatherData.class);
                            System.out.println("Wetterdaten von " + city + " erhalten: " + weatherData);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
            );
            connectedClients.add(client);
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
    }
}
