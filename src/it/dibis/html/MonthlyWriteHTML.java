package it.dibis.html;

import java.io.File;

import it.dibis.common.Utils;
import it.dibis.config.ConfigHtml;
import it.dibis.config.GetConfig;
import it.dibis.dataObjects.DataOfMonth;
import it.dibis.dataObjects.SharedData;
import it.dibis.xml.MonthReadXML;

public class MonthlyWriteHTML extends WriteHTML {

	/**
	 *  Revision control id
	 */
	public static String cvsId = "$Id: MonthlyWriteHTML.java,v 0.15 12/09/2017 23:59:59 adalborgo $";

	private DataOfMonth dataOfMonth = new DataOfMonth();

	private MonthReadXML monthDataXmlRead = null;

	private float[] dataRow = new float[ARRAY_DATA_TYPE];
	private int year;
	private int month;

	public MonthlyWriteHTML(SharedData shared, ConfigHtml configHtml) {
		super (shared, configHtml);
	}

	/**
	 * @param filename without suffix
	 */
	public void writeFile(String filename, String readXmlPath, String writeHtmlPath, int year, int month) {
		this.year = year;
		this.month = month;

		// Xml pathname
		String fullReadPathname = readXmlPath + filename +  ".xml";

		// Check if file .xml exists
		if (!new File(fullReadPathname).exists()) {
			System.out.println("File: " + fullReadPathname + ", not found!");
			return;
		}

		// Read file xml
		// (Only for debug!) System.out.println("File: " + fullReadPathname + ", found, reading ...");

		monthDataXmlRead = new MonthReadXML(year, month);
		dataOfMonth = monthDataXmlRead.getXmlDataOfMonth(fullReadPathname);

		// Open HTML file
		String fullPathname = writeHtmlPath + filename + ".htm";

		// (Only for debug!) System.out.println("Write file: " + fullPathname);
		outbuf = openFile(fullPathname);
		if (outbuf==null) {
			System.out.println("Not possible to write the html file!");
			return;
		}

		// Month and year in letters
		String month_year = Utils.getNameOfMonth_year(dataOfMonth.getYear(), dataOfMonth.getMonth(), locale);

		//--- Start HTML page---//
		openHtmlDocument(cvsId);
		writeHeadDocument(configHtml.getMonthlyTitle());
		openHtmlBody(configHtml.getHeaderHtmlRaw1(), configHtml.getHeaderHtmlRaw2());

		writeMonthYear(month_year);

		writeRainTable();

		writeMonthYearTableHeader(unitSymbol, configHtml.getDay()); // Header of table

		// Write data table
		for (int day = 0; day<dataOfMonth.getLastDayOfMonth(); day++) {
			for (int i = 0; i<dataRow.length; i++) {
				dataRow[i] = dataOfMonth.getDataArray(i, day);
			}

			writeRow(day+1, dataRow, false);
		}

		// Write summary raw
		dataRow[TEMPERATURE_MIN_INDEX] = dataOfMonth.getTemperatureMin();
		dataRow[TEMPERATURE_MAX_INDEX] = dataOfMonth.getTemperatureMax();
		dataRow[TEMPERATURE_MEAN_INDEX] = dataOfMonth.getTemperatureMean();
		dataRow[HUMIDITY_MIN_INDEX] = dataOfMonth.getHumidityMin();
		dataRow[HUMIDITY_MAX_INDEX] = dataOfMonth.getHumidityMax();
		dataRow[HUMIDITY_MEAN_INDEX] = dataOfMonth.getHumidityMean();
		dataRow[PRESSURE_MIN_INDEX] = dataOfMonth.getPressureMin();
		dataRow[PRESSURE_MAX_INDEX] = dataOfMonth.getPressureMax();
		dataRow[PRESSURE_MEAN_INDEX] = dataOfMonth.getPressureMean();
		dataRow[WINDSPEED_MAX_INDEX] = dataOfMonth.getWindSpeedMax();
		dataRow[WINDSPEED_MEAN_INDEX] = dataOfMonth.getWindSpeedMean();
		dataRow[WINDDIR_MEAN_INDEX] = dataOfMonth.getWindDirectionMean();
		dataRow[RAINALL_INDEX] = dataOfMonth.getRain_all();
		dataRow[SUNRAD_MEAN_INDEX] = dataOfMonth.getSunradMean();
		
		// Write summary data
		writeRow(0, dataRow, true);

		closeTable();
		closeHtml();

		closeFile(outbuf);	// Close file
	}

	/**
	 * @param month_year
	 */
	private void writeMonthYear(String str) {
		write("<h2 align=\"center\">");
		write("<font size=\"4\">" + Utils.firstUppercase(str) + "</font>");
		write("</h2>\r\n\r\n");
	}

	/**
	 * Table of rain
	 */
	private void writeRainTable() {

		writeRainMonthYearTableHeader();

		// 3rd raw
		openRaw();
		writeDataCell(true, dataOfMonth.getRain02(), frmInt, 0, false);
		writeDataCell(true, dataOfMonth.getRain2(), frmInt, 0, false);
		writeDataCell(true, dataOfMonth.getRain20(), frmInt, 0, false);;
		writeDataCell(true, dataOfMonth.getRain_all(), frmD1, 0, false);
		writeDataCell(true, dataOfMonth.getRainRateMax(), frmD1, 0, false);
		openCell(true, "border-right: 1px double #808080;");
			write(Utils.setDate(dataOfMonth.getRainRateMaxDay(), month, year, "d-MM-yyyy", locale));
		closeCell(true);

		closeRaw();

		closeTable();

		newLine();
	}


	/**
	 * 
	 * @param int day
	 * @param float[] dataRow
	 * @param boolean bold
	 */
	private void writeRow(int day, float[] dataRow, boolean bold) {

		float totPrec = 0;	// Total rainfall of the month
		float value;

		openRaw();

		// Day of the month
		openCell(bold);
			if (day>0) {
				write(String.valueOf(day));
			}
			else write(configHtml.getMonth());
		closeCell(bold);

		// Rain
		value = dataRow[dataOfMonth.RAINALL_INDEX];
		if (value>0) {
			writeDataCell(bold, value,  frmD1, 0, true);
			totPrec += value; // Total rainfall
		} else {
			writeCell(bold,"-");
		}

		// temperatureMax
		value = dataRow[dataOfMonth.TEMPERATURE_MAX_INDEX];
		writeDataCell(bold, value,  frmD1, TNODATA, true);

		// temperatureMin
		value = dataRow[dataOfMonth.TEMPERATURE_MIN_INDEX];
		writeDataCell(bold, value,  frmD1, TNODATA, true);

		// temperatureMean
		value = dataRow[dataOfMonth.TEMPERATURE_MEAN_INDEX];
		writeDataCell(bold, value,  frmD1, TNODATA, true);

		// humidityMax
		value = dataRow[dataOfMonth.HUMIDITY_MAX_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// humidityMin
		value = dataRow[dataOfMonth.HUMIDITY_MIN_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// humidityMean
		value = dataRow[dataOfMonth.HUMIDITY_MEAN_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// pressureMax
		value = dataRow[dataOfMonth.PRESSURE_MAX_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// pressureyMin
		value = dataRow[dataOfMonth.PRESSURE_MIN_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// pressureMean
		value = dataRow[dataOfMonth.PRESSURE_MEAN_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// maxWindVel
		float xSpeed = windvelUnitFactor*dataRow[DataOfMonth.WINDSPEED_MAX_INDEX];
		writeDataCell(bold, xSpeed, frmInt, -1, true);

		// windSpeedMean
		xSpeed = windvelUnitFactor*dataRow[dataOfMonth.WINDSPEED_MEAN_INDEX];
		writeDataCell(bold, xSpeed, frmInt, -1, true);

		// windDirMean
		openCell(bold);
		float xDir = dataRow[dataOfMonth.WINDDIR_MEAN_INDEX];
		if ( (xSpeed>0) && (xDir>=0&&xDir<=360)) write(frmInt.format(xDir)); else write("-");
		closeCell(bold);

		// Sun mean radiation (sunradMean)
		value = dataRow[dataOfMonth.SUNRAD_MEAN_INDEX];
		writeDataCell(bold, "border-right: 1px double #808080;", value, frmInt, 0, true);

		closeRaw();
	}


	//---------------------------------------//
	// --- Only for Debugging and Testing ---//
	//---------------------------------------//
	public static void main(String args[]) {

		int year = 2013;
		int month = 2;
		String filename = "022013";

		String readXmlPath = "C:/Users/admin/MeteoStazione/meteofa/new/xml/2013/";
		String writeHtmlPath = "";

		GetConfig getConfig = new GetConfig();
		int error = getConfig.getError();
		if (error!=0) {
			System.out.println("Error = " + error);
			System.exit(error); // -->> Exit on error
		}

		SharedData shared = new GetConfig().getConfigData();
		ConfigHtml configHtml = new GetConfig().getConfigHtml();

		MonthlyWriteHTML app = new MonthlyWriteHTML(shared, configHtml);
		app.writeFile(filename, readXmlPath, writeHtmlPath, year, month);
	}
}
