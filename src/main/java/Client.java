public class Client {

    public static void main(String[] args) {
        Client client = new Client();
        ServerRequests requests = new ServerRequests();

        ServerConnectGUI serverConnect = new ServerConnectGUI(requests);
        ClientGUI gui = new ClientGUI(client, requests);
    }
}
