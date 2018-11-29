package at.schrer.lycheeupload.cli;

import at.schrer.lycheeupload.upload.Album;
import at.schrer.lycheeupload.upload.LycheeUploaderHttp;
import org.apache.http.auth.AuthenticationException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LycheeCLIUpload {

    private static Logger LOGGER = Logger.getLogger(LycheeCLIUpload.class.getName());

    public static void main(String args[]){


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
    private static void runOnArgs(String args[]) throws IOException, AuthenticationException {

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
    private static void getStandardAlbums(String args[]) throws IOException, AuthenticationException {

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
    private static void uploadImage(String args[]) throws IOException, AuthenticationException {

        LycheeUploaderHttp lup = login(args);

        String filePath = args[1];
        String albumId = args[2];

        String imageId = lup.uploadImage(albumId, filePath);

        writeToStdOut("Image ID: " + imageId);
    }

    /**
     * Finds username, password and server address from the arguments given to the program.
     *
     * @param args all arguments given to the program.
     * @return array with username (index=0), password (index=1), server address (index=2).
     * @throws IllegalArgumentException if either username, password or server address is not specified.
     */
    private static String[] getLoginData(String args[]) throws IllegalArgumentException {

        String userPwInput[] = new String[3];
        boolean foundUser=false;
        boolean foundPassword=false;
        boolean foundServer=false;


        // Loop through arguments to check for user/password/server
        for (int i=1; i<args.length-1; i++){
            if ("--user".equals(args[i])) {
                userPwInput[0]=args[i+1];
                foundUser=true;
            }

            else if ("--password".equals(args[i])){
                userPwInput[1]=args[i+1];
                foundPassword=true;
            }

            else if ("--server".equals(args[i])){
                userPwInput[2]=args[i+1];
                foundServer=true;
            }
        }

        // Throw Exception if login data is not complete
        if (!foundUser || !foundPassword || !foundServer){
            throw new IllegalArgumentException("You need to specify username, password and server address with options \"--user\", \"--password\" and \"--server\"");
        }

        return userPwInput;
    }

    /**
     * Authenticate with the server to get a valid LycheeUploaderHttp-object.
     * @param args the arguments passed to the program.
     * @return the authenticated uploader.
     * @throws IOException if an error occurs during communication with the server.
     * @throws AuthenticationException if an error occurs during authentication.
     */
    private static LycheeUploaderHttp login(String args[]) throws IOException, AuthenticationException {

        String loginData[] = getLoginData(args);

        return new LycheeUploaderHttp(loginData[2],loginData[0],loginData[1]);

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
                " --user <username> --password <password> --server <serverAddress>\n" +
                "\n"+
                "-u <filepath> <albumId> to upload an image\n"+
                "-l to list albums available on the server";

        writeToStdOut(usage);
        System.exit(1);
    }
}
