import javax.swing.*;

public class ClientGUI extends JFrame{

    private Client client;
    private ServerRequests requests;

    private JPanel mainPanel;
    private JTable shelf;
    private JButton detailsButton;
    private JMenuItem aboutMenuItem;
    private JMenuItem openServerConnectionMenuItem;
    private JMenuItem quitMenuItem;

    ClientGUI(Client client, ServerRequests requests){
        this.client = client;
        this.requests = requests;

        setContentPane(mainPanel);
        setTitle(String.format("Shelves - %s", requests.getServerURL()));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);

        addMenuBar();
        addActionListeners();
    }

    private void addMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        aboutMenuItem = new JMenuItem("About");
        openServerConnectionMenuItem = new JMenuItem("Open Server Connection");
        quitMenuItem = new JMenuItem("Quit");

        fileMenu.add(aboutMenuItem);
        fileMenu.add(openServerConnectionMenuItem);
        fileMenu.add(quitMenuItem);
        setJMenuBar(menuBar);
    }

    private void addActionListeners(){
        detailsButton.addActionListener(actionEvent -> {

        });

        aboutMenuItem.addActionListener(actionEvent -> {

        });

        openServerConnectionMenuItem.addActionListener(actionEvent -> {
            new ServerConnectGUI(client); // will open a new ClientGUI for the new connection
        });

        quitMenuItem.addActionListener(actionEvent -> dispose());
    }
}
