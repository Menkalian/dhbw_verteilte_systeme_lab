package de.dhbw.mosbach.chat;

import java.util.UUID;

public class Connector {
    String clientId;
    String savedUsername = "Username";
    String savedTopic = "";
    Chat chat;
    UserInterface ui;

    public Connector() {
        clientId = UUID.randomUUID().toString();
        chat = new Chat();
        ui = new UserInterface();

        chat.addListener((cId, topic, user, message) -> {
            if (savedTopic.equals(topic)) {
                ui.showReceivedMessage(cId, topic, user, message);
            }
        });
        ui.addListener(new UIListener());
    }
    public class UIListener implements IUserInterface.IInputListener {
        @Override
        public void onTopicSet(String topic) {
            savedTopic = topic;
        }

        @Override
        public void onUsernameSet(String username) {
            savedUsername = username;
        }

        @Override
        public void onMessageInput(String input) {
            chat.sendMessage(savedTopic, savedUsername, input) ;
        }
    }
}
