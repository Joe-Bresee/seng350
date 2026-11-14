import java.net.Inet4Address;

import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import Demo.CallbackPrx;
import Demo.ChatManagerPrx;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Base64;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;


public class Client {
    public static void main(String[] args) {
        // for (int i = 0; i < args.length; i++) {
        //     System.out.println("arg at index " + i + ": " + args[i]);
        // }

        String busID = args[0];
        int max_images = Integer.parseInt(args[1]);
        int freq = Integer.parseInt(args[2]);
        int p_critical = Integer.parseInt(args[3]);
        Random rand = new Random();
        


        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "client.cfg")) {

            Demo.ChatManagerPrx chatManagerPrx = Demo.ChatManagerPrx
                    .checkedCast(communicator.propertyToProxy("ChatManager.Proxy"));

            try {

                CallbackImp callbackImp = new CallbackImp(chatManagerPrx);

                ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Callback", "tcp -h 0.0.0.0 -p 0");

                ObjectPrx objectPrx = adapter.add(callbackImp, Util.stringToIdentity("Callback"));
                adapter.activate();

                CallbackPrx prx = CallbackPrx.uncheckedCast(objectPrx);
                chatManagerPrx.subscribe(prx);
                chatManagerPrx.sendMessage("REGISTER|" + busID);

                String hostname = Inet4Address.getLocalHost().getHostName();
                System.out.println("hostname: " + hostname);


                long runStart = System.nanoTime();
                for (int i = 0; i < max_images; i++) {
                    String tag = rand.nextInt(100) < p_critical ? "CRITICAL" : "NORMAL";
                    byte[] imageBytes = captureScreenshot();

                    LocalDateTime timestamp = LocalDateTime.now();
                    ImageMessage imagemessage = new ImageMessage(
                        busID,
                        tag,
                        timestamp,
                        imageBytes
                    );

                    imagemessage.printInfo();

                    

                    String serialized = serializeMessage(imagemessage);

                    // System.out.println("Sending image" + i);
                    // System.out.println("Original Bytes " + imageBytes.length);
                    // System.out.println("Serialized message characters: " + serialized.length());

                    long sendStart = System.nanoTime();
                    chatManagerPrx.sendMessage(serialized);
                    System.out.printf("LATENCY_MS bus=%s image=%d value=%.3f%n",
                            busID, i + 1, (System.nanoTime() - sendStart) / 1_000_000.0);

                    try{
                        Thread.sleep(freq);
                    }catch(InterruptedException e){
                        Thread.currentThread().interrupt();
                        return;
                    }
                    // wait(freq);
                }

                // chatManagerPrx.sendMessage(hostname + ": Hello World");
                double elapsedSeconds = (System.nanoTime() - runStart) / 1_000_000_000.0;
                System.out.printf("THROUGHPUT bus=%s messages=%d value=%.3f%n",
                        busID, max_images, max_images / elapsedSeconds);
                System.out.println("Client shutting down communicator...");
                communicator.shutdown();
                communicator.waitForShutdown();

            } catch (Exception e) {
                // saveMessage(dest,)
                e.printStackTrace();
            }

        }

    }
    private static byte[] captureScreenshot() {
        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            BufferedImage image = new Robot().createScreenCapture(new Rectangle(screen));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", output);
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to capture a screenshot", e);
        }
    }

    private static String serializeMessage(ImageMessage message) {

        String imageBase64 = Base64.getEncoder()
            .encodeToString(message.getImage());

            return message.getBusID() + "|" +
                message.getTag() + "|" +
                message.getTimestamp() + "|" +
                imageBase64;
    }
}
