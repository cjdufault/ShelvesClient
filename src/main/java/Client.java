import java.awt.*;

public class Client {

    // get the size of the active display
    private final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private final int screenWidth = gd.getDisplayMode().getWidth();
    private final int screenHeight = gd.getDisplayMode().getHeight();

    public static void main(String[] args) {
        Client client = new Client();

        // ServerConnectGUI opens first, and the main GUI will be opened once a connection is successfully established
        new ServerConnectGUI(client);
    }

    // will be called by the ServerConnectGUI when a successful connection has been established
    public void openClientGUI(ServerRequests requests){
        new ClientGUI(this, requests, screenWidth, screenHeight);
    }

    public void openTaskDetailsGUI(Task task, ServerRequests requests, ClientGUI parentGUI){
        new TaskDetailsGUI(task, requests, parentGUI, screenWidth, screenHeight);
    }

    public void openAddTaskForm(ServerRequests requests, ClientGUI parentGUI){
        new AddTaskGUI(requests, parentGUI, screenWidth, screenHeight);
    }
}
