import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.List;

public class ClientGUI extends JFrame{
    private final Client client;
    private final ServerRequests requests;
    private final ClientGUI thisGUI = this;
    private final ClientSideAuthentication auth;
    private List<Task> allTasks;
    private List<Task> completeTasks;
    private List<Task> incompleteTasks;
    private List<Task> currentTasksList;
    private DefaultTableModel tableModel;

    private JPanel mainPanel;
    private JTable shelf;
    private JLabel tableLabel;
    private JButton allTasksButton;
    private JButton incompleteTasksButton;
    private JButton completeTasksButton;
    private JButton refreshButton;
    private JTextField searchField;
    private JButton searchButton;
    private JButton addTaskButton;
    private JMenuItem aboutMenuItem;
    private JMenuItem authenticateMenuItem;
    private JMenuItem newServerConnectionMenuItem;
    private JMenuItem quitMenuItem;

    ClientGUI(Client client, ServerRequests requests, int screenWidth, int screenHeight){
        this.client = client;
        this.requests = requests;
        auth = requests.getAuth();

        // determine the size of the window
        double scalingFactor = 0.75;
        Dimension windowSize = new Dimension((int) (screenWidth * scalingFactor), (int) (screenHeight * scalingFactor));

        // configure window and set visible
        setContentPane(mainPanel);
        setTitle(String.format("Shelves - %s", requests.getServerURL()));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(windowSize);
        pack();
        setVisible(true);

        addMenuBar();
        addActionListeners();

        // get data from server and display it on the table
        updateTaskLists();
        showTasksOnShelf(incompleteTasks);
        shelf.setModel(tableModel);
        shelf.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void addMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Menu");
        menuBar.add(fileMenu);

        aboutMenuItem = new JMenuItem("About");
        authenticateMenuItem = new JMenuItem("Login as Admin");
        newServerConnectionMenuItem = new JMenuItem("New Server Connection");
        quitMenuItem = new JMenuItem("Quit");

        fileMenu.add(aboutMenuItem);
        fileMenu.add(authenticateMenuItem);
        fileMenu.add(newServerConnectionMenuItem);
        fileMenu.add(quitMenuItem);
        setJMenuBar(menuBar);
    }

    private void addActionListeners(){
        searchButton.addActionListener(actionEvent -> {
            String query = searchField.getText();

            if (!query.isBlank()) { // only do search if field has something that's not whitespace typed in
                tableLabel.setText(String.format("Search Results for \"%s\":", query));
                showTasksOnShelf(requests.search(query));
            }

            searchField.setText(""); // clear field
        });
        searchField.addActionListener(actionEvent -> searchButton.doClick());

        // re-requests the server for task lists and redraws the table
        refreshButton.addActionListener(actionEvent -> {
            updateTaskLists();
            switch (tableLabel.getText()) { // show the tasks list that was previously being displayed, but updated
                case "All tasks:": {
                    currentTasksList = allTasks;
                    showTasksOnShelf(allTasks);
                    break;
                }
                case "Complete Tasks:": {
                    currentTasksList = completeTasks;
                    showTasksOnShelf(completeTasks);
                    break;
                }
                case "Incomplete Tasks:": {
                    currentTasksList = incompleteTasks;
                    showTasksOnShelf(incompleteTasks);
                    break;
                }
                default: { // show incomplete tasks if the label is something else, like a search result
                    incompleteTasksButton.doClick();
                    break;
                }
            }
        });

        allTasksButton.addActionListener(actionEvent -> {
            tableLabel.setText("All tasks:");
            showTasksOnShelf(allTasks);
        });

        completeTasksButton.addActionListener(actionEvent -> {
            tableLabel.setText("Complete Tasks:");
            showTasksOnShelf(completeTasks);
        });

        incompleteTasksButton.addActionListener(actionEvent -> {
            tableLabel.setText("Incomplete Tasks:");
            showTasksOnShelf(incompleteTasks);
        });

        addTaskButton.addActionListener(actionEvent -> {
            if (auth.passwordIsSet()){
                client.openAddTaskForm(requests, this);
            }
            else {
                showNotAuthenticatedMessage();
            }
        });

        aboutMenuItem.addActionListener(actionEvent -> {
            showAbout();
        });

        authenticateMenuItem.addActionListener(actionEvent -> new PasswordInputGUI(requests));

        newServerConnectionMenuItem.addActionListener(actionEvent -> {
            new ServerConnectGUI(client); // will open a new ClientGUI for the new connection
        });

        quitMenuItem.addActionListener(actionEvent -> dispose());

        // show task details for the task corresponding to a row that has been double clicked
        shelf.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            Point point = e.getPoint(); // location of click
            if (e.getClickCount() == 2) { // only do if double clicked
                int selectedRow = shelf.rowAtPoint(point);
                if (selectedRow >= 0) {
                    client.openTaskDetailsGUI(currentTasksList.get(selectedRow), requests, thisGUI);
                }
            }
            }
        });
    }

    private void showTasksOnShelf(List<Task> tasks){
        currentTasksList = tasks;

        String[] tableColumnNames = {"Task Name", "Description", "Date Due", "Complete?"};
        String[][] tableData = new String[tasks.size()][4];

        // make a 2D array that represents each task and the attributes of that task that will be displayed on the table
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            tableData[i][0] = task.getTaskName();
            tableData[i][1] = task.getDescription();
            tableData[i][2] = task.getDateDue().toString();

            if (task.getComplete()) {
                tableData[i][3] = "Yes";
            }
            else {
                tableData[i][3] = "No";
            }
        }

        // add the data to the table
        tableModel = new DefaultTableModel(tableData, tableColumnNames) {
            // override this method so that cells cannot be edited
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        shelf.setModel(tableModel);
    }

    private void updateTaskLists(){
        allTasks = requests.getAllTasks();
        completeTasks = requests.getCompleteTasks();
        incompleteTasks = requests.getIncompleteTasks();
    }

    public void updateAndReset(){
        updateTaskLists();
        showTasksOnShelf(incompleteTasks);
        tableLabel.setText("Incomplete Tasks");
    }

    public void showNotAuthenticatedMessage(){
        JOptionPane.showMessageDialog(
                null, "Not authenticated.\nUse \"Menu\" → \"Login as Admin\" to authenticate.",
                "Not Authenticated", JOptionPane.WARNING_MESSAGE);
    }

    private void showAbout(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader("about.txt"));
            StringBuilder messageBuilder = new StringBuilder();

            String line = reader.readLine();
            while(line != null){
                messageBuilder.append(line).append("\n");
                line = reader.readLine();
            }

            JOptionPane.showMessageDialog(null, messageBuilder.toString(), "About", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (FileNotFoundException e){
            JOptionPane.showMessageDialog(null, "About message not found");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}