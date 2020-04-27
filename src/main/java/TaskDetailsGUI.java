import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;

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
    private ServerRequests requests;

    TaskDetailsGUI(Task task, ServerRequests requests, Component parentComponent){
        this.task = task;
        this.requests = requests;

        dependencies = requests.getDependencies(task.getID());
        dependents = requests.getDependents(task.getID());

        setContentPane(mainPanel);
        setTitle(String.format("Task Details - %s", task.getTaskName()));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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

        addDoubleClickMouseListener(dependenciesList, dependencies);
        addDoubleClickMouseListener(dependentsList, dependents);
    }

    private void addDoubleClickMouseListener(JList<String> jList, List<Task> taskList){
        Component thisGUI = this;

        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                if (e.getClickCount() == 2){
                    int selectedIndex = jList.locationToIndex(point);
                    if (selectedIndex >= 0){
                        new TaskDetailsGUI(taskList.get(selectedIndex), requests, thisGUI);
                    }
                }
            }
        });
    }
}
