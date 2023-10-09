package it.dibis.html;

/*
 * FileJS.java
 * Writes file 'data.js' from dataOfday object
 *
 * @author adalborgo@gmail.com
 * Note: DateTime for data.js uses ZoneId = System Default)
 */

import it.dibis.common.Constants;
import it.dibis.common.SunData;
import it.dibis.common.Utils;
import it.dibis.common.CalendarUtils;
import it.dibis.config.ConfigHtml;
import it.dibis.dataObjects.DataOfDay;
import it.dibis.dataObjects.SharedData;
import it.dibis.dataLogger.MeteoFunctions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class FileDataJS implements Constants {

	// Revision control id
	public static final String cvsId = "$Id: FileDataJs.java,v 0.28 05/10/2023 23:59:59 adalborgo $";

	DecimalFormat formD = new DecimalFormat();
	MeteoFunctions funct = new MeteoFunctions();
	BufferedWriter outbuf = null;

	SharedData shared = SharedData.getInstance();

	ConfigHtml configHtml = ConfigHtml.getInstance();
	float windvelUnitFactor = configHtml.getUnitFactor()[WINDSPEED_INDEX];

	// Geographic coordinates of the station
	double latitude = shared.getLatitude();   // North: positive; South: negative
	double longitude = shared.getLongitude(); // West: positive; East: negative;
	boolean coordinates = shared.getCoordinates();
	boolean dst = false; // Daylight saving time
	TimeZone timeZone = shared.getTimeZoneJS();
	Calendar calendar = SunData.setCalendar(timeZone);

	SunData sunData; // = new SunData(latitude, longitude, calendar);

	//--------------------------------------------------------------//
	public FileDataJS() { /* Default constructor */ }

	public void genJs(DataOfDay dataOfDay, float monthRainAll, float yearRainAll,
					  TimeZone timeZone, String writePathName) {

		this.dst = CalendarUtils.getDST(new Date(), timeZone.getID());

		double value;
		try {
			outbuf = new BufferedWriter(new FileWriter(writePathName));

			// Current Time
			// LocalDateTime.now(ZoneId.systemDefault())
			write("currentTimeMillis", System.currentTimeMillis(), "#");

			/*
			long yourmilliseconds = System.currentTimeMillis();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			Date resultdate = new Date(yourmilliseconds);
			System.out.println("FileDatJS -> System.currentTimeMillis(): " + sdf.format(resultdate));
			*/

			// Temperature
			writeTemperature("temperature", dataOfDay.getTemperature(), "0.0");

			// Temperature min
			writeTemperature("temperatureMin", dataOfDay.getTemperatureMin(), "0.0");
			writeTimeInMinute("temperatureMinTime", dataOfDay.getTemperatureMinTime());

			// Temperature max
			writeTemperature("temperatureMax", dataOfDay.getTemperatureMax(), "0.0");
			writeTimeInMinute("temperatureMaxTime", dataOfDay.getTemperatureMaxTime());

			// Humidity
			value = dataOfDay.getHumidity();
			if (value<0) value = 0;
			write("humidity", value, "#");

			// Humidity min
			value = dataOfDay.getHumidityMin();
			if (value<0) value = 0;
			write("humidityMin", value, "#");
			writeTimeInMinute("humidityMinTime", dataOfDay.getHumidityMinTime());

			// Humidity max
			value = dataOfDay.getHumidityMax();
			if (value<0) value = 0;
			write("humidityMax", value, "#");
			writeTimeInMinute("humidityMaxTime", dataOfDay.getHumidityMaxTime());

			// Pressure
			value = dataOfDay.getPressure();
			if (value<0) value = 0;
			write("pressure", value, "0.0");

			// Pressure min
			value = dataOfDay.getPressureMin();
			if (value<0) value = 0;
			write("pressureMin", value, "0.0");
			writeTimeInMinute("pressureMinTime", dataOfDay.getPressureMinTime());

			// Pressure max
			value = dataOfDay.getPressureMax();
			if (value<0) value = 0;
			write("pressureMax", value, "0.0");
			writeTimeInMinute("pressureMaxTime", dataOfDay.getPressureMaxTime());

			// Wind speed
			value = dataOfDay.getWindSpeed();
			if (value>0) {
				value = windvelUnitFactor*value;
			} else {
				value = 0;
			}
			write("windSpeed", value, "#");

			// Wind direction
			value = dataOfDay.getWindDirection();
			if (!(value>=0&& value<=360)) value = 0;
			write("windDirection", value, "#");

			// Max data of wind
			formD.applyPattern("#");	// Solo cifre intere
			value = dataOfDay.getWindSpeedMax();
			if (value>0) {
				value = windvelUnitFactor*value;
			} else {
				value = 0;
			}
			write("windSpeedMax", value, "#");

			// Direction and time of gust
			if (dataOfDay.getWindSpeedMax()>0) {
				write("windDirectionOfMaxSpeed", dataOfDay.getWindDirectionOfMaxSpeed(), "#");
				writeTimeInMinute("windSpeedMaxTime", dataOfDay.getWindSpeedMaxTime());
			} else { // No data valid
				write("windDirectionOfMaxSpeed", "");
				write("windSpeedMaxTime", "");
			}

			// Rainfall
			value = dataOfDay.getRain_all();
			if (value<0) value = 0.0f;
			write("rainfall", value, "#.#");

			//----- AGGIUNTE -----//
			// Rainfall of the month
			value = monthRainAll;
			if (value<0) value = 0.0f;
			write("rainfallMonth", value, "#.#");

			// Rainfall of the year
			value = yearRainAll;
			if (value<0) value = 0.0f;
			write("rainfallYear", value, "#.#");

			// Solar radiation
			value = dataOfDay.getSunrad();
			if (value<0) value = 0;
			write("radiation", value, "0.0");

			value = dataOfDay.getSunradMax();
			if (value<0) value = 0;
			write("radiationMax", value, "0.0");
			writeTimeInMinute("radiationMaxTime", dataOfDay.getSunradMaxTime());

			// Heat index
			writeTemperature("heatIndex", funct.heatIndex(dataOfDay.getTemperature(), dataOfDay.getHumidity()), "0.0");

			// windChill
			writeTemperature("windChill", funct.windChill(dataOfDay.getTemperature(), dataOfDay.getWindSpeed()), "0.0");

			// Dew point
			writeTemperature("dewPoint", funct.dewPoint(dataOfDay.getTemperature(), dataOfDay.getHumidity()), "0.0");

			// Sunrise, sunset, etc.
			if (coordinates) { //http://it.wikipedia.org/wiki/Radiazione_solare_globale_in_Italia

				sunData = new SunData(latitude, longitude, calendar);
				int offsetFromUTC = sunData.getOffsetFromUTC(timeZone);

				write("sunrise", sunData.getSunrise(offsetFromUTC));
				write("sunset", sunData.getSunset(offsetFromUTC));
				write("sunNoon", sunData.getSunNoon(offsetFromUTC)); // Orario del mezzogiorno solare

				write("trueSolarTime", sunData.getSolarTime()); // Not add DST
				// writeTimeInHour("trueSolarTime", sunData.getSolarTimeInMinute()); // Add DST

				write("sunAzimuth", sunData.getAzimuth("0.0").replace(',', '.'));
				write("sunAltitude", sunData.getAltitude("0.0").replace(',', '.'));

				if(dataOfDay.getSunrad()>10 && sunData.getAltitude()>5) {
					value = (100*dataOfDay.getSunrad())/(SOLAR_CONSTANT *Math.sin(Math.PI*sunData.getAltitude()/180));
				} else {
					value = -1;
				}

				write("skytransparency", value, "0");
			}

			// Close file
			outbuf.close();
		} catch  (IOException e) {
			System.out.println("Error: " + e);
		}
	}

	/**
	 *
	 * @param varStr
	 * @param value
	 * @param frmt
	 */
	private void write(String varStr, double value, String frmt) {
		formD.applyPattern(frmt);
		try {
			outbuf.write("var " + varStr + " = '");
			if (value>=0)
				outbuf.write(formD.format(value).replace(',', '.'));
			outbuf.write("';\r\n");
		} catch  (IOException e) {
			System.out.println("Error: " + e);
		}
	}

	/**
	 *
	 * @param varStr
	 * @param value
	 * @param frmt
	 */
	private void writeTemperature(String varStr, double value, String frmt) {
		formD.applyPattern(frmt);
		try {
			outbuf.write("var " + varStr + " = '");
			if (value>TNODATA)
				outbuf.write(formD.format(value).replace(',', '.'));
			outbuf.write("';\r\n");
		} catch  (IOException e) {
			System.out.println("Error: " + e);
		}
	}

	/**
	 *
	 * @param varStr
	 * @param iTime
	 */
	private void writeTimeInMinute(String varStr, int iTime) {
		if (dst) iTime += 60; // Add one hour for daylight saving time
		if(iTime>=0) iTime = (iTime)%1440;
		try {
			outbuf.write("var " + varStr + " = '");
			if (iTime>=0)
				outbuf.write(Utils.minutesToHHMM(iTime));
			outbuf.write("';\r\n");
		} catch  (IOException e) {
			System.out.println("Error: " + e);
		}
	}

	/**
	 *
	 * @param varStr
	 * @param data
	 */
	private void write(String varStr, String data) {
		try {
			outbuf.write("var " + varStr + " = '" + data + "';\r\n");
		} catch  (IOException e) {
			System.out.println("Error: " + e);
		}
	}
}
