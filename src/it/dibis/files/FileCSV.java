package it.dibis.files;

import it.dibis.common.Constants;
import it.dibis.dataObjects.DataOfDay;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * Write file ddmmyyyy.csv
 * 
 * Ora;      T;  U;   P;   V;  D;   Pg; Rad;
 * hh:mm;+xx.x;xxx;xxxx;xx.x;xxx;xxx.x;xxxx;
 * 
 * @author adalborgo@gmail.com
 */
public class FileCSV implements Constants {

	// Revision control id
	public static final String cvsId = "$Id: FileCSV.java,v 0.8 28/09/2023 23:59:59 adalborgo $";

	DecimalFormat formD = new DecimalFormat();
	BufferedWriter outbuf = null;

	/**
	 * Append new data line to file
	 *
	 * @param dataOfDay
	 * @param writePathName
	 */
	public void write(DataOfDay dataOfDay, String writePathName) {
		float value;
		try {
			outbuf = new BufferedWriter(new FileWriter(writePathName, true));

			// DataOfDay time
			int hour = dataOfDay.getHour();
			int minute = dataOfDay.getMinute();
			
			// Header
			if (hour==0 && minute==0) {
				outbuf.write("hh:mm;Tempe;Hum;Pressu;WiSp;WiD;RainA;SunRad;\n");
			}

			// Time
			outbuf.write(floatToString(hour, "00") + ":" + floatToString(minute, "00") + ";");

			// Temperature
			value = dataOfDay.getTemperature();
			// outbuf.write(floatToString(value, "'+'00.0;'-'00.0").replace(".", ",") + ";");
			write(value, "'+'00.0;'-'00.0");

			// Humidity
			value = dataOfDay.getHumidity();
			//outbuf.write(floatToString(value, "000").replace(".", ",") + ";");
			write(value, "000");

			// Pressure
			value = dataOfDay.getPressure();
			//outbuf.write(floatToString(value, "0000.0").replace(".", ",") + ";");
			write(value, "0000.0");

			// Wind speed
			value = dataOfDay.getWindSpeed();
			//outbuf.write(floatToString(value, "00.0").replace(".", ",") + ";");
			write(value, "00.0");

			// Wind speed max
//			value = dataOfDay.getWindSpeedMax();
//			outbuf.write(floatToString(value, "00.0").replace(".", ",") + ";");

			// Wind direction
			value = dataOfDay.getWindDirection();
			// outbuf.write(floatToString(value, "000").replace(".", ",") + ";");
			write(value, "000");

			// Rain
			value = dataOfDay.getRain_all();
			//outbuf.write(floatToString(value, "000.0").replace(".", ",") + ";");
			write(value, "000.0");

			// Sunrad
			value = dataOfDay.getSunrad();
			// outbuf.write(floatToString(value, "0000.0").replace(".", ",") + ";");
			write(value, "0000.0");

			outbuf.write("\n");

			// Close file
			outbuf.close();
		} catch  (IOException e) {
			System.out.println("Error: " + e);
		}
	}

	/**
	 * 
	 * @param value
	 * @param pattern
	 * @return
	 */
	private String floatToString(double value, String pattern) {
		return new DecimalFormat(pattern).format(value);
	}

	/**
	 * 
	 * @param value
	 * @param frmt
	 */
	private void write(double value, String frmt) {
		formD.applyPattern(frmt);
		try {
			if (value>-999.0) outbuf.write(formD.format(value).replace(',', '.'));
			outbuf.write(";");
		} catch  (IOException e) {
			System.out.println("Error: " + e);
		}
	}
}
