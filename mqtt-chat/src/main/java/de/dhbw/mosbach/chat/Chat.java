package de.dhbw.mosbach.chat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import de.dhbw.mosbach.chat.pojo.MessagePayload;

public class Chat implements IChat {
    private final Mqtt5AsyncClient client;
    private final List<IMessageListener> listeners = new LinkedList<>();
    private final JsonMapper json = new JsonMapper();

    public Chat() {
        this.client = Mqtt5Client.builder().serverHost(Configuration.INSTANCE.serverHost).buildAsync();
        try {
            //client.connect().get();
            client.connectWith().willPublish()
                  .topic(Configuration.INSTANCE.rootTopic + Configuration.INSTANCE.stateSubTopic)
                  .payload(("Chat Client " + Configuration.INSTANCE.clientId + " stopped").getBytes(StandardCharsets.UTF_8))
                  .contentType("text/plain")
                  .qos(MqttQos.AT_MOST_ONCE)
                  .applyWillPublish()
                  .send().get();
            client.publishWith()
                  .topic(Configuration.INSTANCE.rootTopic + Configuration.INSTANCE.stateSubTopic)
                  .qos(MqttQos.AT_LEAST_ONCE)
                  .payload(("Chat Client " + Configuration.INSTANCE.clientId + " started").getBytes(StandardCharsets.UTF_8))
                  .contentType("text/plain")
                  .send();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.client.subscribe(
                Mqtt5Subscribe.builder()
                              .topicFilter(Configuration.INSTANCE.rootTopic + Configuration.INSTANCE.chatSubTopic)
                              .build(),
                pubMsg -> {
                    try {
                        MessagePayload payload = json.readValue(pubMsg.getPayloadAsBytes(), MessagePayload.class);
                        synchronized (listeners) {
                            for (IMessageListener listener : this.listeners) {
                                listener.messageReceived(payload);
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
        );
    }

    @Override
    public void close() {
        // Publish disconnected Message on a proper Disconnect
        try {
            this.client.publishWith()
                       .topic(Configuration.INSTANCE.rootTopic + Configuration.INSTANCE.stateSubTopic)
                       .payload(("Chat Client " + Configuration.INSTANCE.clientId + " stopped").getBytes(StandardCharsets.UTF_8))
                       .contentType("text/plain")
                       .qos(MqttQos.AT_MOST_ONCE)
                       .send().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        client.disconnect();
    }

    @Override
    public void addListener(IMessageListener listener) {
        // Synchronized to avoid a concurrent modification while iterating the list
        synchronized (listeners) {
            this.listeners.add(listener);
        }
    }

    @Override
    public void removeListener(IMessageListener listener) {
        synchronized (listeners) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public void sendMessage(String user, String message) {
        MessagePayload payload = new MessagePayload(user, message, Configuration.INSTANCE.clientId, Configuration.INSTANCE.chatSubTopic);

        try {
            this.client.publishWith()
                       .topic(Configuration.INSTANCE.rootTopic + Configuration.INSTANCE.chatSubTopic)
                       .contentType("application/json")
                       .payload(this.json.writeValueAsBytes(payload))
                       .send();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
