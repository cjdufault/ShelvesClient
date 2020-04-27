import java.awt.*;

public class Client {

    // get the size of the active display
    private GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private int screenWidth = gd.getDisplayMode().getWidth();
    private int screenHeight = gd.getDisplayMode().getHeight();

    public static void main(String[] args) {
        Client client = new Client();

        // ServerConnectGUI opens first, and the main GUI will be opened once a connection is successfully established
        ServerConnectGUI serverConnect = new ServerConnectGUI(client);
    }

    // will be called by the ServerConnectGUI when a successful connection has been established
    public void openClientGUI(ServerRequests requests){
        ClientGUI gui = new ClientGUI(this, requests, screenWidth, screenHeight);
    }
}
