import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
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

    private String serverURL;

    public boolean testConnection(String url){
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
            if (jsonResponse != null) {
                long statusCode = (long) jsonResponse.get("status_code");
                return statusCode == 0;
            }
            return false;
        }
        catch (NullPointerException e){
            return false;
        }
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
        List<Task> results = new ArrayList<>();

        if (jsonResponse != null) {
            JSONArray taskJsons = (JSONArray) jsonResponse.get("results");
            for (Object taskJson : taskJsons){
                results.add(parseTaskJSON((JSONObject) taskJson));
            }
        }
        return results;
    }

    private JSONObject getRequest(String queryString){
        try {
            URL requestURL = new URL(serverURL + queryString);
            HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();

            // setup connection
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");
            connection.connect();

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
        catch (ParseException | IOException e){
            e.printStackTrace();
        }
        return null;
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
}
