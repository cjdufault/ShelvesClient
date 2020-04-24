import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

    public String getServerURL(){
        return serverURL;
    }

    /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~GET METHODS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ **/

    // attempts to make a connection with the specified server; sets the serverURL if connection is successful
    public boolean testConnection(String url){
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

        JSONObject jsonResponse = getRequest(TEST_CONNECTION_QUERY);
        try {
            // check status code and that the server is a ShelvesServer
            if (jsonResponse != null) {
                long statusCode = (long) jsonResponse.get("status_code");
                String serviceName = (String) jsonResponse.get("service_name");
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
            JSONArray jsonArray = (JSONArray) jsonResponse.get("results");
            return parseTaskJSON((JSONObject) jsonArray.get(0));
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

    /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~POST METHODS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ **/

    public boolean addTask(){
        return false;
    }

    public boolean addDependency(){
        return false;
    }

    public boolean removeDependency(){
        return false;
    }

    public boolean updateClaim(){
        return false;
    }

    /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~HELPER METHODS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ **/

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
            } catch (ParseException | IOException e) {
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
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private JSONObject readInputStreamFromHTTPConnection(HttpURLConnection connection) throws IOException, ParseException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();

        // read response from server
        String line;
        while ((line = reader.readLine()) != null){
            responseBuilder.append(line);
        }
        String response = responseBuilder.toString();

        // parse response
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(response);

        connection.disconnect();
        return (JSONObject) obj;
    }

    private List<Task> convertJSONResponseToTaskList(JSONObject jsonResponse){
        List<Task> results = new ArrayList<>();

        if (jsonResponse != null) {
            JSONArray taskJsons = (JSONArray) jsonResponse.get("results");
            for (Object taskJson : taskJsons){
                results.add(parseTaskJSON((JSONObject) taskJson));
            }
        }
        return results;
    }

    // takes the JSON from the server and makes a Task out of it
    private Task parseTaskJSON(JSONObject response){
        // the easy ones
        int ID = (int) response.get("ID");
        String taskName = (String) response.get("taskName");
        String description = (String) response.get("description");
        Date dateCreated = new Date((Long) response.get("dateCreated"));
        Date dateDue = new Date((Long) response.get("dateDue"));

        // convert the JSONArrays to Lists
        List<String> requirements = new ArrayList<>();
        for (Object requirement : (org.json.simple.JSONArray) response.get("requirements")){
            requirements.add(requirement.toString());
        }
        List<String> dependencies = new ArrayList<>();
        for (Object dependency : (org.json.simple.JSONArray) response.get("dependencies")){
            dependencies.add(dependency.toString());
        }
        List<String> dependents = new ArrayList<>();
        for (Object dependent : (org.json.simple.JSONArray) response.get("dependents")){
            dependents.add(dependent.toString());
        }

        return new Task(ID, taskName, description, requirements, dateCreated,
                dateDue, false, dependencies, dependents);
    }

    private boolean checkStatusCode(JSONObject jsonResponse){
        if (jsonResponse != null){
            int statusCode = (int) jsonResponse.get("status_code");
            return statusCode == 0;
        }
        return false;
    }
}
