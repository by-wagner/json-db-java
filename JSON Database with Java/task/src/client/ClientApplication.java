package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.gson.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ClientApplication {

    @Parameter(names = {"-t", "--type"}, description = "Type of the request: get, set, delete, or exit")
    private String type;

    @Parameter(names = {"-k", "--key"}, description = "Key for the request")
    private String key;

    @Parameter(names = {"-v", "--value"}, description = "Value to be set")
    private String value = "";

    @Parameter(names = {"-in"}, description = "File name for the request")
    private String fileName;

    public static void main(String[] args) {
        ClientApplication client = new ClientApplication();
        JCommander jCommander = JCommander.newBuilder().addObject(client).build();
        try {
            jCommander.parse(args);
            client.run();
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            jCommander.usage();
        }
    }

    private void run() {
        System.out.println("Client started!");
        String address = "127.0.0.1";
        int port = 23456;

        try (Socket socket = new Socket(InetAddress.getByName(address), port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            JsonObject requestJson;
            if (fileName != null) {
                String path = System.getProperty("user.dir") + "/src/client/data/" + fileName;
                String content = new String(Files.readAllBytes(Paths.get(path)));
                requestJson = JsonParser.parseString(content).getAsJsonObject();
            } else {
                requestJson = new JsonObject();
                requestJson.addProperty("type", type);
                if (type != null && !type.equals("exit")) {
                    if (key.contains("/")) {
                        JsonArray keyArray = new JsonArray();
                        Arrays.stream(key.split("/")).forEach(keyArray::add);
                        requestJson.add("key", keyArray);
                    } else {
                        requestJson.addProperty("key", key);
                    }
                }
                if (type != null && type.equals("set")) {
                    JsonElement valueElement;
                    try {
                        valueElement = JsonParser.parseString(value);
                    } catch (JsonSyntaxException e) {
                        valueElement = new JsonPrimitive(value);
                    }
                    requestJson.add("value", valueElement);
                }
            }

            String request = requestJson.toString();
            System.out.println("Sent: " + request);
            output.writeUTF(request);

            String response = input.readUTF();
            System.out.println("Received: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}