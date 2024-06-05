package org.example;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientHandler implements Runnable {// класс ClientHandler реализует интерфейс Runnable
    private Socket clientSocket;//поле которое хранит сокет Клиента
    private final Handler handler;//реализация управления игрой
    private MessageListener messageListener;
    private BufferedWriter out;
    private BufferedReader in;
    public ClientHandler(Socket socket, Handler handler) {// конструктор класса
        this.clientSocket = socket;
        this.handler = handler;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }
    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(this::handleRead);
        executor.submit(this::handleWrite);
        executor.shutdown();
    }
    public void handleRead() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                Request request = parseRequest(message.toString());
                Response response = handler.handle(request);
                if (messageListener != null) {
                    messageListener.onMessage(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));// поток для чтения данных от клиента
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())))// поток для записи данных для клиента
        {
            CharBuffer charBuffer = CharBuffer.allocate(256);//месьл где будут хранится данные
            StringBuffer requestJson = new StringBuffer();//место где будут хранится прочтенная строка
            boolean keepReading = true; // Флаг для продолжения чтения.

            int readResult = -1; // Переменная для хранения результата чтения.
            while (keepReading) { // Цикл для чтения данных от клиента.
                readResult = in.read(charBuffer); // Читаем данные в буфер.
                if (readResult == -1) { // Если данные закончились, выходим из цикла.
                    break;
                }
                keepReading = readResult == 256; // Продолжаем читать, если буфер заполнен.

                char[] array = Arrays.copyOfRange(charBuffer.array(), 0, readResult); // Копируем прочитанные данные в массив.
                requestJson.append(new String(array)); // Добавляем данные к сообщению.

                charBuffer.clear(); // Очищаем буфер.
            }

            Request request = parseRequest(requestJson.toString());
            Response response = handler.handle(request);

            out.write(response.toJson());//запись данныех в поток для клиента
            out.newLine();//добавили /n
            out.flush();//отправляем клиенту
            System.out.println(clientSocket.isConnected());
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Request parseRequest(String requestJson) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(requestJson);

            String action = (String) json.get("action");
            String direction = (String) json.get("direction");
            String playerId = (String) json.get("playerId");

            return new Request(action, direction, playerId);//вернуть новый созданный объект запроса от Клиента
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void sendMessage(String message) {
        messageQueue.offer(message);
    }
}