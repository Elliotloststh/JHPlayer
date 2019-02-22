package search;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import static Utils.header.randomUserAgent;

public class QQSearch {

    public static void main(String[] args)  throws Exception{
        QQSearch qs = new QQSearch();
        String res = qs.search("something just like this");
//        String res = ns.search("凉风 有信", "1");
        System.out.println(res);
//        ns.search("有点甜", "1");
//        ns.search("吻别", "1");
    }

    public String search(String key) throws Exception{
        Connection.Response
                response = Jsoup.connect("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?ct=24&qqmusic_ver=1298&new_json=1&remoteplace=txt.yqq.song&searchid=58903213088584211&t=0&aggr=1&cr=1&catZhida=1&lossless=0&flag_qc=0&p=1&n=5&w="+key+"&g_tk=62918087&loginUin=1125126388&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq.json&needNewCode=0")
                .userAgent(randomUserAgent())
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Cookie", "os=uwp; osver=10.0.10586.318; appver=1.2.1;")
                .header("Connection", "keep-alive")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("origin","https://y.qq.com")
                .header("referer", "https://y.qq.com/portal/search.html")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();
        return response.body();
    }
}
