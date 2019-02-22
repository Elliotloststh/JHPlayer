package search;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static Utils.header.randomUserAgent;

public class NeteaseSearch {
    private static Invocable inv;
    public static final String encText = "encText";
    public static final String encSecKey = "encSecKey";

    public NeteaseSearch() {
        try {
            Path path = Paths.get("src/main/resources/core.js");
            byte[] bytes = Files.readAllBytes(path);
            String js = new String(bytes);
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("JavaScript");
            engine.eval(js);
            inv = (Invocable) engine;
            System.out.println("Init completed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ScriptObjectMirror get_params(String paras) throws Exception {
        ScriptObjectMirror so = (ScriptObjectMirror) inv.invokeFunction("myFunc", paras);
        return so;
    }

    public HashMap<String, String> getDatas(String paras) {
        try {
            ScriptObjectMirror so = (ScriptObjectMirror) inv.invokeFunction("myFunc", paras);
            HashMap<String, String> datas = new HashMap<>();
            datas.put("params", so.get(encText).toString());
            datas.put("encSecKey", so.get(encSecKey).toString());
            return datas;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void main(String[] args)  throws Exception{
        NeteaseSearch ns = new NeteaseSearch();
        String res = ns.search("Somethingjustlikethis", "1");
//        String res = ns.search("凉风 有信", "1");
        System.out.println(res);
//        ns.search("有点甜", "1");
//        ns.search("吻别", "1");
    }

    synchronized public String search(String s,String type)  throws Exception{
        HashMap<String, String> p = new HashMap<>();

        UrlParamPair upp = UrlParamPair.SearchMusicList(s,type);
        String req_str = upp.getParas().toString();
//        System.out.println("req_str:"+req_str);
        Connection.Response
                response = Jsoup.connect("http://music.163.com/weapi/cloudsearch/get/web?csrf_token="+req_str)
                .userAgent(randomUserAgent())
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Cookie", "os=uwp; osver=10.0.10586.318; appver=1.2.1;")
                .header("Connection", "keep-alive")
                .header("Host", "music.163.com")
                .header("Accept-Language", "zh-CN,en-US;q=0.7,en;q=0.3")
                .header("DNT", "1")
                .header("Pragma", "no-cache")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("referer", "http://music.163.com")
                .data(getDatas(req_str))
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();
        return response.body();
    }
}
