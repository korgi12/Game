package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Objects;

public class GameBoard extends Application {
    private Client client;
    private Rectangle[][] cells;
    private Lunokhod player1;
    private Lunokhod player2;
    private Stage primaryStage;
    private TextArea textArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        client = new Client("localhost");//создание клеинта

        this.primaryStage = primaryStage;
        // Инициализация графического интерфейса

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(5);
        grid.setVgap(5);

        cells = new Rectangle[8][8];
        initializeBoard(grid);

        Button btnUp = new Button("Up");
        btnUp.setOnAction(e -> move("up"));

        Button btnDown = new Button("Down");
        btnDown.setOnAction(e -> move("down"));

        Button btnLeft = new Button("Left");
        btnLeft.setOnAction(e -> move("left"));

        Button btnRight = new Button("Right");
        btnRight.setOnAction(e -> move("right"));

        Button btnShootUp = new Button("Shoot Up");
        btnShootUp.setOnAction(e -> shoot("up"));

        Button btnShootDown = new Button("Shoot Down");
        btnShootDown.setOnAction(e -> shoot("down"));

        Button btnShootLeft = new Button("Shoot Left");
        btnShootLeft.setOnAction(e -> shoot("left"));

        Button btnShootRight = new Button("Shoot Right");
        btnShootRight.setOnAction(e -> shoot("right"));

        GridPane controls = new GridPane();
        controls.setHgap(5);
        controls.setVgap(5);
        controls.add(btnUp, 1, 0);
        controls.add(btnLeft, 0, 1);
        controls.add(btnRight, 2, 1);
        controls.add(btnDown, 1, 2);
        controls.add(btnShootUp, 1, 3);
        controls.add(btnShootDown, 1, 4);
        controls.add(btnShootLeft, 0, 3);
        controls.add(btnShootRight, 2, 3);

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefHeight(200);
        textArea.setWrapText(true);

        GridPane mainLayout = new GridPane();
        mainLayout.setHgap(10);
        mainLayout.setVgap(10);
        mainLayout.add(grid, 0, 0);
        mainLayout.add(controls, 1, 0);
        mainLayout.add(textArea, 0, 1, 2, 1);

        Scene scene = new Scene(mainLayout, 800, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Начальное состояние игры
        updateBoard();
    }

    private void initializeBoard(GridPane grid) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Rectangle cell = new Rectangle(50, 50);
                cell.setFill(Color.LIGHTGRAY);
                cell.setStroke(Color.BLACK);
                cells[row][col] = cell;
                StackPane cellStack = new StackPane();
                cellStack.getChildren().add(cell);
                grid.add(cellStack, col, row);
            }
        }
    }

    private void move(String direction) {
        // Отправляем запрос на сервер для перемещения
        String response = client.sendMoveRequest(direction);
        handleResponse(response);
    }

    private void shoot(String direction) {
        // Отправляем запрос на сервер для стрельбы
        String response = client.sendShootRequest(direction);
        handleResponse(response);
    }

    private void handleResponse(String response) {
        // Обрабатываем ответ от сервера и обновляем состояние доски
        // Примерный JSON ответ: {"board": [[0, -1, 0, ...], ...], "player1": {"x": 0, "y": 0}, "player2": {"x": 7, "y": 7}}
        updateBoard(response);
    }

    private void updateBoard() {
        // Получаем начальное состояние доски с сервера
        String response = client.getInitialState();
        updateBoard(response);
    }

    private void updateBoard(String jsonResponse) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonResponse);
            if (Objects.isNull(primaryStage.getTitle()))
                primaryStage.setTitle((String) json.get("playerId"));
            textArea.setText((String) json.get("statusText"));
            JSONObject body = (JSONObject) json.get("body");
            JSONArray boardArray = (JSONArray) body.get("board");
            JSONObject player1Json = (JSONObject) body.get("player1");
            JSONObject player2Json = (JSONObject) body.get("player2");

            // Очистка доски
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    cells[row][col].setFill(Color.LIGHTGRAY);
                }
            }

            // Расстановка препятствий
            for (int row = 0; row < 8; row++) {
                JSONArray rowArray = (JSONArray) boardArray.get(row);
                for (int col = 0; col < 8; col++) {
                    int cellValue = ((Long) rowArray.get(col)).intValue();
                    if (cellValue == -1) {
                        cells[row][col].setFill(Color.DARKGRAY);
                    }
                }
            }

            // Расстановка игроков
            Long code = (Long) json.get("statusCode");
            if (code==300) {
                if (primaryStage.getTitle().equals("player1")) {
                    int player1X = ((Long) player1Json.get("x")).intValue();
                    int player1Y = ((Long) player1Json.get("y")).intValue();
                    cells[player1Y][player1X].setFill(Color.BLUE);

                    int player2X = ((Long) player2Json.get("x")).intValue();
                    int player2Y = ((Long) player2Json.get("y")).intValue();
                    cells[player2Y][player2X].setFill(Color.BLACK);
                } else {
                    int player2X = ((Long) player2Json.get("x")).intValue();
                    int player2Y = ((Long) player2Json.get("y")).intValue();
                    cells[player2Y][player2X].setFill(Color.RED);

                    int player1X = ((Long) player1Json.get("x")).intValue();
                    int player1Y = ((Long) player1Json.get("y")).intValue();
                    cells[player1Y][player1X].setFill(Color.BLACK);
                }
            } else {

                int player1X = ((Long) player1Json.get("x")).intValue();
                int player1Y = ((Long) player1Json.get("y")).intValue();
                cells[player1Y][player1X].setFill(Color.BLUE);

                int player2X = ((Long) player2Json.get("x")).intValue();
                int player2Y = ((Long) player2Json.get("y")).intValue();
                cells[player2Y][player2X].setFill(Color.RED);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
