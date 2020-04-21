import javax.swing.*;
import java.awt.*;

public class ServerConnectGUI extends JFrame {

    private JTextField serverURLTextField;
    private JButton connectButton;
    private JPanel serverConnectPanel;
    private JLabel statusLabel;

    private Client client;
    private ServerRequests requests;

    ServerConnectGUI(Client client){
        this.client = client;
        this.requests = new ServerRequests();

        setContentPane(serverConnectPanel);
        setTitle("Connect to a Server");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400, 100));
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setVisible(true);

        connectButton.addActionListener(actionEvent -> testConnection());
        serverURLTextField.addActionListener(actionEvent -> testConnection());
    }

    private void testConnection(){
        String serverURL = serverURLTextField.getText();

        if (!serverURL.strip().equals("")) {
            boolean successfulConnection = requests.testConnection(serverURL);

            if (successfulConnection){
                client.openMainGUI(requests);
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
