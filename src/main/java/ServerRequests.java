import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerRequests {

    private static final String TEST_CONNECTION_QUERY = "/test_connection";
    private static final String GET_ALL_TASKS_QUERY = "/get_all_tasks/";
    private static final String GET_COMPLETE_TASKS_QUERY = "/get_complete_tasks/";
    private static final String GET_INCOMPLETE_TASKS_QUERY = "/get_incomplete_tasks";
    private static final String GET_TASK_QUERY = "/get_task/";
    private static final String SEARCH_QUERY = "/search/";
    private static final String GET_DEPENDENCIES_QUERY = "/get_dependencies/";
    private static final String GET_DEPENDENTS_QUERY = "/get_dependents/";

    private String serverURL;

    public Task getTask(){
        JSONObject jsonResponse = getRequest(GET_TASK_QUERY);

        if (jsonResponse != null) {
            return parseTaskJSON(jsonResponse);
        }
        return null;
    }

    public void setServerURL(String url){
        serverURL = url;
    }

    public boolean testConnection(String url){
        GetRequest response = Unirest.get(url + TEST_CONNECTION_QUERY);
        System.out.println(response.getBody());

        return response != null;
    }

    private JSONObject getRequest(String queryString){
        try {
            String requestURL = serverURL + queryString; // concatenate base url w/ query string
            String response = Unirest.get(requestURL) // request to server
                    .asString()
                    .getBody();

            // parse response
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(response);

            return (JSONObject) obj;
        }
        catch (ParseException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // takes the JSON from the server and makes a Task out of it
    private Task parseTaskJSON(JSONObject response){
        // the easy ones
        int ID = response.getInt("ID");
        String taskName = response.getString("taskName");
        String description = response.getString("description");
        Date dateCreated = new Date(response.getLong("dateCreated"));
        Date dateDue = new Date(response.getLong("dateDue"));

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
                dateDue, false, dependencies, dependents);
    }
}
