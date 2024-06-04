package org.example;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public class GameHandler implements Handler {
    private Game game;
    private ArrayDeque<String> dequePlayer;
    public GameHandler() {
        game = new Game();
        dequePlayer = new ArrayDeque<>();
        dequePlayer.add("player1");
        dequePlayer.add("player2");
    }

    @Override
    public Response handle(Request request) {
        String action = request.getAction();
        Response response = new Response();//новый оюъект ответ для пользователя

        switch (action) {
            case "MOVE":
                String direction = request.getDirection();
                game.movePlayer(request.getPlayerId(), direction);
                response.setStatusCode(200);
                response.setStatusText("OK");
                response.setBody(game.toJson());
                break;
            case "SHOOT":
                String shootDirection = request.getDirection();
                String statusText = game.shootPlayer(request.getPlayerId(), shootDirection);
                if (statusText.contains(" Выйграл!"))
                    response.setStatusCode(300);
                else response.setStatusCode(200);
                response.setStatusText(statusText);
                response.setBody(game.toJson());
                break;
            case "GET":
                response.setStatusCode(200);
                response.setStatusText("OK");
                response.setBody(game.toJson());
                response.setPlayerId(dequePlayer.pollFirst());
                break;
            default:
                response.setStatusCode(400);
                response.setStatusText("Bad Request");
                break;
        }

        return response;
    }
}
