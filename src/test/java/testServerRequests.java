import org.junit.Before;
import org.junit.Test;

public class testServerRequests {

    private static final String serverURL = "localhost:5000";
    private ServerRequests requests;

    @Before
    public void start(){
        requests = new ServerRequests();
        requests.setServerURL(serverURL);
    }

    @Test
    public void testTestConnection(){
        boolean connectionSuccessful = requests.testConnection();
        assert connectionSuccessful;
    }

    @Test
    public void testGetAllTasks(){
        assert false;
    }
}
