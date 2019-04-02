package br.com.mdd4clinicalscores.model;

public class AVPU {
    private String value;
    private int score;

    public AVPU(String value, int score) {
        this.value = value;
        this.score = score;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
