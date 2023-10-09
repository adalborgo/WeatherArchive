package it.dibis.dataLogger;

import java.io.File;
import java.time.LocalDateTime;

import it.dibis.common.Constants;
import it.dibis.dataObjects.DataOfDay;
import it.dibis.dataObjects.SharedData;
import it.dibis.station.*;
import it.dibis.xml.DayReadXML;
import it.dibis.xml.DayWriteXML;

/*
  DataManager.java
  Manage all Weather station data (called from 'MainApp')
  @author Antonio Dal Borgo (adalborgo@gmail.com)
 */

public class DataManager implements Constants {

    //--- DEBUG ---//
    //=============== DEBUG ===============//
    private final boolean DEBUG = false;
    private final boolean SIMUL_RESCUE = false;
    //=====================================//

    // Revision control id
    public static final String CVSID = "$Id: DataManager.java,v 0.10 05/10/2023 23:59:59 adalborgo $";

    private final int COMBILOG = -1;
    private final int DAVIS_JSON = 0;
    private final int DAVIS_PHP = 1;
    private final int METEOFA_JS = 2;

    // --- Variables ---//
    private int year = -1;
    private int month = -1;
    private int day = -1;
    private int hour = -1;
    private int minute = -1;
    private int minutesOfDay = -1; // 0..1439
    private LocalDateTime now = null;

    // Last values
    private float lastRainfallOfDay = 0;

    // Write files .xml and .htm of month at 00:03
    private int lastStationMinuteOfDay = -1;

    private int error = 0; // No error

    //--- Objects ---//
    // All stations use and can share the stationData object
    private StationData stationData = StationData.getInstance(); // Singleton object

    private StationInterface stationInterface = null;

    private DataOfDay dataOfDay = null; // new DataOfDay();

    private UpdateFiles updateFiles = new UpdateFiles();

    private SharedData shared = SharedData.getInstance();
    private int typeOfData = shared.getTypeOfData();

    private String stationId = shared.getStationId();

    //====== Public methods ======//
    public DataManager() {
        // Select instance of stationInterface
        switch (typeOfData) {
            case COMBILOG -> // Combilog
                    stationInterface = new StationCombilog();
            case DAVIS_JSON -> // https://www.meteoproject.it/ftp/stazioni/faenza/wflexp.json
                    stationInterface = new StationDavisJson();
            case DAVIS_PHP -> // https://www.meteosystem.com/wlip/piancavallaro/piancavallaro.php
                    stationInterface = new StationDavisPhp();
            case METEOFA_JS -> // https://meteofa.it/meteofa/dati/data.js
                    stationInterface = new StationMeteofaJs();
            //stationInterface = new StationMeteoJS_NoMinMaxTime();
        }
    }

    /**
     * Open communication port with weather station
     *
     * @return int error
     */
    public int open() {
        return stationInterface.open();
    }

    /**
     * Instance and initialize dataOfDay, ecc.
     * if rootOfXmlPath + DATA_NAME + ".xml" exists check his date
     * else exec 'newDay'
     * else create rootOfXmlPath + DATA_NAME + ".xml"
     *
     * @return true if is active
     */
    public boolean init() {
        DayReadXML dayDataXmlRead = new DayReadXML();
        DayWriteXML dayWriteXML = new DayWriteXML();
        String pathName = shared.getRootOfXmlPath() + DATA_NAME + ".xml";

        boolean today = false;

        // Get station data
        boolean dataValid = getStationData();
        if (!dataValid) {
            return false; // Exit: the station is offline
        }

        /*
        System.out.print("StationData.Time: ");
        System.out.println(stationData.getHour() + ":" +
                stationData.getMinute() + ":" + stationData.getSecond());
        */

        //--- Instance and init dataOfDay ---//
        // Read file 'data.xml' and check if contains data of today
        if (new File(pathName).exists()) {
            if (DEBUG) System.out.println("Read data of today from file: " + pathName);

            // Read the data.xml file and check if it contains today's data
            dataOfDay = dayDataXmlRead.getXmlDataOfDay(pathName, true);
            if (dataOfDay != null) {
                // Check if file contains data of today
                today = (dataOfDay.getDay() == day) &&
                        (dataOfDay.getMonth() == month) && (dataOfDay.getYear() == year);
                dataOfDay.setStationId(stationId);
            } else {
                dataOfDay = new DataOfDay();
                dataOfDay.init(SAMPLES_OF_DAY);
                dataOfDay.setStationId(stationId);
                today = false;
            }

            if (!today) { // New day: cancel all data and reinitialize data file
                if (DEBUG) System.out.println(">>> New day <<<");
                dataOfDay.clearAllData(); // Cancel all data of DataOfDay
                dataOfDay.setStationId(stationId);
                dayWriteXML.writeFile(pathName, dataOfDay);
            }

        } else { // Not found data of today
            if (DEBUG) System.out.println("Not found data of today.");

            // Instance dataOfDay
            dataOfDay = new DataOfDay();
            dataOfDay.init(SAMPLES_OF_DAY);
            dataOfDay.setStationId(stationId);
            dayWriteXML.writeFile(pathName, dataOfDay);
        }

        // Copy stationData to dataOfDay
        saveLastStationDataToDataOfDay(-1);

        System.out.println(">>> Check for any missing data!");
        boolean rescueDataRequest = false;
        int last15minute = (((60 * hour + minute) / DELTA_TIME) % SAMPLES_OF_DAY) + 1;
        for (int i = 0; i < last15minute; i++) {
            if (dataOfDay.getDataArray(TEMPERATURE_INDEX, i) == TNODATA) {
                rescueDataRequest = true;
                break;
            }
        }

        //////////////////////////
        if (DEBUG && SIMUL_RESCUE) {
            System.out.println("DEBUG: simulRescueData() ABILITATO!"); //
            simulRescueData();
        }
        //////////////////////////

        if (rescueDataRequest) {
            /// rescueDataOfDay();
            if (typeOfData == COMBILOG) {
                // Only Combilog can recover lost data!
                rescueDataFromCombilog();
            }
        } else {
            System.out.println("No data to recover.");
        }

        // Get rainAll of the month and the year
        updateFiles.getAllRain(day, month, year);

        dataOfDay.calcMean(); // dataOfDay.printFlash();

        // Now update DATA_NAME + ".htm" and graph files
        updateFiles.updateEveryMinuteFiles(dataOfDay);

        return true; // Station is active
    }

    //===========================//
    //====== EVENT methods ======//
    //===========================//

    /**
     * Manage new minute event: update station data, data of day, files csv and js
     */
    public void newMinuteEvent() {
        if (DEBUG) System.out.println(">>> DataManager.newMinuteEvent() <<<");
        boolean dataValid = getStationData(); // Read station data

        if (DEBUG) {
            if (!dataValid) System.out.println("Dati non validi!");
            System.out.println("StationData >>> " + stationData.getHour() + ":" + stationData.getMinute() + ":" + stationData.getSecond() +
                    "\t" + stationData.getDay() + "-" + stationData.getMonth() + "-" + stationData.getYear());
            // stationData.print();
            System.out.println("--->> dataOfDay.printHeader()");
            dataOfDay.printHeader();
        }

        if (dataValid) {
            // Copy stationData to dataOfDay, but no dataArray
            saveLastStationDataToDataOfDay(-1);
            // Replace the timestamp of stationData with dataOfDay!!!
            dataOfDay.setHeader("", day, month, year, hour, minute);
            // Update files .csv and .js
            // SunData sunData: uses Calendar!!!
            updateFiles.updateEveryMinuteFiles(dataOfDay);
        }
    }

    /**
     * Manage new 15 minutes event
     *
     * @param arrayIndex
     */
    public void new15minuteEvent(int arrayIndex) {
        if (DEBUG) System.out.println(">>> DataManager.new15minuteEvent() <<<");
        // Update station data, data of day, files Csv and Js
        // Update files: data.xml, data.htm, ddmmyyyy.xml, ddmmyyyy.htm
        saveLastStationDataToDataOfDay(arrayIndex); // Copy stationData to dataOfDay
        if (arrayIndex >= 0 && arrayIndex < SAMPLES_OF_DAY) {
            // Update files of 15 minutes
            // Sostituito updateFiles.update15MinutesFiles(dataOfDay) con:
            updateFiles.update15MinutesDataXml(dataOfDay);
            updateFiles.update15MinutesCurrentFiles(dataOfDay);
            updateFiles.update15MinutesFilesWithDate(dataOfDay);
        }
    }

    /**
     * Manage new day event
     *
     * @param yesterDay
     * @param yesterMonth
     * @param yesterYear
     * @param arrayIndex
     */
    public void newDayEvent(int yesterDay, int yesterMonth, int yesterYear, int arrayIndex) {
        if (DEBUG) System.out.println(">>> DataManager.newDayEvent() <<<");

        //--- YESTERDAY ---//
        // Update last record of the yesterday
        saveLastStationDataToDataOfDay(SAMPLES_OF_DAY);

        // Rainfall now is equal to zero: retrieve the last data ???
        dataOfDay.setRain_all(lastRainfallOfDay);
        dataOfDay.setRawOfArray(SAMPLES_OF_DAY); // Meglio ripetere!?

        // Get rainAll of the month and the year
        updateFiles.getAllRain(yesterDay, yesterMonth, yesterYear);

        // Don't use dataOfDay.setHeader()
        dataOfDay.setDay(yesterDay);
        dataOfDay.setMonth(yesterMonth);
        dataOfDay.setYear(yesterYear);
        dataOfDay.setLastSample(SAMPLES_OF_DAY);

        // write file ddmmyyyy + ".xml" to rootOfXmlPath/yyyy/mm/
        // write file ddmmyyyy.htm to rootOfHtmlPath/yyyy/mm/
        // write File MeteoNetwork to rootOfHtmlPath
        updateFiles.update15MinutesDataXml(dataOfDay);
        updateFiles.update15MinutesFilesWithDate(dataOfDay); // Yesterday

        //------ NEW DAY ------//
        // Inizializza i dati del nuovo giorno
        lastRainfallOfDay = 0; // Reset
        dataOfDay.clearAllData(); // Cancel all data of DataOfDay
        dataOfDay.clearMinMax(); // Ecc...

        dataOfDay.setHeader("", day, month, year, 0, 0);
        saveLastStationDataToDataOfDay(0); // Copy stationData to first index of dataOfDay
        updateFiles.update15MinutesDataXml(dataOfDay); // write DATA_NAME + ".xml" to rootOfXmlPath
        updateFiles.update15MinutesCurrentFiles(dataOfDay);
        updateFiles.update15MinutesFilesWithDate(dataOfDay);
    }

    /**
     * Update all file of the month (mmyyyy.xml, mmyyyy.htm)
     *
     * @param yesterDay
     * @param yesterMonth
     * @param yesterYear
     */
    public void updateMonthEvent(int yesterDay, int yesterMonth, int yesterYear) {
        if (DEBUG) System.out.println(">>> DataManager.updateMonthEvent() <<<");
        // Write|Refresh month files mmyyyy.xml, mmyyyy.htm
        updateFiles.updateFileOfMonth(yesterDay, yesterMonth, yesterYear);
    }

    /**
     * Update all file of the year (yyyy.xml, yyyy.htm)
     *
     * @param yesterMonth
     * @param yesterYear
     */
    public void updateYearEvent(int yesterMonth, int yesterYear) {
        if (DEBUG) System.out.println(">>> DataManager.updateYearEvent() <<<");
        // Write|Refresh year files yyyy.xml, yyyy.htm
        updateFiles.updateFileOfYear(yesterMonth, yesterYear);
    }

    //--------------------------------------------------------

    /**
     * Read data from the station and update date and time
     *
     * @return true if data is ready
     */
    private boolean getStationData() {
        int syear = -1;
        int smonth = -1;
        int sday = -1;
        int shour = -1;
        int sminute = -1;
        int ssecond = -1;

        int tryCounter = 5; // MAX_TRY;
        boolean dataIsReady = false;
        do {
            // Read data from station
            stationInterface.clearStationData();
            stationInterface.getDataFromAllChannels(); // Update stationData

            // NB. I dati di direzione del vento e pioggia sono prelevati dalla Davis!!!
            if (typeOfData == COMBILOG) {
                new MergeDataFromStationDavis();
            }

            syear = stationData.getYear();
            smonth = stationData.getMonth();
            sday = stationData.getDay();
            shour = stationData.getHour();
            sminute = stationData.getMinute();
            ssecond = stationData.getSecond();

            dataIsReady = (syear > 0) && (smonth >= 0 && smonth <= 12) &&
                    (sday >= 0 && sday <= 31) && (shour >= 0 && shour <= 24) &&
                    (sminute >= 0 && sminute <= 59) && (ssecond >= 0 && ssecond <= 59);
            // The station day must be the same as the system day.
            dataIsReady = dataIsReady && (sday == day);

        } while (!dataIsReady && (--tryCounter > 0));

        if (dataIsReady) { // lastStationMinuteOfDay
            int sminutesOfDay = 60 * shour + sminute;
            dataIsReady = sminutesOfDay != lastStationMinuteOfDay;
            lastStationMinuteOfDay = sminutesOfDay;
        }

        return dataIsReady;
    }

    //===========================================================

    /**
     * Copy stationData to dataOfDay
     */
    protected void saveLastStationDataToDataOfDay(int arrayIndex) {
        //--- HEADER ---
        // Copy timestamp
        dataOfDay.setHeader("", day, month, year, hour, minute);

        //--- TEMPERATURE ---
        dataOfDay.setTemperature(stationData.getTemperature());

        //--- HUMIDITY ---
        dataOfDay.setHumidity(stationData.getHumidity());

        //--- PRESSURE ---
        dataOfDay.setPressure(stationData.getPressure());

        //--- WIND ---
        // Update windSpeed
        dataOfDay.setWindSpeed(stationData.getWindSpeed());

        // Update windDirection and windDirectionOfMaxSpeed
        dataOfDay.setWindDirection(stationData.getWindDirection());
        dataOfDay.setWindDirectionOfMaxSpeed(stationData.getWindDirectionOfMaxSpeed());

        //--- RAIN ---
        // For 1st record, rainfall = 0
        if (arrayIndex == 0) {
            dataOfDay.setRain_all(0);
            dataOfDay.setRainRateMax(0);
            dataOfDay.setRainRateMaxTime(-1);
        } else {
            dataOfDay.setRain_all(stationData.getRain()); //???????
            dataOfDay.calcRainRateMax(); // Update rainRateMax
        }

        // Reset lastRainfallOfDay when new day
        if (dataOfDay.getRain_all() > lastRainfallOfDay)
            lastRainfallOfDay = dataOfDay.getRain_all();

        //--- SUN RADIATION ---
        dataOfDay.setSunrad(stationData.getSunrad());

        // --- Update min-max and array of data ---
        if (arrayIndex < SAMPLES_OF_DAY) {
            // The update of the last record is done on 'newDayEvent'!
            //if (stationData.getTemperatureMin() < stationData.getTemperatureMax()) {
            if (stationData.getDay() == day && minutesOfDay > 7) {
                setMinMax(); // Update min-max values
            }

            dataOfDay.setRawOfArray(arrayIndex); // Update dataArray[]
        }
    }

    /**
     * Copy min-max value from stationData to dataOfDay
     */
    protected void setMinMax() {
        //--- TEMPERATURE ---
        // Check temperatureMinTime (some stations do not have a min time)
        if (stationData.getTemperatureMinTime() >= 0) {
            dataOfDay.setTemperatureMinTime(stationData.getTemperatureMinTime());
        } else {
            if (stationData.getTemperatureMin() < dataOfDay.getTemperatureMin() ||
                    dataOfDay.getTemperatureMinTime() < 0) {
                dataOfDay.setTemperatureMinTime(minutesOfDay);
            }
        }
        dataOfDay.setTemperatureMin(stationData.getTemperatureMin());

        // Check temperatureMaxTime (some stations do not have a max time)
        if (stationData.getTemperatureMaxTime() >= 0) {
            dataOfDay.setTemperatureMaxTime(stationData.getTemperatureMaxTime());
        } else {
            if (stationData.getTemperatureMax() > dataOfDay.getTemperatureMax() ||
                    dataOfDay.getTemperatureMaxTime() < 0) {
                dataOfDay.setTemperatureMaxTime(minutesOfDay);
            }
        }
        dataOfDay.setTemperatureMax(stationData.getTemperatureMax());

        //--- HUMIDITY ---
        // Check humidityMinTime (some stations do not have a min time)
        if (stationData.getHumidityMinTime() >= 0) {
            dataOfDay.setHumidityMinTime(stationData.getHumidityMinTime());
        } else {
            if (stationData.getHumidityMin() < dataOfDay.getHumidityMin() ||
                    dataOfDay.getHumidityMinTime() < 0) {
                dataOfDay.setHumidityMinTime(minutesOfDay);
            }
        }
        dataOfDay.setHumidityMin(stationData.getHumidityMin());

        // Check humidityMaxTime (some stations do not have a max time)
        if (stationData.getHumidityMaxTime() >= 0) {
            dataOfDay.setHumidityMaxTime(stationData.getHumidityMaxTime());
        } else {
            if (stationData.getHumidityMax() > dataOfDay.getHumidityMax() ||
                    dataOfDay.getHumidityMaxTime() < 0) {
                dataOfDay.setHumidityMaxTime(minutesOfDay);
            }
        }
        dataOfDay.setHumidityMax(stationData.getHumidityMax());

        //--- PRESSURE ---
        // Check pressureMinTime (some stations do not have a min time)
        if (stationData.getPressureMinTime() >= 0) {
            dataOfDay.setPressureMinTime(stationData.getPressureMinTime());
        } else {
            if (stationData.getPressureMin() < dataOfDay.getPressureMin() ||
                    dataOfDay.getPressureMinTime() < 0) {
                dataOfDay.setPressureMinTime(minutesOfDay);
            }
        }
        dataOfDay.setPressureMin(stationData.getPressureMin());

        // Check pressureMaxTime (some stations do not have a max time)
        if (stationData.getPressureMaxTime() >= 0) {
            dataOfDay.setPressureMaxTime(stationData.getPressureMaxTime());
        } else {
            if (stationData.getPressureMax() > dataOfDay.getPressureMax() ||
                    dataOfDay.getPressureMaxTime() < 0) {
                dataOfDay.setPressureMaxTime(minutesOfDay);
            }
        }
        dataOfDay.setPressureMax(stationData.getPressureMax());

        //--- WIND ---
        // Update windSpeed
        // Check windSpeedMaxTime (some stations do not have a max time)
        if (stationData.getWindSpeedMaxTime() >= 0) {
            dataOfDay.setWindSpeedMaxTime(stationData.getWindSpeedMaxTime());
        } else {
            if (stationData.getWindSpeedMax() > dataOfDay.getWindSpeedMax() ||
                    dataOfDay.getWindSpeedMaxTime() < 0) {
                dataOfDay.setWindSpeedMaxTime(minutesOfDay);
            }
        }
        dataOfDay.setWindSpeedMax(stationData.getWindSpeedMax());

        dataOfDay.setWindDirection(stationData.getWindDirection());
        dataOfDay.setWindDirectionOfMaxSpeed(stationData.getWindDirectionOfMaxSpeed());

        // ??? RAIN ???
        // Reset lastRainfallOfDay when new day

        //--- SUN RADIATION ---
        // Check SunradMaxTime (some stations do not have a max time)
        if (stationData.getSunradMaxTime() >= 0) {
            dataOfDay.setSunradMaxTime(stationData.getSunradMaxTime());
        } else {
            if (stationData.getSunradMax() > dataOfDay.getSunradMax() ||
                    dataOfDay.getSunradMaxTime() < 0) {
                dataOfDay.setSunradMaxTime(minutesOfDay);
            }
        }
        dataOfDay.setSunradMax(stationData.getSunradMax());
    }

    /**
     * Update system time
     *
     * @param day
     * @param month
     * @param year
     * @param hour
     * @param minute
     */
    public void setSystemTime(LocalDateTime now) {
        this.now = now;
        this.hour = now.getHour();
        this.minute = now.getMinute();
        this.day = now.getDayOfMonth();
        this.month = now.getMonthValue();
        this.year = now.getYear();
        this.minutesOfDay = 60 * hour + minute; // 0..1439
    }

    //----- Data recovery -----//

    /**
     * Simulation of data recovery from logger memory
     */
    public void simulRescueData() {
        int syear = -1;
        int smonth = -1;
        int sday = -1;
        int shour = -1;
        int sminute = -1;
        int ssecond = -1;
        boolean dataIsReady = false;

        // Today in ddmmyyyy format
        String ddmmyyyy = it.dibis.common.Utils.dateToString();

        // Convert format from ddmmyyyy to yymmdd
        String dateToRead = ddmmyyyy.substring(6, 8) + ddmmyyyy.substring(2, 4) + ddmmyyyy.substring(0, 2);

        // The 'stationData' object is temporary, the data is updated on dataOfDay
        StationData stationData = null;

        int last15minute = (((60 * hour + minute) / DELTA_TIME) % SAMPLES_OF_DAY) + 1;
        int loggedData = (last15minute >= 0 && last15minute < SAMPLES_OF_DAY) ? last15minute : -1;
        if (DEBUG) System.out.println("---> loggedData: " + loggedData);
        for (int i = 0; i < loggedData; i++) {
            stationInterface.clearStationData();

            // Load simulation data into a 'stationData' object
            stationData = SimulStationData.getNextStoredData(i, dateToRead);

            syear = stationData.getYear();
            smonth = stationData.getMonth();
            sday = stationData.getDay();
            shour = stationData.getHour();
            sminute = stationData.getMinute();
            ssecond = stationData.getSecond();
            dataIsReady = (syear > 0) && (smonth >= 0 && smonth <= 12) &&
                    (sday >= 0 && sday <= 31) && (shour >= 0 && shour <= 24) &&
                    (sminute >= 0 && sminute <= 59) && (ssecond >= 0 && ssecond <= 59);

            // Update only if dataIsReady && no data in dataOfDay array
            if (dataIsReady && dataOfDay.getDataArray(TEMPERATURE_INDEX, i) == TNODATA) {
                if (DEBUG) System.out.println("---> Index: " + i);

                // Update timeIndex of array with decoded time
                int timeIndex = ((60 * shour + sminute + 5) / DELTA_TIME) % SAMPLES_OF_DAY;
                timeIndex = (timeIndex >= 0 && timeIndex < SAMPLES_OF_DAY) ? timeIndex : -1;

                //--- HEADER ---
                dataOfDay.setHeader("", sday, smonth, syear, shour, sminute);

                //--- TEMPERATURE ---
                dataOfDay.setTemperature(stationData.getTemperature());
                dataOfDay.setTemperatureMin(stationData.getTemperatureMin());
                dataOfDay.setTemperatureMax(stationData.getTemperatureMax());
                dataOfDay.setTemperatureMinTime(stationData.getTemperatureMinTime());
                dataOfDay.setTemperatureMaxTime(stationData.getTemperatureMaxTime());

                //--- HUMIDITY ---
                dataOfDay.setHumidity(stationData.getHumidity());
                dataOfDay.setHumidityMin(stationData.getHumidityMin());
                dataOfDay.setHumidityMax(stationData.getHumidityMax());
                dataOfDay.setHumidityMinTime(stationData.getHumidityMinTime());
                dataOfDay.setHumidityMaxTime(stationData.getHumidityMaxTime());

                //--- PRESSURE ---
                dataOfDay.setPressure(stationData.getPressure());
                dataOfDay.setPressureMin(stationData.getPressureMin());
                dataOfDay.setPressureMax(stationData.getPressureMax());
                dataOfDay.setPressureMinTime(stationData.getPressureMinTime());
                dataOfDay.setPressureMaxTime(stationData.getPressureMaxTime());

                //--- WIND ---
                dataOfDay.setWindSpeed(stationData.getWindSpeed());
                dataOfDay.setWindSpeedMax(stationData.getWindSpeedMax());
                dataOfDay.setWindSpeedMaxTime(stationData.getWindSpeedMaxTime()); // NON carica!!!

                float wdir = dataOfDay.getDataArray(WINDDIR_INDEX, timeIndex); // i = timeIndex
                float wdirmax = dataOfDay.getWindDirectionOfMaxSpeed();
                dataOfDay.setWindDirection(wdir);
                dataOfDay.setWindDirectionOfMaxSpeed(wdirmax);

                //--- RAIN ---
                // For 1st record, rainfall = 0
                if (timeIndex > 0) {
                    dataOfDay.setRain_all(stationData.getRain());
                    dataOfDay.calcRainRateMax(); // Update rainRateMax
                } else {
                    dataOfDay.setRain_all(0);
                    dataOfDay.setRainRateMax(0);
                    dataOfDay.setRainRateMaxTime(-1);
                }

                // Reset lastRainfallOfDay when new day
                if (dataOfDay.getRain_all() > lastRainfallOfDay)
                    lastRainfallOfDay = dataOfDay.getRain_all();

                //--- SUN RADIATION ---
                dataOfDay.setSunrad(stationData.getSunrad());
                dataOfDay.setSunradMax(stationData.getSunradMax());
                dataOfDay.setSunradMaxTime(stationData.getSunradMaxTime());

                // --- Update record of array ---
                if (timeIndex >= 0) {
                    // Copy the current data into the array at the last location 'timeIndex'
                    dataOfDay.setRawOfArray(timeIndex);
                }
            }
        }
    }

    /**
     * Retrieves all rows of data from the memory of the logger
     * <p>
     * >>> Errori???
     * Tmin = -1
     * Wspeed manca l'orario, dir media diversa
     * stationData.getWindSpeedMaxTime(); // NON carica!!!
     * Per direzione_vento e pioggia BISOGNA LASCIARE quelli di data.xml
     */
    public void rescueDataFromCombilog() {
        int syear = -1;
        int smonth = -1;
        int sday = -1;
        int shour = -1;
        int sminute = -1;
        int ssecond = -1;
        boolean dataIsReady = false;

        // Today in ddmmyyyy format
        String ddmmyyyy = it.dibis.common.Utils.dateToString();

        // Convert format from ddmmyyyy to yymmdd
        String dateToRead = ddmmyyyy.substring(6, 8) + ddmmyyyy.substring(2, 4) + ddmmyyyy.substring(0, 2);

        StationData stationData = null;

        int loggedData = stationInterface.getNumberOfStoredData(dateToRead);
        for (int i = 0; i < loggedData; i++) {
            stationInterface.clearStationData();

            // Read stored data from station
            stationData = stationInterface.getNextStoredData(dateToRead);

            syear = stationData.getYear();
            smonth = stationData.getMonth();
            sday = stationData.getDay();
            shour = stationData.getHour();
            sminute = stationData.getMinute();
            ssecond = stationData.getSecond();
            dataIsReady = (syear > 0) && (smonth >= 0 && smonth <= 12) &&
                    (sday >= 0 && sday <= 31) && (shour >= 0 && shour <= 24) &&
                    (sminute >= 0 && sminute <= 59) && (ssecond >= 0 && ssecond <= 59);

            // Update only if dataIsReady && no data in dataOfDay array
            if (dataIsReady && dataOfDay.getDataArray(TEMPERATURE_INDEX, i) == TNODATA) {
                // Update index of array with rescue time
                int timeIndex = ((60 * shour + sminute + 5) / DELTA_TIME) % SAMPLES_OF_DAY;
                timeIndex = (timeIndex >= 0 && timeIndex < SAMPLES_OF_DAY) ? timeIndex : -1;

                //--- HEADER ---
                dataOfDay.setHeader("", sday, smonth, syear, shour, sminute);

                //--- TEMPERATURE ---
                dataOfDay.setTemperature(stationData.getTemperature());
                dataOfDay.setTemperatureMin(stationData.getTemperatureMin());
                dataOfDay.setTemperatureMax(stationData.getTemperatureMax());
                dataOfDay.setTemperatureMin(stationData.getTemperatureMinTime());
                dataOfDay.setTemperatureMax(stationData.getTemperatureMaxTime());

                //--- HUMIDITY ---
                dataOfDay.setHumidity(stationData.getHumidity());
                dataOfDay.setHumidityMin(stationData.getHumidityMin());
                dataOfDay.setHumidityMax(stationData.getHumidityMax());
                dataOfDay.setHumidityMin(stationData.getHumidityMinTime());
                dataOfDay.setHumidityMax(stationData.getHumidityMaxTime());

                //--- PRESSURE ---
                dataOfDay.setPressure(stationData.getPressure());
                dataOfDay.setPressureMin(stationData.getPressureMin());
                dataOfDay.setPressureMax(stationData.getPressureMax());
                dataOfDay.setPressureMin(stationData.getPressureMinTime());
                dataOfDay.setPressureMax(stationData.getPressureMaxTime());

                //--- WIND ---
                dataOfDay.setWindSpeed(stationData.getWindSpeed());
                dataOfDay.setWindSpeedMax(stationData.getWindSpeedMax());
                dataOfDay.setWindSpeedMaxTime(stationData.getWindSpeedMaxTime()); // NON carica!!!

                /*
                Il sensore di direzione del Combilog e' guasto!!!
                dataOfDay.setWindDirection(stationData.getWindDirection());
                dataOfDay.setWindDirectionOfMaxSpeed(stationData.getWindDirectionOfMaxSpeed(());
                */

                //--- RAIN ---
                // For 1st record, rainfall = 0
                if (timeIndex > 0) {
                    // Il pluviometro e' stato scollegato dal Combilog
                    // dataOfDay.setRain_all(stationData.getRain());
                    // dataOfDay.calcRainRateMax(); // Update rainRateMax
                } else {
                    dataOfDay.setRain_all(0);
                    dataOfDay.setRainRateMax(0);
                    dataOfDay.setRainRateMaxTime(-1);
                }

                // Reset lastRainfallOfDay when new day
                if (dataOfDay.getRain_all() > lastRainfallOfDay)
                    lastRainfallOfDay = dataOfDay.getRain_all();

                //--- SUN RADIATION ---
                dataOfDay.setSunrad(stationData.getSunrad());
                dataOfDay.setSunradMaxTime(stationData.getSunradMaxTime());

                // --- Update record of array ---
                if (timeIndex >= 0) {
                    // Copy the current data into the array at the last location 'timeIndex'
                    dataOfDay.setRawOfArray(timeIndex);
                }
            }
        }
    }

    //====== No usage ======//

    /**
     * Synch date and time with PC clock
     * NB. DISATTIVATO!?
     */
    private int synchDataTime() {
        return stationInterface.synchDataTime();
    }

    /**
     * Reset error
     */
    private void resetError() {
        this.error = 0;
        /// stationInterface.resetError();
    }

}

