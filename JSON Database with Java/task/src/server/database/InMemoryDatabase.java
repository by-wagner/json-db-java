package server.database;

import com.google.gson.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryDatabase implements Database {
    private final JsonObject db = new JsonObject();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Gson gson = new Gson();
    private final String path = System.getProperty("user.dir") + "/src/server/data/db.json";

    public InMemoryDatabase() {
        loadDatabase();
    }

    @Override
    public boolean set(List<String> key, JsonElement value) {
        lock.writeLock().lock();
        try {
            JsonObject current = db;
            for (int i = 0; i < key.size() - 1; i++) {
                String part = key.get(i);
                if (!current.has(part) || !current.get(part).isJsonObject()) {
                    current.add(part, new JsonObject());
                }
                current = current.getAsJsonObject(part);
            }
            current.add(key.get(key.size() - 1), value);
            saveDatabase();
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public JsonElement get(List<String> key) {
        lock.readLock().lock();
        try {
            JsonElement current = db;
            for (String part : key) {
                if (!current.getAsJsonObject().has(part)) {
                    return null;
                }
                current = current.getAsJsonObject().get(part);
            }
            return current;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean delete(List<String> key) {
        lock.writeLock().lock();
        try {
            JsonObject current = db;
            for (int i = 0; i < key.size() - 1; i++) {
                String part = key.get(i);
                if (!current.has(part) || !current.get(part).isJsonObject()) {
                    return false;
                }
                current = current.getAsJsonObject(part);
            }
            if (current.has(key.get(key.size() - 1))) {
                current.remove(key.get(key.size() - 1));
                saveDatabase();
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void saveDatabase() {
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(db, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDatabase() {
        try (FileReader reader = new FileReader(path)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            if (jsonObject != null) {
                db.add("", jsonObject);
            }
        } catch (IOException e) {
            // Database file not found, ignore.
        }
    }
}