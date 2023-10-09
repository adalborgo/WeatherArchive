package it.dibis.station;

import java.util.Calendar;
import it.dibis.common.Constants;

/**
 * StationData.java
 * Azione : Manage current data from weather station
 * Nota   : La classe viene gestita come singleton
 *          Aggiunto il metodo demoData()
 * @author: Antonio Dal Borgo
 */

public class StationData implements Constants {

	// --- Costanti --- //

	/**
	 *  Revision control id
	 */
	private final static String cvsId = "$Id: StationData.java,v 0.7 16/04/2013 22:00:00 adalborgo $";

	// Singleton
	private static StationData instance = null;

	//--- Variabili ---//
	protected static Calendar calendar; // Added 01-09-2023
	protected static int day, month, year;
	protected static int hour, minute, second;

	private static float temperature, temperatureMin, temperatureMax;
	private static int temperatureMinTime, temperatureMaxTime;
   
	private static float humidity, humidityMin, humidityMax;
	private static int humidityMinTime, humidityMaxTime;

	private static float pressure, pressureMin, pressureMax;
	private static int pressureMinTime, pressureMaxTime;

	private static float windSpeed, windSpeedMax;
	private static float windDirection, windDirectionOfMaxSpeed;
	private static int windSpeedMaxTime;

	private static float rain;

	private static float sunrad;
	private static float sunradMax;
	private static int sunradMaxTime;

   	// Data Range (min/max)
   	public static float[] minValues; 
   	public static float[] maxValues;
	float[] offset;
	float[] factor;

	/*
	 * DO NOT instantiate with the operator new, use:
	 * StationData stationData = getSingletonObject(); // Create the Singleton Object
	*/

	/**
	 * A private Constructor prevents any other class from instantiating
	 */
	private StationData() { /* Default constructor */ }

	/**
	 * 
	 * @return StationData
	 */
	public static synchronized StationData getInstance() {
		if (instance == null) instance = new StationData();
		return instance;
	}

	public Object clone()throws CloneNotSupportedException {
		throw new CloneNotSupportedException(); 
	}

	/**
	 * 
	 * @return cvsId
	 */
	public static String getCvsid() {
		return cvsId;
	}

	// ---------------------------------------------- //
	// Header
	public synchronized void setCalendar(Calendar cal) { this.calendar = cal; }
	public synchronized Calendar  getCalendar() { return calendar; }

	public synchronized void setDay(int day) { this.day = day; }
	public synchronized void setMonth(int month) { this.month = month; }
	public synchronized void setYear(int year) { this.year = year; }
	public synchronized void setHour(int hour) { this.hour = hour; }
	public synchronized void setMinute(int minute) { this.minute = minute; }
	public synchronized void setSecond(int second) { this.second = second; }

	public synchronized int  getDay() { return day; }
	public synchronized int  getMonth() { return month; }
	public synchronized int  getYear() { return year; }
	public synchronized int  getHour() { return hour; }
	public synchronized int  getMinute() { return minute; }
	public synchronized int  getSecond() { return second; }

	// Temperature
	public synchronized void setTemperature(float x) { temperature = x; }
	public synchronized void setTemperatureMin(float x) { temperatureMin = x; }
	public synchronized void setTemperatureMax(float x) { temperatureMax = x; }
	public synchronized void setTemperatureMinTime(int x) { temperatureMinTime = x; }
	public synchronized void setTemperatureMaxTime(int x) { temperatureMaxTime = x; }

	public synchronized float getTemperature() { return temperature; }
	public synchronized float getTemperatureMin() { return temperatureMin; }
	public synchronized float getTemperatureMax() { return temperatureMax; }
	public synchronized int   getTemperatureMinTime() { return temperatureMinTime; }
	public synchronized int   getTemperatureMaxTime() { return temperatureMaxTime; }

	// Humidity
	public synchronized void setHumidity(float x) { humidity = x; }
	public synchronized void setHumidityMin(float x) { humidityMin = x; }
	public synchronized void setHumidityMax(float x) { humidityMax = x; }
	public synchronized void setHumidityMinTime(int x) { humidityMinTime = x; }
	public synchronized void setHumidityMaxTime(int x) { humidityMaxTime = x; }

	public synchronized float getHumidity() { return humidity; }
	public synchronized float getHumidityMin() { return humidityMin; }
	public synchronized float getHumidityMax() { return humidityMax; }
	public synchronized int   getHumidityMinTime() { return humidityMinTime; }
	public synchronized int   getHumidityMaxTime() { return humidityMaxTime; }

	// Pressure
	public synchronized void setPressure(float x) { pressure = x; }
	public synchronized void setPressureMin(float x) { pressureMin = x; }
	public synchronized void setPressureMax(float x) { pressureMax = x; }
	public synchronized void setPressureMinTime(int x) { pressureMinTime = x; }
	public synchronized void setPressureMaxTime(int x) { pressureMaxTime = x; }

	public synchronized float getPressure() { return pressure; }
	public synchronized float getPressureMin() { return pressureMin; }
	public synchronized float getPressureMax() { return pressureMax; }
	public synchronized int   getPressureMinTime() { return pressureMinTime; }
	public synchronized int   getPressureMaxTime() { return pressureMaxTime; }

	// Wind
	public synchronized void setWindSpeed(float x) { windSpeed = x; }
	public synchronized void setWindSpeedMax(float x) { windSpeedMax = x; }
	public synchronized void setWindSpeedMaxTime(int x) { windSpeedMaxTime = x; }

	public synchronized void setWindDirection(float x) { windDirection = x; }
	public synchronized void setWindDirectionOfMaxSpeed(float x) {
		windDirectionOfMaxSpeed = x; }

	public synchronized float getWindSpeed() { return windSpeed; }
	public synchronized float getWindSpeedMax() { return windSpeedMax; }
	public synchronized int   getWindSpeedMaxTime() { return windSpeedMaxTime; }

	public synchronized float getWindDirection() { return windDirection; }
	public synchronized float getWindDirectionOfMaxSpeed() { return windDirectionOfMaxSpeed; }

	// Rain
	public synchronized void setRain(float x) { rain = x; }
	public synchronized float getRain() { return rain; }

	// Solar Radiation
	public synchronized void setSunrad(float x) { sunrad = x; }
	public synchronized void setSunradMax(float x) { sunradMax = x; }
	public synchronized void setSunradMaxTime(int x) { sunradMaxTime = x; }

	public synchronized float getSunrad() { return sunrad; }
	public synchronized float getSunradMax() { return sunradMax; }
	public synchronized int   getSunradMaxTime() { return sunradMaxTime; }

	/**
	 * Clear all data
	 */
	public void clear() {
		day = month = year = -1;
		hour = minute = -1;

		temperature = TNODATA;
		temperatureMin = temperatureMax = TNODATA;
		temperatureMinTime = temperatureMaxTime = -1;

		humidity = -1;
		humidityMin = humidityMax = -1;
		humidityMinTime = humidityMaxTime = -1;

		pressure = -1;
		pressureMin = pressureMax = -1;
		pressureMinTime = pressureMaxTime = -1;

		windSpeed = -1;
		windSpeedMax = -1;
		windSpeedMaxTime = -1;
		windDirection = -1;
		windDirectionOfMaxSpeed = -1;

		rain = -1;

		sunrad = -1;
		sunradMax = -1;
		sunradMaxTime = -1;
	}

	//------------------- Solo per Debug --------------------//

	/**
	 * Demo data por debug
	 */
	public void demoData() {
		day = 27;
		month = 8;
		year = 2012;
		hour = 15;
		minute = 27;

		temperature = 28.4f;
		temperatureMin = 16.4f;
		temperatureMax = 30.7f;
		temperatureMinTime = 6*60+34;
		temperatureMaxTime = 14*60+47;

		humidity = 30f;
		humidityMin = 27f;
		humidityMax = 86f;
		humidityMinTime = 60*15+8;
		humidityMaxTime = 60*5+19;

		pressure = 1017f;
		pressureMin = 1014f;
		pressureMax = 1019f;
		pressureMinTime = 1*60+0;
		pressureMaxTime = 10*60+14;

		windSpeed = 1.2f; // m/s
		windSpeedMax = 21f;
		windSpeedMaxTime = 12*60+50;
		windDirection = 118;
		windDirectionOfMaxSpeed = 123;

		rain = 0;

		sunrad = 680;
		sunradMax = -1;
		sunradMaxTime = -1;
	}

	public static synchronized void print() {

		// Header
		System.out.print("StationData >>> " + hour + ":" + minute + ":" + second + "\t" + day + "-" + month + "-" + year);

		// Temperature
		System.out.println("\nTemperature"); 
		System.out.println("t: " + temperature + " celsius");
		System.out.print("temperatureMin: " + temperatureMin + " celsius");
		System.out.println("  (" + temperatureMinTime/60 +":" + temperatureMinTime%60+")");
		System.out.print("temperatureMax: " + temperatureMax + " celsius");
		System.out.println("  (" + temperatureMaxTime/60 + ":" + temperatureMaxTime%60+")");

		// Humidity
		System.out.println("\nHumidity");
		System.out.println("u: " + humidity + " %");
		System.out.print("humidityMin: " + humidityMin + " %");
		System.out.println("  (" + humidityMinTime/60 +":" + humidityMinTime%60+")");
		System.out.print("humidityMax: " + humidityMax + " %");
		System.out.println("  (" + humidityMaxTime/60 + ":" + humidityMaxTime%60+")");

		// Wind
		System.out.println("\nWind");
		System.out.println("w: " + windSpeed + " m/s");
		System.out.print("windSpeedMax: " + windSpeedMax + " m/s");
		System.out.println("  (" + windSpeedMaxTime/60 + ":" + windSpeedMaxTime%60+")");
		System.out.println("windDirection: " + windDirection + " gradi");
		System.out.println("windDirectionOfMaxSpeed: " + windDirectionOfMaxSpeed + " gradi");

		// Pressure
		System.out.println("\nPressure");
		System.out.println("p: " + pressure + " hPa");
		System.out.print("pressureMin: " + pressureMin + " hPa");
		System.out.println("  (" + pressureMinTime/60 +":" + pressureMinTime%60+")");
		System.out.print("pressureMax: " + pressureMax + " hPa");
		System.out.println("  (" + pressureMaxTime/60 + ":" + pressureMaxTime%60+")");

		// Rain of day
		System.out.println("\nRain of day");
		System.out.println("rain: " + rain + " mm");

		// Solar radiation
		System.out.println("\nSolar radiation");
		System.out.println("sunrad: " + sunrad + " W/m^2");
		System.out.println("sunradMax: " + sunradMax + " W/m^2");
		System.out.println("  (" + sunradMaxTime/60 + ":" + sunradMaxTime%60+")");
	}

}
