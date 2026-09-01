package com.example.crocktail.model;

public class ConfidenceResult {
    private String level; // "high", "medium", "low"
    private int score;    // 0-100
    private String explanation;

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}