package de.dhbw.mosbach.chat;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import de.dhbw.mosbach.chat.pojo.MessagePayload;

public class UserInterface implements IUserInterface, TerminalResizeListener {
    private final ExecutorService inputReaderExecutor = Executors.newSingleThreadExecutor();
    private final List<IInputListener> listeners = new LinkedList<>();
    private final List<MessagePayload> messages = new LinkedList<>();
    private final Terminal term;

    private StringBuilder inputBuffer = new StringBuilder();
    private String currentUsername = "";

    private String inputIdentifier = "Nachricht";
    private Consumer<String> onSubmit = (s) -> fireListeners(l -> l.onMessageInput(s));

    public UserInterface() {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        try {
            term = terminalFactory.createTerminal();
            term.addResizeListener(this);
            term.setCursorVisible(false);

            // If the default Terminal is a GUI-Implementation we should listen when it's closed
            if (term instanceof Frame) {
                Frame termFrame = (Frame) term;
                termFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        // Exit directly. Cleanup is handled by last will
                        System.exit(0);
                    }
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Initialization failed", ex);
        }
        inputReaderExecutor.submit(this::readInput);
        usernameMode();
        redrawMessages();
    }

    @Override
    public void addListener(IInputListener listener) {
        synchronized (listeners) {
            listener.onUsernameSet(currentUsername);
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(IInputListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void showReceivedMessage(MessagePayload payload) {
        synchronized (messages) {
            messages.add(payload);
        }
        redrawMessages();
    }

    @Override
    public void onResized(Terminal terminal, TerminalSize newSize) {
        redrawMessages();
    }

    private void fireListeners(Consumer<IInputListener> action) {
        synchronized (listeners) {
            listeners.forEach(action);
        }
    }

    private void messageMode() {
        inputBuffer = new StringBuilder();
        inputIdentifier = "Nachricht";
        onSubmit = (s) -> fireListeners(l -> l.onMessageInput(s));
    }

    private void usernameMode() {
        inputBuffer = new StringBuilder(currentUsername);
        inputIdentifier = "Name";
        onSubmit = (s) -> {
            currentUsername = s;
            fireListeners(l -> l.onUsernameSet(s));
            messageMode();
        };
    }

    private void redrawMessages() {
        try {
            term.clearScreen();
            int rows = term.getTerminalSize().getRows() - 1;

            term.setCursorPosition(0, rows);
            term.putString("F3: Quit    F4: Change Name    Enter: Submit");
            rows--;

            term.setCursorPosition(0, rows);
            term.putString(inputIdentifier + ": " + inputBuffer.toString());
            rows--;

            synchronized (messages) {
                for (int i = messages.size() - 1 ; i >= 0 && rows > 0 ; i--) {
                    term.setCursorPosition(0, rows);
                    MessagePayload p = messages.get(i);
                    term.setForegroundColor(TextColor.ANSI.GREEN);
                    term.putString("T: " + p.getTopic() + "\t");
                    term.setForegroundColor(TextColor.ANSI.MAGENTA);
                    term.putString("U: " + p.getSender() + "\t");
                    term.setForegroundColor(TextColor.ANSI.CYAN);
                    term.putString("M: " + p.getText());
                    term.setForegroundColor(TextColor.ANSI.DEFAULT);
                    rows--;
                }
            }

            term.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void readInput() {
        try {
            KeyStroke k;
            while ((k = term.readInput()) != null) {
                // Send message on enter
                if (k.getKeyType() == KeyType.Enter) {
                    String input = inputBuffer.toString();
                    inputBuffer = new StringBuilder();
                    onSubmit.accept(input);
                    redrawMessages();
                    continue;
                }

                // Quit on F3 (Clean shutdown)
                if (k.getKeyType() == KeyType.F3) {
                    term.close();
                    inputReaderExecutor.shutdown();
                    synchronized (listeners) {
                        for (IInputListener listener : listeners) {
                            listener.onClosed();
                        }
                    }
                    break;
                }

                // Quit on F4
                if (k.getKeyType() == KeyType.F4) {
                    usernameMode();
                    redrawMessages();
                    continue;
                }

                // Backspace
                if (k.getKeyType() == KeyType.Backspace) {
                    if (inputBuffer.length() == 0) {
                        continue;
                    }

                    inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                    redrawMessages();
                    continue;
                }

                // Else append the string
                inputBuffer.append(k.getCharacter());
                redrawMessages();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
