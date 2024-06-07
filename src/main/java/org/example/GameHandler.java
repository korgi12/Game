package org.example;


import java.util.Random;

public class GameHandler implements Handler {
    private Game game;
    private Random random;

    public GameHandler() {
        random = new Random();
        game = new Game();

    }

    @Override
    public Response handle(Request request) {
        String action = request.getAction();
        Response response = new Response();//новый оюъект ответ для пользователя
        int points = random.nextInt(6) + 1;
        switch (action) {
            case "MOVE":
                String direction = request.getDirection();
                game.movePlayer(request.getPlayerId(), direction, points);
                response.setStatusCode(200);
                response.setStatusText("OK");
                response.setBody(game.toJson());
                response.setPoints(points);
                break;
            case "SHOOT":
                String shootDirection = request.getDirection();
                String statusText = game.shootPlayer(request.getPlayerId(), shootDirection, points);
                if (statusText.contains(" Выйграл!")) {
                    response.setStatusCode(300);
                    response.setWinner(statusText.split(" ")[0]);
                } else response.setStatusCode(200);
                response.setStatusText(statusText);
                response.setBody(game.toJson());
                response.setPoints(points);
                break;
            case "GET":
                response.setStatusCode(200);
                response.setStatusText("OK");
                response.setBody(game.toJson());
                response.setPoints(points);
                break;
            case "SERIALIZE":
                game.saveToFile();
                response.setStatusCode(200);
                response.setStatusText("Сохранено в файл GameSave");
                response.setBody(game.toJson());
                response.setPoints(points);
                break;
            case "DESERIALIZE":
                game.loadFromFile();
                response.setStatusCode(200);
                response.setStatusText("Загружено последняя игра из файла GameSave");
                response.setPlayer1Turn(game.isPlayer1Turn());
                response.setBody(game.toJson());
                response.setPoints(points);
                break;
            default:
                response.setStatusCode(400);
                response.setStatusText("Bad Request");
                break;
        }

        return response;
    }


}
