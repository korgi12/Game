package org.example;

import org.json.simple.JSONObject;

public class Response {
    private int statusCode;
    private String statusText;
    private String action;
    private String direction;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    private String playerId;
    private JSONObject body;

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("playerId", playerId);
        json.put("statusText", statusText);
        json.put("statusCode", statusCode);
        json.put("action", action);
        json.put("body",body);
        return json.toString();
    }
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
    public void setBody(JSONObject body) {
        this.body = body;
    }
    public JSONObject getBody() {
        return body;
    }

}
