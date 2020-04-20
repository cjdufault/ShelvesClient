import javax.swing.*;

public class ClientGUI extends JFrame{
    private JPanel mainPanel;
    private JTable shelf;
    private JButton detailsButton;

    ClientGUI(Client client, ServerRequests requests){
        setContentPane(mainPanel);
        setTitle("Shelves");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        addMenuBar();
        addActionListeners();
    }

    private void addMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem aboutMenuItem = new JMenuItem("About");
        JMenuItem openServerConnectionMenuItem = new JMenuItem("Open Server Connection");
        JMenuItem quitMenuItem = new JMenuItem("Quit");

        fileMenu.add(aboutMenuItem);
        fileMenu.add(openServerConnectionMenuItem);
        fileMenu.add(quitMenuItem);

        quitMenuItem.addActionListener(actionEvent -> dispose());

        setJMenuBar(menuBar);
    }

    private void addActionListeners(){
        detailsButton.addActionListener(actionEvent -> {

        });
    }
}
