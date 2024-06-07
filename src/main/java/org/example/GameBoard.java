package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Objects;

public class GameBoard extends Application {
    private Client client;
    private Rectangle[][] cells;
    private Lunokhod player1;
    private Lunokhod player2;
    private Stage primaryStage;
    private TextArea textArea;
    private Label scoreLabel;
    private Label currentPlayerLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        client = new Client("localhost", this); //создание клиента

        this.primaryStage = primaryStage;

        // Инициализация графического интерфейса
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(5);
        grid.setVgap(5);

        cells = new Rectangle[8][8];
        initializeBoard(grid);

        // Создание кнопок
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

        // Создание панели управления
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

        // Создание кнопки сериализации
        Button btnSerialize = new Button("Serialize");
        btnSerialize.setOnAction(e -> serializeGameState());
        // Создание кнопки загрузки
        Button btnDeserialize = new Button("Load");
        btnDeserialize.setOnAction(e -> deserializeGameState());
        // Создание текстового поля для сообщений
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefHeight(200);
        textArea.setWrapText(true);

        // Создание меток для счёта и текущего игрока
        scoreLabel = new Label("Очки: 0");
        currentPlayerLabel = new Label("Ходит: Player 1");

        // Создание панели для информации о счёте и текущем игроке
        HBox infoPane = new HBox(10);
        infoPane.getChildren().addAll(scoreLabel, currentPlayerLabel);

        // Создание основного макета
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.getChildren().addAll(infoPane, grid, controls, btnSerialize, textArea,btnDeserialize);

        // Создание сцены
        Scene scene = new Scene(mainLayout, 800, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Начальное состояние игры
        initialState();
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
        client.sendMoveRequest(direction);
    }

    private void shoot(String direction) {
        client.sendShootRequest(direction);
    }

    private void initialState() {
        client.getInitialState();
    }
    private void deserializeGameState() {
        client.deserializeGameState();
    }

    private void serializeGameState() {
        client.serializeGameState();
    }
    public void updateBoard(String jsonResponse) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonResponse);
            if (Objects.isNull(primaryStage.getTitle()))
                primaryStage.setTitle((String) json.get("playerId"));



            textArea.setText((String) json.get("statusText"));
            JSONObject body = (JSONObject) json.get("body");
            Platform.runLater(() -> {
                if (body.get("isPlayer1Turn").equals(true))
                    currentPlayerLabel.setText("Ходит: Player 1");
                else currentPlayerLabel.setText("Ходит: Player 2");
                scoreLabel.setText("Очки: "+ json.get("points"));
            });
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
            if (code == 300) {
                if (json.get("winner").equals("player1")) {
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
