package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedServer {

    public static final int PORT = 51;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        Handler handler = new GameHandler();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {// Создание сервера сокета
            System.out.println("Сервер запущен, ожидание подключения клиентов...");

            while (true) {
                Socket clientSocket = serverSocket.accept();//ждем клиента
                System.out.println("Клиент подключен: " + clientSocket.getInetAddress());
                threadPool.execute(new ClientHandler(clientSocket,handler));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
