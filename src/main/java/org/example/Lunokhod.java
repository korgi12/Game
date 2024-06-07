package org.example;

import org.json.simple.JSONObject;

public class Lunokhod {
    private int x;
    private int y;

    public Lunokhod(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void moveUp() {
        y--;
    }

    public void moveDown() {
        y++;
    }

    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x++;
    }
    public boolean isAtPosition(int x, int y) {//метод для определения убийства соперника
        return this.x == x && this.y == y;
    }
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("x", x);
        json.put("y", y);
        return json;
    }

    public static Lunokhod fromJson(JSONObject jsonObject) {
        int x = ((Long) jsonObject.get("x")).intValue();
        int y = ((Long) jsonObject.get("y")).intValue();
        return new Lunokhod(x, y);
    }

    // Методы для перемещения и стрельбы
}
