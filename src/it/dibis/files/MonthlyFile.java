package it.dibis.files;

/*
 * Summary with monthly data file .htm e .xml (file 'mmaaaa.htm')
 * 
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 */

import java.io.*;

import it.dibis.common.Constants;
import it.dibis.common.Utils;
import it.dibis.config.ConfigHtml;
import it.dibis.config.GetConfig;
import it.dibis.dataObjects.DataOfDay;
import it.dibis.dataObjects.DataOfMonth;
import it.dibis.dataObjects.SharedData;
import it.dibis.html.MonthlyWriteHTML;
import it.dibis.xml.DayReadXML;
import it.dibis.xml.MonthWriteXML;

public class MonthlyFile implements Constants {

    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: MonthlyFile.java,v 0.21 12/09/2017 23:59:59 adalborgo $";

	DayReadXML dayDataXmlRead = new DayReadXML();
	MonthWriteXML monthDataXmlWrite = new MonthWriteXML();

	MonthlyWriteHTML monthlyWriteHTML = null;

	DataOfDay dataOfDay = new DataOfDay();
	DataOfMonth dataOfMonth = new DataOfMonth();

	private SharedData shared = SharedData.getInstance();
	private ConfigHtml configHtml = ConfigHtml.getInstance();

	/**
	 * Generates the html file with the month's data from a file .xml
	 *
	 * @param rootXMLDataPath
	 * @param writeXMLPath
	 * @param writeHTMLPath
	 * @param currentLastDayOfMonth
	 * @param year
	 * @param month
	 */
	public void writeFile(String rootXMLDataPath, String writeXMLPath, String writeHTMLPath,
						  int currentLastDayOfMonth, int year, int month) {

		dataOfMonth.init(year, month);

		// Check month and year
		if ( !(month>0 && month<=12) || !(year>=2000 && year<=9999) ) {
			System.out.println("Wrong month: " + month);
			return;
		}

		// Check if exists directory with dataOfDay files
		String stringOfDate = Utils.dateStringConvert(1, month, year); // 01mmyyyy
		boolean dirFound = Utils.makeDirOfMonth(rootXMLDataPath, stringOfDate);
		if (dirFound) {
			// (Only for debug!) System.out.println("Directory with dataOfDay not found!");
			return;
		}

		// Filename 'mmaaaa.htm' to read
		int lastDayOfMonth = dataOfMonth.getLastDayOfMonth();
		if (currentLastDayOfMonth<1 || currentLastDayOfMonth>lastDayOfMonth)
			currentLastDayOfMonth = lastDayOfMonth;

		String fullReadPathname = null;

		dataOfMonth.setCurrentLastDayOfMonth(currentLastDayOfMonth);
		dataOfMonth.clearDataArray();

		float rainRateMaxTemp = 0;
		int rainRateMaxDay = -1;

		float rainfall = 0;
		int rain02 = 0;
		int rain2 = 0;
		int rain20 = 0;

		// --- READ data file for every day of the month ---
		for (int day = 0; day<currentLastDayOfMonth; day++) {

			dataOfDay.init(year, month, (day+1), SAMPLES_OF_DAY); // Init dataOfDay

			// Get path and file name with day of loop: return String "year/mm" + ddmmyyyy
			fullReadPathname = Utils.getFullPathnameOfDate(rootXMLDataPath, (day+1), month, year) + ".xml"; 
			// (Only for debug!) System.out.println(fullReadPathname);

			// Check if file .xml exists
			if (new File(fullReadPathname).exists()) {
				// (Only for debug!) System.out.println("File: " + fullReadPathname + ", found, reading ...");

				// Read file xml
				//!!! readXMLDataOfDay(fullReadPathname);
				dataOfDay = dayDataXmlRead.getXmlDataOfDay(fullReadPathname, false);

				// Get statistics for one day
				copyDataOfDayToMonth(dataOfDay, day);
				
				// Get rainRateMax of day
				float maxRate = dataOfDay.getRainRateMax();
				if (maxRate>rainRateMaxTemp) {
					rainRateMaxTemp = maxRate;
					rainRateMaxDay = day+1;
				}

				// Get days of rain
				rainfall = dataOfDay.getRain_all();
				if (rainfall>0) {
					if (rainfall<=0.2f)
						dataOfMonth.setRain02(++rain02);
					else if (rainfall<=2.0f)
						dataOfMonth.setRain2(++rain2);
					else
						dataOfMonth.setRain20(++rain20);
				}

			} else {
				dataOfDay.clearAllData();
				// (Only for debug!) System.out.println("File " + fullReadPathname + " not found!");
				// (Only for debug!) System.out.println("No data file for this day!");
			}

		} // end for

		// Get mean value of month
		dataOfMonth.calculateAndSetSummaryData();
		
		// Set rain rate max
		dataOfMonth.setRainRateMax(rainRateMaxTemp);
		dataOfMonth.setRainRateMaxDay(rainRateMaxDay);

		dataOfMonth.setStationId(dataOfDay.getStationId());

		// --- WRITE files xml and html ---
		// (Only for debug!) System.out.println("Write files XML and HTML.");
		String monthFilename = Utils.dateStringConvert(1, month, year).substring(2,8);

		// Write file .xml
		String dataPathOfYear = writeXMLPath + monthFilename.substring(2,6) + "/"; // writeXMLPath + yyyy/
		if (writeXMLPath!=null && writeXMLPath.length()>0) {
			if (!Utils.makeDir(dataPathOfYear)) { // mkdir if not exists
				// (Only for debug!) System.out.println("Write files XML: " + dataPathOfYear + monthFilename + ".xml");
				monthDataXmlWrite.writeFile(dataPathOfYear + monthFilename + ".xml", dataOfMonth);
			}
		}

		// --- Write file .html ---
		String publicPathOfYear = writeHTMLPath + monthFilename.substring(2,6) + "/"; // writeHTMLPath + yyyy/
		if (writeHTMLPath!=null && writeHTMLPath.length()>0) {
			if (!Utils.makeDir(publicPathOfYear)) { // mkdir if not exists
				// (Only for debug!) System.out.println("Write files HTML: " + publicPathOfYear + monthFilename + ".html");
				monthlyWriteHTML = new MonthlyWriteHTML(shared, configHtml);
				monthlyWriteHTML.writeFile(monthFilename, dataPathOfYear, publicPathOfYear, year, month);
			}
		}
	}

	// --- Private methods ---
    /**
     * Get statistics for one day
	 * @param dataOfDay
	 * @param day
	 * @return MeteoStats
     */
	private void copyDataOfDayToMonth(DataOfDay dataOfDay, int day) {

		// Get means for this day
		dataOfDay.calcMean();

		// --- Set min/max and mean value ---//
		// Temperature
        dataOfMonth.setTemperatureMax(dataOfDay.getTemperatureMax());
        dataOfMonth.setTemperatureMin(dataOfDay.getTemperatureMin());
        dataOfMonth.setTemperatureMean(dataOfDay.getTemperatureMean());

		// Humidity
		dataOfMonth.setHumidityMax(dataOfDay.getHumidityMax());
		dataOfMonth.setHumidityMin(dataOfDay.getHumidityMin());
        dataOfMonth.setHumidityMean(dataOfDay.getHumidityMean());

		// Pressure
		dataOfMonth.setPressureMax(dataOfDay.getPressureMax());
		dataOfMonth.setPressureMin(dataOfDay.getPressureMin());
        dataOfMonth.setPressureMean(dataOfDay.getPressureMean());

		// Wind
		dataOfMonth.setWindSpeedMax(dataOfDay.getWindSpeedMax());
		dataOfMonth.setWindSpeedMean(dataOfDay.getWindSpeedMean());
		dataOfMonth.setWindDirectionMean(dataOfDay.getWindDirectionMean());

		// Solar radiation
        dataOfMonth.setSunradMean(dataOfDay.getSunradMean());

		// Overall rainfall
		dataOfMonth.setRain_all(dataOfDay.getRain_all());

		// -->> Copy data to DataArray
		dataOfMonth.setDataArray(day); // day = 0..lastDayOfMonth-1
	}

	//---------------------------------------//
	// --- Only for Debugging and Testing ---//
	//---------------------------------------//
	public static void main(String args[]) {

		// Usare CmdLine -month 2013 1
		String rootOfXmlDataPath = "C:/Users/admin/MeteoStazione/meteofa/new/xml/";
		String rootOfPublicPath = "C:/Users/admin/MeteoStazione/meteofa/new/html/";
		// String rootOfPublicPath = "";
		
		//----------------------------------------------------------------------------//
		int year = 2001;
		int beginMonth = 5;
		int endMonth = 12;
		int lastDay = 0;

		GetConfig getConfig = new GetConfig();
		int error = getConfig.getError();
		if (error!=0) {
			System.out.println("Error = " + error);
			System.exit(error); // -->> Exit on error
		}

		SharedData shared = new GetConfig().getConfigData();
		ConfigHtml configHtml = new GetConfig().getConfigHtml();

		for (int month=beginMonth; month<=endMonth; month++) {
			MonthlyFile monthlyFile = new MonthlyFile();
			monthlyFile.writeFile(rootOfXmlDataPath, rootOfXmlDataPath, rootOfPublicPath, lastDay, year, month);
		}
    }
}
