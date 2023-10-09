package it.dibis.config;

import it.dibis.common.CalendarUtils;
import it.dibis.common.Constants;
import it.dibis.common.Utils;
import it.dibis.dataObjects.SharedData;

import java.text.DateFormat;
import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * GetConfig.java
 * Get all configuration info
 *
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 * <p>
 * For debug: java CmdLine -teststation
 * <p>
 * La classe carica i dati di 2 file di configurazione:
 * 1. station.config: informazioni sulla stazione, sulla modalita' di acquisizione dati e sul percorso dei file
 * 2. html.config: informazioni localizzate per la creazione delle pagine html
 */
public class GetConfig implements Constants {

    // Revision control id
    public static final String CVSID = "$Id: GetConfig.java,v 0.19 27/09/2023 23:53 adalborgo@gmail.com $";

    private Properties properties = new Properties();

    // Range of values
    private static float[] minValues = new float[DATA_TYPE];
    private static float[] maxValues = new float[DATA_TYPE];

    // Calibration
    private static float[] offset = new float[DATA_TYPE];
    private static float[] factor = new float[DATA_TYPE];

    //--- HTML ---//
    private static float[] unitFactor = new float[DATA_TYPE];
    private static String[] unitSymbol = null;

    private int error = 0;

    SharedData shared = SharedData.getInstance();
    ConfigHtml configHtml = ConfigHtml.getInstance();

    /**
     * Constructor
     */
    public GetConfig() {
        error = loadStationConfig();
        error = makePaths();
        if (shared.getSaveHtml()) error = loadHtmlConfig();
    }

    public SharedData getConfigData() {
        return shared;
    }

    public ConfigHtml getConfigHtml() {
        return configHtml;
    }

    public void printConfigData() {
        System.out.println(shared);
    }

    public void printConfigHtml() {
        System.out.println(configHtml);
    }

    /**
     * @return error
     */
    public int getError() {
        return error;
    }

    /**
     * Load and check configuration parameters of the station
     *
     * @return error
     */
    private int loadStationConfig() {

        error = 0; // Default

        boolean isOpen = open(STATION_CONFIG_FILENAME, false);
        if (isOpen) {

            // Logger port
            shared.setLoggerPort(getValue("loggerPort"));

            // Davis port
            shared.setDavisPort(getValue("davisPort"));

            // Device number
            shared.setDeviceNumber(getValue("deviceNumber"));
            if (shared.getDeviceNumber().length() == 1) {
                shared.setDeviceNumber("0" + shared.getDeviceNumber());
            } else if (shared.getDeviceNumber().length() != 2 || shared.getLoggerPort() == null) {
                error = ERROR_LOGGER;
            }

            // Type of data file
            try {
                int typeOfData = Integer.parseInt(getValue("typeOfData"));
                shared.setTypeOfData(typeOfData);
            } catch (NumberFormatException e) {
                System.out.println("TypeOfData error: " + e);
            }

            // Get station Id
            shared.setStationId(getValue("stationId"));

            // Get MeteoNetwork Id
            shared.setMeteoNetworkId(getValue("MeteoNetworkId"));

            // Latitude, longitude, altitude, coordinates
            try {
                shared.setLatitude(Float.parseFloat(getValue("latitude")));
                shared.setLongitude(Float.parseFloat(getValue("longitude")));
                shared.setAltitude(Float.parseFloat(getValue("altitude")));

                shared.setCoordinates(true);

            } catch (NumberFormatException e) {
                shared.setCoordinates(false);
                System.out.println("Coordinates error: " + e);
            }

            // set zoneId
            shared.setZoneId(getValue("zoneId"));

            // Set timezoneJS
            String stz = getValue("timezoneJS");
            shared.setTimeZoneJS(CalendarUtils.getTimeZone(stz));

            try {
                // Get minValues
                String[] minStr = getValue("minValues").split(",");
                for (int i = 0; i < DATA_TYPE; i++) {
                    if (i < minStr.length) minValues[i] = Float.parseFloat(minStr[i].trim());
                }

                shared.setMinValues(minValues);
                if (minValues == null) error += ERROR_MINMAX;

                // Get maxValues
                String[] maxStr = getValue("maxValues").split(",");
                for (int i = 0; i < DATA_TYPE; i++) {
                    if (i < maxStr.length) maxValues[i] = Float.parseFloat(maxStr[i].trim());
                }

                shared.setMaxValues(maxValues);
                if (maxValues == null) error += ERROR_MINMAX;

                // Calibration: value = factor * x + factor
                String[] offsetStr = getValue("offset").split(",");
                for (int i = 0; i < DATA_TYPE; i++) {
                    if (i < offsetStr.length) offset[i] = Float.parseFloat(offsetStr[i].trim());
                }

                shared.setOffset(offset);
                if (offset == null) error += ERROR_CALIBRATION;

                // Get factor
                String[] factorStr = getValue("factor").split(",");
                for (int i = 0; i < DATA_TYPE; i++) {
                    if (i < factorStr.length) factor[i] = Float.parseFloat(factorStr[i].trim());
                }

                shared.setFactor(factor);
                if (factor == null) error += ERROR_CALIBRATION;

                // Files to save: saveJs, saveCsv, saveHtml, saveGraph
                try {
                    boolean saveJs = Boolean.parseBoolean(getValue("saveJs"));
                    shared.setSaveJs(saveJs);

                    boolean saveCsv = Boolean.parseBoolean(getValue("saveCsv"));
                    shared.setSaveCsv(saveCsv);

                    boolean saveHtml = Boolean.parseBoolean(getValue("saveHtml"));
                    shared.setSaveHtml(saveHtml);

                    boolean saveGraph = Boolean.parseBoolean(getValue("saveGraph"));
                    shared.setSaveGraph(saveGraph);

                } catch (NumberFormatException e) {
                    System.out.println("Format error (saveJs, saveCsv, saveHtml, saveGraph): " + e);
                }

                // --- Get paths ---
                // Root of data path
                shared.setRootOfPath(getValue("rootOfPath"));
                if (shared.getRootOfPath() == null || shared.getRootOfPath().length() < 2) {
                    System.out.println("Error: rootOfPath!");
                    return ERROR_PATH_NOT_FOUND;
                }
            } catch (NullPointerException e) {
                error += ERROR_CONFIG_SEVERE;
                System.out.println("Exception: " + e);
            }

        } else {
            error = ERROR_CONFIG_FILENAME_NOT_FOUND;
            System.out.print("File " + STATION_CONFIG_FILENAME + " not found!");

            return error;
        }

        return error;
    }

    /**
     * Upload localized information to create htm pages
     *
     * @return error
     */
    private int loadHtmlConfig() {

        error = 0; // Default

        boolean isOpen = open(HTML_CONFIG_FILENAME, false);
        if (isOpen) {

            // Get Locale
            String sLocale = getValue("LOCALE");
            Locale[] lList = DateFormat.getAvailableLocales();
            boolean found = false;
            for (Locale locale : lList) {
                if (locale.toString().equals(sLocale)) {
                    found = true;
                    configHtml.setLocale(locale);
                    break;
                }
            }

            // If LOCALE not found, set default
            if (!found) configHtml.setLocale(Locale.getDefault());

            try {
                // Get unitFactor
                String[] unitFactorStr = getValue("unitFactor").split(",");
                for (int i = 0; i < DATA_TYPE; i++) {
                    if (i < unitFactorStr.length) unitFactor[i] = Float.parseFloat(unitFactorStr[i].trim());
                }

                configHtml.setUnitFactor(unitFactor);

                // Get unitSymbol
                unitSymbol = getValue("unitSymbol").split(",");
                configHtml.setUnitSymbol(unitSymbol);

                if (unitFactor == null || unitSymbol == null) {
                    System.out.print("Error: unitFactor or unitSymbol not found\n");
                    error += ERROR_UNIT;
                }

            } catch (NullPointerException e) {
                error += ERROR_CONFIG_SEVERE;
                System.out.println("Exception: " + e);
            }

            // Titles
            configHtml.setDailyTitle(getValue("dailyTitle"));
            configHtml.setMonthlyTitle(getValue("monthlyTitle"));
            configHtml.setYearlyTitle(getValue("yearlyTitle"));

            // Names of data
            configHtml.setTemperature(getValue("temperature"));
            configHtml.setHumidity(getValue("humidity"));
            configHtml.setPressure(getValue("pressure"));
            configHtml.setWind(getValue("wind"));
            configHtml.setRain(getValue("rain"));
            configHtml.setSunrad(getValue("sunrad"));

            // Names
            configHtml.setDay(getValue("day"));
            configHtml.setMonth(getValue("month"));
            configHtml.setYear(getValue("year"));
            configHtml.setTime(getValue("time"));
            configHtml.setDate(getValue("date"));
            configHtml.setMin(getValue("min"));
            configHtml.setMax(getValue("max"));
            configHtml.setAvg(getValue("avg"));

            // Wind
            configHtml.setGust(getValue("gust"));
            configHtml.setWindAvg(getValue("windAvg"));
            configHtml.setDirAvg(getValue("dirAvg"));
            configHtml.setSpeed(getValue("speed"));
            configHtml.setDirection(getValue("direction"));

            // Rain
            configHtml.setRainIntensity(getValue("rainIntensity"));
            configHtml.setDaysRain1(getValue("daysRain1"));
            configHtml.setDaysRain2(getValue("daysRain2"));

            // Others
            configHtml.setOverall(getValue("overall"));

            // Headers
            configHtml.setHeaderHtmlRaw1(getValue("headerHtmlRaw1"));
            configHtml.setHeaderHtmlRaw2(getValue("headerHtmlRaw2"));

        } else {
            error = ERROR_CONFIG_FILENAME_NOT_FOUND;
            System.out.print("File " + HTML_CONFIG_FILENAME + " not found!");
        }

        return error;
    }

    /**
     * Make the paths
     *
     * @return error
     */
    public int makePaths() {

        error = 0; // Default

        // Check rootPath
        String rootDir = shared.getRootOfPath();
        if (rootDir != null) {
            if (Utils.checkMkDir(rootDir)) {
                // Check trailing slash
                if (!(rootDir.substring(rootDir.length() - 1).equals("/"))) rootDir += "/";

                // xml data path
                if (!Utils.makeDir(rootDir + "xml/")) {
                    shared.setRootOfXmlPath(rootDir + "xml/");
                } else {
                    error = ERROR_MKDIR;
                }

                // html and js path
                if (!Utils.makeDir(rootDir + "html/")) {
                    shared.setRootOfHtmlPath(rootDir + "html/");
                    shared.setRootOfJsPath(rootDir + "html/");
                } else {
                    error = ERROR_MKDIR;
                }

                // csv path
                if (!Utils.makeDir(rootDir + "csv/")) {
                    shared.setRootOfCsvPath(rootDir + "csv/");
                } else {
                    error = ERROR_MKDIR;
                }
            }

        } else {
            error = ERROR_MKDIR;
            System.out.print("Config Error #4: " + error + "\n");
        }

        // Ulteriore controllo!
        if (shared.getRootOfXmlPath() == null || !Utils.checkMkDir(shared.getRootOfXmlPath())) {
            System.out.print("Error: rootOfXmlPath not found\n");
            error = ERROR_PATH_NOT_FOUND;
        }

        return error;
    }

    /**
     * Open configuration file
     *
     * @param configFile config filename
     * @param jar        true if config file in jar file
     * @return boolean false if file not found
     */
    public boolean open(String configFile, boolean jar) {
        boolean fileOpen = false;

        try {
            if (jar) {
                properties.load(getClass().getResourceAsStream(configFile));
            } else {
                properties.load(new FileInputStream(configFile));
            }

            fileOpen = true;

        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }

        return fileOpen;
    }

    /**
     * Returns a string with the value corresponding to the key
     * Ignore any comments followed by the # character
     *
     * @param key
     * @return
     */
    public String getValue(String key) {
        String keyValue = null;
        try {
            // String getProperty(String key, String defaultValue)
            keyValue = properties.getProperty(key);
            if (keyValue != null) {
                int pntRem = keyValue.indexOf("#");
                if (pntRem >= 0) {
                    keyValue = keyValue.substring(0, pntRem).trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return keyValue;
    }

    //---------------------------------------//
    // --- Only for Debugging and Testing ---//
    //---------------------------------------//
    public static void main(String args[]) {
        GetConfig app = new GetConfig();
        if (app.getError() == 0) {
            app.printConfigData();
            app.printConfigHtml();
        } else
            System.out.println("Error = " + app.getError());
    }

} // end class
