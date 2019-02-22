package Utils;

public class SongInfo {
    boolean downloaded;
    String id;
    String title;
    String singer;
    String album;
    int src;
    String url;
    String localurl;
    String images;
    long length;
    boolean starred;

    public SongInfo() {
        starred = false;
    }

    public void setId(String _id) {
        id = _id;
    }

    public void setTitle(String _title) {
        title = _title;
    }

    public void setSinger(String _singer) {
        singer = _singer;
    }

    public void setAlbum(String _album) {
        album = _album;
    }

    public void setSrc(int _src) {
        src = _src;
    }

    public void setUrl(String _url) {
        url = _url;
    }

    public void setLocalurl(String _url) {
        localurl = _url;
    }

    public void setImages(String _img) {
        images = _img;
    }

    public void setDownloaded(boolean _d) {
        downloaded = _d;
    }

    public void setLength(long _l) {
        length = _l;
    }

    public void setStarred(boolean _s) {
        starred = _s;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSinger() {
        return singer;
    }

    public String getAlbum() {
        return album;
    }

    public int getSrc() {
        return src;
    }

    public String getUrl() {
        return url;
    }

    public String getLocalurl() {
        return localurl;
    }

    public String getImages() {
        return images;
    }

    public long getLength() {
        return length;
    }

    public boolean getDownloaded() {
        return downloaded;
    }

    public boolean getStarred() {
        return starred;
    }

    public void print() {
        System.out.println("id: "+id);
        System.out.println("title: "+title);
        System.out.println("singer: "+singer);
        System.out.println("album: "+album);
        System.out.println("src: "+src);
        System.out.println("url: "+url);
        System.out.println("length: "+length);
        System.out.println("starred: "+starred);
    }
}
