import javax.swing.*;
import java.awt.*;

public class ServerConnectGUI extends JFrame {

    private JTextField serverURLTextField;
    private JButton connectButton;
    private JPanel serverConnectPanel;
    private JLabel statusLabel;

    private final Client client;
    private final ServerRequests requests;

    ServerConnectGUI(Client client){
        this.client = client;
        this.requests = new ServerRequests();

        setContentPane(serverConnectPanel);
        setTitle("Connect to a Server");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(400, 100));
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setVisible(true);

        connectButton.addActionListener(actionEvent -> testConnection());
        serverURLTextField.addActionListener(actionEvent -> testConnection());
    }

    // tries to establish a connection with the server, and creates a ClientGUI if connection is successful
    private void testConnection(){
        String serverURL = serverURLTextField.getText();

        if (!serverURL.strip().equals("")) {
            requests.setServerURL(serverURL);
            boolean successfulConnection = requests.testConnection();

            if (successfulConnection){
                client.openClientGUI(requests);
                dispose();
            }
            else {
                statusLabel.setText("Connection failed");
            }
        }
        else {
            statusLabel.setText("No address");
        }
    }
}
