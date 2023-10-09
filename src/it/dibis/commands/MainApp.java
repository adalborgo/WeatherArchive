package it.dibis.commands;

/*
 * Main class for WeatherArchive
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 */

/*
	NOTE:
	The task runs every 30 s (TIMERTASK_PERIOD = 30000L): events are managed by dataManager
	The files xml are R+W, the others only W
 */

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.dibis.common.Constants;
import it.dibis.common.Utils;
import it.dibis.dataObjects.SharedData;
import it.dibis.config.GetConfig;
import it.dibis.dataLogger.DataManager;

public class MainApp implements Constants {

    // Version info id
    public static final String CVSID = "$Id: RunApp.java, Release 0.1 29/09/2023 23:59:59 adalborgo@gmail.com";

    //------ DEBUG ------//
    private boolean DEBUG = false;
    private final int MINUTE_OFFSET = 0; // 13*60 + 10; // h*60 + m
    //-------------------//

    private final long TIMERTASK_PERIOD = 30000L; // 15000L; // milliseconds (15 s)

    /*
       For debug the newDay:
       ZoneId zone =
        ZoneId.systemDefault();
        ZoneOffset.of("+11:00");
        ZoneId.of("UTC+10")
        ZoneId.of("Asia/Kolkata")

        DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.parse("2023-09-23 23:43", FORMATTER);
     */

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    ZoneId zone = null; //ZoneId.systemDefault();

    LocalDateTime now; // convert the instant to a local date time of your system time zone

    DataManager dataManager = null;

    private SharedData shared = SharedData.getInstance();

    // Daily values
    private int secondsOfDay = -1; // 0..86399
    private int minutesOfDay = -1; // 0..1439
    private int minutes15ofDay = -1; // 0..95

    private int year = -1;
    private int month = -1;
    private int day = -1;
    private int hour = -1;
    private int minute = -1;
    private int second = -1;
    private int milliseconds = -1;

    private int yesterDay = -1;
    private int yesterMonth = -1;
    private int yesterYear = -1;

    // Last values
    private int currentLastSecondOfDay = -1;
    private int lastSecondsOfDay = -1;
    private int lastMinuteOfDay = -1;
    private int lastMinutes15ofDay = -1;
    private int lastDay = -1;
    private int lastMonth = -1;
    private int lastYear = -1;

    private float lastRainfallOfDay = 0;

    private boolean newMinute = false;
    private boolean new15Minute = false;
    private boolean newDay = false;

    private boolean fullMinute = false;
    private boolean halfMinute = false;

    // Constructor
    public MainApp() {
        // Load the configuration data in object
        GetConfig getConfig = new GetConfig();
        int error = getConfig.getError();
        if (error != 0) {
            System.out.println("Error = " + error);
            System.exit(error); // -->> Exit on error
        }

        if (shared.getZoneId() == null || shared.getZoneId().isEmpty()) {
            zone = ZoneId.systemDefault();
        } else {

            try {
                zone = ZoneId.of(shared.getZoneId());
            } catch (Exception ex) {
                zone = ZoneId.systemDefault();
                System.out.println("Error: Invalid ZoneId format. Set System Default.");
            }

        }

        System.gc(); // Garbage collection

        // Run datalogger
        datalogger();
    }

    /**
     * RunApp DataLogger
     */
    private void datalogger() {
        now = LocalDateTime.now(zone);
        // now = LocalDateTime.now(zone).plusMinutes(15);

        System.out.println("--- Start Weather Archive ---\n");
        System.out.print("-> DateTime for files .html, .csv (");
        System.out.print("ZoneId = " + zone + "): ");
        System.out.println(now.format(FORMATTER));

        System.out.print("-> DateTime for data.js (ZoneId: System Default): ");
        System.out.println(LocalDateTime.now(ZoneId.systemDefault()).format(FORMATTER));
        System.out.println();

        dataManager = new DataManager();

        // Open communication port with weather station (if necessary)
        int error = dataManager.open();
        if (error != 0) {
            System.out.println("MainApp: Open error!");
            System.exit(1);
        }

        // Send time to dataManager for init()
        getAllLocalTime(MINUTE_OFFSET); // Get system time
        dataManager.setSystemTime(now);
        System.out.println("< DataManager-NOW > " + now);

        boolean initError = dataManager.init(); // Init data of day
        if (!initError) {
            System.out.println(">>> The station is offline!");
            System.exit(1);
        }

        // Init status file to "run"
        Utils.writeStringToFile(WEATHER_STATUS, "run\n");

        //===================== scheduleTask() =====================//
        // Initialize and synchronize the event handler
        second = now.getSecond();
        milliseconds = now.get(ChronoField.MILLI_OF_SECOND);
        long startDelay = (60100 - (1000 * second + milliseconds)); // Add 0.1 s

        //--- Init last time data ---//
        getAllLocalTime(MINUTE_OFFSET);
        lastMinuteOfDay = minutesOfDay;
        lastMinutes15ofDay = minutes15ofDay;
        lastSecondsOfDay = secondsOfDay;
        lastDay = day;
        lastMonth = month;
        lastYear = year;

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // Check request of end
                if (readStatus() != 0) {
                    System.out.println(">>> EndApp Request!");
                    executorService.shutdown();
                    System.exit(0); // -->> Exit on end request
                }

                getAllLocalTime(MINUTE_OFFSET); // Get actual time
                manageEvents(); // Handle any event requests
            }
        }, startDelay, TIMERTASK_PERIOD, TimeUnit.MILLISECONDS);
    }

    private void manageEvents() {
        // Send time to dataManager
        dataManager.setSystemTime(now);

        // Set time flag
        newMinute = minutesOfDay != lastMinuteOfDay;
        new15Minute = minutes15ofDay != lastMinutes15ofDay;
        newDay = day != lastDay;

        // Change minute
        if (newMinute && fullMinute) { // s = 0..29
            lastMinuteOfDay = minutesOfDay;
            if (DEBUG) System.out.println(">>> newMinute");
            // Use the timestamp of dataOfDay instead of stationData!!!
            // Problema dell'azzeramento della pioggia a mezzanotte?!!!
            dataManager.newMinuteEvent();
        }

        // Update only in the second half of the minute (s = 30..59)
        if (halfMinute) {
            // Change 15Minute
            if (new15Minute) {
                lastMinutes15ofDay = minutes15ofDay;
                if (!newDay) {
                    if (DEBUG) System.out.println(">>> new15Minute");
                    dataManager.new15minuteEvent(minutes15ofDay);
                }
            }

            // Change day
            if (newDay) {
                // Set yesterday
                yesterDay = lastDay;
                yesterMonth = lastMonth;
                yesterYear = lastYear;

                // Update
                lastDay = day;
                lastMonth = month;
                lastYear = year;

                if (DEBUG) System.out.println(">>> newDay <<<");
                dataManager.newDayEvent(yesterDay, yesterMonth, yesterYear, minutes15ofDay);
            }

            // New day: update month and year files
            if (hour == 0) { // s = 30..59
                if (minute == 3) { // (00:03:30..59)
                    dataManager.updateMonthEvent(yesterDay, yesterMonth, yesterYear);
                } else if (minute == 5) { // (00:05:30..59)
                    dataManager.updateYearEvent(yesterMonth, yesterYear);
                }
            }
        }
    }

    /**
     * Get all time and date
     *
     * @param minuteOffset only for DEBUG 'new day'
     */
    private void getAllLocalTime(int minuteOffset) {
        //now = LocalDateTime.now(zone);
        if (!DEBUG) minuteOffset = 0;
        now = LocalDateTime.now(zone).plusMinutes(minuteOffset);
        hour = now.getHour();
        minute = now.getMinute();
        second = now.getSecond();
        day = now.getDayOfMonth();
        month = now.getMonthValue();
        year = now.getYear();
        milliseconds = now.get(ChronoField.MILLI_OF_SECOND);

        fullMinute = (second >= 0 && second < 29);  // s = 0..29
        halfMinute = (second >= 30 && second < 59); // s = 30..59

        minutesOfDay = 60 * hour + minute; // 0..1439
        secondsOfDay = 60 * minutesOfDay + second; // 0..86399
        minutes15ofDay = (minutesOfDay / DELTA_TIME) % SAMPLES_OF_DAY; // 0..95

        if (DEBUG) System.out.println("< NOW > " + now);
    }

    //---------------------------------------//

    /**
     * Read the status
     *
     * @return int status (0:run; 1: end)
     */
    private int readStatus() {
        int status = 0; // Default: is running

        String s = Utils.readFileToString(WEATHER_STATUS);
        if (s != null && (s.trim().equals("end"))) {
            status = 1; // EndApp
            new File(WEATHER_STATUS).delete();
        }

        return status;
    }

    // RunApp main app
    public static void main(String args[]) {
        System.out.println("\n" + CVSID + "\n");
        new MainApp(); // .datalogger();
    }
}
