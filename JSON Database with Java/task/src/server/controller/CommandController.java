package server.controller;

import com.google.gson.*;
import server.database.Database;

import java.util.ArrayList;
import java.util.List;

public class CommandController {

    public static String handleRequest(String request, Database database) {
        JsonObject requestJson = JsonParser.parseString(request).getAsJsonObject();
        String type = requestJson.get("type").getAsString();
        JsonObject responseJson = new JsonObject();

        switch (type) {
            case "set":
                List<String> keySet = parseKey(requestJson.get("key"));
                JsonElement value = requestJson.get("value");
                database.set(keySet, value);
                responseJson.addProperty("response", "OK");
                break;
            case "get":
                List<String> keyGet = parseKey(requestJson.get("key"));
                JsonElement result = database.get(keyGet);
                if (result != null) {
                    responseJson.addProperty("response", "OK");
                    responseJson.add("value", result);
                } else {
                    responseJson.addProperty("response", "ERROR");
                    responseJson.addProperty("reason", "No such key");
                }
                break;
            case "delete":
                List<String> keyDelete = parseKey(requestJson.get("key"));
                if (database.delete(keyDelete)) {
                    responseJson.addProperty("response", "OK");
                } else {
                    responseJson.addProperty("response", "ERROR");
                    responseJson.addProperty("reason", "No such key");
                }
                break;
            case "exit":
                responseJson.addProperty("response", "OK");
                break;
            default:
                responseJson.addProperty("response", "ERROR");
                responseJson.addProperty("reason", "Unknown type");
                break;
        }

        return responseJson.toString();
    }

    private static List<String> parseKey(JsonElement keyElement) {
        List<String> keyList = new ArrayList<>();
        if (keyElement.isJsonArray()) {
            for (JsonElement element : keyElement.getAsJsonArray()) {
                keyList.add(element.getAsString());
            }
        } else {
            keyList.add(keyElement.getAsString());
        }
        return keyList;
    }
}