import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import com.zeroc.Ice.Current;

import Demo.CallbackPrx;

import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatManagerImp implements Demo.ChatManager {
    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");
    private final CopyOnWriteArrayList<CallbackPrx> subscribers = new CopyOnWriteArrayList<>();
    private final Map<String, CallbackPrx> busSubscribers = new ConcurrentHashMap<>();
    private final Map<String, CallbackPrx> connectionSubscribers = new ConcurrentHashMap<>();

    @Override
    public void subscribe(CallbackPrx callback, Current current) {
        subscribers.add(callback);
        if (current.con != null) {
            connectionSubscribers.put(current.con.toString(), callback);
        }
        System.out.println("Subscribed an unscoped observer");
    }

    @Override
    public String[] getState(Current current) {
        return new String[0];
    }

    @Override
    public void sendMessage(String msg, Current current) {
        if (msg.startsWith("REGISTER|")) {
            String busId = msg.substring("REGISTER|".length());
            CallbackPrx callback = current.con == null ? null :
                    connectionSubscribers.get(current.con.toString());
            if (callback != null) {
                busSubscribers.put(busId, callback);
            }
            return;
        }
        processMessage(msg);
    }

    private void sendMessageToSubscribers(String message) {
        for (CallbackPrx subscriber : subscribers) {
            try {
                subscriber.notifyCallbackAsync(message);
            } catch (Exception e) {
                subscribers.remove(subscriber);
            }
        }
    }

    private void sendMessageToBus(String busId, String message) {
        CallbackPrx subscriber = busSubscribers.get(busId);
        if (subscriber == null) {
            return;
        }
        try {
            subscriber.notifyCallback(message);
        } catch (Exception e) {
            busSubscribers.remove(busId, subscriber);
        }
    }

    private void processMessage(String serialized) {
        String[] parts = serialized.split("\\|", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid image message");
        }

        String busId = parts[0];
        String tag = parts[1].trim();
        byte[] imageBytes = Base64.getDecoder().decode(parts[3]);

        Path directory = Paths.get("data/serverImages", busId);
        try {
            Files.createDirectories(directory);
            String filename = tag + "-" + LocalDateTime.now().format(FILE_TIME) + ".jpg";
            Files.write(directory.resolve(filename), imageBytes);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store image for bus " + busId, e);
        }

        if ("GENERALINTEREST".equals(tag)) {
            sendMessageToSubscribers("GENERALINTEREST from " + busId);
        } else if ("CRITICAL".equals(tag)) {
            sendMessageToBus(busId, "ACTION for " + busId);
        } else if (!"NORMAL".equals(tag)) {
            throw new IllegalArgumentException("Unknown image tag: " + tag);
        }
    }
}
