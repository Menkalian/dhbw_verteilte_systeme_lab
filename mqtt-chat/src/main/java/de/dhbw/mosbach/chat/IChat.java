package de.dhbw.mosbach.chat;

public interface IChat {
    @FunctionalInterface
    interface IMessageListener {
        void messageReceived(String clientId, String topic, String user, String message);
    }

    void addListener(IMessageListener listener);
    void removeListener(IMessageListener listener);

    void sendMessage(String topic, String user, String message);
    String getClientId();
}
