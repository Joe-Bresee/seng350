import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import com.zeroc.Ice.Current;

import Demo.CallbackPrx;

import java.util.Base64;

import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;





public class ChatManagerImp implements Demo.ChatManager {

    //Performance analysis
    private final AtomicInteger receivedNormalCount = new AtomicInteger(0);
    private final AtomicInteger dispatchedNotificationCount = new AtomicInteger(0);
    private long experimentStartTime = 0;

    public void resetAndStartExperiment() {
        receivedNormalCount.set(0);
        dispatchedNotificationCount.set(0);
        experimentStartTime = System.currentTimeMillis();
        System.out.println("=== EXPERIMENT RUN STARTED ===");
    }

    public void printExperimentResults() {
        long durationMs = System.currentTimeMillis() - experimentStartTime;
        double durationSec = durationMs / 1000.0;
        
        int totalNormal = receivedNormalCount.get();
        int totalNotify = dispatchedNotificationCount.get();

        System.out.println("=== EXPERIMENT RUN RESULTS ===");
        System.out.println("Total Execution Time: " + durationSec + " seconds");
        System.out.println("NORMAL Msg Processed (Received & Stored): " + totalNormal);
        System.out.println("NORMAL Throughput: " + (totalNormal / durationSec) + " msg/sec");
        System.out.println("Notifications Dispatched: " + totalNotify);
        System.out.println("Notification Throughput: " + (totalNotify / durationSec) + " notifications/sec");
    }


    private List<String> messages;
    // Semaphore mSemaphore = new Semaphore(1);
    private CopyOnWriteArrayList<CallbackPrx> subscribers = new CopyOnWriteArrayList<>();

    ChatManagerImp() {
        messages = new ArrayList<>();
    }

    @Override
    public void subscribe(CallbackPrx callback, Current current) {
        System.out.println("subscribed");

        subscribers.add(callback);

        // this.callbackPrx = callback;
    }

    @Override
    public String[] getState(Current current) {
        // System.out.println("GetState");
        String[] state = new String[messages.size()];

        for (int i = 0; i < state.length; i++) {
            state[i] = messages.get(i);
        }
        return state;
    }

        @Override
        public void sendMessage(String msg, Current current) {
            // Process message in a separate thread safely
            new Thread(() -> {
                try {
                    processMessage(msg);
                                // try {

                //     Thread.sleep(2000);
                // } catch (Exception e) {
                //     // TODO: handle exception
                // }
                // callbackPrx.notifyCallback();
                } catch (Exception e) {
                    System.err.println("Critical error processing asynchronous message: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }


    public void sendMessageToSubscribers(String message) {
        for (CallbackPrx subscriber : subscribers) {
            try {
                subscriber.notifyCallbackAsync(message);

                dispatchedNotificationCount.incrementAndGet();
            } catch (Exception e) {
                subscribers.remove(subscriber);
            }
        }
    }


    private void processMessage(String serialized) {
        String[] parts = serialized.split("\\|", 4);

        int busId = Integer.parseInt(parts[0]);
        String tag = parts[1].trim();
        String timestamp = parts[2];

        byte[] imageBytes = Base64.getDecoder().decode(parts[3]);

        System.out.println("Received image bytes: " + imageBytes.length);

        Path directory = Paths.get("data/serverImages", Integer.toString(busId));

        
        if ("NORMAL".equals(tag)) {
            try{

                Files.createDirectories(directory);


            }catch(IOException e){
                System.out.println("Failed to create directory");
            }

            
            String filename = System.currentTimeMillis() + ".jpg";

            Path imagePath = directory.resolve(filename);


            try{

                Files.write(imagePath, imageBytes);

                receivedNormalCount.incrementAndGet();

            }catch(IOException e){
                System.out.println("Failed to save image");
            }
        }

        if("GENERALINTEREST".equals(tag)){

            System.out.println("dispatching general interest message to fleet");
            String message = "GENERALINTEREST from " + busId;

            sendMessageToSubscribers(message);

        }


        // System.out.println("Bus ID: " + busId);
        // System.out.println("Tag: " + tag);
        // System.out.println("Timestamp: " + timestamp);
        // System.out.println("Image size: " + imageBytes.length + " bytes");
    }

}
