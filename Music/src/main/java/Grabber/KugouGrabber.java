package Grabber;

import Utils.SongInfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static Utils.fileOperation.deleteFile;
import static Utils.fileOperation.downloadFile;
import static Utils.header.randomUserAgent;

public class KugouGrabber {
    public static void main(String[] args) throws Exception{
        KugouGrabber myTest = new KugouGrabber();
        boolean abled = myTest.songGet("63D276055EF27D1939AABEDFAFFD6B2D", "1738208");
    }

    public boolean songGet(String mid, String aid) throws Exception{
        SongInfo song =  songInfoGet(mid, aid);
        boolean abled = songFileGet(song);
        return abled;
    }

    public boolean songGet(String mid) throws Exception{
        SongInfo song =  songInfoGet(mid);
        boolean abled = songFileGet(song);
        return abled;
    }

    public String songUrlGet(String id) throws Exception {
        Connection.Response
                response = Jsoup.connect("http://www.kugou.com/yy/index.php?r=play/getdata&hash="+id+"&album_id="+1738208)
                .userAgent(randomUserAgent())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Cache-Control", "max-age=0")
                .header("Connection", "keep-alive")
                .header("Host", "www.kugou.com")
                .header("Referer", "https://music.163.com/")
                .header("Upgrade-Insecure-Requests", "1")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();

        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(response.body()).getAsJsonObject();
        JsonObject data = o.get("data").getAsJsonObject();
        return StringUtils.strip(data.get("play_url").toString(),"\"");
    }

    public boolean songFileGet(SongInfo song) throws Exception {
        String picPath = "../download/Pic/kugou/"+song.getId()+".jpg";
        downloadFile(song.getImages(),picPath, picPath);
        String path = "../download/Media/kugou/"+song.getId()+"~.mp3";
        String path2 = "../download/Media/kugou/"+song.getId()+".mp3";
        boolean res = downloadFile(song.getUrl(), path, path2);

        if(res) {
            Mp3File mp3file = new Mp3File(path);
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                id3v2Tag.setTitle(song.getTitle());
                id3v2Tag.setArtist(song.getSinger());
                id3v2Tag.setAlbum(song.getAlbum());
                mp3file.save(path2);
                deleteFile(path);
            } else {
                File oldfile=new File(path);
                File newfile=new File(path2);
                oldfile.renameTo(newfile);
            }
        }
        return res;
    }

    public SongInfo songInfoGet(String mid, String albumid) throws Exception {
        Connection.Response
                response = Jsoup.connect("http://www.kugou.com/yy/index.php?r=play/getdata&hash="+mid+"&album_id="+albumid)
                .userAgent(randomUserAgent())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Cache-Control", "max-age=0")
                .header("Connection", "keep-alive")
                .header("Host", "www.kugou.com")
                .header("Referer", "https://music.163.com/")
                .header("Upgrade-Insecure-Requests", "1")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();

        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(response.body()).getAsJsonObject();
        JsonObject data = o.get("data").getAsJsonObject();
        SongInfo song = new SongInfo();
        song.setId(mid);
        song.setTitle(StringUtils.strip(data.get("song_name").toString(),"\""));
        song.setSinger(StringUtils.strip(data.get("author_name").toString(),"\""));
        song.setAlbum(StringUtils.strip(data.get("album_name").toString(),"\""));
        song.setUrl(StringUtils.strip(data.get("play_url").toString(),"\""));
        song.setImages(StringUtils.strip(data.get("img").toString(),"\""));
        song.setSrc(2);
        return song;
    }

    public SongInfo songInfoGet(String mid) throws Exception {
        Connection.Response
                response = Jsoup.connect("http://www.kugou.com/yy/index.php?r=play/getdata&hash="+mid)
                .userAgent(randomUserAgent())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Cache-Control", "max-age=0")
                .header("Connection", "keep-alive")
                .header("Host", "www.kugou.com")
                .header("Referer", "https://music.163.com/")
                .header("Upgrade-Insecure-Requests", "1")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();

        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(response.body()).getAsJsonObject();
        String datastr = o.get("data").toString();
        JsonObject data = parser.parse(datastr).getAsJsonObject();
        SongInfo song = new SongInfo();
        song.setId(mid);
        song.setTitle(StringUtils.strip(data.get("song_name").toString(),"\""));
        song.setAlbum(StringUtils.strip(data.get("album_name").toString(),"\""));
        song.setUrl(StringUtils.strip(data.get("play_url").toString(),"\""));
        return song;
    }


}
