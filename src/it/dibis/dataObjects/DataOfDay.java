package it.dibis.dataObjects;

import it.dibis.common.Constants;

/**
 * Object of daily data
 * Gestione array dati giornalieri campionati
 * Nota   : 1. I dati comprendono i dati istantanei (Flash) e
 * un array con i dati di tutto il giorno;
 *
 * @author: Antonio Dal Borgo (adalborgo@gmail.com)
 */

public class DataOfDay implements Constants {

    // --- Constants --- //
    /**
     * Revision control id
     */
    private final String cvsId = "$Id: DataOfDay.java,v 0.26 25/09/2023 23:59:59 adalborgo $";

    //--- Variables ---//
    private String stationId;

    private int sampleOfDay;

    /*
     lastsample: number of array elements of file .xml
     setHeader() calculates the value of lastSample from hour and minute
     // Set last index
     if (hour <= 0 && minute <= 0) {
        this.lastSample = 0;
     } else {
        this.lastSample = (((60 * hour + minute) / DELTA_TIME) % SAMPLES_OF_DAY) + 1;
     }

     Used by DayWriteXML.java:
        writeFile(String filename, DataOfDay dataOfDay)
        writeHeader(dataOfDay.getStationId(), dataOfDay.getSampleOfDay(), dataOfDay.getLastSample())
        writeFloatArray("Temperature", dataOfDay.getDataArray(), TEMPERATURE_INDEX, lastSample, TNODATA):
            contains for with index up to maxIndex = lastSample
     */

    private int lastSample;

    private int day, month, year;
    private int hour, minute;

    private float temperature, temperatureMin, temperatureMax, temperatureMean;
    private int temperatureMinTime, temperatureMaxTime;

    private float humidity, humidityMin, humidityMax, humidityMean;
    private int humidityMinTime, humidityMaxTime;

    private float pressure, pressureMin, pressureMax, pressureMean;
    private int pressureMinTime, pressureMaxTime;

    private float windSpeed, windSpeedMax, windSpeedMean;
    private int windMaxTime;
    private float windDirection, windDirectionOfMaxSpeed, windDirectionMean;

    private float rain_all; // Overall rain
    private float rainRateMax; // mm/h
    private int rainRateMaxTime; // Minutes

    private float sunrad, sunradMax, sunradMean;
    private int sunradMaxTime;

    // Temporary data for getDataArray(int sample) and calcMean
    private float temperatureBuffer;
    private float humidityBuffer;
    private float pressureBuffer;
    private float windSpeedBuffer;
    private float windDirectionBuffer;
    private float rain_allBuffer;
    private float sunradBuffer;

    // Array of daily data
    private float[][] dataArray = null;

    /**
     * Constructor
     */
    public DataOfDay() { /* Default constructor */ }

    /**
     * @param sampleOfDay
     */
    public void init(int sampleOfDay) {
        setSampleOfDay(sampleOfDay); // Set sampleOfDay
        this.dataArray = new float[DATA_TYPE][sampleOfDay + 1];
        clearDataArray(); // Set temperature[] = TNODATA
    }

    /**
     * @param sampleOfDay
     */
    public void init(int year, int month, int day, int sampleOfDay) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.sampleOfDay = sampleOfDay;
        this.dataArray = new float[DATA_TYPE][sampleOfDay + 1];
        clearAllData(); // clearDataFlash() and clearDataArray()
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    // ---------------------------------------------- //

    // Header
    public synchronized void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public synchronized void setSampleOfDay(int sampleOfDay) {
        this.sampleOfDay = sampleOfDay;
    }

    public synchronized void setLastSample(int lastSample) {
        this.lastSample = lastSample;
    }

    public synchronized String getStationId() {
        return this.stationId;
    }

    public synchronized int getSampleOfDay() {
        return this.sampleOfDay;
    }

    public synchronized int getLastSample() {
        return this.lastSample;
    }

    // Day and time
    public synchronized void setDay(int day) {
        this.day = day;
    }

    public synchronized void setMonth(int month) {
        this.month = month;
    }

    public synchronized void setYear(int year) {
        this.year = year;
    }

    public synchronized void setHour(int hour) {
        this.hour = hour;
    }

    public synchronized void setMinute(int minute) {
        this.minute = minute;
    }

    public synchronized int getDay() {
        return this.day;
    }

    public synchronized int getMonth() {
        return this.month;
    }

    public synchronized int getYear() {
        return this.year;
    }

    public synchronized int getHour() {
        return this.hour;
    }

    public synchronized int getMinute() {
        return this.minute;
    }

    // Temperature
    public synchronized void setTemperature(float temperature) {
        this.temperature = temperature;
    }

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

    public synchronized float getTemperature() {
        return this.temperature;
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
    public synchronized void setHumidity(float humidity) {
        this.humidity = humidity;
    }

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

    public synchronized float getHumidity() {
        return this.humidity;
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
    public synchronized void setPressure(float pressure) {
        this.pressure = pressure;
    }

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

    public synchronized float getPressure() {
        return this.pressure;
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
    public synchronized void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public synchronized void setWindSpeedMax(float windSpeedMax) {
        this.windSpeedMax = windSpeedMax;
    }

    public synchronized void setWindSpeedMaxTime(int windMaxTime) {
        this.windMaxTime = windMaxTime;
    }

    public synchronized void setWindDirection(float windDirection) {
        this.windDirection = windDirection;
    }

    public synchronized void setWindDirectionOfMaxSpeed(float windDirectionOfMaxSpeed) {
        this.windDirectionOfMaxSpeed = windDirectionOfMaxSpeed;
    }

    public synchronized float getWindSpeed() {
        return this.windSpeed;
    }

    public synchronized float getWindSpeedMax() {
        return this.windSpeedMax;
    }

    public synchronized int getWindSpeedMaxTime() {
        return this.windMaxTime;
    }

    public synchronized float getWindDirection() {
        return this.windDirection;
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
    public synchronized void setSunrad(float sunrad) {
        this.sunrad = sunrad;
    }

    public synchronized void setSunradMax(float sunradMax) {
        this.sunradMax = sunradMax;
    }

    public synchronized void setSunradMaxTime(int sunradMaxTime) {
        this.sunradMaxTime = sunradMaxTime;
    }

    public synchronized float getSunrad() {
        return this.sunrad;
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

    /**
     * Important! Used by XMLEncoder
     *
     * @param dataArray
     */
    public synchronized void setDataArray(float[][] dataArray) {
        this.dataArray = dataArray;
    }

    /**
     * @return
     */
    public synchronized float[][] getDataArray() {
        return this.dataArray;
    }

    /**
     * @param value
     * @param type
     * @param index
     */
    public synchronized void setDataArray(float value, int type, int index) {
        this.dataArray[type][index] = value;
    }

    /**
     * Return an element of the array
     *
     * @param type, index
     * @return float
     */
    public synchronized float getDataArray(int type, int index) {
        if (index >= 0 && index < dataArray[0].length) { // !!! Modificato (28/12/2020)
            return this.dataArray[type][index];
        } else {
            return (type == TEMPERATURE_INDEX) ? TNODATA : -1;
        }
    }

    /**
     * Return lenght of array
     *
     * @return int
     */
    public synchronized int getDataArrayLength() {
        return this.dataArray[0].length;
    }

    /**
     * Extract a raw from dataArray
     *
     * @param index of day
     */
    public synchronized void getRawOfDataArray(int index) {
        if (index >= 0 && index < dataArray[0].length) { // !!! Modificato (28/12/2020)
            temperatureBuffer = dataArray[TEMPERATURE_INDEX][index];
            humidityBuffer = dataArray[HUMIDITY_INDEX][index];
            pressureBuffer = dataArray[PRESSURE_INDEX][index];
            windSpeedBuffer = dataArray[WINDSPEED_INDEX][index];
            windDirectionBuffer = dataArray[WINDDIR_INDEX][index];
            rain_allBuffer = dataArray[RAIN_INDEX][index];
            sunradBuffer = dataArray[SUNRAD_INDEX][index];
        }
    }

    /**
     * Update dataArray[]
     *
     * @param index of array
     */
    public synchronized void setRawOfArray(int index) {
        if (index >= 0 && index < dataArray[0].length) { // !!! Modificato (28/12/2020)
            dataArray[TEMPERATURE_INDEX][index] = temperature;
            dataArray[HUMIDITY_INDEX][index] = humidity;
            dataArray[PRESSURE_INDEX][index] = pressure;
            dataArray[WINDSPEED_INDEX][index] = windSpeed;
            dataArray[WINDDIR_INDEX][index] = windDirection;
            dataArray[RAIN_INDEX][index] = rain_all;
            dataArray[SUNRAD_INDEX][index] = sunrad;
        }
    }

    /**
     * Clear all data of array (only for DataManager!!!)
     */
    public synchronized void clearDataArray() {
        for (int dataIndex = 0; dataIndex < dataArray[0].length; dataIndex++) {
            for (int typeIndex = 0; typeIndex < DATA_TYPE; typeIndex++) {
                if (typeIndex == TEMPERATURE_MIN_INDEX || typeIndex == TEMPERATURE_MAX_INDEX || typeIndex == TEMPERATURE_MEAN_INDEX) {
                    dataArray[typeIndex][dataIndex] = TNODATA;
                } else {
                    dataArray[typeIndex][dataIndex] = -1;
                }
            }
        }
    }
    //--- EndApp array ---

    /**
     * Reset all data
     * Usato da DataManager e MonthlyFile
     */
    public synchronized void clearAllData() {
        clearDataFlash();
        clearDataArray();

        // clearMinMax(); //???????
    }

    /**
     * Clear current data
     */
    public synchronized void clearDataFlash() {

        this.stationId = ""; // Data not yet assigned
        this.sampleOfDay = SAMPLES_OF_DAY;
        this.lastSample = 0;

        this.day = this.month = this.year = -1;
        this.hour = this.minute = -1;

        this.temperature = this.temperatureMean = TNODATA;
        this.temperatureMin = this.temperatureMax = TNODATA;
        this.temperatureMinTime = this.temperatureMaxTime = -1;

        this.humidity = this.humidityMean = -1;
        this.humidityMin = this.humidityMax = -1;
        this.humidityMinTime = this.humidityMaxTime = -1;

        this.pressure = this.pressureMean = -1;
        this.pressureMin = this.pressureMax = -1;
        this.pressureMinTime = this.pressureMaxTime = -1;

        this.windSpeed = this.windSpeedMax = this.windSpeedMean = -1;
        this.windMaxTime = -1;

        this.windDirection = this.windDirectionMean = -1;
        this.windDirectionOfMaxSpeed = -1;

        this.rain_all = -1;
        this.rainRateMax = -1;
        this.rainRateMaxTime = -1;

        this.sunrad = this.sunradMax = this.sunradMean = -1;
        this.sunradMaxTime = -1;
    }

    /**
     * Clear min & max data
     * Note: Do not set the current values to min/max!
     */
    public synchronized void clearMinMax() {

        this.temperatureMin = this.temperatureMax = TNODATA;
        this.temperatureMinTime = this.temperatureMaxTime = -1;

        this.humidityMin = this.humidityMax = -1;
        this.humidityMinTime = this.humidityMaxTime = -1;

        this.pressureMin = this.pressureMax = -1;
        this.pressureMinTime = this.pressureMaxTime = -1;

        this.windSpeedMax = -1;
        this.windMaxTime = -1;

        this.windDirectionOfMaxSpeed = -1;

        this.rainRateMax = -1;
        this.rainRateMaxTime = -1;

        this.sunradMax = -1;
        this.sunradMaxTime = -1;
    }

    /**
     * Set stationId, date and time
     * Calculate lastSample
     */
    public synchronized void setHeader(String stationId, int day, int month, int year, int hour, int minute) {

        this.stationId = stationId; // For stationId

        this.day = day;
        this.month = month; // 1..12
        this.year = year; // 20xx
        this.hour = hour;
        this.minute = minute;

        // Set last index
        if (hour <= 0 && minute <= 0) {
            this.lastSample = 0;
        } else {
            this.lastSample = (((60 * hour + minute) / DELTA_TIME) % SAMPLES_OF_DAY) + 1;
        }

    }

    /**
     * Get max rain rate
     */
    public void calcRainRateMax() {

        float max = 0;
        float dx = 0;
        int di = 1;
        int mTime = -1;

        // Init
        float x0 = getDataArray(RAIN_INDEX, 0);
        if (x0 < 0) x0 = 0;
        int imax = (lastSample > 0) ? lastSample : dataArray[0].length;

        for (int i = 1; i < imax; i++) { // Modificato 28/12/2020
            float x = getDataArray(RAIN_INDEX, i);
            if (x > 0) {
                dx = (x - x0) / di;
                x0 = x;
                di = 1;
                if (dx > max) {
                    max = dx;
                    mTime = DELTA_TIME * i; // getTimeMinutes();
                }
            } else {
                if (x < 0) ++di;
            }
        }

        this.rainRateMax = max * 60 / DELTA_TIME; // mm/h
        this.rainRateMaxTime = mTime;
    }

    /**
     * Get the mean data for the day (no time!)
     */
    public void calcMean() {

        float tempMeanSum = 0;
        float umidMeanSum = 0;
        float wvelMeanSum = 0;
        float wdirMeanSum = 0;
        float presMeanSum = 0;
        float sunradMeanSum = 0;

        int sampleTemp = 0;
        int sampleUmid = 0;
        int samplePres = 0;
        int sampleWinDir = 0;
        int sampleWinVel = 0;
        int sampleSunrad = 0;

        // Get mean values
        int sampleMax = (lastSample > 0) ? lastSample : dataArray[0].length;
        for (int index = 0; index < sampleMax; index++) {

            // Extract a raw from dataArray
            getRawOfDataArray(index);

            // Temperature
            if (temperatureBuffer > TNODATA) {
                tempMeanSum += temperatureBuffer;
                ++sampleTemp;
            }

            // Humidity
            if (humidityBuffer > 0) {
                umidMeanSum += humidityBuffer;
                ++sampleUmid;
            }

            // Pressure
            if (pressureBuffer > 0) {
                presMeanSum += pressureBuffer;
                ++samplePres;
            }

            // Wind speed
            if (windSpeedBuffer >= 0) { // Wind speed
                wvelMeanSum += windSpeedBuffer;
                ++sampleWinVel;
            }

            // Wind direction
            if ((windSpeedBuffer > 0) && (windDirectionBuffer >= 0 && windDirectionBuffer <= 360)) {
                wdirMeanSum += windDirectionBuffer;
                ++sampleWinDir;
            }

            // Sunrad
            if (sunradBuffer > 0) {
                sunradMeanSum += sunradBuffer;
                ++sampleSunrad;
            }
        }

        // --- Set mean value ---//
        // Temperature
        if (sampleTemp > 0) temperatureMean = tempMeanSum / sampleTemp;

        // Humidity
        if (sampleUmid > 0) humidityMean = umidMeanSum / sampleUmid;

        // Pressure
        if (samplePres > 0) pressureMean = presMeanSum / samplePres;

        // Wind
        if (sampleWinVel > 0) windSpeedMean = wvelMeanSum / sampleWinVel;
        if (sampleWinDir > 0) windDirectionMean = wdirMeanSum / sampleWinDir;

        // Solar radiation
        if (sampleSunrad > 0) sunradMean = sunradMeanSum / sampleUmid;
    }

    //------------------- Solo per Debug --------------------//

    public void print() {
        printHeader();
        printFlash();
        printArray();
    }

    public void printHeader() {
        System.out.print("stationId: " + stationId);
        System.out.print("\tDay: " + day + "-" + month + "-" + year);
        System.out.println("\tHour: " + hour + ":" + minute);
        System.out.println("\tSampleOfDay: " + sampleOfDay);
        System.out.println("\tLastSample: " + lastSample);
        // System.out.println("\tdataArray[0].length: " + dataArray[0].length);
    }

    // Print DataArray
    public void printArray() {
        for (int dataIndex = 0; dataIndex < dataArray[0].length; dataIndex++) {
            System.out.println();
            for (int typeIndex = 0; typeIndex < DATA_TYPE; typeIndex++) {
                System.out.print(this.dataArray[typeIndex][dataIndex]);
                if (typeIndex >= 0 && typeIndex < DATA_TYPE) {
                    System.out.print("\t");
                } else {
                    System.out.println();
                }
            }
        }

        System.out.println();
    }

    public void printFlash() {

        // Temperature
        System.out.println("\nTemperature");
        System.out.println("t: " + temperature + " celsius");
        System.out.print("temperatureMin: " + temperatureMin + " celsius");
        System.out.println("  (" + temperatureMinTime / 60 + ":" + temperatureMinTime % 60 + ")");
        System.out.print("temperatureMax: " + temperatureMax + " celsius");
        System.out.println("  (" + temperatureMaxTime / 60 + ":" + temperatureMaxTime % 60 + ")");

        // Humidity
        System.out.println("\nHumidity");
        System.out.println("u: " + humidity + " %");
        System.out.print("humidityMin: " + humidityMin + " %");
        System.out.println("  (" + humidityMinTime / 60 + ":" + humidityMinTime % 60 + ")");
        System.out.print("humidityMax: " + humidityMax + " %");
        System.out.println("  (" + humidityMaxTime / 60 + ":" + humidityMaxTime % 60 + ")");

        // Wind
        System.out.println("\nWind");
        System.out.println("w: " + windSpeed + " m/s");
        System.out.print("windSpeedMax: " + windSpeedMax + " m/s");
        System.out.println("  (" + windMaxTime / 60 + ":" + windMaxTime % 60 + ")");

        // Wind direction
        System.out.println("dir: " + windDirection + " deg");
        System.out.println("windDirectionOfMaxSpeed: " + windDirectionOfMaxSpeed + " deg");

        // Pressure
        System.out.println("\nPressure");
        System.out.println("p: " + pressure + " hPa");
        System.out.print("pressureMin: " + pressureMin + " hPa");
        System.out.println("  (" + pressureMinTime / 60 + ":" + pressureMinTime % 60 + ")");
        System.out.print("pressureMax: " + pressureMax + " hPa");
        System.out.println("  (" + pressureMaxTime / 60 + ":" + pressureMaxTime % 60 + ")");

        // Rain
        System.out.println("\nRain");
        System.out.println("rain_all: " + rain_all + " mm");
        System.out.println("rainRateMax: " + rainRateMax + " mm/h");
        System.out.println("  (" + rainRateMaxTime / 60 + ":" + rainRateMaxTime % 60 + ")");

        // Solar radiation
        System.out.println("\nSolar Radiation");
        System.out.println("sunrad: " + sunrad + " W/m^2");
        System.out.println("sunradMax: " + sunradMax + " W/m^2");
    }
}
