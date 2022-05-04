package de.dhbw.mosbach.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import de.dhbw.mosbach.chat.pojo.MessagePayload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Chat implements IChat {
    private final Mqtt5AsyncClient client;
    private final List<IMessageListener> listeners = new LinkedList<>();
    private final JsonMapper json = new JsonMapper();

    public Chat() {
        this.client = Mqtt5Client.builder().serverHost("10.50.12.150").buildAsync();
        try {
            client.connect().get();
            client.connectWith().willPublish()
                    .topic("/aichat/clientstate")
                    .payload(("Chat Client " + getClientId() + "stopped").getBytes(StandardCharsets.UTF_8))
                    .contentType("text/plain")
                    .qos(MqttQos.AT_MOST_ONCE)
                    .applyWillPublish()
                    .send();
            client.publishWith()
                    .topic("/aichat/clientstate")
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(("Chat Client " + getClientId() + " started").getBytes(StandardCharsets.UTF_8))
                    .contentType("text/plain")
                    .send();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.client.subscribe(
                Mqtt5Subscribe.builder()
                        .topicFilter("/aichat/default")
                        .build(),
                pubMsg -> {
                    try {
                        MessagePayload payload = json.readValue(pubMsg.getPayloadAsBytes(), MessagePayload.class);
                        for (IMessageListener listener : this.listeners) {
                            listener.messageReceived(payload);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
        );
    }

    @Override
    public void close() {
        client.disconnect();
    }

    @Override
    public void addListener(IMessageListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(IMessageListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void sendMessage(String user, String message) {
        String topic = "default";
        MessagePayload payload = new MessagePayload(user, message, getClientId(), topic);

        try {
            this.client.publishWith()
                    .topic("/aichat/" + topic)
                    .contentType("application/json")
                    .payload(this.json.writeValueAsBytes(payload))
                    .send();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getClientId() {
        return "4b298fe1-67f3-43b7-9ab4-200488a83e90";
    }
}
