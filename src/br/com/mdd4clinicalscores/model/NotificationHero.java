package br.com.mdd4clinicalscores.model;

public class NotificationHero {

    private Double value;
    private String sensorName;
    private String typeOfData;
    private String unityMeasuremente;
    private String obsRule;
    private String score;

    public NotificationHero(Double value, String sensorName, String typeOfData, String unityMeasuremente, String obsRule, String score) {
        this.value = value;
        this.sensorName = sensorName;
        this.typeOfData = typeOfData;
        this.unityMeasuremente = unityMeasuremente;
        this.obsRule = obsRule;
        this.score = score;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getTypeOfData() {
        return typeOfData;
    }

    public void setTypeOfData(String typeOfData) {
        this.typeOfData = typeOfData;
    }

    public String getUnityMeasuremente() {
        return unityMeasuremente;
    }

    public void setUnityMeasuremente(String unityMeasuremente) {
        this.unityMeasuremente = unityMeasuremente;
    }

    public String getObsRule() {
        return obsRule;
    }

    public void setObsRule(String obsRule) {
        this.obsRule = obsRule;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
