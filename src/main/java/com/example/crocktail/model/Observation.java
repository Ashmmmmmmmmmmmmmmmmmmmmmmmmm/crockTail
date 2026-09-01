package com.example.crocktail.model;

public class Observation {
    private String level; // "warning" or "info"
    private String message;

    public Observation() {}

    public Observation(String level, String message) {
        this.level = level;
        this.message = message;
    }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}