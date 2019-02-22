package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Message {
    private int type;
    private JsonObject body;

    public Message(String str) {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(str).getAsJsonObject();
        type = o.get("type").getAsInt();
        body = o.get("data").getAsJsonObject();
    }

    public int type() {
        return type;
    }

    public JsonObject body() {
        return body;
    }

}
