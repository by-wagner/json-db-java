package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.controller.CommandController;
import server.database.InMemoryDatabase;

public class ServerApplication {

    private static volatile boolean running = true;
    private static ServerSocket serverSocket;
    private static ExecutorService executorService;

    public static void main(String[] args) {
        System.out.println("Server started!");
        String address = "127.0.0.1";
        int port = 23456;
        InMemoryDatabase database = new InMemoryDatabase();
        executorService = Executors.newFixedThreadPool(10);

        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(address));
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    executorService.submit(() -> handleClient(socket, database));
                } catch (IOException e) {
                    if (!running) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdownServer();
        }
    }

    private static void handleClient(Socket socket, InMemoryDatabase database) {
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            String request = input.readUTF();
            System.out.println("Received: " + request);

            String response = CommandController.handleRequest(request, database);
            System.out.println("Sent: " + response);
            output.writeUTF(response);

            if (request.contains("\"type\":\"exit\"")) {
                running = false;
                shutdownServer();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void shutdownServer() {
        try {
            running = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}