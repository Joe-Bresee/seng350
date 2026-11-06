public class Server {
    public static void main(String[] args) {
        int expectedCount = 900; 
        if (args.length > 0) {
            try {
                expectedCount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid threshold format, using default: " + expectedCount);
            }
        }

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "server.cfg")) {
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Service");
            ChatManagerImp chatManagerServant = new ChatManagerImp(expectedCount);
            
            adapter.add(chatManagerServant, com.zeroc.Ice.Util.stringToIdentity("ChatManager"));
            adapter.activate();
            
            System.out.println("Server running. Target profile threshold: " + expectedCount);
            
            communicator.waitForShutdown();
        }
    }
}