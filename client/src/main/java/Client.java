import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import Demo.BusNotificationObserverPrx;
import Demo.NotificationSubjectPrx;
import java.time.LocalDateTime;
import java.util.Base64;


public class Client {
    public static void main(String[] args) throws Exception {
        // for (int i = 0; i < args.length; i++) {
        //     System.out.println("arg at index " + i + ": " + args[i]);
        // }

        if (args.length < 4) {
            System.err.println("Usage: Client <busId> <imageCount> <frequencyMs> <criticalPercentage>");
            return;
        }

        String busId = args[0];
        int imageCount = Integer.parseInt(args[1]);
        int frequencyMs = Integer.parseInt(args[2]);
        int criticalPercentage = Integer.parseInt(args[3]);
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "client.cfg")) {
                NotificationSubjectPrx notificationSubject = NotificationSubjectPrx
                    .checkedCast(communicator.propertyToProxy("NotificationSubject.Proxy"));

            BusNotificationSubscriber subscriber = new BusNotificationSubscriber();
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Callback", "tcp -h 0.0.0.0 -p 0");
            ObjectPrx objectPrx = adapter.add(subscriber, Util.stringToIdentity("BusNotificationSubscriber"));
            adapter.activate();
            BusNotificationObserverPrx observer = BusNotificationObserverPrx.uncheckedCast(objectPrx);
            notificationSubject.subscribe(observer, busId);

            long totalNanos = 0;
            long batchStart = System.nanoTime();
            for (int i = 0; i < imageCount; i++) {
                String tag = i < imageCount * criticalPercentage / 100 ? "CRITICAL" : "NORMAL";
                byte[] image = ScreenshotCapture.capture(busId, i + 1);
                ImageMessage message = new ImageMessage(busId, tag, LocalDateTime.now(), image);
                long sendStart = System.nanoTime();
                notificationSubject.sendMessage(serializeMessage(message));
                long latencyNanos = System.nanoTime() - sendStart;
                totalNanos += latencyNanos;
                System.out.printf("sent=%d tag=%s latencyMs=%.3f%n", i + 1, tag,
                        latencyNanos / 1_000_000.0);
                if (frequencyMs > 0) {
                    Thread.sleep(frequencyMs);
                }
            }
            double elapsedSeconds = (System.nanoTime() - batchStart) / 1_000_000_000.0;
            System.out.printf("Client summary: bus=%s messages=%d averageSendLatencyMs=%.3f throughput=%.2f msg/sec%n",
                    busId, imageCount, totalNanos / 1_000_000.0 / imageCount, imageCount / elapsedSeconds);
            if (criticalPercentage > 0) {
                subscriber.awaitNotification(500);
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
