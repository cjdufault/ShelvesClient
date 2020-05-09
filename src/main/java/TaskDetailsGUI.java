import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TaskDetailsGUI extends JFrame{

    int screenWidth;
    int screenHeight;

    private Task task;
    private JPanel mainPanel;
    private JButton closeButton;
    private JLabel nameLabel;
    private JList<String> reqsList;
    private JList<String> dependenciesList;
    private JList<String> dependentsList;
    private JLabel completeLabel;
    private JLabel dateCreatedLabel;
    private JLabel dateDueLabel;
    private JLabel dateCompleteLabel;
    private JTextPane descTextPane;
    private JButton completeTaskButton;
    private JButton deleteTaskButton;
    private JTextField claimTextField;
    private JButton claimButton;

    private final List<Task> dependencies;
    private final List<Task> dependents;
    private final ServerRequests requests;
    private final ClientSideAuthentication auth;
    private final ClientGUI parentGUI;

    TaskDetailsGUI(Task task, ServerRequests requests, ClientGUI parentGUI, int screenWidth, int screenHeight){
        this.task = task;
        this.requests = requests;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.auth = requests.getAuth();
        this.parentGUI = parentGUI;

        double heightScalingFactor = 0.7;
        double widthScalingFactor = 0.5;
        Dimension windowSize = new Dimension((int) (screenWidth * widthScalingFactor), (int) (screenHeight * heightScalingFactor));

        dependencies = requests.getDependencies(task.getID());
        dependents = requests.getDependents(task.getID());

        setContentPane(mainPanel);
        setTitle(String.format("Task Details - %s", task.getTaskName()));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(windowSize);
        setLocation(screenWidth - windowSize.width, 0);
        pack();
        setVisible(true);

        setupDetailsComponents();
        addActionListeners();
    }

    private void addActionListeners(){
        addDoubleClickMouseListener(dependenciesList, dependencies);
        addDoubleClickMouseListener(dependentsList, dependents);

        claimButton.addActionListener(e -> setupClaimButton());
        claimTextField.addActionListener(e -> claimButton.doClick());
        completeTaskButton.addActionListener(e -> completeTask());
        deleteTaskButton.addActionListener(e -> deleteTask());
        closeButton.addActionListener(actionEvent -> dispose());
    }

    private void setupDetailsComponents(){
        // fill in swing items with task info
        nameLabel.setText(task.getTaskName());
        descTextPane.setText(task.getDescription());
        dateCreatedLabel.setText("Date Created: " + task.getDateCreated().toString());
        dateDueLabel.setText("Date Due: " + task.getDateCreated().toString());
        if (task.getComplete()){
            completeLabel.setText("Complete");
            dateCompleteLabel.setText("Date Complete: " + task.getDateComplete().toString());
        }
        else {
            completeLabel.setText("Incomplete");
            dateCompleteLabel.setText("Date Complete: N/A");
        }

        DefaultListModel<String> reqsListModel = new DefaultListModel<>();
        DefaultListModel<String> dependenciesListModel = new DefaultListModel<>();
        DefaultListModel<String> dependentsListModel = new DefaultListModel<>();

        // populate the JLists
        reqsListModel.addAll(task.getRequirements());
        for (Task task : dependencies){
            if (task.getComplete()) {
                dependenciesListModel.addElement(task.getTaskName() + " -- Complete");
            }
            else {
                dependenciesListModel.addElement(task.getTaskName() + " -- Incomplete");
            }
        }
        for (Task task : dependents){
            dependentsListModel.addElement(task.getTaskName());
        }

        reqsList.setModel(reqsListModel);
        dependenciesList.setModel(dependenciesListModel);
        dependentsList.setModel(dependentsListModel);

        setupClaimedByEmail();
        if (task.getComplete()){
            completeTaskButton.setEnabled(false);
        }
    }

    private void setupClaimedByEmail(){
        String claimedByEmail = task.getClaimedByEmail();
        if (claimedByEmail == null){
            claimTextField.setEditable(true);
            claimButton.setText("Claim Task");
        }
        else {
            claimTextField.setEditable(false);
            claimTextField.setText(claimedByEmail);
            claimButton.setText("Clear");
        }
    }

    private void setupClaimButton(){
        if (auth.passwordIsSet()) {
            if (claimButton.getText().equals("Claim Task")) {
                String claimedByEmail = claimTextField.getText().strip();

                // validate email address
                if (claimedByEmail.matches("[\\S]+@\\w+(\\.\\w+)+")){
                    boolean success = requests.updateClaim(task.getID(), claimedByEmail);
                    if (success) {
                        claimTextField.setEditable(false);
                        claimButton.setText("Clear");
                        claimTextField.setText(claimedByEmail);
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Updating email failed");
                    }
                }
                else {
                    JOptionPane.showMessageDialog(null, "Invalid email address");
                }
            }
            else if (claimButton.getText().equals("Clear")) {
                claimButton.setText("Claim Task");
                claimTextField.setText("");
                claimTextField.setEditable(true);
            }
            // if it's something else for some reason, just reset it
            else{
                setupClaimedByEmail();
            }
        }
        else {
            parentGUI.showNotAuthenticatedMessage();
        }
    }

    private void completeTask(){
        if (auth.passwordIsSet()){
            // get task objects for all dependencies
            List<Task> dependencies = new ArrayList<>();
            for (String IDString : task.getDependencies()){
                int ID = Integer.parseInt(IDString);
                dependencies.add(requests.getTask(ID));
            }

            // check all dependencies are complete
            boolean dependenciesComplete = true;
            List<Task> incompleteDependencies = new ArrayList<>();
            for (Task dependency : dependencies){
                if (!dependency.getComplete()) {
                    dependenciesComplete = false;
                    incompleteDependencies.add(dependency);
                }
            }

            if (dependenciesComplete) {
                if (showConfirmationDialog("Are you sure you want to complete this task?") == JOptionPane.YES_OPTION) {
                    boolean success = requests.completeTask(task.getID());
                    if (success) {
                        task = requests.getTask(task.getID());
                        setupDetailsComponents();
                        JOptionPane.showMessageDialog(null, "Task completed successfully");
                        parentGUI.updateAndReset();
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to complete task");
                    }
                }
            }
            else {
                StringBuilder messageBuilder = new StringBuilder("Not all prerequisite tasks are complete.\nIncomplete prerequisites:");
                for (Task incompleteTask : incompleteDependencies){
                    messageBuilder.append(String.format("\n - %s", incompleteTask.getTaskName()));
                }
                JOptionPane.showMessageDialog(null, messageBuilder.toString());
            }
        }
        else {
            parentGUI.showNotAuthenticatedMessage();
        }
    }

    private void deleteTask(){
        if (auth.passwordIsSet()){
            if (showConfirmationDialog("Are you sure you want to delete this task?") == JOptionPane.YES_OPTION){
                boolean success = requests.removeTask(task.getID());
                if (success){
                    JOptionPane.showMessageDialog(null, "Task deleted successfully");
                    parentGUI.updateAndReset();
                    dispose();
                }
                else{
                    JOptionPane.showMessageDialog(null, "Failed to delete task");
                }
            }
        }
        else {
            parentGUI.showNotAuthenticatedMessage();
        }
    }

    // listens for a double click on an item in a JList
    private void addDoubleClickMouseListener(JList<String> jList, List<Task> taskList){
        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                if (e.getClickCount() == 2){
                    int selectedIndex = jList.locationToIndex(point);
                    if (selectedIndex >= 0){
                        new TaskDetailsGUI(taskList.get(selectedIndex), requests, parentGUI, screenWidth, screenHeight);
                    }
                }
            }
        });
    }

    private int showConfirmationDialog(String message){
        return JOptionPane.showConfirmDialog(null, message, "Please Confirm", JOptionPane.YES_NO_OPTION);
    }
}
