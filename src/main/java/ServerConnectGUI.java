import javax.swing.*;

public class ServerConnectGUI extends JFrame {

    private JTextField serverURLTextField;
    private JButton connectButton;
    private JPanel serverConnectPanel;
    private JLabel statusLabel;

    private ServerRequests requests;

    ServerConnectGUI(ServerRequests requests){
        this.requests = requests;

        setContentPane(serverConnectPanel);
        setTitle("Connect to a Server");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setVisible(true);

        connectButton.addActionListener(actionEvent -> {
            testConnection();
        });
    }

    private void testConnection(){
        String serverURL = serverURLTextField.getText();

        if (!serverURL.strip().equals("")) {
            statusLabel.setText("...");
            boolean successfulConnection = requests.testConnection(serverURL);

            if (successfulConnection){
                requests.setServerURL(serverURL);
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
