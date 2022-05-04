package de.dhbw.mosbach.chat;

import de.dhbw.mosbach.chat.pojo.MessagePayload;

public interface IChat {
    @FunctionalInterface
    interface IMessageListener {
        void messageReceived(MessagePayload payload);
    }

    void close();

    void addListener(IMessageListener listener);
    void removeListener(IMessageListener listener);

    void sendMessage(String user, String message);
}
