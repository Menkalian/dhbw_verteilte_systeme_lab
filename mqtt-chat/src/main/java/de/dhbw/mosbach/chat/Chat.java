package de.dhbw.mosbach.chat;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;

public class Chat implements IChat{

    private Mqtt5AsyncClient client;

    public Chat() {
        client = Mqtt5Client.builder().serverHost("10.50.12.150").buildAsync();
        try {
            client.connectWith().willPublish().topic("aichat/clientstate").payload(("Chat Client " + getClientId() + "stopped").getBytes()).contentType("UTF-8").qos(MqttQos.AT_MOST_ONCE).applyWillPublish().send();
            client.publishWith().topic("aichat/clientstate").qos(MqttQos.AT_LEAST_ONCE).payload(("Chat Client " + getClientId() + " started").getBytes()).contentType("UTF-8").send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addListener(IMessageListener listener) {

    }

    @Override
    public void removeListener(IMessageListener listener) {

    }

    @Override
    public void sendMessage(String topic, String user, String message) {

    }

    @Override
    public String getClientId() {
        return null;
    }

    // TODO: Initialisierung/Anmeldung -> Stephan
    // TODO: Externes Interface implementiert -> Lars
}
