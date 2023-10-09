package it.dibis.station;

import java.util.StringTokenizer;

import it.dibis.config.GetConfig;
import it.dibis.dataObjects.SharedData;
import it.dibis.tflogger.Combilog;
import it.dibis.tflogger.GetCombilogConfig;

/**
 * StationInterfaceCombilog.java
 * API per l'accesso alle risorse della stazione meteo Combilog 1020 Friedrichs
 *
 * @author Antonio Dal Borgo adalborgo@gmail.com
 */
public class StationCombilog extends StationInterface {

    boolean debug = false; // false; !!!!!!!!!!!!!!

    // --- Costanti --- //
    public static final float NODATA = -999.0f;    // No data available

    /**
     * Revision control id
     */
    private static final String cvsId = "$Id: StationCombilog.java,v 0.19 19/10/2018 23:59:59 adalborgo $";
    // Nota(19-10-2018): problema su Integer.parseInt, sostituito con il metodo parseInt(String str)
    // Nota(07-07-2019): aggiunto if (debug) per ogni System.out.println()
    // Nota(07-07-2019): Correzione temporanea per direzione vento errata in "stationData.setWindDirection(getDataOfChannel(WINDDIR_CHANNEL));" riga 209-210
	/*
	-> 07-08-2019: modificato StationCombilog
	-> Correzione "Temporanea!!!" 07/08/2019
	- Aggiunta provvisoriamente (???) la correzione sulla direzione del vento in 'station/StationCombilog'
	Righe 208-214
	// Direzione
	// Nota(07-07-2019): Correzione temporanea per direzione vento errata in "stationData.setWindDirection(getDataOfChannel(WINDDIR_CHANNEL));"
	float tmp = getDataOfChannel(WINDDIR_CHANNEL);
	stationData.setWindDirection((tmp<=180) ? tmp+180 : tmp-180);
	System.out.println(">>> (StationCombilog.Correzione!!!): " + tmp +" ->> " + ((tmp<=180) ? tmp+180 : tmp-180));
	// Riga da ripristinare!!!
	// stationData.setWindDirection(getDataOfChannel(WINDDIR_CHANNEL));

	 */

    // TFCombilog: Canali ad accesso diretto (live) (numeri formali) possono essere DIVERSI da quelli fisici di Combilog!
    public final static int TEMPERATURE_CHANNEL = 0;  // Temperatura istantanea
    public final static int TEMPERATURE_MIN_CHANNEL = 1;  // Temperatura minima
    public final static int TEMPERATURE_MAX_CHANNEL = 2;  // Temperatura massima
    public final static int HUMIDITY_CHANNEL = 3;  // Umidita' istantanea
    public final static int HUMIDITY_MIN_CHANNEL = 4;  // Umidita' minima
    public final static int HUMIDITY_MAX_CHANNEL = 5;  // Umidita' massima
    public final static int PRESSURE_CHANNEL = 6;  // Pressione istantanea
    public final static int PRESSURE_MIN_CHANNEL = 7;  // Pressione minima
    public final static int PRESSURE_MAX_CHANNEL = 8;  // Pressione massima
    public final static int WINDSPEED_CHANNEL = 9;  // Vento velocita' istantanea
    public final static int WINDSPEED_MAX_CHANNEL = 10; // Vento velocita' massimo
    public final static int WINDDIR_CHANNEL = 11; // Vento direzione
    public final static int RAINALL_CHANNEL = 12; // Contatore pioggia
    public final static int SUNRAD_CHANNEL = 13; // Radiazione solare
    public final static int SUNRAD_MAX_CHANNEL = 14; // Radiazione solare massima

    // TFCombilog: index of raw_data stored
    public final static int TEMPERATURE_RAW_LOG = 0;  // Temperatura istantanea
    public final static int TEMPERATURE_MIN_RAW_LOG = 1;  // Temperatura minima
    public final static int TEMPERATURE_MAX_RAW_LOG = 2;  // Temperatura massima
    public final static int HUMIDITY_RAW_LOG = 3;  // Umidita' istantanea
    public final static int HUMIDITY_MIN_RAW_LOG = 4;  // Umidita' minima
    public final static int HUMIDITY_MAX_RAW_LOG = 5;  // Umidita' massima
    public final static int PRESSURE_RAW_LOG = 6;  // Pressione istantanea
    public final static int PRESSURE_MIN_RAW_LOG = 7;  // Pressione minima
    public final static int PRESSURE_MAX_RAW_LOG = 8;  // Pressione massima
    public final static int WINDSPEED_RAW_LOG = 9;  // Vento velocita' istantanea
    public final static int WINDSPEED_MAX_RAW_LOG = 10; // Vento velocita' massimo
    public final static int WINDDIR_RAW_LOG = 11; // Vento direzione
    public final static int RAINALL_RAW_LOG = 12; // Contatore pioggia
    public final static int SUNRAD_RAW_LOG = 13; // Radiazione solare
    public final static int SUNRAD_MAX_RAW_LOG = 14; // Radiazione solare massima

    // --- Variables ---//
    private int error = 0; // No error

    private Combilog combilog = null;

    protected StationData stationData = StationData.getInstance(); // Static object

    public int[] channelsIndex = null;
    public int[] channelsRawIndex = null;

    /**
     * Constructor
     */
    public StationCombilog() {

        GetCombilogConfig cfg = new GetCombilogConfig();

        // Instance Station object
        combilog = new Combilog(deviceNumber, loggerPort);

        channelsIndex = cfg.channelsIndex;
        channelsRawIndex = cfg.channelsRawIndex;
    }

    /**
     * Constructor (for debug, see main())
     */
    public StationCombilog(String deviceNumber, String loggerPort) {

        GetCombilogConfig cfg = new GetCombilogConfig();

        // Instance Station object
        combilog = new Combilog(deviceNumber, loggerPort);

        channelsIndex = cfg.channelsIndex;
        channelsRawIndex = cfg.channelsRawIndex;
    }

    //============ API per accesso alle risorse della stazione meteo ============//

    /**
     * Reset variable error
     */
    public void resetError() {
        this.error = 0;
        combilog.resetError();
    }

    /**
     * Read variable error
     *
     * @return int error
     */
    public int getError() {
        return error;
    }

    /**
     * Sincronizza Data e Ora di CombiLog con il PC
     */
    public synchronized int synchDataTime() {
        return combilog.synchDataTime();

    }

    /**
     * Lettura dei parametri di stato di Combilog
     *
     * @return String
     */
    public synchronized String getStatusInfo() {
        return combilog.getStatusInfo();
    }

    /**
     * Status port check
     *
     * @return boolean
     */
    public synchronized boolean getStatus() {
        return combilog.getStatus();
    }

    /**
     * Open communication port with weather station
     *
     * @return int error from port.open()
     */
    public synchronized int open() {
        if (debug) System.out.println("Open station port ... please wait." + "\n");
        error = combilog.open();
        return error;
    }

    /**
     * Close communication port with weather station
     */
    public synchronized void close() {
        combilog.close();
    }

    /**
     * Legge i dati istantanei da ogni singolo canale del logger e li carica nell'oggetto stationData
     * Get also date e time
     */
    public void getDataFromAllChannels() {
        if (debug) System.out.println("combilog.getStatus(): " + combilog.getStatus());
        if (combilog.getStatus()) { // Status port check

            // Date & time string
            String yymmddhhmmss = combilog.getDataTime();
            if (debug) System.out.println("combilog.getDataTime(): " + yymmddhhmmss);

            if (debug) System.out.println("---------------------------");

            // aamsgghhmmss; (NumberFormatException - if the string does not contain a parsable integer)
            // Inizio modifiche!!! stringToInt(str)
            stationData.setYear(2000 + stringToInt(yymmddhhmmss.substring(0, 2).toString()));
            stationData.setMonth(stringToInt(yymmddhhmmss.substring(2, 4).toString()));
            stationData.setDay(stringToInt(yymmddhhmmss.substring(4, 6).toString()));

            stationData.setHour(stringToInt(yymmddhhmmss.substring(6, 8).toString()));
            stationData.setMinute(stringToInt(yymmddhhmmss.substring(8, 10).toString()));

            // --- TEMPERATURE ---//
            stationData.setTemperature(getDataOfChannel(TEMPERATURE_CHANNEL)); // Current
            stationData.setTemperatureMin(getDataOfChannel(TEMPERATURE_MIN_CHANNEL)); // Min
            stationData.setTemperatureMax(getDataOfChannel(TEMPERATURE_MAX_CHANNEL)); // Max

            // --- HUMIDITY ---//
            stationData.setHumidity(getDataOfChannel(HUMIDITY_CHANNEL));
            stationData.setHumidityMin(getDataOfChannel(HUMIDITY_MIN_CHANNEL));
            stationData.setHumidityMax(getDataOfChannel(HUMIDITY_MAX_CHANNEL));

            // --- PRESSURE ---//
            stationData.setPressure(getDataOfChannel(PRESSURE_CHANNEL));
            stationData.setPressureMin(getDataOfChannel(PRESSURE_MIN_CHANNEL));
            stationData.setPressureMax(getDataOfChannel(PRESSURE_MAX_CHANNEL));

            // --- WIND (m/s) ------//
            stationData.setWindSpeed(getDataOfChannel(WINDSPEED_CHANNEL));
            stationData.setWindSpeedMax(getDataOfChannel(WINDSPEED_MAX_CHANNEL));

            // Direzione
            // La correzione è stata eliminata in quanto il sensore si è guastato!!!
            // Nota(07-07-2019): Correzione temporanea per direzione vento errata in "stationData.setWindDirection(getDataOfChannel(WINDDIR_CHANNEL));"
			/*
			float tmp = getDataOfChannel(WINDDIR_CHANNEL);
			stationData.setWindDirection((tmp<=180) ? tmp+180 : tmp-180);
			System.out.println(">>> (StationCombilog.Correzione!!!): " + tmp +" ->> " + ((tmp<=180) ? tmp+180 : tmp-180));
			 */
            // Riga da ripristinare!!!
            // stationData.setWindDirection(getDataOfChannel(WINDDIR_CHANNEL));
            stationData.setWindDirection(getDataOfChannel(WINDDIR_CHANNEL));

            // --- PIOGGIA ---//
            stationData.setRain(getDataOfChannel(RAINALL_CHANNEL));

            // --- RADIAZIONE SOLARE ---//
            stationData.setSunrad(getDataOfChannel(SUNRAD_CHANNEL));
            stationData.setSunradMax(getDataOfChannel(SUNRAD_MAX_CHANNEL));

        } else { // Logger not ready
            error = -1;
        }
    }

    /**
     * Get data from channel
     *
     * @param channelType
     * @return
     */
    private synchronized float getDataOfChannel(int channelType) {
        if (debug) System.out.println("StatioCombilog.getDataOfChannel().channelType: " + channelType);
        float x = NODATA;
        int channel = channelsIndex[channelType];
        if (channel >= 0) x = combilog.getChannelData(intToHex(channel));
        if (debug) System.out.println("channel, combilog.getChannelData(intToHex(channel)): " + channel + ", " + x);
        return x;
        /// return (channel>=0) ? combilog.getChannelData(intToHex(channel)) : NODATA;
    }

    /**
     * Converte un intero 0<=n<=255 in una stringa esadecimale
     *
     * @param n
     * @return String
     */
    private String intToHex(int n) {
        if (n >= 0 && n <= 255) {
            String hex = Integer.toHexString(n).toUpperCase();
            // if(hex.length()==1) hex = "0" + hex;
            return (hex.length() == 1) ? "0" + hex : hex;
        } else {
            return "Errore!";
        }
    }

    // -----------------------------------------------------//
    //             Copiato da RescueCombilogData
    // -----------------------------------------------------//
    // --- Metodi di accesso al puntatore #2 di Combilog ---//
    /**
     * Inizializza il prelievo delle righe dati dall'inizio del giorno dalla memoria del logger
     * Per la conversione dal formato 'ddmmyyyy' al formato 'yymmdd' usa 'dataReverse(ggmmaaaa)'
     *
     * @param yymmdd
     */
    public int getNumberOfStoredData(String yymmdd) { // Attenzione al formato!!!

        // Set pointer to the begin of day
        // NOTA: usare il puntatore #2 (il puntatore #1 non funziona con la data)
        combilog.setDateMemoryPointer2(yymmdd + "000000");

        return combilog.readEventNumber2(); // Logged data
    }

    /*
     * Return stationData from a line stored in datalogger
     * @param String date in yymmdd format
     */
    public StationData getNextStoredData(String yymmdd) {

        final int RAW_LEN_MIN = 14;
        final int DATA_LENGTH = 6;

        String raw = combilog.readIncEventFromPointer2();
        if (debug) System.out.println("-->" + raw); // Visualizza le righe scaricate da Combilog

        boolean dataRequest = (raw.length() > RAW_LEN_MIN && yymmdd.length() == DATA_LENGTH &&
                raw.substring(1, DATA_LENGTH + 1).toString().compareTo(yymmdd) == 0);
        if (dataRequest) {
            return decodeRawToMeteoData(1, raw);
        } else { // Empty buffer
            stationData.clear();
            return stationData;
        }
    }

    /**
     * Aggiorna StationData con i dati di una linea prelevata dalla memoria del logger
     * N.B. Non aggiorna l'intestazione se e' il record delle 24:00 (lastRecord)
     *
     * @param start
     * @param line
     * @return
     */
    public StationData decodeRawToMeteoData(int start, String line) {

        final int DATA_FIELD_LENGTH = 10; // Combilog 1020
        final int DATA_OFFSET = 16;          // Combilog 1020

        stationData.clear(); // Clear stationData

        StringBuffer dataField = new StringBuffer(DATA_FIELD_LENGTH);

        if (line != null && (line.length() >= start + DATA_OFFSET)) {

            // The fields are separated by ';'
            StringTokenizer st = new StringTokenizer(line.toString(), ";");

            int nfield = st.countTokens() - 1; // Data field numbers

            if (debug) System.out.println("nfield: " + nfield);

            // Date & time
            String yymmddhhmmss = st.nextToken().substring(1, start + 12).toString();

            // aamsgghhmmss; (NumberFormatException - if the string does not contain a parsable integer)
            stationData.setYear(2000 + stringToInt(yymmddhhmmss.substring(0, 2).toString()));
            stationData.setMonth(stringToInt(yymmddhhmmss.substring(2, 4).toString()));
            stationData.setDay(stringToInt(yymmddhhmmss.substring(4, 6).toString()));

            stationData.setHour(stringToInt(yymmddhhmmss.substring(6, 8).toString()));
            stationData.setMinute(stringToInt(yymmddhhmmss.substring(8, 10).toString()));

            // Discard the minimum and maximum values when it is midnight
            boolean midnight = (stationData.getHour() == 0 && stationData.getMinute() == 0);

            int channel = 0; // Init channel
            float value = NODATA; // Init value
            Long i = null;

            while (channel < nfield && st.hasMoreTokens()) {

                // Check ArrayIndexOutOfBoundsException
                if (channel >= channelsRawIndex.length) {
                    break;
                }

                // Data in hex format
                dataField.replace(0, DATA_FIELD_LENGTH, st.nextToken());

                // Check if channel is active
                int channelIndex = channelsRawIndex[channel];
                if (channelIndex >= 0) { // Channel is active
                    // Convert dataField from hex to float
                    try {
                        i = Long.parseLong(dataField.toString(), 16); // radix 16-bit (hex)
                        value = Float.intBitsToFloat(i.intValue());
                        if (debug) System.out.println("channel, channelIndex, value: "
                                + channel + "\t" + channelIndex + "\t" + value + "\t" + dataField);
                    } catch (NumberFormatException e) {
                        System.out.println(e);
                        value = NODATA; // Better of NODATA
                    }

                    switch (channelIndex) {
                        case TEMPERATURE_RAW_LOG:
                            stationData.setTemperature(value);
                            break;

                        case TEMPERATURE_MIN_RAW_LOG:
                            if (!midnight)
                                stationData.setTemperatureMin(value);
                            break;

                        case TEMPERATURE_MAX_RAW_LOG:
                            if (!midnight)
                                stationData.setTemperatureMax(value);
                            break;

                        case HUMIDITY_RAW_LOG:
                            stationData.setHumidity(value);
                            break;

                        case HUMIDITY_MIN_RAW_LOG:
                            if (!midnight) stationData.setHumidityMin(value);
                            break;

                        case HUMIDITY_MAX_RAW_LOG:
                            if (!midnight) stationData.setHumidityMax(value);
                            break;

                        case PRESSURE_RAW_LOG:
                            stationData.setPressure(value);
                            break;

                        case PRESSURE_MIN_RAW_LOG:
                            if (!midnight) stationData.setPressureMin(value);
                            break;

                        case PRESSURE_MAX_RAW_LOG:
                            if (!midnight) stationData.setPressureMax(value);
                            break;

                        case WINDSPEED_RAW_LOG:
                            stationData.setWindSpeed(value);
                            break;

                        case WINDSPEED_MAX_RAW_LOG:
                            if (!midnight) stationData.setWindSpeedMax(value);
                            break;

                        case WINDDIR_RAW_LOG:
                            stationData.setWindDirection(value);
                            break;

                        case RAINALL_RAW_LOG:
                            stationData.setRain(value);
                            break;

                        case SUNRAD_RAW_LOG:
                            stationData.setSunrad(value);
                            break;

                        case SUNRAD_MAX_RAW_LOG:
                            if (!midnight) stationData.setSunradMax(value);
                            break;

                        default: // Do nothing
                    } // end switch

                } // end if (check if channel is active)

                ++channel; // Update channel counter
            } // end while

        } // end if

        /// DEBUG ///
        // stationData.print();

        return stationData;
    }

    /**
     * @param str
     * @return int
     */
    protected int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            System.out.println(e);
            return (int) NODATA;
        }
    }

    // --------------------------------------
    // --- Only for Debugging and Testing ---
    // --------------------------------------

    // Rescue from Combilog: copiato da DataManager 'rescueDataFromCombilog()' solo per debug!!!
    public void rescueDataFromCombilog() {

        // Today in ddmmyyyy format
        String ddmmyyyy = it.dibis.common.Utils.dateToString();

        // Convert format from ddmmyyyy to yymmdd
        String dateToRead = ddmmyyyy.substring(6, 8) + ddmmyyyy.substring(2, 4) + ddmmyyyy.substring(0, 2);

        int loggedData = getNumberOfStoredData(dateToRead); //!!!
        for (int i = 0; i < loggedData; i++) {

            stationData = getNextStoredData(dateToRead);
            int year = stationData.getYear();
            int month = stationData.getMonth();
            int day = stationData.getDay();
            int hour = stationData.getHour();
            int minute = stationData.getMinute();
            int second = stationData.getSecond();

            boolean dataIsReady = (year >= 0) && (month >= 0) && (day >= 0) && (hour >= 0) && (minute >= 0) && (second >= 0);
            if (dataIsReady) {

                stationData.print();

                // Update index of array
					/*
					int arrayIndex = ((60*hour + minute+5)/DELTA_TIME) % MAX_INDEX;
					arrayIndex = (arrayIndex>=0 && arrayIndex<SAMPLE_OF_DAY-1) ? arrayIndex : -1;
					boolean flagSaveFileXmlHtml[arrayIndex] = true;

					copyLastStationDataToDataOfDay();
					*/
            } else {
                if (debug) System.out.println("Non ci sono piu' dati!");
                break;
            }
        }

    }

    /**
     * Read all active channels and print data
     */
    public int readAllActiveChannelsAndPrint_DEMO() {
        stationData = StationData.getInstance();

        stationData.clear(); // ClearAllData stationData
        getDataFromAllChannels();
        StationData.print();

        return getError();
    }

    /**
     * Scan and print all active channels
     */
    public void printDataOfAllChannels_DEMO() {
        float value;
        if (debug) System.out.println("\n--->> Read all channels <<---");
        if (debug) System.out.println("\n--->> channelsIndex: " + channelsIndex);

        for (int index = 0; index < channelsIndex.length; index++) {
            if (debug) System.out.print("Channel: " + index + "  ->> ");
            value = combilog.getChannelData(intToHex(index));
            if (error == 0 && debug) System.out.println("Value: " + value);
            else System.out.println("Errore: " + error);
        }
    }

    /**
     * Performs the conversion from string to int
     * Nota: presente anche in Utils
     *
     * @param str
     * @return int
     */
    private int stringToInt(String str) {
        int n = 0;
        try {
            n = Integer.valueOf(str);
        } catch (NumberFormatException e) {
            System.out.println("No int!");
        }

        return n;
    }

    //--------------------------------------//
    // ----- For Debugging and Testing -----//
    //--------------------------------------//

    /**
     * @param args
     */
    public static void main(String[] args) {

        GetConfig getConfig = new GetConfig();
        int error = getConfig.getError(); // Load configuration parameters
        if (error != 0) {
            System.out.println("Error = " + error);
            System.exit(error); // -->> Exit on error
        }

        SharedData configData = SharedData.getInstance();
        StationCombilog app =
                new StationCombilog(configData.getDeviceNumber(), configData.getLoggerPort());

        app.open();

        System.out.println("Eseguito: open()");
        if (app.error != 0) System.out.println("Variabile error: " + app.error);

        String systemMsg = app.getStatusInfo();
        System.out.println("\nEseguito: getStatusInfo()");
        System.out.println(systemMsg);

        app.synchDataTime();
        System.out.println("\nEseguito: synchDataTime()");
        if (app.error != 0) System.out.println("Variabile error: " + app.error);

		/*
		// !Scan all active channels!
		app.printDataOfAllChannels_DEMO();

		// Read all active channels and print data
		app.readAllActiveChannelsAndPrint_DEMO();
		*/
        app.rescueDataFromCombilog();

        app.close();
    }

}
