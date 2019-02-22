package Grabber;
import Utils.SongInfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static Utils.fileOperation.deleteFile;
import static Utils.fileOperation.downloadFile;
import static Utils.header.randomUserAgent;


public class NeteaseGrabber {
    public static void main(String[] args) throws Exception{
        NeteaseGrabber myTest = new NeteaseGrabber();
        boolean abled = myTest.songGet("16837502");
    }

    public boolean songGet(String id) throws Exception{
        SongInfo song =  songInfoGet(id);
        boolean abled = songFileGet(song);
        return abled;
    }

    public boolean songFileGet(SongInfo song) throws Exception {
        String path = "../download/Media/netease/"+song.getId()+"~.mp3";
        String path2 = "../download/Media/netease/"+song.getId()+".mp3";
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

    public String songUrlGet(String id) throws Exception {
        Connection.Response
                response = Jsoup.connect("http://music.163.com/song/media/outer/url?id="+id)
                .userAgent(randomUserAgent())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Connection", "keep-alive")
                .header("Host", "music.163.com")
                .header("Upgrade-Insecure-Requests", "1")
                .followRedirects(false)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();
        return response.header("Location");
    }

    public SongInfo songInfoGet(String id) throws Exception {
        Connection.Response
                response = Jsoup.connect("https://music.163.com/song?id="+id)
                .userAgent(randomUserAgent())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Connection", "keep-alive")
                .header("Host", "music.163.com")
                .header("Referer", "https://music.163.com/")
                .header("Upgrade-Insecure-Requests", "1")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();
        Document doc = Jsoup.parse(response.body());
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(doc.getElementsByTag("script").first().data()).getAsJsonObject();
        SongInfo song = new SongInfo();
        String[] description = o.get("description").toString().split("。");
        song.setId(id);
        song.setTitle(StringUtils.strip(o.get("title").toString(),"\""));
        song.setSinger(description[0].split("：")[1]);
        song.setAlbum(description[1].split("：")[1]);
        song.setSrc(0);
        Connection.Response
                response2 = Jsoup.connect("http://music.163.com/song/media/outer/url?id="+id)
                .userAgent(randomUserAgent())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Connection", "keep-alive")
                .header("Host", "music.163.com")
                .header("Upgrade-Insecure-Requests", "1")
                .followRedirects(false)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();
        song.setUrl(response2.header("Location"));
        return song;
    }


}
