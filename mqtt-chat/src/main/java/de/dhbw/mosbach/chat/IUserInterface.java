package de.dhbw.mosbach.chat;

public interface IUserInterface {
    void addListener(IInputListener listener);
    void removeListener(IInputListener listener);

    void showReceivedMessage(String clientId, String topic, String user, String message);

    interface IInputListener {
        void onTopicSet(String topic);

        void onUsernameSet(String username);

        void onMessageInput(String input);
    }
}
