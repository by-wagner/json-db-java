package server.database;

import com.google.gson.JsonElement;
import java.util.List;

public interface Database {
    boolean set(List<String> key, JsonElement value);
    JsonElement get(List<String> key);
    boolean delete(List<String> key);
}