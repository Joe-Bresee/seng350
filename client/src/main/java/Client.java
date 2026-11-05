import java.net.Inet4Address;
import java.util.Arrays;

import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import Demo.CallbackPrx;
import Demo.ChatManagerPrx;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.Random;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;


public class Client {
    public static void main(String[] args) {
        // for (int i = 0; i < args.length; i++) {
        //     System.out.println("arg at index " + i + ": " + args[i]);
        // }

        String busID = args[0];
        int max_images = Integer.parseInt(args[1]);
        int freq = Integer.parseInt(args[2]);
        int p_generalinterest = Integer.parseInt(args[3]);

        String directoryString = "data/" + busID;
        File directory = new File(directoryString);
        File[] images = directory.listFiles();
        
        // System.out.println(images.length);

        int total_num_images = Math.min(max_images, images.length);

        if (total_num_images == 0){
            total_num_images = images.length;
        }

        byte[][] imageBytes = new byte[total_num_images][];
        for (int i = 0; i < total_num_images; i++) {
            try {
                
                imageBytes[i] = Files.readAllBytes(images[i].toPath());
                
                
                // System.out.println("Loaded image " + i + " with size: " + imageBytes[i].length + " bytes");
            } catch (java.io.IOException e) {
                System.err.println("Failed to read image at index " + i);
                e.printStackTrace();
            }
        }

        String tag;
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

                String hostname = Inet4Address.getLocalHost().getHostName();
                System.out.println("hostname: " + hostname);


                for (int i = 0; i < imageBytes.length; i++) {
                    if (rand.nextInt(100) < p_generalinterest) {
                        tag = "GENERALINTEREST";
                    } else { 
                        tag = "NORMAL";
                    }

                    LocalDateTime timestamp = LocalDateTime.now();
                    ImageMessage imagemessage = new ImageMessage(
                        busID,
                        tag,
                        timestamp,
                        imageBytes[i]
                    );

                    imagemessage.printInfo();

                    

                    String serialized = serializeMessage(imagemessage);

                    // System.out.println("Sending image" + i);
                    // System.out.println("Original Bytes " + imageBytes[i].length);
                    // System.out.println("Serialized message characters: " + serialized.length());

                    chatManagerPrx.sendMessage(serialized);

                    try{
                        Thread.sleep(freq);
                    }catch(InterruptedException e){
                        Thread.currentThread().interrupt();
                        return;
                    }
                    // send(imageBytes[i]);
                    // wait(freq);
                }

                // chatManagerPrx.sendMessage(hostname + ": Hello World");

                communicator.waitForShutdown();

            } catch (Exception e) {
                // saveMessage(dest,)
                e.printStackTrace();
            }
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
