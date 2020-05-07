import javax.swing.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AddTaskForm extends JFrame {

    private JPanel mainPanel;
    private JTextField taskNameField;
    private JTextArea descTextArea;
    private JTextField reqTextField;
    private JButton addRequirementButton;
    private JList<String> reqsJList;
    private JFormattedTextField dateDueFormattedTextField;
    private JButton addTaskButton;
    private JButton cancelButton;
    private JButton addDependencyButton;
    private JButton removeDependencyButton;
    private JList<Task> dependenciesJList;
    private JList<Task> tasksJList;

    private final DefaultListModel<String> reqsListModel = new DefaultListModel<>();
    private final DefaultListModel<Task> dependenciesListModel = new DefaultListModel<>();
    private final DefaultListModel<Task> tasksListModel = new DefaultListModel<>();

    private final ServerRequests requests;
    private final List<String> reqsList;

    AddTaskForm(ServerRequests requests){
        this.requests = requests;
        reqsList = new ArrayList<>();

        reqsJList.setModel(reqsListModel);
        dependenciesJList.setModel(dependenciesListModel);
        tasksJList.setModel(tasksListModel);

        tasksListModel.addAll(requests.getAllTasks());

        setContentPane(mainPanel);
        setTitle("Add Task");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);

        addActionListeners();
    }

    private void addActionListeners(){
        addRequirementButton.addActionListener(e -> {
            String requirement = reqTextField.getText();
            reqsList.add(requirement);
            reqsListModel.addElement(requirement);
            reqTextField.setText("");
        });
        reqTextField.addActionListener(e -> addRequirementButton.doClick());

        addDependencyButton.addActionListener(e -> {
            Task task = tasksJList.getSelectedValue();
            tasksListModel.removeElement(task);
            dependenciesListModel.addElement(task);
        });

        removeDependencyButton.addActionListener(e -> {
            Task task = dependenciesJList.getSelectedValue();
            dependenciesListModel.removeElement(task);
            tasksListModel.addElement(task);
        });

        addTaskButton.addActionListener(e -> submitAddTaskRequest());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void submitAddTaskRequest(){
        String taskName = taskNameField.getText();
        String desc = descTextArea.getText();


        dispose();
    }
}
