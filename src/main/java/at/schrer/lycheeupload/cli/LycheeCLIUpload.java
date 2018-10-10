package at.schrer.lycheeupload.cli;

import at.schrer.lycheeupload.upload.LycheeUploaderHttp;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONArray;
import java.io.IOException;



/**
* WIP! Not usable, basically just a stub.
*/
public class LycheeCLIUpload {
    public static void main(String args[]){

        // Read these and eventual other paramters from the passed arguments.
        String serverAddress = "";
        String username = "";
        String password = "";
        String picturepath = "";


        try {
            LycheeUploaderHttp lup = new LycheeUploaderHttp(serverAddress,username,password);
            JSONArray res = lup.getStandardAlbums();

            // TODO: call correct operation specified in arguments

            res.toString();
        } catch (IOException e) {
            // TODO: notify the user of errors, maybe file not found or problems during reading/writing responses/requests
            e.printStackTrace();
        } catch (AuthenticationException e) {
            // TODO: notify user of error during authentication
            e.printStackTrace();
        }


    }
}
