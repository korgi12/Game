package org.example;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private String action;
    private String direction;
    private String playerId;


    public Request(String action, String direction,  String playerId) {
        this.action = action;
        this.direction = direction;
        this.playerId = playerId;
    }

//    public Request fromJson(String jsonString) {
//        try {
//            JSONParser parser = new JSONParser();
//            JSONObject json = (JSONObject) parser.parse(jsonString);
//            String action = (String) json.get("action");
//            String direction = (String) json.get("direction");
//            String playerId = (String) json.get("playerId");
//            return new Request(action, direction,playerId);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
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
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    public String getPlayerId() {
        return playerId;
    }



}