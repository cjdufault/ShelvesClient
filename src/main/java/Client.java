public class Client {

    public static void main(String[] args) {
        Client client = new Client();

        // ServerConnectGUI opens first, and the main GUI will be opened once a connection is successfully established
        ServerConnectGUI serverConnect = new ServerConnectGUI(client);
    }

    // will be called by the ServerConnectGUI when a successful connection has been established
    public void openMainGUI(ServerRequests requests){
        ClientGUI gui = new ClientGUI(this, requests);
    }
}
