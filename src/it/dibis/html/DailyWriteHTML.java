package it.dibis.html;

import it.dibis.common.Utils;
import it.dibis.config.ConfigHtml;
import it.dibis.config.GetConfig;
import it.dibis.dataObjects.DataOfDay;
import it.dibis.dataObjects.SharedData;
import it.dibis.xml.DayReadXML;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;

/**
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 *
 */
public class DailyWriteHTML extends WriteHTML {

	/**
	 *  Revision control id
	 */
	public static String cvsId = "$Id: DailyWriteHTML.java,v 0.25 12/09/2017 23:59:59 adalborgo $";

	private DataOfDay dataOfDay = null;
	private DayReadXML dayDataXmlRead = null;

	public DailyWriteHTML(SharedData shared, ConfigHtml configHtml) {
		super (shared, configHtml);
	}

	/**
	 * Write file html of day
	 * @see CmdLine and refresh
	 * @param rootXMLDataPath
	 * @param writeHTMLPath
	 * @param year
	 * @param month
	 * @param day
	 * @return int error
	 */
	public int writeFile(String rootXMLDataPath, String writeHTMLPath, int year, int month, int day) {

		int error = 0; // Default: no error

		// Check year, month and day
		if ( !(year>=2000 && year<=9999) || !(month>0 && month<=12) || !(day>0 && day<=31)) {
			System.out.println("Date error!");
			error = -1; // Date error
			return error;
		}

		// --- Write file .html ---
		// String ddmmyyyy
		String dayFilename = Utils.dateStringConvert(day, month, year); 

		// readXMLPath = rootXMLPath + /yyyy/mm/
		String readXMLPath = Utils.getFullPathOfMonth(rootXMLDataPath, day, month, year);

		// writeHTMLFullPath = rootHTMLPath + /yyyy/mm/
		String writeHTMLFullPath = writeHTMLPath + Utils.dateToFilePath(dayFilename);

		if (writeHTMLFullPath!=null && writeHTMLFullPath.length()>0) {
			if (!Utils.makeDir(writeHTMLFullPath)) { // mkdir, if not exists
				// Write html file
				writeFile(dayFilename, readXMLPath, writeHTMLFullPath);
			}
		}

		return error;
	}

	/**
	 * @param filename without suffix
	 */
	public void writeFile(String filename, String readXmlPath, String writeHtmlPath) {

		// Xml pathname
		String fullReadPathname = readXmlPath + filename +  ".xml";

		// Check if file .xml exists
		if (!new File(fullReadPathname).exists()) {
			System.out.println("File: " + fullReadPathname + ", not found!");
			return;
		}

		// Read file xml
		// (Debug) System.out.println("File: " + fullReadPathname + ", found, reading ...");

		dayDataXmlRead = new DayReadXML();
		dataOfDay = dayDataXmlRead.getXmlDataOfDay(fullReadPathname, false);

		// Check dataOfDay
		if (dataOfDay==null) {
			System.out.println("DataOfDay is null!");
			return;
		}

		// Get mean value of day
		dataOfDay.calcMean();

		// Open HTML file
		String fullPathname = writeHtmlPath + filename + ".htm";

		// (Only for debug!) System.out.println("Write file: " + fullPathname);
		outbuf = openFile(fullPathname);
		if (outbuf==null) {
			System.out.println("Not possible to write the html file!");
			return;
		}

		//--- Start HTML page---//

		// Day, month and year
		int year = dataOfDay.getYear();
		if (year<100) year +=2000;

		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, configHtml.getLocale());
		String day_month_year = df.format(
				new GregorianCalendar( year, dataOfDay.getMonth()-1, dataOfDay.getDay() ).getTime()
			);

		//--- Start HTML page---//
		openHtmlDocument(cvsId);
		writeHeadDocument(configHtml.getDailyTitle());
		openHtmlBody(configHtml.getHeaderHtmlRaw1(), configHtml.getHeaderHtmlRaw2());

		writeDayMonthYear(day_month_year);

		writeSummary(); // Summary data table

		writeTableHeader();

		int maxSample = SAMPLES_OF_DAY+1;
		boolean bold;
		for (int q4 = 0; q4<maxSample; q4+=1) { //q4+=4)
			int hour = q4/4; //q4: quarter of an hour

			bold = (q4%4==0) ? true : false; // Change of hour
			writeRow(hour, q4, bold);

			if (hour==24) break; // Last data of the day

		}

		closeTable();

		closeHtml();
		closeFile(outbuf);
	}

	// Header of central table
	private void writeTableHeader() {
		write("<div align=\"center\">\r\n");
		write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\" bgcolor=\"#f0f0ff\" width=\"800\">\r\n");

		openRaw();
		writeRawOfHeaderTable("9%",  configHtml.getTime());
		writeRawOfHeaderTable("13%", configHtml.getTemperature(), unitSymbol[TEMPERATURE_INDEX]);
		writeRawOfHeaderTable("13%", configHtml.getHumidity(), unitSymbol[HUMIDITY_INDEX]);
		writeRawOfHeaderTable("13%", configHtml.getPressure(), unitSymbol[PRESSURE_INDEX]);
		writeRawOfHeaderTable("13%", configHtml.getSpeed(), unitSymbol[WINDSPEED_INDEX]);
		writeRawOfHeaderTable("13%", configHtml.getDirection(), unitSymbol[WINDDIR_INDEX]);
		writeRawOfHeaderTable("13%", configHtml.getRain(), unitSymbol[RAIN_INDEX]);
		writeRawOfHeaderTable("13%", configHtml.getSunrad(), unitSymbol[SUNRAD_INDEX], "border-right: 1px double #808080;");
		closeRaw();
	}

	/**
	 * @param str
	 */
	private void writeDayMonthYear(String str) {
		write("<h2 align=\"center\">");
		write("<font size=\"4\"><b>" + str + "</b></font>\r\n");
		write("</h2>\r\n\r\n");
	}

	/**
	 * 
	 * @param time
	 * @param arrayIndex
	 * @param bold
	 */
	private void writeRow(int time, int arrayIndex, boolean bold) {

		float value;

		openRaw();

		// Time
		DecimalFormat frmInt2 = new DecimalFormat("00");
		writeCell(bold, String.valueOf(time) + ":" + frmInt2.format((arrayIndex%4)*15));

		// Temperature (ti, tmin, tminTime, tmax, tmaxTime)
		value = dataOfDay.getDataArray(TEMPERATURE_INDEX, arrayIndex);
		writeDataCell(bold, value, frmD1, TNODATA, true);

		// Humidity (ui, umin, uminTime, umax, umaxTime)
		value = dataOfDay.getDataArray(HUMIDITY_INDEX, arrayIndex);
		writeDataCell(bold, value, frmInt, 0, true);

		// Pressure (pi, pmin, pminTime, pmax, pmaxTime)
		value = dataOfDay.getDataArray(PRESSURE_INDEX, arrayIndex);
		writeDataCell(bold, value, frmD1, 0, true);

		// Wind speed (wi, wmax, wmaxTime, wdir)
		float xSpeed = windvelUnitFactor*dataOfDay.getDataArray(WINDSPEED_INDEX, arrayIndex);
		writeDataCell(bold, xSpeed, frmInt, -1, true);

		// Wind direction
		openCell(bold);
		float xDir = dataOfDay.getDataArray(WINDDIR_INDEX, arrayIndex);
		if ((xSpeed>0) && (xDir>=0&&xDir<=360)) write(frmInt.format(xDir)); else write("-");
		closeCell(bold);

		// Rain
		value = dataOfDay.getDataArray(RAIN_INDEX, arrayIndex);
		writeDataCell(bold, value, frmD1, 0, false);

		// Sun radiation
		value = dataOfDay.getDataArray(SUNRAD_INDEX, arrayIndex);
		writeDataCell(bold, "border-right: 1px double #808080;", value, frmD1, 0, false);

		closeRaw();
	}

	/**
	 * Write html body
	 */
	private void writeSummary() {

		// Header
		write("<div align=\"center\">\r\n");
		write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\" bgcolor=\"#f0f0ff\" width=\"800\">\r\n");
		write("  <tbody>\r\n");

		// 1st raw (6 cells)
		openRaw();
		writeRawOfSummaryTable("3", "20%", configHtml.getTemperature(), unitSymbol[TEMPERATURE_INDEX]);
		writeRawOfSummaryTable("3", "20%", configHtml.getHumidity(), unitSymbol[HUMIDITY_INDEX]);
		writeRawOfSummaryTable("2", "15%", configHtml.getPressure(), unitSymbol[PRESSURE_INDEX]);
		writeRawOfSummaryTable("3", "20%", configHtml.getSpeed(), unitSymbol[WINDSPEED_INDEX]);
		writeRawOfSummaryTable("2", "15%", configHtml.getRain(), null);
		writeRawOfSummaryTable("1", "10%", configHtml.getSunrad(), unitSymbol[SUNRAD_INDEX], "border-right: 1px double #808080;");
		closeRaw();

		// 2nd raw (14 cells)
		openRaw();

		writeCell(true, configHtml.getMax());
		writeCell(true, configHtml.getMin());
		writeCell(true, configHtml.getAvg());

		writeCell(true, configHtml.getMax());
		writeCell(true, configHtml.getMin());
		writeCell(true, configHtml.getAvg());

		writeCell(true, configHtml.getMax());
		writeCell(true, configHtml.getMin());

		writeCell(true, configHtml.getGust());
		writeCell(true, configHtml.getTime());
		writeCell(true, configHtml.getDirAvg());

		writeCell(true, configHtml.getOverall() + "<br />(mm)");
		writeCell(true, configHtml.getRainIntensity() + "<br />(mm/h)");

		writeCell(true, configHtml.getMax(), "border-right: 1px double #808080;");

		closeRaw();

		openRaw();	// Start row

		boolean bold = false;
		float value;

		// tmax
		value = dataOfDay.getTemperatureMax();
		writeDataCell(bold, value, frmD1, TNODATA, true);

		// tmin
		value = dataOfDay.getTemperatureMin();
		writeDataCell(bold, value, frmD1, TNODATA, true);

		// Temperature mean
		value = dataOfDay.getTemperatureMean(); 
		writeDataCell(bold, value, frmD1, TNODATA, true);

		// umax
		value = dataOfDay.getHumidityMax();
		writeDataCell(bold, value, frmInt, 0, true);

		// umin
		value = dataOfDay.getHumidityMin();
		writeDataCell(bold, value, frmInt, 0, true);

		// Humidity mean
		value = dataOfDay.getHumidityMean();
		writeDataCell(bold, value, frmInt, 0, true);

		// pmax
		value = dataOfDay.getPressureMax();
		writeDataCell(bold, value, frmInt, 0, true);

		// pmin
		value = dataOfDay.getPressureMin();
		writeDataCell(bold, value, frmInt, 0, true);

		// wmax 
		value = windvelUnitFactor*dataOfDay.getWindSpeedMax(); // km/h
		writeDataCell(bold, value, frmInt, -1, true);

		// wmaxTime
		openCell(bold);
		value = dataOfDay.getWindSpeedMaxTime();
		if (value>0)
			write(Utils.minutesToHHMM((int)value));
		else
			write("-");
		closeCell(bold);

		// Wind direction mean
		openCell(bold);
		value = dataOfDay.getWindDirectionMean();
		if (dataOfDay.getWindSpeedMax()>=0 && value>=0 && value<=360) 
			write(frmInt.format(value));
		else
			write("-");
		closeCell(bold);

		// Rainfall
		value = dataOfDay.getRain_all();
		writeDataCell(bold, value, frmD1, 0, false);

		// Rain rate max
		value = dataOfDay.getRainRateMax();
		writeDataCell(bold, value, frmD1, 0, false);

		// --- Sun radiation max (sunradMax) ---
		value = dataOfDay.getSunradMax();
		writeDataCell(bold, "border-right: 1px double #808080;", value, frmInt, 0, false);

		closeRaw();

		write("  </tbody>\r\n");
		write( "</TABLE>\r\n");
		write( "</div>\r\n");

		write("<BR>\r\n");
	}

	//---------------------------------------//
	// --- Only for Debugging and Testing ---//
	//---------------------------------------//
	public static void main(String args[]) {

		String filename = "11062013";

		String readXmlPath = "C:/Users/admin/MeteoStazione/meteofa/data2/xml/2013/06/";
		String writeHtmlPath = "";

		GetConfig getConfig = new GetConfig();
		int error = getConfig.getError();
		if (error!=0) {
			System.out.println("Error = " + error);
			System.exit(error); // -->> Exit on error
		}

		SharedData shared = new GetConfig().getConfigData();
		ConfigHtml configHtml = new GetConfig().getConfigHtml();

		DailyWriteHTML app = new DailyWriteHTML(shared, configHtml);
		app.writeFile(filename, readXmlPath, writeHtmlPath);
	}
}
