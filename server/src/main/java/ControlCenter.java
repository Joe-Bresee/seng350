import com.zeroc.Ice.Current;
import Demo.BusNotificationObserverPrx;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ControlCenter implements Demo.NotificationSubject {
    private final Map<BusNotificationObserverPrx, String> busObservers = new ConcurrentHashMap<>();
    private final ImageStorage imageStorage = new ImageStorage(Paths.get("Group9"));
    private final CriticalityProcessor criticalityProcessor = new CriticalityProcessor();
    private final AtomicInteger processedMessages = new AtomicInteger();

    @Override
    public void subscribe(BusNotificationObserverPrx observer, String busId, Current current) {
        busObservers.put(observer, busId);
        System.out.println("Bus subscribed: " + busId);
    }

    @Override
    public String[] getState(Current current) {
        return new String[0];
    }

    @Override
    public void sendMessage(String serializedMessage, Current current) {
        try {
            ImageMessage message = deserialize(serializedMessage);
            imageStorage.store(message);
            CriticalityProcessor.EventTag tag = criticalityProcessor.determineTag(message.getTag());
            if (tag == CriticalityProcessor.EventTag.GENERALINTEREST) {
                notifyAllBuses("GENERALINTEREST from " + message.getBusID());
            } else if (tag == CriticalityProcessor.EventTag.CRITICAL) {
                notifyOriginatingBus(message.getBusID(), "ACTION for " + message.getBusID());
            }
            int count = processedMessages.incrementAndGet();
            if (count % 100 == 0) {
                System.out.println("Processed messages: " + count);
            }
        } catch (Exception exception) {
            System.err.println("Unable to process message: " + exception.getMessage());
        }
    }

    private void notifyAllBuses(String message) {
        busObservers.forEach((observer, ignoredBusId) -> notifyObserver(observer, message));
    }

    private void notifyOriginatingBus(String busId, String message) {
        busObservers.forEach((observer, subscribedBusId) -> {
            if (busId.equals(subscribedBusId)) {
                notifyObserver(observer, message);
            }
        });
    }

    private void notifyObserver(BusNotificationObserverPrx observer, String message) {
        try {
            observer.notifyCallbackAsync(message);
        } catch (Exception exception) {
            busObservers.remove(observer);
        }
    }

    private ImageMessage deserialize(String serializedMessage) {
        String[] parts = serializedMessage.split("\\|", 4);
        return new ImageMessage(parts[0], parts[1], LocalDateTime.parse(parts[2]),
                Base64.getDecoder().decode(parts[3]));
    }
}