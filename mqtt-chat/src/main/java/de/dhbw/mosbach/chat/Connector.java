package de.dhbw.mosbach.chat;

import java.util.UUID;

public class Connector {
    String savedUsername = "Username";
    Chat chat;
    UserInterface ui;

    public Connector() {
        chat = new Chat();
        ui = new UserInterface();

        chat.addListener(ui::showReceivedMessage);
        ui.addListener(new UIListener());
    }
    public class UIListener implements IUserInterface.IInputListener {

        @Override
        public void onUsernameSet(String username) {
            savedUsername = username;
        }

        @Override
        public void onMessageInput(String input) {
            chat.sendMessage(savedUsername, input) ;
        }
    }
}
