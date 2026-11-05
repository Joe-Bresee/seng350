import java.util.Arrays;

import com.zeroc.Ice.Current;

import Demo.ChatManagerPrx;

public class CallbackImp implements Demo.Callback {

    private ChatManagerPrx server;

    CallbackImp(ChatManagerPrx srvr) {
        server = srvr;
    }

    @Override
    public void notifyCallback(String message, Current current) {
        System.out.println("notified from Server");

        System.out.println("Callback executed: " + message);
    }

}
