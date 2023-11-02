package it.dibis.station;

import it.dibis.common.Utils;
import it.dibis.dataObjects.SharedData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/*
    Aggiunto provvisoriamente il metodo 'saveJsonFile(String alldata)' per il debug
    Come orario viene utilizzato quello della stazione anziche' quello del PC
 */
public class StationDavisJson extends StationInterface {

    //--- Constants ---//

    // Revision control id
    public static final String CVSID = "$Id: StationDavisJson.java,v 0.3 02/11/2023 23:59:59 adalborgo@gmail.com $";

    static final boolean DEBUG = false;

    final String UTC_TIMESTAMP_KEY = "utctime";  // Unix utc time and date
    final String LOCAL_TIMESTAMP_KEY = "loctime";  // Unix local time and date

    final String TEMPERATURE_KEY = "tempout";  // Temperatura istantanea
    final String TEMPERATURE_MINMAX_KEY  = "hltempout"; // Temperatura minima e massima

    final String HUMIDITY_KEY = "humout";  // Umidita' istantanea
    final String HUMIDITY_MINMAX_KEY = "hlhumout"; // Umidita' minima

    final String PRESSURE_KEY = "bar";  // Pressione istantanea
    final String PRESSURE_MINMAX_KEY = "hlbar"; // Pressione minima

    final String WINDSPEED_KEY = "windspd"; // Vento velocita' istantanea
    final String WINDDIR_KEY = "winddir"; // Vento direzione
    final String WINDSPEED_MAX_KEY = "gust"; // Vento velocita' massima
    final String WINDDIR_MAX_KEY = "gustdir"; // Vento direzione della velocita' massima
    // hlwind ???
    //final String WINDSPEED_MAX_TIME_KEY = ""; //
    final String WIND_OTHER_KEY = "hlwind"; // ???

    final String RAINALL_KEY = "raind"; // Pioggia cumulata
    final String RAINMON_KEY = "rainmon"; //
    final String RAINYEAR_KEY = "rainyear"; //

    // hlrain ???
    final String SUNRAD_KEY = "solar";  // Radiazione solare
    final String SUNRAD_MAX_KEY = "hlsolar"; // Radiazione solare massima

    final String PATHNAME = "wflexp.json";

    String alldata = null;

    int index = 0;
    float x = 0;
    int time = 0;

    /**
     * Carica i dati istantanei da ogni singolo canale del logger e li carica nell'oggetto stationData
     */
    public void getDataFromAllChannels() {
        stationData.clear(); // ClearAllData stationData
        alldata = dataLoad(loggerPort); // URL_TO_READ
        if (alldata!=null && alldata.length()>3000) {
            if (DEBUG) System.out.println(alldata);

            // Date & Time
            if (getTimestamp()) { // Data is valid
                // Temperature
                getTemperature();

                // Humidity
                getHumidity();

                // Pressure
                getPressure();

                // Wind
                getWind();

                // Rain all
                getRain();

                // Solar radiation
                getSunrad();

                //stationData.print();
            }
        } else {
            if (DEBUG) saveJsonFile(alldata); // For DEBUG
        }
    }

    /**
     * Get time of data
     *
     * @return (false if error)
     */
      private boolean getTimestamp() {
        index = 0;
        // LOCAL_TIMESTAMP_KEY o UTC_TIMESTAMP_KEY???
        //String localTimestamp = getSingleField(LOCAL_TIMESTAMP_KEY); // unixTime
        String localTimestamp = getSingleField(UTC_TIMESTAMP_KEY); // unixTime
        long unixTime = Utils.stringToLong(localTimestamp);
        // On error, unixTime = Long.MIN_VALUE
        if (unixTime<0) {
            stationData.setHour(-1);
            stationData.setMinute(-1);
            stationData.setSecond(-1); // Not used!
            stationData.setDay(-1);
            stationData.setMonth(-1);
            stationData.setYear(-1);
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(unixTime*1000L);
        stationData.setCalendar(calendar);
        // System.out.println("StationDavisJson Calendar: " + CalendarUtils.getDate(calendar, true));

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        stationData.setHour(hour);
        stationData.setMinute(minute);
        stationData.setSecond(second); // Not used!

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        stationData.setDay(day);
        stationData.setMonth(month);
        stationData.setYear(year);
        return true;
    }

    private void getTemperature() {
        // 0:tmin; 1:tmax; 2:time_min; 3:time_max; 4:max_mese; 5:min_mese; 6:max_anno; 7:min_anno
        index = 0;
        x = temperatureConvert(getSingleField(TEMPERATURE_KEY));
        stationData.setTemperature(x);

        String[] tempMinMax = getFullField(TEMPERATURE_MINMAX_KEY);
        x = temperatureConvert(tempMinMax[0]);
        stationData.setTemperatureMin(x);
        x = temperatureConvert(tempMinMax[1]);
        stationData.setTemperatureMax(x);

        time = getTime(tempMinMax[2]);
        stationData.setTemperatureMinTime(time);
        time = getTime(tempMinMax[3]);
        stationData.setTemperatureMaxTime(time);
    }

    private void getHumidity() {
        index = 0;
        x = s2f(getSingleField(HUMIDITY_KEY));
        stationData.setHumidity(x);

        String[] humiMinMax = getFullField(HUMIDITY_MINMAX_KEY);
        x = s2f(humiMinMax[0]);
        stationData.setHumidityMin(x);
        x = s2f(humiMinMax[1]);
        stationData.setHumidityMax(x);

        time = getTime(humiMinMax[2]);
        stationData.setHumidityMinTime(time);
        time = getTime(humiMinMax[3]);
        stationData.setHumidityMaxTime(time);
    }

    private void getWind() {
          /*
        stationData.setWindSpeedMax(windConvert(getSingleField(WINDSPEED_MAX_KEY))); // gust
        stationData.setWindDirectionOfMaxSpeed(s2f(getSingleField( WINDDIR_MAX_KEY))); // gustdir
           */
        index = 0;
        x = windConvert(getSingleField(WINDSPEED_KEY));
        stationData.setWindSpeed(x); // windspeed

        x = s2f(getSingleField(WINDDIR_KEY));
        stationData.setWindDirection(x); // windir

        // The direction of the gusts presents some problems?!
        x = s2f(getSingleField( WINDDIR_MAX_KEY));
        stationData.setWindDirectionOfMaxSpeed(x); // gustdir

        /*
        WindSpeedMax: Which is the correct one?
        stationData.setWindSpeedMax(windConvert(getSingleField(WINDSPEED_MAX_KEY))); // gust
         */
        // WindSpeedMax
        String[] windOther = getFullField(WIND_OTHER_KEY);
        x = windConvert(windOther[1]);
        stationData.setWindSpeedMax(x); // WindSpeedMax

        time = getTime(windOther[3]);
        stationData.setWindSpeedMaxTime(time); // WindSpeedMaxTime
        // System.out.println(windOther[3]);

        // WindOther
        //System.out.println("windOther[1]: " + windConvert(windOther[1])*3.6f); // Max day?
        //System.out.println("windOther[4]: " + windConvert(windOther[4])*3.6f); // Max month?
        //System.out.println("windOther[6]: " + windConvert(windOther[6])*3.6f); // Max year?
        // WindSpeed in mph
        // return 0.44704F*x; // mph -> m/s
        // return 1.60934F*x; // mph -> km/h
        // System.out.println("WindSpeedMax(): " + stationData.getWindSpeedMax());
    }

    private void getRain() { // Rain all
        index = 0;
        float x = s2f(getSingleField(RAINALL_KEY));
        stationData.setRain((x>=0) ? 25.4F*x : -1); // Convert to mm
        /*
        x = s2f(getSingleField(RAINMON_KEY));
        System.out.println("211 RAINMON: " + ((x>=0) ? 25.4F*x : -1));

        x = s2f(getSingleField(RAINYEAR_KEY));
        System.out.println("214 RAINYEAR: " + ((x>=0) ? 25.4F*x : -1));
        */
    }

    private void getSunrad() { // Solar Radiation
        // Dati non assegnati
        stationData.setSunrad(-1);
        stationData.setSunradMax(-1);
        stationData.setSunradMaxTime(-1);
    }

    private void getPressure() {
        index = 0;
        x = pressureConvert(getSingleField(PRESSURE_KEY));
        stationData.setPressure(x);

        String[] pressMinMax = getFullField(PRESSURE_MINMAX_KEY);
        stationData.setPressureMin(pressureConvert(pressMinMax[0]));
        stationData.setPressureMax(pressureConvert(pressMinMax[1]));

        time = getTime(pressMinMax[2]);
        stationData.setPressureMinTime(time);
        time = getTime(pressMinMax[3]);
        stationData.setPressureMaxTime(time);
    }

    private String getSingleField(String key) {
        String arg = null;
        int pnt = alldata.indexOf("\"" + key + "\"" ,  index);
        int keylen = key.length() + 2; // Add quote
        int pntL = alldata.indexOf(":" ,  pnt + keylen) +1;
        int pntR = alldata.indexOf("," ,  pntL);
        arg = alldata.substring(pntL, pntR).replaceAll("\"","").trim();
        if (DEBUG) System.out.println("arg: " + arg);

        index = pntR; // Update index

        return arg;
    }

    private String [] getFullField(String key) {
        int pnt = alldata.indexOf("\"" + key + "\":" , index);
        int pntL = alldata.indexOf("[" ,  pnt) + 1;
        int pntR = alldata.indexOf("]" ,  pntL);
        String arg = alldata.substring(pntL, pntR); // Skip []
        //System.out.println("getFullField: " + arg);
        //String[] items = getSubField(arg);
        //for (int i=0; i<items.length; i++) { System.out.println(items[i]); }

        String[] items = null;
        if (arg.contains(",")) {
            String[] tmp = arg.split(",");
            items = new String[tmp.length];
            for (int i=0; i<items.length; i++) {
                items[i] = tmp[i].replaceAll("\"","");
            }
        }

        index = pntR; // Update index

        return items;
    }

    private float temperatureConvert(String s) {
        float f = s2f(s);
        if (f>-1000) return (f - 32) * 5/9;
        else return f;
    }

    private float pressureConvert(String s) {
        float x = s2f(s);
        if (x>0) return 33.8637526F*x;
        else return x;
    }

    private float windConvert(String s) {
        // // 1 nodo = 0,514444 m/s = 1,852 km/h
        // miglio orario = 0,44704 m/s = 1,60934 km/h
        float x = s2f(s);
        //if (x>0) return 1.60934F*x; // mph -> km/h
        if (x>0) return 0.44704F*x; // mph -> m/s
        else return x;
    }

    private float s2f(String s) {
        if (s.equals("---")) return -Float.MAX_VALUE;
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            return -Float.MAX_VALUE;
        }
    }

    ////////////////

    /**
     * Save the json file every 5 minutes (only for DEBUG!!!)
     * @param alldata
     */
    private void saveJsonFile(String alldata) {

        SharedData shared = SharedData.getInstance();

        String rootOfHtmlPath = shared.getRootOfHtmlPath();

        SimpleDateFormat fmt = new SimpleDateFormat("ddMMyyyy-HH_mm");
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);
        if (minute%5!=0) return;
        fmt.setCalendar(calendar);
        String datePath = fmt.format(calendar.getTime());
        String jsonSavePathName = rootOfHtmlPath + datePath + ".json";

        //System.out.println("jsonSavePathName: " + jsonSavePathName);
        //System.out.println("alldata: " + alldata);

        BufferedWriter outbuf = null;
        try {
            outbuf = new BufferedWriter(new FileWriter(jsonSavePathName));
            outbuf.write(alldata);
            outbuf.close(); // Close file
        } catch  (IOException e) {
            System.out.println("Error: " + e);
        }

    }

    // --- Method abstract in StationInterface, here not used ---//
    public int open() { return 0; }
    public int synchDataTime() { return 0; }
    public StationData getNextStoredData(String yymmdd) { return null; }  // Not used
    public int getNumberOfStoredData(String yymmdd) { return 0; }  // Not used
}

