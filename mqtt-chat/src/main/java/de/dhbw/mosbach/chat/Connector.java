package de.dhbw.mosbach.chat;

public class Connector {
    String savedUsername = "Username";
    Chat chat;
    UserInterface ui;

    public Connector() {
        chat = new Chat();
        ui = new UserInterface();

        chat.addListener(ui::showReceivedMessage);
        ui.addListener(new UIListener(chat));
    }

    public class UIListener implements IUserInterface.IInputListener {
        private final Chat chat;

        public UIListener(Chat chat) {
            this.chat = chat;
        }

        @Override
        public void onUsernameSet(String username) {
            savedUsername = username;
        }

        @Override
        public void onMessageInput(String input) {
            chat.sendMessage(savedUsername, input) ;
        }

        @Override
        public void onClosed() {
            chat.close();
        }
    }
}
