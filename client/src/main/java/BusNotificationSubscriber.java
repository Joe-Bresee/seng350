import com.zeroc.Ice.Current;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BusNotificationSubscriber implements Demo.BusNotificationObserver {
    private final CountDownLatch notificationReceived = new CountDownLatch(1);

    @Override
    public void notifyCallback(String message, Current current) {
        System.out.println("Notification received: " + message);
        notificationReceived.countDown();
    }

    public void awaitNotification(long timeoutMs) throws InterruptedException {
        notificationReceived.await(timeoutMs, TimeUnit.MILLISECONDS);
    }
}