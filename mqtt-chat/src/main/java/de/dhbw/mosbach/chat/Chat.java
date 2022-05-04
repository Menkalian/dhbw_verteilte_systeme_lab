package de.dhbw.mosbach.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import de.dhbw.mosbach.chat.pojo.MessagePayload;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Chat implements IChat {
    private final Mqtt5AsyncClient client;
    private final UUID uuid = UUID.randomUUID();
    private final List<IMessageListener> listeners = new LinkedList<>();
    private final JsonMapper json = new JsonMapper();


    // TODO: Initialisierung/Anmeldung -> Stephan
    // TODO: Externes Interface implementiert -> Lars
    public Chat() {

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
    public void sendMessage(String topic, String user, String message) {
        MessagePayload payload = new MessagePayload(user, message, getClientId(), topic);
        Mqtt5Publish publish = null;

        try {
            publish = Mqtt5Publish.builder()
                    .topic("/aichat/" + topic)
                    .contentType("application/json")
                    .payload(this.json.writeValueAsBytes(payload))
                    .build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (publish != null) this.client.publish(publish);
    }

    @Override
    public String getClientId() {
        return this.uuid.toString();
    }
}
