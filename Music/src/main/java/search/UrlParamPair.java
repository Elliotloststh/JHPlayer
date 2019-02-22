package search;

import com.google.gson.JsonObject;

public class UrlParamPair {
    public String url;
    public JsonObject paras;

    public UrlParamPair() {
        this.paras = new JsonObject();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JsonObject getParas() {
        return paras;
    }

    public void setParas(JsonObject paras) {
        this.paras = paras;
    }

    public UrlParamPair addPara(String key, Object value) {
        this.paras.addProperty(key, value.toString());
        return this;
    }

    public UrlParamPair(String url, JsonObject paras) {
        this.url = url;
        this.paras = paras;
    }

    @Override
    public String toString() {
        return "UrlParamPair{" +
                "url='" + url + '\'' +
                ", paras=" + paras +
                '}';
    }

    public static UrlParamPair SearchMusicList(String s,String type) {
        UrlParamPair upp = new UrlParamPair();
        upp.addPara("s", s);
        upp.addPara("type",type);
        upp.addPara("offset", 0);
        upp.addPara("total", "True");
        upp.addPara("limit", 5);
        upp.addPara("n", 1000);
        upp.addPara("csrf_token", "nothing");
        return upp;
    }
}
