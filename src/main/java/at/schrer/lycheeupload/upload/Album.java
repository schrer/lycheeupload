package at.schrer.lycheeupload.upload;

public class Album {

    private String albumId;
    private String name;
    private boolean passwordProtected;

    public Album(String albumId, String name, boolean passwordProtected) {
        this.albumId = albumId;
        this.name = name;
        this.passwordProtected = passwordProtected;
    }

    public String getAlbumId() {
        return albumId;
    }

    public String getName() {
        return name;
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    @Override
    public String toString() {
        return "Album{" +
                "albumId='" + albumId + '\'' +
                ", name='" + name + '\'' +
                ", passwordProtected=" + passwordProtected +
                '}';
    }
}
