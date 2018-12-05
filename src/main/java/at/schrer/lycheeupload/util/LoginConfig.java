package at.schrer.lycheeupload.util;

import java.io.*;
import java.util.Properties;


/**
 * A class for storing the configuration for login information to the Lychee server.
 * Can be used with or without a config file, none of the variables are enforced to not be null.
 */
public class LoginConfig {

    private String serverAddress=null;
    private String username=null;
    private String password=null;

    public LoginConfig(String configPath) throws IOException {
        File configFile = new File(configPath);
        InputStream stream = new FileInputStream(configFile);

        Properties properties = new Properties();
        properties.load(stream);

        this.serverAddress  = properties.getProperty("server");
        this.username       = properties.getProperty("username");
        this.password       = properties.getProperty("password");

        stream.close();
    }

    public LoginConfig(){}

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Checks if all three fields are set.
     * @return true if none of the fields is null, false otherwise.
     */
    public boolean isComplete(){
        return serverAddress!=null && username!=null && password!=null;
    }
}
