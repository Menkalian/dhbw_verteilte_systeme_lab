package de.dhbw.mosbach.chat;

import de.dhbw.mosbach.chat.pojo.MessagePayload;

public interface IUserInterface {
    void addListener(IInputListener listener);
    void removeListener(IInputListener listener);

    void showReceivedMessage(MessagePayload payload);

    interface IInputListener {
        void onUsernameSet(String username);

        void onMessageInput(String input);

        void onClosed();
    }
}
