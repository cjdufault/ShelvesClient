import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    private List<Task> dependencies;
    private List<Task> dependents;
    private ServerRequests requests;

    TaskDetailsGUI(Task task, ServerRequests requests, int screenWidth, int screenHeight){
        this.task = task;
        this.requests = requests;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        double heightScalingFactor = 0.7;
        double widthScalingFactor = 0.4;
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

        closeButton.addActionListener(actionEvent -> dispose());
        setupDetailsComponents();
    }

    private void setupDetailsComponents(){
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
        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                if (e.getClickCount() == 2){
                    int selectedIndex = jList.locationToIndex(point);
                    if (selectedIndex >= 0){
                        new TaskDetailsGUI(taskList.get(selectedIndex), requests, screenWidth, screenHeight);
                    }
                }
            }
        });
    }
}
