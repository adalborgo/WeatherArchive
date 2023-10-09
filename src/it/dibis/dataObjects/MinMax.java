package it.dibis.dataObjects;

public class MinMax {

    private float temperatureMin, temperatureMax, temperatureMean;
    private int temperatureMinTime, temperatureMaxTime;

    private float humidityMin, humidityMax, humidityMean;
    private int humidityMinTime, humidityMaxTime;

    private float pressureMin, pressureMax, pressureMean;
    private int pressureMinTime, pressureMaxTime;

    private float windSpeedMax, windSpeedMean;
    private int windMaxTime;
    private float windDirectionOfMaxSpeed, windDirectionMean;

    private float rain_all; // Overall rain
    private float rainRateMax; // mm/h
    private int rainRateMaxTime; // Minutes

    private float sunradMax, sunradMean;
    private int sunradMaxTime;
    public synchronized void setTemperatureMin(float temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public synchronized void setTemperatureMax(float temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public synchronized void setTemperatureMinTime(int temperatureMinTime) {
        this.temperatureMinTime = temperatureMinTime;
    }

    public synchronized void setTemperatureMaxTime(int temperatureMaxTime) {
        this.temperatureMaxTime = temperatureMaxTime;
    }

    public synchronized float getTemperatureMin() {
        return this.temperatureMin;
    }

    public synchronized float getTemperatureMax() {
        return this.temperatureMax;
    }

    public synchronized int getTemperatureMinTime() {
        return this.temperatureMinTime;
    }

    public synchronized int getTemperatureMaxTime() {
        return this.temperatureMaxTime;
    }

    public synchronized float getTemperatureMean() {
        return this.temperatureMean;
    }

    // Humidity
    public synchronized void setHumidityMin(float humidityMin) {
        this.humidityMin = humidityMin;
    }

    public synchronized void setHumidityMax(float humidityMax) {
        this.humidityMax = humidityMax;
    }

    public synchronized void setHumidityMinTime(int humidityMinTime) {
        this.humidityMinTime = humidityMinTime;
    }

    public synchronized void setHumidityMaxTime(int humidityMaxTime) {
        this.humidityMaxTime = humidityMaxTime;
    }

    public synchronized float getHumidityMin() {
        return this.humidityMin;
    }

    public synchronized float getHumidityMax() {
        return this.humidityMax;
    }

    public synchronized int getHumidityMinTime() {
        return this.humidityMinTime;
    }

    public synchronized int getHumidityMaxTime() {
        return this.humidityMaxTime;
    }

    public synchronized float getHumidityMean() {
        return this.humidityMean;
    }

    // Pressure
    public synchronized void setPressureMin(float pressureMin) {
        this.pressureMin = pressureMin;
    }

    public synchronized void setPressureMax(float pressureMax) {
        this.pressureMax = pressureMax;
    }

    public synchronized void setPressureMinTime(int pressureMinTime) {
        this.pressureMinTime = pressureMinTime;
    }

    public synchronized void setPressureMaxTime(int pressureMaxTime) {
        this.pressureMaxTime = pressureMaxTime;
    }

    public synchronized float getPressureMin() {
        return this.pressureMin;
    }

    public synchronized float getPressureMax() {
        return this.pressureMax;
    }

    public synchronized int getPressureMinTime() {
        return this.pressureMinTime;
    }

    public synchronized int getPressureMaxTime() {
        return this.pressureMaxTime;
    }

    public synchronized float getPressureMean() {
        return this.pressureMean;
    }

    // Wind
    public synchronized void setWindSpeedMax(float windSpeedMax) {
        this.windSpeedMax = windSpeedMax;
    }

    public synchronized void setWindSpeedMaxTime(int windMaxTime) {
        this.windMaxTime = windMaxTime;
    }

    public synchronized void setWindDirectionOfMaxSpeed(float windDirectionOfMaxSpeed) {
        this.windDirectionOfMaxSpeed = windDirectionOfMaxSpeed;
    }

    public synchronized float getWindSpeedMax() {
        return this.windSpeedMax;
    }

    public synchronized int getWindSpeedMaxTime() {
        return this.windMaxTime;
    }

    public synchronized float getWindDirectionOfMaxSpeed() {
        return this.windDirectionOfMaxSpeed;
    }

    public synchronized float getWindSpeedMean() {
        return this.windSpeedMean;
    }

    public synchronized float getWindDirectionMean() {
        return this.windDirectionMean;
    }

    // Rain
    public synchronized void setRain_all(float rain_all) {
        this.rain_all = rain_all;
    }

    public synchronized void setRainRateMax(float rainRateMax) {
        this.rainRateMax = rainRateMax;
    }

    public synchronized void setRainRateMaxTime(int rainRateMaxTime) {
        this.rainRateMaxTime = rainRateMaxTime;
    }

    public synchronized float getRain_all() {
        return this.rain_all;
    }

    public synchronized float getRainRateMax() {
        return this.rainRateMax;
    }

    public synchronized int getRainRateMaxTime() {
        return this.rainRateMaxTime;
    }

    // Solar Radiation
    public synchronized void setSunradMax(float sunradMax) {
        this.sunradMax = sunradMax;
    }

    public synchronized void setSunradMaxTime(int sunradMaxTime) {
        this.sunradMaxTime = sunradMaxTime;
    }

    public synchronized float getSunradMax() {
        return this.sunradMax;
    }

    public synchronized int getSunradMaxTime() {
        return this.sunradMaxTime;
    }

    public synchronized float getSunradMean() {
        return this.sunradMean;
    }


}
