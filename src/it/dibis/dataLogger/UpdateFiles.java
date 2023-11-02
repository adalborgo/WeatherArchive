package it.dibis.dataLogger;

import it.dibis.common.Constants;
import it.dibis.common.Utils;
import it.dibis.config.ConfigHtml;
import it.dibis.dataObjects.DataOfDay;
import it.dibis.dataObjects.DataOfMonth;
import it.dibis.dataObjects.DataOfYear;
import it.dibis.dataObjects.SharedData;
import it.dibis.files.FileCSV;
import it.dibis.files.MonthlyFile;
import it.dibis.files.YearlyFile;
import it.dibis.html.DailyWriteHTML;
import it.dibis.html.FileDataJS;
import it.dibis.html.WriteGraphics;
import it.dibis.files.FileMeteoNetwork;
import it.dibis.xml.DayWriteXML;
import it.dibis.xml.MonthReadXML;
import it.dibis.xml.YearReadXML;

import java.io.File;

public class UpdateFiles implements Constants {

    // Revision control id
    public static final String CVSID = "$Id: UpdateFiles.java,v 0.12 27/09/2023 23:59:59 adalborgo $";

    private SharedData shared = SharedData.getInstance();
    private ConfigHtml configHtml = ConfigHtml.getInstance();

    private String rootOfCsvPath = shared.getRootOfCsvPath();
    private String rootOfXmlPath = shared.getRootOfXmlPath();
    private String rootOfHtmlPath = shared.getRootOfHtmlPath();

    private DailyWriteHTML dailyWriteHTML = new DailyWriteHTML(shared, configHtml);
    private MonthlyFile monthlyFile = new MonthlyFile();
    private YearlyFile yearlyFile = new YearlyFile();
    DayWriteXML dayWriteXML = new DayWriteXML();
    FileDataJS fileDataJS = new FileDataJS();
    FileCSV fileCSV = new FileCSV();
    WriteGraphics writeGraphics = new WriteGraphics();
    FileMeteoNetwork fileMeteoNetwork = new FileMeteoNetwork();

    /**
     * Set rainAll of the month and the year
     *
     * @param day
     * @param month
     * @param year
     * @return
     */
    public boolean setAllRain(int day, int month, int year) {
        boolean error = false;
        String fullReadPathnameOfMonth = Utils.getFullPathnameOfMonth(rootOfXmlPath, day, month, year) + ".xml";
        if (new File(fullReadPathnameOfMonth).exists()) {
            MonthReadXML monthDataXmlRead = new MonthReadXML(year, month);
            DataOfMonth currDataOfMonth = monthDataXmlRead.getXmlDataOfMonth(fullReadPathnameOfMonth);
            shared.setMonthRainAll(currDataOfMonth.getRain_all());
        } else {
            System.out.println("File: " + fullReadPathnameOfMonth + ", not found!");
            error = true;
        }

        // Get Rain_all of the year
        String fullReadPathnameOfYear = Utils.getFullPathnameOfYear(rootOfXmlPath, year) + ".xml";
        if (new File(fullReadPathnameOfYear).exists()) {
            YearReadXML yearDataXmlRead = new YearReadXML(year);
            DataOfYear currDataOfYear = yearDataXmlRead.getXmlDataOfYear2(fullReadPathnameOfYear);
            shared.setYearRainAll(currDataOfYear.getRain_all());
        } else {
            System.out.println("File: " + fullReadPathnameOfYear + ", not found!");
            error = true;
        }

        return !error;
    }

    /**
     * Update files .csv and .js every minute (No .xml, .html)
     *
     * @param dataOfDay
     */
    public void updateEveryMinuteFiles(DataOfDay dataOfDay) {
        // Append record to file yyyy/mm/ddmmyyyy.csv
        if (shared.getSaveCsv()) {
            updateFileCsv(dataOfDay);
        }

        // Save DATA_NAME + ".js" (data.js)
        if (shared.getSaveJs()) {
            // New file of data.js
            fileDataJS.genJs(dataOfDay,
                    shared.getMonthRainAll() + dataOfDay.getRain_all(),
                    shared.getYearRainAll() + dataOfDay.getRain_all(),
                    shared.getTimeZoneJS(), rootOfHtmlPath + DATA_NAME + ".js");
        }
    }

    /**
     * Update data.xml every 15 minutes (from DataManager):
     * write DATA_NAME + ".xml" to rootOfXmlPath
     *
     * @param dataOfDay
     */
    public void update15MinutesDataXml(DataOfDay dataOfDay) {
        dayWriteXML.writeFile(rootOfXmlPath + DATA_NAME + ".xml", dataOfDay);
    }

    /**
     * Update current files if request every 15 minutes (from DataManager):
     * write file data.htm to currentPath
     * write all graphics to rootOfHtmlPath (??? currentPath)
     *
     * @param dataOfDay
     */
    public void update15MinutesCurrentFiles(DataOfDay dataOfDay) {
         // Save DATA_NAME + ".htm" (data.htm) to currentPath
        if (shared.getSaveHtml()) {
            dailyWriteHTML.writeFile(DATA_NAME, rootOfXmlPath, rootOfHtmlPath);
        }

        // Save graphics files to currentPath
        if (shared.getSaveGraph()) {
            writeGraphics.writeAllGraphics(dataOfDay, shared, configHtml, rootOfHtmlPath);
        }
    }

    /**
     * Update files every 15 minutes (from DataManager):
     * write file ddmmyyyy + ".xml" to rootOfXmlPath/yyyy/mm/
     * write file ddmmyyyy.htm to rootOfHtmlPath/yyyy/mm/
     * write File MeteoNetwork to rootOfHtmlPath
     *
     * @param dataOfDay
     * @return
     */
    public boolean update15MinutesFilesWithDate(DataOfDay dataOfDay) {
        boolean error = false;

        //--- Updates files with the dates contained in dataOfDay ---//
        int day = dataOfDay.getDay();
        int month = dataOfDay.getMonth();
        int year = dataOfDay.getYear();

        // String of data files
        String ddmmyyyy = Utils.dateStringConvert(day, month, year); // String "ddmmyyyy" from (day, month, year)
        String datePath = Utils.dateToFilePath(ddmmyyyy); // Full path with yyyy/mm/

        // Check or make directory: rootOfXmlPath + ddmmyyyy
        boolean mkDirError = Utils.makeDirOfMonth(rootOfXmlPath, ddmmyyyy);
        if (!mkDirError) {
            // Save file ddmmyyyy.xml
            dayWriteXML.writeFile(rootOfXmlPath + datePath + ddmmyyyy + ".xml", dataOfDay);
        } else {
            System.out.println("mkDirError for rootOfXmlPath + filedate");
            return true; // error = true
        }

        if (shared.getSaveHtml()) {
            // Check or make directory: rootOfHtmlPath + ddmmyyyy
            mkDirError = Utils.makeDirOfMonth(rootOfHtmlPath, ddmmyyyy);
            if (!mkDirError) {
                // Save file ddmmyyyy.htm
                String readXMLPath = Utils.getFullPathOfMonth(rootOfXmlPath, day, month, year);
                String writeHTMLFullPath = rootOfHtmlPath + Utils.dateToFilePath(ddmmyyyy);
                dailyWriteHTML.writeFile(ddmmyyyy, readXMLPath, writeHTMLFullPath);
            } else {
                System.out.println("mkDirError for rootOfHtmlPath + filedate");
                return true; // error = true
            }

            // https://my.meteonetwork.it/station/xxxyyy/
            if (shared.getMeteoNetworkId() != null && shared.getMeteoNetworkId().length() == 6) {
                fileMeteoNetwork.writeFile(dataOfDay, shared.getMeteoNetworkId(), shared.getMonthRainAll(), shared.getYearRainAll(), rootOfHtmlPath); ///  currentPath);
            }
        }

        return error;
    }

    /**
     * Append new record to file yyyy/mm/ddmmyyyy.csv
     */
    private void updateFileCsv(DataOfDay dataOfDay) {

        if (!shared.getSaveCsv()) return;

        int day = dataOfDay.getDay();
        int month = dataOfDay.getMonth();
        int year = dataOfDay.getYear();

        String ddmmyyyy = Utils.dateStringConvert(day, month, year); // String "ddmmyyyy" from (day, month, year)
        String datePath = Utils.dateToFilePath(ddmmyyyy); // Full path with yyyy/mm/

        // Check or make directory
        boolean mkDirError = Utils.makeDirOfMonth(rootOfCsvPath, ddmmyyyy);
        if (mkDirError) System.out.println("mkDirError for rootOfCsvPath + filedate");

        // --- Save with date filename
        fileCSV.write(dataOfDay,  rootOfCsvPath + datePath + ddmmyyyy + ".csv");
    }

    /**
     * Write files .xml and .htm of month
     */
    public void updateFileOfMonth(int yesterday, int month, int year) {
        // -> Write file mmyyyy.htm to rootOfPublicPath + 'yyyy/mm/mmyyyy.htm'
        monthlyFile.writeFile(rootOfXmlPath, rootOfXmlPath, rootOfHtmlPath, yesterday, year, month);
    }

    /**
     * Write files .xml and .htm of year
     */
    public void updateFileOfYear(int month, int year) {
        // -> Write file yyyy.htm to rootOfPublicPath + 'yyyy.htm'
        yearlyFile.writeFile(rootOfXmlPath, rootOfXmlPath, rootOfHtmlPath, month, year);
    }

}
