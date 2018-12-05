package at.schrer.lycheeupload.cli;

import at.schrer.lycheeupload.upload.Album;
import at.schrer.lycheeupload.upload.LycheeUploaderHttp;
import at.schrer.lycheeupload.util.LoginConfig;
import org.apache.http.auth.AuthenticationException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LycheeCLIUpload {

    private static final Logger LOGGER = Logger.getLogger(LycheeCLIUpload.class.getName());

    private static final String DEFAULT_CONFIG_PATH = System.getProperty("user.home")+"/.config/lycheeupload.conf";

    public static void main(String[] args){


        try {

            runOnArgs(args);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Error in server communication.",e);
        } catch (AuthenticationException e) {
            LOGGER.log(Level.SEVERE, "Error while logging in. Check your login data", e);
        }

    }


    /**
     * Run the operations specified in arguments.
     *
     * @param args the arguments passed to the program.
     * @throws IOException if an error occurs during communication with the server.
     * @throws AuthenticationException if an error occurs during authentication.
     */
    private static void runOnArgs(String[] args) throws IOException, AuthenticationException {

        if(args.length < 2){
            writeUsage();
        }

        //Check for supported operations
        switch (args[0]){
            case "-u":
                uploadImage(args);
                return;
            case "-l":
                getStandardAlbums(args);
                return;
            case "-c":
                writeToStdErrAndExit("Album creation not yet supported on commandline.\n");
                return;
            default:
                writeToStdErrAndExit("Unknown operation: "+args[1] + "\n");
        }
    }


    /**
     * Get and print a list of standard albums on the Lychee server.
     * @param args the arguments passed to the program.
     * @throws IOException if an error occurs during communication with the server.
     */
    private static void getStandardAlbums(String[] args) throws IOException, AuthenticationException {

        LycheeUploaderHttp lup = login(args);

        List<Album> albums = lup.getStandardAlbums();

        StringBuilder albumOutput = new StringBuilder();

        for (Album album: albums) {
            albumOutput.append(album.toString());
            albumOutput.append("\n");
        }

        writeToStdOut(albumOutput.toString());

    }

    /**
     * Upload an image to the server.
     * @param args the arguments passed to the program.
     * @throws IOException if an error occurs during communication with the server.
     * @throws AuthenticationException if an error occurs during authentication.
     */
    private static void uploadImage(String[] args) throws IOException, AuthenticationException {

        LycheeUploaderHttp lup = login(args);

        String filePath = args[1];
        String albumId = args[2];

        String imageId = lup.uploadImage(albumId, filePath);

        writeToStdOut("Image ID: " + imageId);
    }

    /**
     * Finds username, password and server address from the arguments given to the program.
     * Values given be option ("--user","--password",...) will be selected over values from config files.
     *
     * @param args all arguments given to the program.
     * @return complete login information.
     * @throws IOException if the given config file can't be read or if either username, password or server address is not specified.
     */
    private static LoginConfig getLoginData(String[] args) throws IOException {

        LoginConfig loginConfig = createConfigFromFile(args);


        // Loop through arguments to check for user/password/server
        for (int i=1; i<args.length-1; i++){

            if ("--user".equals(args[i])) {
                loginConfig.setUsername(args[i+1]);
            }

            else if ("--password".equals(args[i])){
                loginConfig.setPassword(args[i+1]);
            }

            else if ("--server".equals(args[i])){
                loginConfig.setServerAddress(args[i+1]);
            }

        }

        // Throw Exception if login data is not complete
        if (!loginConfig.isComplete()){
            throw new IOException("You need to specify username, password and server address with options or a config file.");
        }

        return loginConfig;
    }

    /**
     * Checks if the arguments contain a path to a config file and will load it. If no path is given, the default path is tried, if there is no file, an empty LoginConfig will be returned.
     * The default path is "USERHOME/.config/lycheeupload.conf".
     * @param args the arguments passed to the program.
     * @return the available login information.
     */
    private static LoginConfig createConfigFromFile(String[] args) throws IOException {
        for (int i=1; i<args.length-1; i++){
            if ("--config".equals(args[i])) {
                return new LoginConfig(args[i+1]);
            }
        }

        try {
            return new LoginConfig(DEFAULT_CONFIG_PATH);

        } catch (IOException e){
            LOGGER.log(Level.INFO, "Unable to open default config path. Will use empty config.",e);
        }

        // Will only be used if neither a default config file, nor a file given by arguments exist.
        return new LoginConfig();
    }

    /**
     * Authenticate with the server to get a valid LycheeUploaderHttp-object.
     * @param args the arguments passed to the program.
     * @return the authenticated uploader.
     * @throws IOException if an error occurs during communication with the server.
     * @throws AuthenticationException if an error occurs during authentication.
     */
    private static LycheeUploaderHttp login(String[] args) throws IOException, AuthenticationException {

        LoginConfig loginData= getLoginData(args);

        return new LycheeUploaderHttp(loginData.getServerAddress(),loginData.getUsername(),loginData.getPassword());

    }

    /**
     * Write message to stdout.
     * @param output the message.
     */
    private static void writeToStdOut(String output){
        System.out.print(output);
    }

    /**
     * Write to stderr and exit the program with exit status 1.
     * @param output error message to write.
     */
    private static void writeToStdErrAndExit(String output){
        System.err.println(output);
        System.exit(1);
    }

    /**
     * Write usage instructions and exit with exit code 1.
     */
    private static void writeUsage(){
        String usage = "Usage: java -jar lycheeUpload.jar [-u <filepath> <albumId> | -l ]\n" +
                " [--user <username>] [--password <password>] [--server <serverAddress>] [--config <path>]\n" +
                "\n"+
                "Options:\n"+
                "   -u <filepath> <albumId> to upload an image\n"+
                "   -l to list albums available on the server";

        writeToStdOut(usage);
        System.exit(1);
    }
}
