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

public class QQGrabber {

    public static void main(String[] args) throws Exception{
        QQGrabber g = new QQGrabber();
        g.songGet("000CdJYT0YTBhf");
    }

    public boolean songGet(String id) throws Exception{
        SongInfo song = songInfoGet(id);
        return songFileGet(song);
    }

    public boolean songFileGet(SongInfo song) throws Exception{
        String path = "../download/Media/qqmusic/"+song.getId()+"~.mp3";
        String path2 = "../download/Media/qqmusic/"+song.getId()+".mp3";
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

    private static String getVkey(String mid,String media_id, String guid, String uin) throws Exception{
        Connection.Response
                response = Jsoup.connect("http://c.y.qq.com/base/fcgi-bin/fcg_music_express_mobile3.fcg?g_tk=0&loginUin=" + uin + "&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&cid=205361747&uin=" + uin + "&songmid="+mid+"&filename=M500"+media_id+".mp3&guid=" + guid)
                .userAgent(randomUserAgent())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Connection", "keep-alive")
                .header("Host", "c.y.qq.com")
                .header("Upgrade-Insecure-Requests", "1")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(response.body()).getAsJsonObject();
        String datastr = o.get("data").toString();
        JsonObject data = parser.parse(datastr).getAsJsonObject();
        String itemstr = StringUtils.strip(data.get("items").toString(),"[]");
        JsonObject item = parser.parse(itemstr).getAsJsonObject();
        return StringUtils.strip(item.get("vkey").toString(),"\"");
    }

    private static String randomGuid() {
        int intFlag = (int)(Math.random() * 1000000);
        return String.valueOf(intFlag);
    }

    public String songUrlGet(String mid) throws Exception {
        SongInfo s = songInfoGet(mid);
        return s.getUrl();
    }

    public SongInfo songInfoGet(String mid) throws Exception{
        Connection.Response
                response = Jsoup.connect("https://y.qq.com/n/yqq/song/"+mid+".html")
                .userAgent(randomUserAgent())
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("accept-encoding", "gzip, deflate, br")
                .header("accept-language", "zh-CN,zh;q=0.9")
                .header("referer", "https://y.qq.com/")
                .header("upgrade-insecure-requests", "1")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();
        Document doc = Jsoup.parse(response.body());
        String date = doc.getElementsByTag("script").eq(4).first().data();

        String jsonstr = date.split("g_SongData = ")[1];
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(jsonstr.split(";")[0]).getAsJsonObject();

        SongInfo song = new SongInfo();
        song.setId(mid);
        song.setTitle(StringUtils.strip(o.get("songname").toString(),"\""));
        song.setSinger(StringUtils.strip(o.get("singername").toString(),"\""));
        song.setAlbum(StringUtils.strip(o.get("albumname").toString(),"\""));
        song.setSrc(1);
        String media_id = StringUtils.strip(o.get("strMediaMid").toString(), "\"");
        String guid = randomGuid();
        String vkey = getVkey(mid,media_id, guid, "0");
        song.setUrl("http://dl.stream.qqmusic.qq.com/M500"+media_id+".mp3?guid="+guid+"&vkey="+vkey+"&uin=0&fromtag=66");
        return song;

    }


}
