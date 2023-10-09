package it.dibis.files;

import it.dibis.common.Constants;
import it.dibis.common.Utils;
import it.dibis.config.ConfigHtml;
import it.dibis.config.GetConfig;
import it.dibis.dataObjects.DataOfMonth;
import it.dibis.dataObjects.DataOfYear;
import it.dibis.dataObjects.SharedData;
import it.dibis.html.YearlyWriteHTML;
import it.dibis.xml.MonthReadXML;
import it.dibis.xml.YearWriteXML;

import java.io.BufferedWriter;
import java.io.File;

/**
 * @author Antonio Dal Borgo adalborgo@gmail.com
 *
 */
public class YearlyFile implements Constants {

    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: YearlyFile.java,v 0.23 28/09/2023 23:59:59 adalborgo $";

	private static final int MONTH_OF_YEAR = 12;

	private int year;

	BufferedWriter outbuf = null;

	private SharedData shared = SharedData.getInstance();
	private ConfigHtml configHtml = ConfigHtml.getInstance();

	private MonthReadXML monthDataXmlRead = null;

	private YearWriteXML yearDataXmlWrite = new YearWriteXML();
	private YearlyWriteHTML yearlyWriteHTML = null;

	private DataOfMonth dataOfMonth = new DataOfMonth();
	private DataOfYear dataOfYear = null;

	/**
	 * Generates the html file with the year's data from a file .xml
	 *
	 * @param rootXMLDataPath
	 * @param writeXMLPath
	 * @param writeHTMLPath
	 * @param lastMonth
	 * @param year
	 */
	public void writeFile(String rootXMLDataPath, String writeXMLPath, String writeHTMLPath,
						  int lastMonth, int year) {

		// Check year
		if ( !(year>=2000 && year<=9999) ) {
			System.out.println("Year error: " + year);
			return;
		}

		// Instance data of month
		dataOfYear = new DataOfYear();
		dataOfYear.init(year);

		// Check if exists directories to read in rootXMLDataPath + "yyyy/"
		if (!Utils.checkDirOfYear(rootXMLDataPath, year)) {
			// (Only for debug!) System.out.println("Directory with dataOfMonth not found!");
			return;
		}

		// Check if exists directories to write
		if (!Utils.checkDir(writeXMLPath)) {
			// (Only for debug!) System.out.println("Directory " + writeXMLPath + " not found!");
			return;
		}

		if (!Utils.checkDir(writeHTMLPath)) {
			// (Only for debug!) System.out.println("Directory " + writeHTMLPath + " not found!");
			return;
		}

		boolean dirFound = Utils.checkDirOfYear(rootXMLDataPath, year);
		if (!dirFound) {
			// (Only for debug!) System.out.println("Directory with dataOfMonth not found!");
			return;
		}

		// Get rootXMLDataPath + "yyyy/"
		String fullPathOfYear = Utils.getFullPathOfYear(rootXMLDataPath, year);
		// (Only for debug!) System.out.println("fullPathOfYear: " + fullPathOfYear);

		String fullReadPathname = null;

		dataOfYear.clearDataArray();

		float rainRateMaxTemp = 0;
		int rainRateMaxMonth = -1;
		int rain02 = 0;
		int rain2 = 0;
		int rain20 = 0;

		// --- READ data file for every month of the year ---

		// Last month to scan
 		if (lastMonth<1) lastMonth = MONTH_OF_YEAR;
 
		for (int month=0; month<lastMonth; month++) {

			dataOfMonth.init(year, (month+1)); // Init dataOfMonth
			monthDataXmlRead = new MonthReadXML(year, (month+1));

			// Get pathname of month
			fullReadPathname = fullPathOfYear + Utils.dateStringConvert(1, (month+1), year).substring(2,8)+ ".xml";
			// (Only for debug!) System.out.println("fullReadPathname: " + fullReadPathname);

			// Check if file .xml exists
			if (new File(fullReadPathname).exists()) {
				// (Only for debug!) System.out.println("File: " + fullReadPathname + ", found, reading ...");

				// Read file xml
				dataOfMonth = monthDataXmlRead.getXmlDataOfMonth(fullReadPathname);

				// Get statistics for one month
				copyDataOfMonthToYear(dataOfMonth, month);

				// Get rainRateMax of month
				float maxRate = dataOfMonth.getRainRateMax();
				if (maxRate>rainRateMaxTemp) {
					rainRateMaxTemp = maxRate;
					rainRateMaxMonth = month+1;	
				}

				// Get days of rain
				int rainDays = dataOfMonth.getRain02();
				if (rainDays>0) rain02 += rainDays;
				dataOfYear.setRain02(rain02);

				rainDays = dataOfMonth.getRain2();
				if (rainDays>0) rain2 += rainDays;
				dataOfYear.setRain2(rain2);

				rainDays = dataOfMonth.getRain20();
				if (rainDays>0) rain20 += rainDays;
				dataOfYear.setRain20(rain20);

			} else {
				dataOfMonth.clearAllData();
				// (Only for debug!) System.out.println("File " + fullReadPathname + " not found!");
				// (Only for debug!) System.out.println("No data file for this month!");
			}

		} // end for

		// Get mean value of year
		dataOfYear.calculateAndSetSummaryData();

		// Set rain rate max
		dataOfYear.setRainRateMax(rainRateMaxTemp);
		dataOfYear.setRainRateMaxMonth(rainRateMaxMonth);

		dataOfYear.setStationId(dataOfMonth.getStationId());

		// WRITE files in writeXMLPath and writeHTMLPath (out of fullPathOfYear) 
		// (Only for debug!) System.out.println("Write files XML and HTML.");
		String yearFilename = Utils.dateStringConvert(1, 1, year).substring(4,8);

		// Write file .xml
		// (Only for debug!) System.out.println("Write: " + writeXMLPath + yearFilename + ".xml");

		// WriteMonthXML(writeXMLPath + yearFilename + ".xml");
		yearDataXmlWrite.writeFile(writeXMLPath + yearFilename + ".xml", dataOfYear);

		// --- Write file .html ---
		// (Only for debug!) System.out.println("Write: " + writeHTMLPath + yearFilename + ".html");
		if (writeHTMLPath!=null && writeHTMLPath.length()>0) {
			if (!Utils.makeDir(writeHTMLPath)) { // mkdir if not exists
				// (Only for debug!) System.out.println("Write files HTML: " + writeHTMLPath + yearFilename + ".html");
				yearlyWriteHTML = new YearlyWriteHTML(shared, configHtml);
				yearlyWriteHTML.writeFile(yearFilename, writeXMLPath, writeHTMLPath, year);
			}
		}
	}

	/**
	 * Get statistics for one month
	 *
	 * @param dataOfMonth
	 * @param month
	 */
	private void copyDataOfMonthToYear(DataOfMonth dataOfMonth, int month) {

		// --- Set min/max and mean value ---//
		// Temperature
        dataOfYear.setTemperatureMax(dataOfMonth.getTemperatureMax());
        dataOfYear.setTemperatureMin(dataOfMonth.getTemperatureMin());
        dataOfYear.setTemperatureMean(dataOfMonth.getTemperatureMean());

		// Humidity
		dataOfYear.setHumidityMax(dataOfMonth.getHumidityMax());
		dataOfYear.setHumidityMin(dataOfMonth.getHumidityMin());
        dataOfYear.setHumidityMean(dataOfMonth.getHumidityMean());

		// Pressure
		dataOfYear.setPressureMax(dataOfMonth.getPressureMax());
		dataOfYear.setPressureMin(dataOfMonth.getPressureMin());
        dataOfYear.setPressureMean(dataOfMonth.getPressureMean());

		// Wind
		dataOfYear.setWindSpeedMax(dataOfMonth.getWindSpeedMax());
		dataOfYear.setWindSpeedMean(dataOfMonth.getWindSpeedMean());
		dataOfYear.setWindDirectionMean(dataOfMonth.getWindDirectionMean());

		// Solar radiation
        dataOfYear.setSunradMean(dataOfMonth.getSunradMean());

		// Overall rainfall
		dataOfYear.setRain_all(dataOfMonth.getRain_all());	

		// -->> Copy data to DataArray
		dataOfYear.setDataArray(month); // month = 0..lastDayOfMonth-1
	}
}
