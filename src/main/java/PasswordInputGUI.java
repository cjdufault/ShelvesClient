import javax.swing.*;

public class PasswordInputGUI extends JFrame{
    private final ClientSideAuthentication auth;
    private final ServerRequests requests;

    private JPanel mainPanel;
    private JPasswordField passwordField;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    PasswordInputGUI(ServerRequests requests){
        this.requests = requests;
        auth = requests.getAuth();

        setContentPane(mainPanel);
        setTitle("Input Password");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);

        submitButton.addActionListener(e -> setPassword());
        cancelButton.addActionListener(e -> dispose());
        passwordField.addActionListener(e -> submitButton.doClick());
    }

    private void setPassword(){
        char[] pwChars = passwordField.getPassword();

        if (pwChars.length > 0) {
            StringBuilder pwBuilder = new StringBuilder();
            for (char c : pwChars) {
                pwBuilder.append(c);
            }
            auth.setCredentials(pwBuilder.toString());

            // check if password is correct
            boolean authenticated = requests.authenticate();
            if (authenticated) {
                dispose();
            } else {
                auth.clearCredentials();
                statusLabel.setText("Authentication failed");
            }
        }
    }
}
