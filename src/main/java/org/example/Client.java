package org.example;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String playerId;

    public Client(String serverAddress) throws IOException {
        socket = new Socket(serverAddress, MultiThreadedServer.PORT);//подключение к серверу
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void sendRequest(String request) throws IOException {
        out.write(request);
        out.newLine();
        out.flush();//отправил на сервер

    }

    public String getResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        System.out.println(socket.isConnected());
        response.append(in.readLine());//читаем данные с сервера

        return response.toString();
    }

    public String sendMoveRequest(String direction) {// это действие падает и не выполняется на сервере если не будет
        String requestJson = buildJsonRequest("MOVE", direction);


        try {
            socket = new Socket("localhost", MultiThreadedServer.PORT);// этого
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            sendRequest(requestJson);

            return getResponse();
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    public String sendShootRequest(String direction) {// это действие падает и не выполняется на сервере если не будет
        String requestJson = buildJsonRequest("SHOOT", direction);
        try {
            socket = new Socket("localhost", MultiThreadedServer.PORT);// этого
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
            return  answer;

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
