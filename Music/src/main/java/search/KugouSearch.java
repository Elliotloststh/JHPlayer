package search;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import static Utils.header.randomUserAgent;

public class KugouSearch {

    public static void main(String[] args)  throws Exception{
        KugouSearch ks = new KugouSearch();
        String res = ks.search("something just like this");
//        String res = ns.search("凉风 有信", "1");
        System.out.println(res);
//        ns.search("有点甜", "1");
//        ns.search("吻别", "1");
    }

    public String search(String key) throws Exception{
        Connection.Response
                response = Jsoup.connect("http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword="+key+"&page=1&pagesize=5&showtype=1")
                .userAgent(randomUserAgent())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Cache-Control", "max-age=0")
                .header("Cookie", "kg_mid=deb5d2d5f49bbb5eb5817d248e2bf06a; buyUserFirst={\"undefined\":1}; Hm_lvt_aedee6983d4cfc62f509129360d6bb3d=1546152553,1547024104,1548060934")
                .header("Connection", "keep-alive")
                .header("Host", "mobilecdn.kugou.com")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Pragma", "no-cache")
                .header("Accept-Encoding", "gzip, deflate")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();
        return response.body();
    }

}
