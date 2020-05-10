import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddTaskGUI extends JFrame {

    private JPanel mainPanel;
    private JTextField taskNameField;
    private JTextArea descTextArea;
    private JTextField reqTextField;
    private JButton addRequirementButton;
    private JList<String> reqsJList;
    private JButton addTaskButton;
    private JButton cancelButton;
    private JButton addDependencyButton;
    private JButton removeDependencyButton;
    private JList<Task> dependenciesJList;
    private JList<Task> tasksJList;
    private JTabbedPane tabbedPane;
    private JSpinner dateDueSpinner;
    private JButton removeRequirementButton;

    private final DefaultListModel<String> reqsListModel = new DefaultListModel<>();
    private final DefaultListModel<Task> dependenciesListModel = new DefaultListModel<>();
    private final DefaultListModel<Task> tasksListModel = new DefaultListModel<>();
    private final SpinnerDateModel dateDueModel;
    private final List<Task> dependencies = new ArrayList<>();

    private final ServerRequests requests;
    private final ClientGUI parentGUI;
    private final List<String> reqsList;

    // a form to create tasks based on user input and submit the task to the server
    AddTaskGUI(ServerRequests requests, ClientGUI parentGUI, int screenWidth, int screenHeight){
        this.requests = requests;
        this.parentGUI = parentGUI;
        reqsList = new ArrayList<>();

        reqsJList.setModel(reqsListModel);
        dependenciesJList.setModel(dependenciesListModel);
        tasksJList.setModel(tasksListModel);

        tasksListModel.addAll(requests.getAllTasks());

        // set up date spinner
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.DATE, -1); // earliest allowed date
        Date startDate = cal.getTime();
        cal.add(Calendar.DATE, 1); // add that day back
        cal.add(Calendar.YEAR, 101); // go forward 100 years
        Date endDate = cal.getTime(); // the latest allowed time

        dateDueModel = new SpinnerDateModel(now, startDate, endDate, Calendar.YEAR);
        dateDueSpinner.setModel(dateDueModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateDueSpinner, "dd MMMM yyyy");
        dateDueSpinner.setEditor(dateEditor);

        setContentPane(mainPanel);
        setTitle("Add Task");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(screenWidth / 3, (int) (screenHeight * .6)));
        pack();
        setVisible(true);

        addActionListeners();
    }

    private void addActionListeners(){
        addRequirementButton.addActionListener(e -> {
            String requirement = reqTextField.getText();
            if (requirement.contains("*")){ // DB uses * to separate list items in a string
                JOptionPane.showMessageDialog(null, "Requirements cannot contain the character \"*\"",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }
            else {
                reqsList.add(requirement);
                reqsListModel.addElement(requirement);
                reqTextField.setText("");
            }
        });
        reqTextField.addActionListener(e -> addRequirementButton.doClick());

        removeRequirementButton.addActionListener(e -> {
            String requirement = reqsJList.getSelectedValue();
            reqsList.remove(requirement);
            reqsListModel.removeElement(requirement);
        });

        addDependencyButton.addActionListener(e -> {
            Task task = tasksJList.getSelectedValue();
            tasksListModel.removeElement(task);
            dependenciesListModel.addElement(task);
            dependencies.add(task);
        });

        removeDependencyButton.addActionListener(e -> {
            Task task = dependenciesJList.getSelectedValue();
            dependenciesListModel.removeElement(task);
            tasksListModel.addElement(task);
            dependencies.remove(task);
        });

        addTaskButton.addActionListener(e -> submitAddTaskRequest());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void submitAddTaskRequest(){
        String taskName = taskNameField.getText();
        String desc = descTextArea.getText();
        Date dateDue = dateDueModel.getDate();

        // validate input
        if (taskName.isBlank()){
            JOptionPane.showMessageDialog(null, "Task Name field is empty",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (desc.isBlank()){
            JOptionPane.showMessageDialog(null, "Description field is empty",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (reqsList.size() == 0){
            JOptionPane.showMessageDialog(null, "Add at least one Requirement",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> dependenciesList = new ArrayList<>();
        for (Task task : dependencies){
            dependenciesList.add(Integer.toString(task.getID()));
        }

        Task newTask = new Task(taskName, desc, reqsList, dateDue, false, dependenciesList, new ArrayList<>());
        boolean success = requests.addTask(newTask);

        if (!success){
            JOptionPane.showMessageDialog(null, "Failed to create task.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        parentGUI.updateAndReset();
        dispose();
    }
}
