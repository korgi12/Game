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
    private String playerId;
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public Client(String serverAddress) throws IOException {
        socket = new Socket(serverAddress, MultiThreadedServer.PORT);//подключение к серверу
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(this::ReadTask);

    }

    public void ReadTask() {
        try {
            while (true) {
                String message;
                while ((message = in.readLine()) != null) {
                    queue.put(message);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(String request) throws IOException {
        out.write(request);
        out.newLine();
        out.flush();//отправил на сервер

    }

    public String getResponse() {
        StringBuilder response = new StringBuilder();
        try {
            response.append(queue.take());//читаем данные с сервера
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }


        return response.toString();
    }

    public String sendMoveRequest(String direction) {// это действие падает и не выполняется на сервере если не будет
        String requestJson = buildJsonRequest("MOVE", direction);
        try {
            sendRequest(requestJson);
            return getResponse();
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    public String sendShootRequest(String direction) {// это действие падает и не выполняется на сервере если не будет
        String requestJson = buildJsonRequest("SHOOT", direction);
        try {
            sendRequest(requestJson);
            return getResponse();
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    public String getInitialState() {//все рабоатет изначально
        String requestJson = "{\"action\":\"GET\"}";
        try {
            sendRequest(requestJson);
            String answer = getResponse();

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(answer);

            playerId = (String) json.get("playerId");
            return answer;

        } catch (IOException | ParseException ex) {
            return ex.getMessage();
        }
    }

    public String buildJsonRequest(String action, String direction) {
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("action", action);
        bodyJson.put("direction", direction);
        bodyJson.put("playerId", playerId);

        return bodyJson.toString();
    }
}
