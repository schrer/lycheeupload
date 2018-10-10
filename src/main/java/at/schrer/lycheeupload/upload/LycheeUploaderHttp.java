package at.schrer.lycheeupload.upload;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

// TODO: choose consistent way of returning results. Preferably refrain from passing out JSON-objects since these are not native and just another dependency for bigger projects.
// TODO: write missing documentation
public class LycheeUploaderHttp {

    private CookieStore cookieStore;
    private String serverAddress;

    /**
     * Creates a new Object of type LycheeUploaderHttp. Since the login process is done during instantiation (because most other functions can't be used without being logged in on the Lychee server),
     * this constructor may return Exceptions that occur while authenticating.
     * @param serverAddress the URL of the Lychee-installation.
     * @param username the username for this Lychee-instance.
     * @param password the password to the username.
     * @throws IOException if an error occurs on the connection level while querying the server for login.
     * @throws AuthenticationException if username and password are wrong or some other error like an internal server error occur (status other than 200 gets returned).
     */
    public LycheeUploaderHttp(String serverAddress, String username, String password) throws IOException, AuthenticationException {
        // Remove trailing slashes from the server address
        this.serverAddress = serverAddress.replaceAll("/+$","");

        this.cookieStore = new BasicCookieStore();

        this.authenticate(username,password);
    }


    public JSONArray getStandardAlbums() throws IOException {
        return getAllAlbums().getJSONArray("albums");
    }

    public JSONObject getSmartAlbums() throws IOException {
        return getAllAlbums().getJSONObject("smartalbums");
    }

    public JSONObject getAllAlbums() throws IOException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("function", "Albums::get"));
        HttpResponse res = runRequest(params);

        this.checkStatusCode(res);

        return getResponseBodyAsJSON(res);
    }

    // TODO: change return type to something that is not protocol specific.
    public HttpResponse getAlbumById(String id) throws IOException {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("function", "Album::get"));
        params.add(new BasicNameValuePair("albumID", id));
        params.add(new BasicNameValuePair("password", ""));

        HttpResponse res = runRequest(params);

        return res;

    }

    /**
     * Creates a new album on the server with the given name. The password has to be set with {@link #setAlbumAccess(String, String, boolean, boolean, boolean)} separately.
     * @param title the title of the new album
     * @return the ID of the new album.
     * @throws IOException if an error occurs while processing the request.
     */
    public String createAlbum(String title) throws IOException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("function", "Album::add"));
        params.add(new BasicNameValuePair("title", title));

        HttpResponse response = runRequest(params);

        checkStatusCode(response);

        String albumId = getResponseBodyAsString(response);

        return albumId;

    }

    /**
     * Sets all access options for a given album.
     * Because Lychee does not give a clear response if the album ID is not known on the server you should be really cautious to use an ID that actually exists
     * @param albumID the album that shall be changed.
     * @param password the password of the album, can be null or empty if no password shall be set.
     * @param setDownloadable if set to true people can download the whole album or single pictures.
     * @param setVisible if false, the album won't be displayed on the Lychee site, only people with a direct link can access it.
     * @param setPublic needs to be true for users to see, otherwise only a logged in admin will see the album.
     * @throws IOException if an error occurs while processing the request.
     */
    public void setAlbumAccess(String albumID, String password, boolean setDownloadable, boolean setVisible, boolean setPublic) throws IOException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("function", "Album::setPublic"));
        params.add(new BasicNameValuePair("albumID", albumID));
        params.add(new BasicNameValuePair("password", (password==null)?"":password));
        params.add(new BasicNameValuePair("downloadable", (setDownloadable)?"1":"0"));
        params.add(new BasicNameValuePair("public", (setPublic)?"1":"0"));
        params.add(new BasicNameValuePair("visible", (setVisible)?"1":"0"));

        HttpResponse response = runRequest(params);
        checkStatusCode(response);
    }

    public void addPhotoToAlbum(String filepath, String albumId) throws IOException {

        File photo = new File(filepath);

        if(!photo.isFile()){
            throw new IOException("Can't find file in path "+filepath);
        }


        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("function", "Photo::add");
        builder.addTextBody("albumID", albumId);
        builder.addBinaryBody("0", new File(filepath), ContentType.APPLICATION_OCTET_STREAM, filepath);
        HttpEntity multipart = builder.build();

        HttpResponse res = runRequest(multipart);

        checkStatusCode(res);
    }

    /**
     * Logs in the LycheeUploaderHttp-object with the given user/password combination.
     * @param username a valid username for the Lychee-installation.
     * @param password the password for the given username.
     * @throws IOException if an error occurs while processing the request.
     * @throws AuthenticationException if the user credentials are wrong.
     */
    private void authenticate(String username, String password) throws IOException, AuthenticationException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("function", "Session::login"));
        params.add(new BasicNameValuePair("user", username));
        params.add(new BasicNameValuePair("password", password));
        HttpResponse res = runRequest(params);


        String body = getResponseBodyAsString(res);
        int statusCode = res.getStatusLine().getStatusCode();

        checkStatusCode(res);

        if(!"true".equals(body)){
            throw new AuthenticationException("Unable to login. Statuscode: "+statusCode+"; Response body: "+body);
        }
    }

    /**
     * Runs a request to the Lychee-server
     * @param entity the HTTPEntity containing the request parameters.
     * @return the HttpResponse of the Lychee server.
     * @throws IOException if an error occurs while sending/receiving to/from the server.
     */
    private HttpResponse runRequest(HttpEntity entity) throws IOException {
        String url = serverAddress+"/php/index.php";

        Executor executor = Executor.newInstance();
        Request req = Request.Post(url).body(entity);
        return executor.use(this.cookieStore).execute(req).returnResponse();
    }

    /**
     * Runs a request to the Lychee-server.
     * @param params the parameters for the request.
     * @return the HttpResponse of the Lychee server.
     * @throws IOException if an error occurs while sending/receiving to/from the server.
     */
    private HttpResponse runRequest(List<NameValuePair> params) throws IOException {
        UrlEncodedFormEntity urlEncParams = new UrlEncodedFormEntity(params, Charset.forName("UTF-8"));
        return runRequest(urlEncParams);
    }

    private String getResponseBodyAsString(HttpResponse response) throws IOException {
        return IOUtils.toString(response.getEntity().getContent(), "UTF-8").trim();
    }

    private JSONObject getResponseBodyAsJSON (HttpResponse response) throws IOException {
        return new JSONObject(getResponseBodyAsString(response));
    }

    /**
     * Checks if the status code of a response is in the range 200-299 (indicating successful transmission and execution of a response), throws an exception otherwise.
     * @param response the response that shall be checked.
     * @throws HttpResponseException if the status code is smaller than 200 or bigger than 299.
     */
    private void checkStatusCode(HttpResponse response) throws HttpResponseException {
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode<200 || statusCode > 299){
            throw new HttpResponseException(statusCode, "Statuscode is not 2xx.");
        }
    }

}
