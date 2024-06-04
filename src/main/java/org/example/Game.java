package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Random;

public class Game {
    private int[][] board; // 8x8 шахматная доска
    private Lunokhod player1;
    private Lunokhod player2;
    private boolean isPlayer1Turn;
    private Random random;

    public Game()
    {
        board = new int[8][8];
        random = new Random();

        // Начальные позиции луноходов
        int player1StartX = 0;
        int player1StartY = 0;
        int player2StartX = 7;
        int player2StartY = 7;

        // Расставляем препятствия
        for (int i = 0; i < 10; i++) {
            int x, y;
            do {
                x = random.nextInt(8);
                y = random.nextInt(8);
            } while ((x == player1StartX && y == player1StartY) ||
                    (x == player2StartX && y == player2StartY) ||
                    board[x][y] == -1); // Проверка, что место уже не занято препятствием

            board[x][y] = -1; // Препятствие
        }

        player1 = new Lunokhod(player1StartX, player1StartY);
        player2 = new Lunokhod(player2StartX, player2StartY);
        isPlayer1Turn = true;
    }

    public synchronized String movePlayer(String playerId, String direction) {//synchronized
        Lunokhod player = playerId.equals("player1") ? player1 : player2;
        if ((isPlayer1Turn && playerId.equals("player2")) || (!isPlayer1Turn && playerId.equals("player1"))) {
            return "Not your turn";
        }
        int points = random.nextInt(6) + 1; // Очки действия от 1 до 6
        boolean validMove = false;

        while (points > 0 && !validMove) {
            switch (direction) {
                case "up":
                    if (player.getY() > 0 && board[player.getY() - 1][player.getX()] == 0) {
                        player.moveUp();
                        validMove = true;
                    }
                    break;
                case "down":
                    if (player.getY() < 7 && board[player.getY() + 1][player.getX()] == 0) {
                        player.moveDown();
                        validMove = true;
                    }
                    break;
                case "left":
                    if (player.getX() > 0 && board[player.getY()][player.getX() - 1] == 0) {
                        player.moveLeft();
                        validMove = true;
                    }
                    break;
                case "right":
                    if (player.getX() < 7 && board[player.getY()][player.getX() + 1] == 0) {
                        player.moveRight();
                        validMove = true;
                    }
                    break;
                default:
                    return "Invalid move direction";
            }
            if (validMove) {
                points--;
                isPlayer1Turn = !isPlayer1Turn;
            }
        }
        return "Invalid move or not enough points";
    }
    public synchronized String shootPlayer(String playerId, String direction) {
        Lunokhod player = playerId.equals("player1") ? player1 : player2;
        Lunokhod opponent = playerId.equals("player1") ? player2 : player1;
        if ((isPlayer1Turn && playerId.equals("player2")) || (!isPlayer1Turn && playerId.equals("player1"))) {
            return "Not your turn";
        }
        int points = random.nextInt(6) + 1; // Очки действия от 1 до 6

        if (points < 3) {
            return "Not enough points to shoot";
        }

        boolean targetHit = false;
        boolean opponentHit = false;

        switch (direction) {
            case "left":
                for (int x = player.getX() - 1; x >= 0; x--) {
                    if (board[player.getY()][x] != 0) {
                        board[player.getY()][x] = 0; // Уничтожаем объект
                        targetHit = true;
                        break;
                    }
                    if (opponent.isAtPosition(x, player.getY())) {
                        opponentHit = true;
                        break;
                    }
                }
                break;
            case "right":
                for (int x = player.getX() + 1; x < 8; x++) {
                    if (board[player.getY()][x] != 0) {
                        board[player.getY()][x] = 0; // Уничтожаем объект
                        targetHit = true;
                        break;
                    }
                    if (opponent.isAtPosition(x, player.getY())) {
                        opponentHit = true;
                        break;
                    }
                }
                break;
            case "up":
                for (int y = player.getY() - 1; y >= 0; y--) {
                    if (board[y][player.getX()] != 0) {
                        board[y][player.getX()] = 0; // Уничтожаем объект
                        targetHit = true;
                        break;
                    }
                    if (opponent.isAtPosition(player.getX(), y)) {
                        opponentHit = true;
                        break;
                    }
                }
                break;
            case "down":
                for (int y = player.getY() + 1; y < 8; y++) {
                    if (board[y][player.getX()] != 0) {
                        board[y][player.getX()] = 0; // Уничтожаем объект
                        targetHit = true;
                        break;
                    }
                    if (opponent.isAtPosition(player.getX(), y)) {
                        opponentHit = true;
                        break;
                    }
                }
                break;
            default:
                return "Invalid shoot direction";
        }

        if (opponentHit) {
            return playerId + " Выйграл!";
        } else if (targetHit) {
            points -= 3;
            isPlayer1Turn = !isPlayer1Turn;
        }

        return "No target hit";
    }
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONArray boardArray = new JSONArray();

        for (int i = 0; i < board.length; i++) {
            JSONArray rowArray = new JSONArray();
            for (int j = 0; j < board[i].length; j++) {
                rowArray.add(board[i][j]);
            }
            boardArray.add(rowArray);
        }

        json.put("board", boardArray);
        json.put("player1", player1.toJson());
        json.put("player2", player2.toJson());
        return json;
    }
}