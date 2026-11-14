public class Server {
    public static void main(String[] args) {
        String configFile = args.length > 0 ? args[0] : "server.cfg";
        try (com.zeroc.Ice.Communicator communicator =
                     com.zeroc.Ice.Util.initialize(args, configFile)) {
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Service");
            ChatManagerImp chatManagerServant = new ChatManagerImp();
            
            adapter.add(chatManagerServant, com.zeroc.Ice.Util.stringToIdentity("ChatManager"));
            adapter.activate();
            
            System.out.println("Server running.");
            
            communicator.waitForShutdown();
        }
    }
}