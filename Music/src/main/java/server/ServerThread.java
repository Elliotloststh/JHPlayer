package server;

import Grabber.KugouGrabber;
import Grabber.QQGrabber;
import Utils.SongInfo;
import com.google.gson.*;
import org.apache.commons.lang.StringUtils;
import search.KugouSearch;
import search.QQSearch;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedList;

import static Utils.dbConnecter.*;

public class ServerThread extends Thread {
    public Socket socket;
    public InputStream in = null;
    public OutputStream out = null;
    boolean started;

    public ServerThread(Socket socket) {
        this.socket = socket;
        started = false;
    }

    public void run() {
        try {
            // 获取输入输出流
            in = socket.getInputStream();
            out = socket.getOutputStream();
            started = true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                in.close();
                out.close();
                socket.close();
                MyServer.list.remove(this);
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
        while(started) {
            try {
                Message clientMsg = readMsg(in);
                switch (clientMsg.type()) {
                    case 0: {
                        handleLogin(clientMsg.body().get("account").getAsString(), clientMsg.body().get("pwd").getAsString());break;
                    }
                    case 1: {
                        handleRegister(clientMsg.body().get("account").getAsString(), clientMsg.body().get("pwd").getAsString());break;
                    }
                    case 2: {
                        handleLoveSongs(clientMsg.body().get("account").getAsString());break;
                    }
                    case 3: {
                        handleQueryStarred(clientMsg.body().get("account").getAsString(), clientMsg.body().get("songId").getAsString(), clientMsg.body().get("songSrc").getAsInt());break;
                    }
                    case 4: {
                        handleUnStar(clientMsg.body().get("account").getAsString(), clientMsg.body().get("songId").getAsString(), clientMsg.body().get("songSrc").getAsInt());break;
                    }
                    case 5: {
                        String id = clientMsg.body().get("songId").getAsString();
                        int src = clientMsg.body().get("songSrc").getAsInt();
                        String url = clientMsg.body().get("songUrl").getAsString();
                        String singer = clientMsg.body().get("singer").getAsString();
                        String album = clientMsg.body().get("album").getAsString();
                        int length = clientMsg.body().get("length").getAsInt();
                        String title = clientMsg.body().get("title").getAsString();
                        handleStar(clientMsg.body().get("account").getAsString(), id, src, url, singer, album, length, title);
                        break;
                    }
                    case 6: {
                        handleSearch(clientMsg.body().get("word").getAsString());break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    in.close();
                    out.close();
                    socket.close();
                    MyServer.list.remove(this);
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    public Message readMsg(InputStream ins) {
        try {
            InputStreamReader inputStreamReader=new InputStreamReader(ins);
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            Message msg = new Message(line);
            return msg;

        }catch (Exception e) {
            started = false;
        }
        return null;
    }

    public void handleLogin(String id, String pwd) throws Exception{
        String sql = "select * from user where account='"+id+"' and password='"+md5(pwd)+"'";
        ResultSet res = sqlQuery(sql);
        if(res.next()) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            JsonObject o = new JsonObject();
            JsonObject d = new JsonObject();
            o.addProperty("type", 0);
            d.addProperty("state", 1);
            o.add("data", d);
            writer.write(o.toString()+"\n");
            writer.flush();
        } else {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            JsonObject o = new JsonObject();
            JsonObject d = new JsonObject();
            o.addProperty("type", 0);
            d.addProperty("state", 0);
            o.add("data", d);
            writer.write(o.toString()+"\n");
            writer.flush();
        }

    }

    public void handleRegister(String id, String pwd) throws Exception {
        String sql1 = "select * from user where account='"+id+"'";
        ResultSet res = sqlQuery(sql1);
        if(res.next()) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            JsonObject o = new JsonObject();
            JsonObject d = new JsonObject();
            o.addProperty("type", 1);
            d.addProperty("state", 0);
            d.addProperty("msg", "账户已存在");
            o.add("data", d);
            writer.write(o.toString()+"\n");
            writer.flush();
        } else {
            String sql2 = "insert into user values('"+id+"','"+md5(pwd)+"')";
            int res2 = sqlExecute(sql2);
            if(res2 == 1) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                JsonObject o = new JsonObject();
                JsonObject d = new JsonObject();
                o.addProperty("type", 1);
                d.addProperty("state", 1);
                d.addProperty("msg", "成功");
                o.add("data", d);
                writer.write(o.toString()+"\n");
                writer.flush();
            } else if(res2 == 0) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                JsonObject o = new JsonObject();
                JsonObject d = new JsonObject();
                o.addProperty("type", 1);
                d.addProperty("state", 0);
                d.addProperty("msg", "注册失败，请重试");
                o.add("data", d);
                writer.write(o.toString()+"\n");
                writer.flush();
            }
        }
    }

    public void handleLoveSongs(String id) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        JsonObject o = new JsonObject();
        JsonObject d = new JsonObject();
        JsonArray array = new JsonArray();
        o.addProperty("type", 2);
        String sql = "select * from favorites where account='"+id+"'";
        ResultSet res = sqlQuery(sql);
        LinkedList<Integer> ids = new LinkedList<>();
        while(res.next()) {
            ids.add(res.getInt("id"));
        }
        Iterator it = ids.iterator();
        int counter=0;
        while(it.hasNext()) {
            String sql2 = "select * from song where id='"+it.next()+"'";
            ResultSet res2 = sqlQuery(sql2);
            if(res2.next()) {
                JsonObject s = new JsonObject();
                s.addProperty("mid", res2.getString("mid"));
                s.addProperty("src", res2.getInt("src"));
                s.addProperty("url", res2.getString("url"));
                s.addProperty("title", res2.getString("title"));
                s.addProperty("singer", res2.getString("singer"));
                s.addProperty("album", res2.getString("album"));
                s.addProperty("length", res2.getInt("length"));
                array.add(s);
            }
            counter++;
        }
        d.add("song", array);
//        d.addProperty("counter", counter);
        o.add("data", d);
        writer.write(o.toString()+"\n");
        writer.flush();
    }

    public void handleQueryStarred(String account, String id, int src) throws Exception{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        JsonObject o = new JsonObject();
        o.addProperty("type", 3);
        JsonObject d = new JsonObject();
        String sql1 = "select id from song where mid='"+id+"' and src="+src;
        ResultSet res = sqlQuery(sql1);
        int ID;
        if(res.next()) {
            ID = res.getInt("id");
        } else {
            d.addProperty("star", false);
            o.add("data", d);
            writer.write(o.toString()+"\n");
            writer.flush();
            return;
        }
        String sql2 = "select * from favorites where account='"+account+"' and id="+ID;
        ResultSet res2 = sqlQuery(sql2);
        if(res2.next()) {
            d.addProperty("star", true);
            o.add("data", d);
            writer.write(o.toString()+"\n");
            writer.flush();
        } else {
            d.addProperty("star", false);
            o.add("data", d);
            writer.write(o.toString()+"\n");
            writer.flush();
        }
    }

    public void handleUnStar(String account, String id, int src) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        JsonObject o = new JsonObject();
        o.addProperty("type", 4);
        JsonObject d = new JsonObject();
        String sql1 = "select id from song where mid='"+id+"' and src="+src;
        ResultSet res = sqlQuery(sql1);
        int ID = -1;
        if(res.next()) {
            ID = res.getInt("id");
        }
        String sql2 = "delete from favorites where account='"+account+"' and id="+ID;
        int res2 = sqlExecute(sql2);
        if(res2==1) {
            d.addProperty("success", true);
            o.add("data", d);
            writer.write(o.toString()+"\n");
            writer.flush();
        } else {
            d.addProperty("success", false);
            o.add("data", d);
            writer.write(o.toString()+"\n");
            writer.flush();
        }
    }

    public void handleStar(String account, String id, int src, String url, String singer, String album, int length, String title) throws Exception{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        JsonObject o = new JsonObject();
        o.addProperty("type", 5);
        JsonObject d = new JsonObject();
        String sql1 = "select * from song where mid='"+id+"' and src="+src;
        ResultSet res = sqlQuery(sql1);
        int ID = -1;
        if(res.next()) {
            ID = res.getInt("id");
        } else {
            String sql2 = "insert into song values(null, '"+id+"',"+src+",'"+url+"',null,'"+singer+"','"+album+"',"+length+",'"+title+"')";
            System.out.println(sql2);
            sqlExecute(sql2);
            res = sqlQuery(sql1);
            if(res.next()) {
                ID = res.getInt("id");
            }
        }
        String sql3 = "insert into favorites values('"+account+"',"+ID+",null)";
        int res3 = sqlExecute(sql3);
        if(res3==1) {
            d.addProperty("success", true);
            o.add("data", d);
            writer.write(o.toString()+"\n");
            writer.flush();
        } else {
            d.addProperty("success", false);
            o.add("data", d);
            writer.write(o.toString()+"\n");
            writer.flush();
        }
    }

    public void handleSearch(String word) throws Exception{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        JsonObject o = new JsonObject();
        JsonObject d = new JsonObject();
        JsonArray array = new JsonArray();
        o.addProperty("type", 6);

        //netease
        String nsJsonStr = MyServer.ns.search(word.replaceAll(" ", ""), "1");
        JsonParser parser = new JsonParser();
        JsonObject nsJson = parser.parse(nsJsonStr).getAsJsonObject();
        JsonArray nsArray = nsJson.getAsJsonObject("result").getAsJsonArray("songs");
        for(JsonElement e : nsArray) {
            JsonObject o2 = e.getAsJsonObject();
            JsonObject s = new JsonObject();
            s.addProperty("mid", String.valueOf(o2.get("id").getAsInt()));
            s.addProperty("src", 0);
            s.addProperty("url", "haha");
            s.addProperty("title", o2.get("name").getAsString());
            JsonArray singers = o2.getAsJsonArray("ar");
            String singer = "";
            for(JsonElement ee : singers) {
                JsonObject ss = ee.getAsJsonObject();
                singer = singer+StringUtils.strip(ss.get("name").toString(), "\"") +"、";
            }
            singer = singer.substring(0, singer.length()-1);
            s.addProperty("singer", singer);
            s.addProperty("album", o2.getAsJsonObject("al").get("name").getAsString());
            s.addProperty("length",o2.get("dt").getAsInt()/1000);
            s.addProperty("image", o2.getAsJsonObject("al").get("picUrl").getAsString());
            array.add(s);
        }

        try {
            //qqmusic b事多
            QQSearch qs = new QQSearch();
            QQGrabber qg = new QQGrabber();
            String qqJsonStr = qs.search(word);
            JsonObject qqJson = parser.parse(qqJsonStr).getAsJsonObject();
            JsonArray qqArray = qqJson.getAsJsonObject("data").getAsJsonObject("song").getAsJsonArray("list");
            for(JsonElement e : qqArray) {
                JsonObject o2 = e.getAsJsonObject();
                JsonObject s = new JsonObject();
                String mid = o2.get("mid").getAsString();
//            System.out.println(mid);
                SongInfo song = qg.songInfoGet(mid);
                s.addProperty("mid", mid);
                s.addProperty("src", 1);
                s.addProperty("url", song.getUrl());
                s.addProperty("title", o2.get("name").getAsString());
                s.addProperty("singer", song.getSinger());
                s.addProperty("album", song.getAlbum());
                s.addProperty("length",String.valueOf(o2.get("interval").getAsInt()));
                s.addProperty("image", "haha");
                array.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //kugou
        KugouSearch ks = new KugouSearch();
        KugouGrabber kg = new KugouGrabber();
        String kgJsonStr = ks.search(word);
        JsonObject kgJson = parser.parse(kgJsonStr).getAsJsonObject();
        JsonArray kgArray = kgJson.getAsJsonObject("data").getAsJsonArray("info");
        for(JsonElement e : kgArray) {
            JsonObject o2 = e.getAsJsonObject();
            JsonObject s = new JsonObject();
            String mid = o2.get("hash").getAsString();
            s.addProperty("mid", mid);
            s.addProperty("src", 2);
            SongInfo song = kg.songInfoGet(mid,o2.get("album_id").getAsString());
            s.addProperty("url", song.getUrl());
            s.addProperty("title", song.getTitle());
//            s.addProperty("singer", song.getSinger());
            s.addProperty("album", song.getAlbum());
//            s.addProperty("title", o2.get("songname_original").getAsString());
            s.addProperty("singer", o2.get("singername").getAsString());
//            s.addProperty("album", o2.get("album_name").getAsString());
            s.addProperty("length",o2.get("duration").getAsInt());
            s.addProperty("image", song.getImages());
            array.add(s);
        }


        d.add("song", array);
        o.add("data", d);
        writer.write(o.toString()+"\n");
        writer.flush();
    }

    public String md5(String plainText) {
        //定义一个字节数组
        byte[] secretBytes = null;
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            //对字符串进行加密
            md.update(plainText.getBytes());
            //获得加密后的数据
            secretBytes = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有md5这个算法！");
        }
        //将加密后的数据转换为16进制数字
        String md5code = new BigInteger(1, secretBytes).toString(16);// 16进制数字
        // 如果生成数字未满32位，需要前面补0
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }
}
