package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedServer {

    public static final int PORT = 51;
    private static final Set<ClientHandler> clientHandlers =ConcurrentHashMap.newKeySet();
    private final ArrayDeque<String> dequePlayer = new ArrayDeque<>();

    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        Handler handler = new GameHandler();

        dequePlayer.add("player1");
        dequePlayer.add("player2");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {// Создание сервера сокета
            System.out.println("Сервер запущен, ожидание подключения клиентов...");

            while (true) {
                Socket clientSocket = serverSocket.accept();//ждем клиент
                System.out.println("Клиент подключен: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, handler,Objects.requireNonNull(dequePlayer.poll()));
                clientHandlers.add(clientHandler);
                clientHandler.setMessageListener(MultiThreadedServer::broadcastMessage);
                threadPool.execute(clientHandler);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(Response message) {
      for(ClientHandler clientHandler : clientHandlers){
          if (clientHandlers.size()==2){
              clientHandler.sendMessage(message);
          }
      }

    }
}
