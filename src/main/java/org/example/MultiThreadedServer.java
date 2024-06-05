package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedServer {

    public static final int PORT = 51;
    private static Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private static ServerObservable serverObservable = new ServerObservable();
    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        Handler handler = new GameHandler();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {// Создание сервера сокета
            System.out.println("Сервер запущен, ожидание подключения клиентов...");

            while (true) {
                Socket clientSocket = serverSocket.accept();//ждем клиент
                System.out.println("Клиент подключен: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket,handler);
                clientHandlers.add(clientHandler);
                clientHandler.setMessageListener(message -> broadcastMessage(message, clientHandler));
                threadPool.execute(clientHandler);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void broadcastMessage(Response message, ClientHandler sender) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != sender) {
                clientHandler.sendMessage(message);
            }
        }
}
