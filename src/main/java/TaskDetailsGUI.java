import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TaskDetailsGUI extends JFrame{
    private Task task;
    private JPanel mainPanel;
    private JButton closeButton;
    private JLabel nameLabel;
    private JLabel descLabel;
    private JList<String> reqsList;
    private JList<String> dependenciesList;
    private JList<String> dependentsList;

    private List<Task> dependencies;
    private List<Task> dependents;

    TaskDetailsGUI(Task task, ServerRequests requests, Component parentComponent){
        this.task = task;

        dependencies = requests.getDependencies(task.getID());
        dependents = requests.getDependents(task.getID());

        setContentPane(mainPanel);
        setTitle(String.format("Task Details - %s", task.getTaskName()));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(500, 400));
        setLocationRelativeTo(parentComponent);
        pack();
        setVisible(true);

        closeButton.addActionListener(actionEvent -> dispose());
        setupDetailsComponents();
    }

    private void setupDetailsComponents(){
        nameLabel.setText(task.getTaskName());
        descLabel.setText(task.getDescription());

        DefaultListModel<String> reqsListModel = new DefaultListModel<>();
        DefaultListModel<String> dependenciesListModel = new DefaultListModel<>();
        DefaultListModel<String> dependentsListModel = new DefaultListModel<>();

        reqsListModel.addAll(task.getRequirements());
        for (Task task : dependencies){
            if (task.getComplete()) {
                dependenciesListModel.addElement(task.getTaskName() + "\t\tComplete");
            }
            else {
                dependenciesListModel.addElement(task.getTaskName() + "\t\tIncomplete");
            }
        }
        for (Task task : dependents){
            dependentsListModel.addElement(task.getTaskName());
        }

        reqsList.setModel(reqsListModel);

        // TODO: make these clickable, opening their own TDGUIs
        dependenciesList.setModel(dependenciesListModel);
        dependentsList.setModel(dependentsListModel);
    }
}
