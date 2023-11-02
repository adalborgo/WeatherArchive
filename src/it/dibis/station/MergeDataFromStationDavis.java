package it.dibis.station;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import it.dibis.common.Utils;
import it.dibis.dataObjects.SharedData;

/*
  Recupero dati dalla stazione Davis per guasti dalla vecchia stazione:
  - direzione del vento (03/10/21)
  - pioggia cumulata (01/11/2021)
  Vedi: loadAlldataFromUrl(urlToRead);
 */
public class MergeDataFromStationDavis extends StationInterface {

    //--- Constants ---//
    /**
     *  Revision control id
     */
    public static final String CVSID = "$Id: StationDavisJson.java,v 0.3 02/11/2023 23:59:59 adalborgo@gmail.com $";

    final String TIMESTAMP_KEY = "utctime";  // Current time and date

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
    // hlrain ???
    final String SUNRAD_KEY = "solar";  // Radiazione solare
    final String SUNRAD_MAX_KEY = "hlsolar"; // Radiazione solare massima

    SharedData shared = SharedData.getInstance();

    final String urlToRead = shared.getDavisPort();
    //"http://www.meteoproject.it/ftp/stazioni/faenza/wflexp.json";

    String alldata = null;

    int index = 0;

    /*
     * Correct 'Wind direction & Rain all' from Json file (Davis station)
     */
    public MergeDataFromStationDavis() {
        loadAlldataFromUrl(urlToRead);
        // System.out.println("CorrectData - Davis-alldata: "+ alldata);
        if (alldata!=null && alldata.length()>3000) {
            // see: getWind();
            getOnly_WindDirection(); //--> Sostituisci Direzione vento

            // see: getRain()
            getRain(); //--> Sostituisci Pioggia

            //--- Provvisorio per sostituzione capannina!!! ---//
            /*/--> Sostituisci temperatura
            getTemperature();

            //--> Sostituisci umidita'
            getHumidity();
             */
            //-------------------------------------------------//
        }
        //stationData.print();
    }

    /**
     * Carica i dati istantanei da ogni singolo canale del logger e
     *  li carica nell'oggetto stationData
     */
    public void loadAlldataFromUrl(String urlToRead) {
        URL url = null;
        String inputLine=null;
        StringBuffer data = new StringBuffer();
        try {
            url = new URL(urlToRead);
        } catch (MalformedURLException e) {
            System.out.println("Bad URL: " + e.getMessage());
            alldata = "";
            return;
        }

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((inputLine = in.readLine()) != null) data.append(inputLine);
            in.close();
        } catch (Exception e) {
            System.err.println("Exception error: " + e.toString());
        }

        //return data.toString();
        alldata = data.toString();
    }

    private void getTimestamp() {
        index = 0;

        String timestamp = getSingleField(TIMESTAMP_KEY);
        long unixTime = Utils.stringToLong(timestamp);
        // Integer.parseInt(timestamp); // Manca il Controllo delle eccezioni!

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(unixTime*1000L);
        //System.out.println("Calendar: " + CalendarUtils.getDate(calendar));

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
    }

    private void getTemperature() {
        index = 0;

        stationData.setTemperature(temperatureConvert(getSingleField(TEMPERATURE_KEY)));

        String[] tempMinMax = getFullField(TEMPERATURE_MINMAX_KEY);
        stationData.setTemperatureMin(temperatureConvert(tempMinMax[0]));
        stationData.setTemperatureMax(temperatureConvert(tempMinMax[1]));
        stationData.setTemperatureMinTime(getTime(tempMinMax[2]));
        stationData.setTemperatureMaxTime(getTime(tempMinMax[3]));
    }

    private void getHumidity() {
        index = 0;

        stationData.setHumidity(s2f(getSingleField(HUMIDITY_KEY)));

        String[] humiMinMax = getFullField(HUMIDITY_MINMAX_KEY);
        stationData.setHumidityMin(s2f(humiMinMax[0]));
        stationData.setHumidityMax(s2f(humiMinMax[1]));
        stationData.setHumidityMinTime(getTime(humiMinMax[2]));
        stationData.setHumidityMaxTime(getTime(humiMinMax[3]));
    }

    private void getWind() {
        index = 0;

        float x = windConvert(getSingleField(WINDSPEED_KEY));
        stationData.setWindSpeed(x); // windspeed

        x = s2f(getSingleField(WINDDIR_KEY));
        stationData.setWindDirection(x); // windir

        // The direction of the gusts presents some problems?!
        x = s2f(getSingleField(WINDDIR_MAX_KEY));
        stationData.setWindDirectionOfMaxSpeed(x); // gustdir

        /*
        WindSpeedMax: Which is the correct one?
        stationData.setWindSpeedMax(windConvert(getSingleField(WINDSPEED_MAX_KEY))); // gust
         */
        // WindSpeedMax
        String[] windOther = getFullField(WIND_OTHER_KEY);
        x = windConvert(windOther[1]);
        stationData.setWindSpeedMax(x); // WindSpeedMax

        int time = getTime(windOther[3]);
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

    private void getOnly_WindDirection() {
        index = 0;

        float x = s2f(getSingleField(WINDDIR_KEY));
        stationData.setWindDirection(x); // windir

        // The direction of the gusts presents some problems?!
        x = s2f(getSingleField(WINDDIR_MAX_KEY));
        stationData.setWindDirectionOfMaxSpeed(x); // gustdir
    }

    private void getRain() { // Rain all
        index = 0;

        float x = s2f(getSingleField(RAINALL_KEY));
        stationData.setRain((x>=0) ? 25.4F*x : -1); // Convert to mm
    }

    private void getSunrad() { // Solar Radiation
        // Dati non assegnati
        stationData.setSunrad(-1);
        stationData.setSunradMax(-1);
        stationData.setSunradMaxTime(-1);
    }

    private void getPressure() {
        index = 0;
        stationData.setPressure(pressureConvert(getSingleField(PRESSURE_KEY)));

        String[] pressMinMax = getFullField(PRESSURE_MINMAX_KEY);
        stationData.setPressureMin(pressureConvert(pressMinMax[0]));
        stationData.setPressureMax(pressureConvert(pressMinMax[1]));
        stationData.setPressureMinTime(getTime(pressMinMax[2]));
        stationData.setPressureMaxTime(getTime(pressMinMax[3]));
    }

    private String getSingleField(String key) {
        String arg = null;
        int pnt = alldata.indexOf("\"" + key + "\"" ,  index);
        int keylen = key.length() + 2; // Add quote
        int pntL = alldata.indexOf(":" ,  pnt + keylen) +1;
        int pntR = alldata.indexOf("," ,  pntL);
        arg = alldata.substring(pntL, pntR).replaceAll("\"","").trim();
        // System.out.println("arg: " + arg);

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
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            return -Float.MAX_VALUE;
        }
    }

    // --- Method abstract in StationInterface, here not used ---//
    public void getDataFromAllChannels() { }

    public int open() { return 0; }
    public int synchDataTime() { return 0; }
    public StationData getNextStoredData(String yymmdd) { return null; }  // Not used
    public int getNumberOfStoredData(String yymmdd) { return 0; }  // Not used
}
