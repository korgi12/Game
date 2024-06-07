package org.example;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private GameBoard gameBoard;

private ExecutorService executor;
    private String playerId;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public Client(String serverAddress, GameBoard gameBoard) throws IOException {
        this.gameBoard = gameBoard;
        socket = new Socket(serverAddress, MultiThreadedServer.PORT);//подключение к серверу
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        executor = Executors.newFixedThreadPool(2);
        executor.submit(this::ReadTask);


    }

    public void ReadTask() {
        try {
            while (true) {
                String message = in.readLine();
                if (queue.offer(message)) {
                    System.out.println("Добавлено в очередь" + playerId);
                } else {
                    System.out.println("НЕЕЕ Добавлено в очередь" + playerId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(String request) throws IOException {
        out.write(request);
        out.newLine();
        out.flush();//отправил на сервер

    }

    public void updateBord() {
        while (true){
            try {
                gameBoard.updateBoard(queue.take());
            }catch (InterruptedException ex){
                System.out.println(ex);
            }

        }
    }

    public void sendMoveRequest(String direction) {// это действие падает и не выполняется на сервере если не будет
        String requestJson = buildJsonRequest("MOVE", direction);
        try {
            sendRequest(requestJson);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void sendShootRequest(String direction) {// это действие падает и не выполняется на сервере если не будет
        String requestJson = buildJsonRequest("SHOOT", direction);
        try {
            sendRequest(requestJson);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    public void serializeGameState() {// это действие падает и не выполняется на сервере если не будет
        String requestJson = "{\"action\":\"SERIALIZE\"}";
        try {
            sendRequest(requestJson);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }public void deserializeGameState() {// это действие падает и не выполняется на сервере если не будет
        String requestJson = "{\"action\":\"DESERIALIZE\"}";
        try {
            sendRequest(requestJson);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void getInitialState() {//все рабоатет изначально
        String requestJson = "{\"action\":\"GET\"}";
        try {
            sendRequest(requestJson);
            String answer = queue.take();

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(answer);

            playerId = (String) json.get("playerId");
            gameBoard.updateBoard(answer);
            executor.submit(this::updateBord);
        } catch (IOException | ParseException ex) {
            System.out.println(ex.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String buildJsonRequest(String action, String direction) {
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("action", action);
        bodyJson.put("direction", direction);
        bodyJson.put("playerId", playerId);

        return bodyJson.toString();
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
