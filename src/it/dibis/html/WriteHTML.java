package it.dibis.html;

import java.io.BufferedWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Locale;

import it.dibis.common.Constants;
import it.dibis.config.ConfigHtml;
import it.dibis.dataObjects.SharedData;

public class WriteHTML implements Constants {

    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: MonthlyWriteHTML.java,v 0.4 12/09/2017 23:59:59 adalborgo $";

    protected final String TD1 = ".td1 {\n\tborder-top: none; border-bottom: 1px double #808080; border-left: 1px double #808080; border-right: none; padding: 0.1cm; text-align: center; font-family: Arial, Times New Roman; font-size:13px; background-color: #f0f0ff;\n}";
    protected final String TD1bold = ".td1bold {\n\tborder-top: 1px double #808080; border-bottom: 1px double #808080; border-left: 1px double #808080; border-right: none; padding: 0.1cm; text-align: center; font-family: Arial, Times New Roman; font-size:13px; background-color: #d9ddf2; font-weight:bold;\n}";
    protected final String TD2 = ".td2 {\n\tborder-top: 1px double #808080; border-bottom: 1px double #808080; border-left: 1px double #808080; border-right: none; padding: 0.1cm; text-align: center; font-family: Arial, Times New Roman; font-size:13px; background-color: #d9ddf2;\n}";

    protected final DecimalFormat frmInt = new DecimalFormat("#");
    protected final DecimalFormat frmD1 = new DecimalFormat("0.0");

	protected BufferedWriter outbuf = null;

	protected SharedData shared; // = SharedData.getDataFlash();
	protected ConfigHtml configHtml; // = ConfigHtml.getDataFlash();

	protected Locale locale = null;
	protected String[] MONTH_NAME = null;

	protected String[] unitSymbol = null; // configHtml.getUnitSymbol();
	protected float windvelUnitFactor; //configHtml.getUnitFactor()[WINDSPEED_INDEX];

	/**
	 * Constructor
	 */
	public WriteHTML(SharedData shared, ConfigHtml configHtml) {
		this.shared = shared;
		this.configHtml = configHtml;
		unitSymbol = configHtml.getUnitSymbol();
		windvelUnitFactor = configHtml.getUnitFactor()[WINDSPEED_INDEX];
		
		this.locale = configHtml.getLocale();

		// Localized name of month: IT
		 MONTH_NAME = new DateFormatSymbols(locale).getMonths();
	}

	/**
	 * Open HTML page
	 * @param String cvsId
	 */
	protected void openHtmlDocument(String cvsId) {
		// Begin of file HTML
		write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\r\n");
		write("  \"http://www.w3.org/TR/html4/loose.dtd\">\r\n");

		// begin HTML file
		write("\r\n<html>\r\n");

		// Remarks
		write("<!-- Author: Antonio Dal Borgo -->\r\n");
		write("<!-- CVSId : " + cvsId + " -->\r\n");
		write("\r\n");
	}

	/**
	 * Open HTML body
	 * @param String headerRaw1
	 * @param String headerRaw2
	 */
	protected void openHtmlBody(String headerRaw1, String headerRaw2) {
		write("<BODY  alink=\"#FF0000\" link=\"#0000ff\" text=\"#0000a0\" vlink=\"#0080a0\">\r\n\r\n");
		write("<p align=\"center\">\r\n");
		write("<FONT face=\"Arial,Helvetica\" size=\"3\" >" + headerRaw1 + "</font><BR>\r\n");
		write("<FONT face=\"Arial,Helvetica\" size=\"2\" >" + headerRaw2 + "</font><BR>\r\n");
		write("</p>\r\n\r\n");
	}

	/**
	 * Header
	 * @param title
	 */
	protected void writeHeadDocument(String title) {
		write( "<HEAD>\r\n");
		write( "<meta http-equiv=" + "\"Content-Type\" " + " content=" + "\"" + "text/html; charset=iso-8859-1" + "\"" + ">\r\n");
		write("<TITLE>" + title + "</TITLE>\r\n");

		// Write styles
		write("<STYLE TYPE=\"text/css\">\n");
		write(TD1 + "\n" );
		write(TD2 + "\n" );
		write(TD1bold + "\n" );
		write("</STYLE>\r\n");

		write("</HEAD>\r\n\r\n");
	}

	/**
	 * Close HTML page
	 */
	protected void closeHtml() {
		write( "</BODY>\r\n");
		write( "</html>\r\n");
	}

	protected void newLine() {
		write( "<br />\r\n");
	}

	/**
	 * Close table
	 */
	protected void closeTable() {
		write( "</TABLE>\r\n");
		write( "</div>\r\n");
	}

	protected void openRaw() {
		write("  <tr>\r\n");
	}

	protected void closeRaw() {
		write("  </tr>\r\n");
	}

	protected void openCell(boolean setBoldOn) {
		write("    <TD class=\"td1\">");
		if (setBoldOn) write("<b>");
	}

	protected void openCell(boolean setBoldOn, String style) {
		write("    <TD class=\"td1\" style=\"" + style + "\">");
		if (setBoldOn) write("<b>");
	}

	protected void closeCell(boolean setBoldOn) {
		if (setBoldOn) write("</b>");
		write("</td>\r\n");
	}

	protected void writeCell(boolean bold, String data) {
		openCell(bold); write(data); closeCell(bold);
	}

	protected void writeCell(boolean bold, String data, String style) {
		openCell(bold, style); write(data); closeCell(bold);
	}

	/**
	 * @param bold
	 * @param data
	 * @param style
	 */
	protected void writeCellHd2(boolean bold, String style, String data) {
		write("    <TD class=\"td2\" style=\"border-top: none; " + style + "\">");
		if (bold) write("<b>");
		write(data);
		closeCell(bold);
	}

	protected void writeDataCell(boolean bold, float value, DecimalFormat formD, float nodata, boolean zero) {
		openCell(bold);
		writeData(value, formD, nodata, zero);
		closeCell(bold);
	}

	protected void writeDataCell(boolean bold, String style, float value, DecimalFormat formD, float nodata, boolean zero) {
		openCell(bold, style);
		writeData(value, formD, nodata, zero);
		closeCell(bold);
	}

	protected void writeRawOfSummaryTable(String colspan, String width, String str, String unit) {
		write("    <TD class=\"td1bold\" colspan=\"" + colspan + "\"");
		write(" width=\"" + width + "\">" + str);
		if (unit!=null) write("<br>" + "(" + unit + ")");
		write("</TD>\r\n");
	}

	protected void writeRawOfSummaryTable(String colspan, String width, String str, String unit, String style) {
		write("    <TD class=\"td1bold\" colspan=\"" + colspan + "\" style=\"" + style + "\"");
		write(" width=\"" + width + "\">" + str);
		if (unit!=null) write("<br>" + "(" + unit + ")");
		write("</TD>\r\n");
	}

	protected void writeRawOfHeaderTable(String width, String str, String unit) {
		write("    <TD class=\"td1bold\"");
		write(" width=\"" + width + "\">" + str + "<br>" + "(" + unit + ")</TD>\r\n");
	}

	protected void writeRawOfHeaderTable(String width, String str) {
		write("    <TD class=\"td1bold\"");
		write(" width=\"" + width + "\">" + str + "</TD>\r\n");
	}

	protected void writeRawOfHeaderTable(String width, String str, String unit, String style) {
		write("    <TD class=\"td1bold\" style=\"" + style + "\"");
		write(" width=\"" + width + "\">" + str + "<br>" + "(" + unit + ")</TD>\r\n");
	}

	/**
	 * @param value
	 * @param formD
	 * @param nodata
	 * @param zero
	 */
	protected void writeData(float value, DecimalFormat formD, float nodata, boolean zero) {
		if ( (value>nodata) && (value!=nodata || zero) )
			write(formD.format(value)); 
		else
			write("-");
	}

	/**
	 * 
	 * @param String str
	 */
	protected void write(String str) {
		try {
			outbuf.write(str);
		} catch (IOException e) {
			System.out.println("Exc: " + e);
		}
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	protected BufferedWriter openFile(String fileName) {
		try {
			return outbuf = new BufferedWriter (
				new FileWriter(fileName)
			);
		} catch  (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param outbuf
	 */
	protected void closeFile(BufferedWriter outbuf) { //closeHtmlFile
		try {
			outbuf.close();
		} catch  (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param color
	 * @return
	 */
	protected String getStyleColorTD(String color) {
		return ".td1 {\n\tborder-top: 1px double #808080; border-bottom: 1px double #808080; border-left: 1px double #808080; border-right: none; padding: 0.1cm; text-align: center; font-family: Arial, Times New Roman; font-size:13px; background-color: #" + color + ";\n}";
	}

	/**
	 * @param color
	 * @return
	 */
	protected String setColor(String color) {
		return "\"#" + color + "\"";
	}

	// Header of table
	protected void writeMonthYearTableHeader(String[] unitSymbol, String dayOrMonth) {
		write("<div align=\"center\">\r\n");
		write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\" bgcolor=\"#f0f0ff\"  width=\"800\">\r\n");

		// First raw of header
		openRaw();
		write("    <TD class=\"td1bold\" ");
			write(" rowspan=\"2\" width=\"10%\">" + dayOrMonth);
		closeCell(false);
		writeRawOfSummaryTable("1", "7%",  configHtml.getRain(), unitSymbol[RAIN_INDEX]);
		writeRawOfSummaryTable("3", "19%", configHtml.getTemperature(), unitSymbol[TEMPERATURE_INDEX]);
		writeRawOfSummaryTable("3", "19%", configHtml.getHumidity(), unitSymbol[HUMIDITY_INDEX]);
		writeRawOfSummaryTable("3", "19%", configHtml.getPressure(), unitSymbol[PRESSURE_INDEX]);
		writeRawOfSummaryTable("3", "19%", configHtml.getSpeed(), unitSymbol[WINDSPEED_INDEX]);
		writeRawOfSummaryTable("1", "7%",  configHtml.getSunrad(), unitSymbol[SUNRAD_INDEX], "border-right: 1px double #808080;");
		closeRaw();

		// Second raw of header
		openRaw();

		// Rain
		writeCellHd2(true, "", configHtml.getOverall());

		// Temperature
		writeCellHd2(true, "", configHtml.getMax());
		writeCellHd2(true, "", configHtml.getMin());
		writeCellHd2(true, "", configHtml.getAvg());

		// Humidity
		writeCellHd2(true, "", configHtml.getMax());
		writeCellHd2(true, "", configHtml.getMin());
		writeCellHd2(true, "", configHtml.getAvg());

		// Pressure
		writeCellHd2(true, "", configHtml.getMax());
		writeCellHd2(true, "", configHtml.getMin());
		writeCellHd2(true, "", configHtml.getAvg());

		// Wind
		writeCellHd2(true, "", configHtml.getGust());
		writeCellHd2(true, "", configHtml.getWindAvg());
		writeCellHd2(true, "", configHtml.getDirAvg());

		// Sun radiation
		writeCellHd2(true, "border-top: none; border-right: 1px double #808080;", configHtml.getAvg());

		closeRaw();
	}

	/**
	 * Header of rain
	 */
	protected void writeRainMonthYearTableHeader() {
		write("<div align=\"center\">\r\n");
		write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\" bgcolor=\"#f0f0ff\" width=\"400\">\r\n");

		// 1st raw
		openRaw();
		writeRawOfSummaryTable("3", "50%", configHtml.getDaysRain1(), configHtml.getDaysRain2());
		writeRawOfSummaryTable("3", "50%", configHtml.getRain(), null, "border-right: 1px double #808080;");
		closeRaw();

		// 2nd raw
		openRaw();
		writeCell(true, "h&le;0.2");
		writeCell(true, "0.2&lt;h&le;2.0");
		writeCell(true, "h&gt;2.0");
		writeCell(true, configHtml.getOverall() + "<BR>(mm)");
		writeCell(true, configHtml.getRainIntensity() + "<BR>(mm/h)");
		openCell(true, "border-right: 1px double #808080;"); write(configHtml.getDate()); closeCell(true);
		closeRaw();
	}
}
