package helper;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;


public class DataProviderDemoTest {

    @DataProvider(name = "loginData")
    public Object[][] getData() {
        return new Object[][]{
                {"user1", "password1"},
                {"user2", "password2"},
                {"user3", "password"}
        };
    }

    @Test(dataProvider = "loginData")
    public void testLogin(String username, String password) { // this will run 3 times with different data sets (loop)
        System.out.println("Username: " + username + ", Password: " + password);
        // Add your login test logic here
    }


    @DataProvider(name = "hashMapData")
    public Object[][] hashMapData() {
        HashMap<String, String> map1 = new HashMap<>();
        map1.put("username", "user1");
        map1.put("password", "password1");

        HashMap<String, String> map2 = new HashMap<>();
        map2.put("username", "user2");
        map2.put("password", "password2");

        HashMap<String, String> map3 = new HashMap<>();
        map3.put("username", "user3");
        map3.put("password", "password3");

        return new Object[][]{
                {map1},
                {map2},
                {map3}
        };
    }

    @Test(dataProvider = "hashMapData")
    public void testLoginWithHashMap(HashMap<String, String> credentials) { //will run 3 times, each with different HashMap
        String username = credentials.get("username");
        String password = credentials.get("password");
        System.out.println("Username: " + username + ", Password: " + password);
    }
}
