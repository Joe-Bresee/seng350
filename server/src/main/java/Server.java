public class Server {
    public static void main(String[] args) {
        String configFile = "server.cfg";
        java.util.List<String> iceArguments = new java.util.ArrayList<>();
        for (String argument : args) {
            if (argument.startsWith("--config=")) {
                configFile = argument.substring("--config=".length());
            } else {
                iceArguments.add(argument);
            }
        }

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(
                iceArguments.toArray(new String[0]), configFile)) {
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Service");
            ControlCenter controlCenter = new ControlCenter();
            
            adapter.add(controlCenter, com.zeroc.Ice.Util.stringToIdentity("NotificationSubject"));
            adapter.activate();
            
            System.out.println("Control center running with configuration: " + configFile);
            
            communicator.waitForShutdown();
        }
    }
}