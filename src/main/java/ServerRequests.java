import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerRequests {

    private static final String TEST_CONNECTION_QUERY = "/test_connection";
    private static final String GET_ALL_TASKS_QUERY = "/get_all_tasks";
    private static final String GET_COMPLETE_TASKS_QUERY = "/get_complete_tasks";
    private static final String GET_INCOMPLETE_TASKS_QUERY = "/get_incomplete_tasks";
    private static final String GET_TASK_QUERY = "/get_task/";
    private static final String SEARCH_QUERY = "/search/";
    private static final String GET_DEPENDENCIES_QUERY = "/get_dependencies/";
    private static final String GET_DEPENDENTS_QUERY = "/get_dependents/";
    private static final String REMOVE_TASK_QUERY = "/remove_task/";
    private static final String COMPLETE_TASK_QUERY = "/complete_task/";
    private static final String ADD_TASK_UPDATE = "/add_task";
    private static final String ADD_DEPENDENCY_UPDATE = "/add_dependency";
    private static final String REMOVE_DEPENDENCY_UPDATE = "/remove_dependency";
    private static final String CLAIM_UPDATE = "/update_claim";

    private static final int TIMEOUT = 15000; // milliseconds until timeout
    private String serverURL;

    public void setServerURL(String url) {
        // make sure the prefix is "http://"
        if (url.startsWith("https://")){
            serverURL = url.replace("https://", "http://");
        }
        else if (!url.startsWith("http://")){
            serverURL = "http://" + url;
        }
        else {
            serverURL = url;
        }
    }
    public String getServerURL(){
        return serverURL;
    }

    /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~GET REQUESTS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ **/

    // attempts to make a connection with the specified server; sets the serverURL if connection is successful
    public boolean testConnection(){
        JSONObject jsonResponse = getRequest(TEST_CONNECTION_QUERY);
        try {
            // check status code and that the server is a ShelvesServer
            if (jsonResponse != null) {
                int statusCode = jsonResponse.getInt("status_code");
                String serviceName = jsonResponse.getString("service_name");
                return statusCode == 0 && serviceName.equals("ShelvesServer");
            }
            serverURL = null; // set serverURL to null if connection unsuccessful
            return false;
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        serverURL = null;
        return false;
    }

    public List<Task> getAllTasks(){
        JSONObject jsonResponse = getRequest(GET_ALL_TASKS_QUERY);
        return convertJSONResponseToTaskList(jsonResponse);
    }

    public List<Task> getCompleteTasks(){
        JSONObject jsonResponse = getRequest(GET_COMPLETE_TASKS_QUERY);
        return convertJSONResponseToTaskList(jsonResponse);
    }

    public List<Task> getIncompleteTasks(){
        JSONObject jsonResponse = getRequest(GET_INCOMPLETE_TASKS_QUERY);
        return convertJSONResponseToTaskList(jsonResponse);
    }

    public Task getTask(int ID){
        JSONObject jsonResponse = getRequest(GET_TASK_QUERY + ID);

        if (jsonResponse != null) {
            JSONArray jsonArray = jsonResponse.getJSONArray("results");
            return parseTaskJSON(jsonArray.getJSONObject(0));
        }
        return null;
    }

    public List<Task> search(String searchString){
        JSONObject jsonResponse = getRequest(SEARCH_QUERY + searchString);
        return convertJSONResponseToTaskList(jsonResponse);
    }

    public List<Task> getDependencies(int ID){
        JSONObject jsonResponse = getRequest(GET_DEPENDENCIES_QUERY + ID);
        return convertJSONResponseToTaskList(jsonResponse);
    }

    public List<Task> getDependents(int ID){
        JSONObject jsonResponse = getRequest(GET_DEPENDENTS_QUERY + ID);
        return convertJSONResponseToTaskList(jsonResponse);
    }

    public boolean removeTask(int ID){
        JSONObject jsonResponse = getRequest(REMOVE_TASK_QUERY + ID);
        return checkStatusCode(jsonResponse);
    }

    public boolean completeTask(int ID){
        JSONObject jsonResponse = getRequest(COMPLETE_TASK_QUERY + ID);
        return checkStatusCode(jsonResponse);
    }

    /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~POST REQUESTS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ **/

    public boolean addTask(Task task){
        JSONObject jsonResponse = postRequest(ADD_TASK_UPDATE, task.toJSON().toString());
        return checkStatusCode(jsonResponse);
    }

    public boolean addDependency(int dependentID, int dependencyID){
        String requestBody = String.format("{\"dependent_id\":%d,\"dependency_id\":%d}", dependentID, dependencyID);
        JSONObject jsonResponse = postRequest(ADD_DEPENDENCY_UPDATE, requestBody);
        return checkStatusCode(jsonResponse);
    }

    public boolean removeDependency(int dependentID, int dependencyID){
        String requestBody = String.format("{\"dependent_id\":%d,\"dependency_id\":%d}", dependentID, dependencyID);
        JSONObject jsonResponse = postRequest(REMOVE_DEPENDENCY_UPDATE, requestBody);
        return checkStatusCode(jsonResponse);
    }

    public boolean updateClaim(int ID, String claimedByEmail){
        String requestBody = String.format("{\"id\":%d,\"claimed_by_email\":%s}", ID, claimedByEmail);
        JSONObject jsonResponse = postRequest(CLAIM_UPDATE, requestBody);
        return checkStatusCode(jsonResponse);
    }

    /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~REQUEST METHODS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ **/

    private JSONObject getRequest(String queryString){
        if (serverURL != null) {
            try {
                URL requestURL = new URL(serverURL + queryString);
                HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();

                // setup connection
                connection.setReadTimeout(TIMEOUT);
                connection.setRequestMethod("GET");
                connection.connect();

                return readInputStreamFromHTTPConnection(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private JSONObject postRequest(String updateString, String requestBody){
        if (serverURL != null) {
            try {
                URL updateURL = new URL(serverURL + updateString);
                HttpURLConnection connection = (HttpURLConnection) updateURL.openConnection();

                // setup connection
                connection.setReadTimeout(TIMEOUT);
                connection.setRequestMethod("POST");
                connection.connect();

                // send request body to the server
                OutputStream out = connection.getOutputStream();
                out.write(requestBody.getBytes());

                out.close();
                return readInputStreamFromHTTPConnection(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~HELPER METHODS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ **/

    private JSONObject readInputStreamFromHTTPConnection(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();

        // read response from server
        String line;
        while ((line = reader.readLine()) != null){
            responseBuilder.append(line);
        }
        String response = responseBuilder.toString();

        // parse response
        JSONObject json = new JSONObject(response);

        connection.disconnect();
        return json;
    }

    private List<Task> convertJSONResponseToTaskList(JSONObject jsonResponse){
        List<Task> results = new ArrayList<>();

        if (jsonResponse != null) {
            JSONArray taskJsons = jsonResponse.getJSONArray("results");
            for (Object taskJson : taskJsons){
                results.add(parseTaskJSON((JSONObject) taskJson));
            }
        }
        return results;
    }

    // takes the JSON from the server and makes a Task out of it
    private Task parseTaskJSON(JSONObject response){
        // the easy ones
        int ID = response.getInt("id");
        String taskName = response.getString("task_name");
        String description = response.getString("description");
        Date dateCreated = new Date(response.getLong("date_created"));
        Date dateDue = new Date(response.getLong("date_due"));
        boolean isComplete = response.getBoolean("is_complete");
        boolean isClaimed = response.getBoolean("is_claimed");

        Date dateComplete = null;
        if (isComplete){
            dateComplete = new Date(response.getLong("date_complete"));
        }

        String claimedByEmail = null;
        if (isClaimed) {
            claimedByEmail = response.getString("claimed_by_email");
        }

        // convert the JSONArrays to Lists
        List<String> requirements = new ArrayList<>();
        for (Object requirement : response.getJSONArray("requirements")){
            requirements.add(requirement.toString());
        }
        List<String> dependencies = new ArrayList<>();
        for (Object dependency : response.getJSONArray("dependencies")){
            dependencies.add(dependency.toString());
        }
        List<String> dependents = new ArrayList<>();
        for (Object dependent : response.getJSONArray("dependents")){
            dependents.add(dependent.toString());
        }

        return new Task(ID, taskName, description, requirements, dateCreated,
                dateDue, dateComplete, isComplete, isClaimed, claimedByEmail, dependencies, dependents);
    }

    private boolean checkStatusCode(JSONObject jsonResponse){
        if (jsonResponse != null){
            int statusCode = jsonResponse.getInt("status_code");
            return statusCode == 0;
        }
        return false;
    }
}
