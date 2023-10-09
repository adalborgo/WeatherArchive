package it.dibis.html;

import it.dibis.common.Utils;
import it.dibis.config.ConfigHtml;
import it.dibis.config.GetConfig;
import it.dibis.dataObjects.DataOfYear;
import it.dibis.dataObjects.SharedData;
import it.dibis.xml.YearReadXML;

import java.io.File;

/**
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 *
 */
public class YearlyWriteHTML extends WriteHTML {

	// --- Constants --- //
    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: YearlyWriteHTML.java,v 0.15 12/09/2017 23:59:59 adalborgo $";

    private DataOfYear dataOfYear = null;
    private YearReadXML yearDataXmlRead = null;

	private float[] dataRow = new float[ARRAY_DATA_TYPE];
	private int year;

	public YearlyWriteHTML(SharedData shared, ConfigHtml configHtml) {
		super (shared, configHtml);
	}

	/**
	 * @param filename without suffix
	 */
	public void writeFile(String filename, String readXmlPath, String writeHtmlPath, int year) {
		this.year = year;

		// Xml pathname
		String fullReadPathname = readXmlPath + filename +  ".xml";

		// Check if file .xml exists
		if (!new File(fullReadPathname).exists()) {
			System.out.println("File: " + fullReadPathname + ", not found!");
			return;
		}

		// Read file xml
		// (Only for debug!) System.out.println("File: " + fullReadPathname + ", found, reading ...");

		yearDataXmlRead = new YearReadXML(year);
		dataOfYear = yearDataXmlRead.getXmlDataOfYear2(fullReadPathname);

		// Open HTML file
		String fullPathname = writeHtmlPath + filename + ".htm";

		// (Only for debug!) System.out.println("Write file: " + fullPathname);
		outbuf = openFile(fullPathname);
		if (outbuf==null) {
			System.out.println("Not possible to write the htm file!");
			return;
		}

		//--- Start HTML page---//
		openHtmlDocument(cvsId);
		writeHeadDocument(configHtml.getYearlyTitle());
		openHtmlBody(configHtml.getHeaderHtmlRaw1(), configHtml.getHeaderHtmlRaw2());

		writeTitle();

		writeRainTable();

		writeMonthYearTableHeader(unitSymbol, configHtml.getMonth()); // Header of table

		// Write data table
		for (int month = 0; month<MONTH_OF_YEAR; month++) {
			for (int i = 0; i<dataRow.length; i++) {
				dataRow[i] = dataOfYear.getDataArray(i, month);
			}

			writeRow(month+1, dataRow, false);
		}

		// Write summary raw
		dataRow[TEMPERATURE_MIN_INDEX] = dataOfYear.getTemperatureMin();
		dataRow[TEMPERATURE_MAX_INDEX] = dataOfYear.getTemperatureMax();
		dataRow[TEMPERATURE_MEAN_INDEX] = dataOfYear.getTemperatureMean();
		dataRow[HUMIDITY_MIN_INDEX] = dataOfYear.getHumidityMin();
		dataRow[HUMIDITY_MAX_INDEX] = dataOfYear.getHumidityMax();
		dataRow[HUMIDITY_MEAN_INDEX] = dataOfYear.getHumidityMean();
		dataRow[PRESSURE_MIN_INDEX] = dataOfYear.getPressureMin();
		dataRow[PRESSURE_MAX_INDEX] = dataOfYear.getPressureMax();
		dataRow[PRESSURE_MEAN_INDEX] = dataOfYear.getPressureMean();
		dataRow[WINDSPEED_MAX_INDEX] = dataOfYear.getWindSpeedMax();
		dataRow[WINDSPEED_MEAN_INDEX] = dataOfYear.getWindSpeedMean();
		dataRow[WINDDIR_MEAN_INDEX] = dataOfYear.getWindDirectionMean();
		dataRow[RAINALL_INDEX] = dataOfYear.getRain_all();
		dataRow[SUNRAD_MEAN_INDEX] = dataOfYear.getSunradMean();

		// Write summary data
		writeRow(0, dataRow, true);

		closeTable();
		closeHtml();

		closeFile(outbuf);	// Close file
	}

	public void writeTitle() {
		write("<h2 align=\"center\"><font size=\"4\">" + configHtml.getYear() + " ");
		write(Utils.firstUppercase("" + year));
		write("</font></h2>\r\n\r\n");
	}

	/**
	 * Table of rain
	 */
	public void writeRainTable() {

		writeRainMonthYearTableHeader();

		// 3rd raw
		openRaw();
		writeDataCell(true, dataOfYear.getRain02(), frmInt, 0, false);
		writeDataCell(true, dataOfYear.getRain2(), frmInt, 0, false);
		writeDataCell(true, dataOfYear.getRain20(), frmInt, 0, false);;
		writeDataCell(true, dataOfYear.getRain_all(), frmD1, 0, false);
		writeDataCell(true, dataOfYear.getRainRateMax(), frmD1, 0, false);
		openCell(true, "border-right: 1px double #808080;");
		write(Utils.firstUppercase(Utils.getNameOfMonth(dataOfYear.getRainRateMaxMonth(), configHtml.getLocale())));
		closeCell(true);
		closeRaw();

		closeTable();
		newLine();
	}

	/**
	 * 
	 * @param int month
	 * @param dataOfYear dataOfYear
	 */
	public void writeRow(int month, float[] dataRow, boolean bold) {

		float totPrec = 0;	// Total rainfall of the year
		float value;

		openRaw();

		// Month
		openCell(bold);
			if (month>0) write(Utils.firstUppercase(MONTH_NAME[month-1])); else write(configHtml.getYear());
		closeCell(bold);

		// Rain
		value = dataRow[dataOfYear.RAINALL_INDEX];
		if (value>0) {
			writeDataCell(bold, value,  frmD1, 0, true);
			totPrec += value; // Total rainfall
		} else {
			writeCell(true,"-");
		}

		// temperatureMax
		value = dataRow[dataOfYear.TEMPERATURE_MAX_INDEX];
		writeDataCell(bold, value,  frmD1, TNODATA, true);

		// temperatureMin
		value = dataRow[dataOfYear.TEMPERATURE_MIN_INDEX];
		writeDataCell(bold, value,  frmD1, TNODATA, true);

		// temperatureMean
		value = dataRow[dataOfYear.TEMPERATURE_MEAN_INDEX];
		writeDataCell(bold, value,  frmD1, TNODATA, true);

		// humidityMax
		value = dataRow[dataOfYear.HUMIDITY_MAX_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// humidityMin
		value = dataRow[dataOfYear.HUMIDITY_MIN_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// humidityMean
		value = dataRow[dataOfYear.HUMIDITY_MEAN_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// pressureMax
		value = dataRow[dataOfYear.PRESSURE_MAX_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// pressureyMin
		value = dataRow[dataOfYear.PRESSURE_MIN_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// pressureMean
		value = dataRow[dataOfYear.PRESSURE_MEAN_INDEX];
		writeDataCell(bold, value,  frmInt, 0, true);

		// maxWindVel
		float xSpeed = windvelUnitFactor*dataRow[dataOfYear.WINDSPEED_MAX_INDEX];
		writeDataCell(bold, xSpeed, frmInt, -1, true);

		// windSpeedMean
		xSpeed = windvelUnitFactor*dataRow[dataOfYear.WINDSPEED_MEAN_INDEX];
		writeDataCell(bold, xSpeed, frmInt, -1, true);

		// windDirMean
		openCell(bold);
		float xDir = dataRow[dataOfYear.WINDDIR_MEAN_INDEX];
		if ( (xSpeed>0) && (xDir>=0&&xDir<=360)) write(frmInt.format(xDir)); else write("-");
		closeCell(bold);

		// Sun mean radiation (sunradMean)
		value = dataRow[dataOfYear.SUNRAD_MEAN_INDEX];
		writeDataCell(bold, "border-right: 1px double #808080;", value, frmInt, 0, true);

		closeRaw();
	}

	//---------------------------------------//
	// --- Only for Debugging and Testing ---//
	//---------------------------------------//
	public static void main(String args[]) {

		int year = 2013;
		String filename = "2013";

		String readXmlPath = "C:/Users/admin/MeteoStazione/meteofa/new/xml/";
		String writeHtmlPath = "";

		GetConfig getConfig = new GetConfig();
		int error = getConfig.getError();
		if (error!=0) {
			System.out.println("Error = " + error);
			System.exit(error); // -->> Exit on error
		}

		SharedData shared = new GetConfig().getConfigData();
		ConfigHtml configHtml = new GetConfig().getConfigHtml();

		YearlyWriteHTML app = new YearlyWriteHTML(shared, configHtml);
		app.writeFile(filename, readXmlPath, writeHtmlPath, year);
    }
}
