package it.dibis.dataObjects;

import it.dibis.common.Constants;

import java.util.TimeZone;

/**
 * @author Antonio Dal Borgo adalborgo@gmail.com
 */

public class SharedData implements Constants {

	// Revision control id
	public static final String CVSID = "$Id: SharedData.java,,v 0.9 27/09/2023 23:53 adalborgo@gmail.com $";

	//  Singleton Object for data sharing
	private static SharedData instance = null;

	//--- Variables ---//
	// Range of values
	private static float[] minValues = new float[DATA_TYPE];
	private static float[] maxValues = new float[DATA_TYPE];

	// Calibration
	private static float[] offset = new float[DATA_TYPE];
	private static float[] factor = new float[DATA_TYPE];

	private static String loggerPort = null;
	private static String davisPort = null;
	private static String deviceNumber = null;

	private static int typeOfData = 0;

	private static int samplesOfDay = 0;

	// Data of station
	private static String stationId = null;
	private static String meteoNetworkId = null;

	private static double latitude;
	private static double longitude;
	private static double altitude;
	private static boolean coordinates = false;

	private static String zoneId = null;

	private static TimeZone timezoneJS = null;

	// Files to save
	private boolean saveJs = false;
	private boolean saveCsv = false;

	private boolean saveHtml = false;
	private boolean saveGraph = false;

	//--- Path of file archive ---//
	private static String rootOfPath = null; // Path of data files .xml

	private static String rootOfXmlPath = null; // Path of data files .xml
	private static String rootOfJsPath = null; // Path of file .js
	private static String rootOfCsvPath = null; // Path of data files .csv

	private static String rootOfHtmlPath = null; // Path of files .html

	//--- Other variables ---//
	// Monthly and yearly rain
	private static float monthRainAll = -1;
	private static float yearRainAll = -1;

	// ---------------------------------------------- //

	/*
	 * DO NOT instantiate with the operator new, use:
	 * SharedData instance = getSingletonObject(); // Create the Singleton Object
	 */

	/**
	 * A private Constructor prevents any other class from instantiating
	 */
	private SharedData() { /* Default constructor */ }

	/**
	 *
	 * @return
	 */
	public static synchronized SharedData getInstance() {
		if (instance == null) instance = new SharedData();
		return instance;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// Range of values
	public void setMinValues(float[] minValues) { this.minValues = minValues; }
	public float[] getMinValues() { return this.minValues; }

	public void setMaxValues(float[] maxValues) { this.maxValues = maxValues; }
	public float[] getMaxValues() { return this.maxValues; }

	public void setOffset(float[] offset) { this.offset = offset; }
	public float[] getOffset() { return this.offset; }

	public void setFactor(float[] factor) { this.factor = factor; }
	public float[] getFactor() { return this.factor; }

	public void setLoggerPort(String loggerPort) { this.loggerPort = loggerPort; }
	public String getLoggerPort() { return this.loggerPort; }

	public void setDavisPort(String davisPort) { this.davisPort = davisPort; }
	public String getDavisPort() { return this.davisPort; }

	public void setDeviceNumber(String deviceNumber) { this.deviceNumber = deviceNumber; }
	public String getDeviceNumber() { return this.deviceNumber; }

	public void setTypeOfData(int typeOfData) { this.typeOfData = typeOfData; }
	public int getTypeOfData() { return this.typeOfData; }

	// Station Id
	public void setStationId(String stationId) { this.stationId = stationId; }
	public String getStationId() { return this.stationId; }

	// MeteoNetwork Id
	public void setMeteoNetworkId(String meteoNetworkId) { this.meteoNetworkId = meteoNetworkId; }
	public String getMeteoNetworkId() { return this.meteoNetworkId; }

	public void setLatitude(double latitude) { this.latitude = latitude; }
	public double getLatitude() { return this.latitude; }

	public void setLongitude(double longitude) { this.longitude = longitude; }
	public double getLongitude() { return this.longitude; }

	public void setAltitude(double altitude) { this.altitude = altitude; }
	public double getAltitude() { return this.altitude; }

	public void setCoordinates(boolean coordinates) { this.coordinates = coordinates; }
	public boolean getCoordinates() { return this.coordinates; }

	public void setZoneId(String zoneId) { this.zoneId = zoneId; }
	public String getZoneId() { return this.zoneId; }

	// timezoneJS
	public void setTimeZoneJS(TimeZone timezoneJS) { this.timezoneJS = timezoneJS; }
	public TimeZone getTimeZoneJS() { return this.timezoneJS; }

	// Files to save
	public void setSaveJs(boolean saveJs) { this.saveJs = saveJs; }
	public boolean getSaveJs() { return this.saveJs; }

	public void setSaveCsv(boolean saveCsv) { this.saveCsv = saveCsv; }
	public boolean getSaveCsv() { return this.saveCsv; }

	public void setSaveHtml(boolean saveHtml) { this.saveHtml = saveHtml; }
	public boolean getSaveHtml() { return this.saveHtml; }

	public void setSaveGraph(boolean saveGraph) { this.saveGraph = saveGraph; }
	public boolean getSaveGraph() { return this.saveGraph; }

	// Path of data files
	public void setRootOfPath(String rootOfPath) { this.rootOfPath = rootOfPath; }
	public String getRootOfPath() { return this.rootOfPath;	}

	public void setRootOfXmlPath(String rootOfXmlPath) { this.rootOfXmlPath = rootOfXmlPath; }
	public String getRootOfXmlPath() { return this.rootOfXmlPath;	}

	public void setRootOfJsPath(String rootOfJsPath) { this.rootOfJsPath = rootOfJsPath; }
	public String getRootOfJsPath() { return rootOfJsPath; }

	public void setRootOfCsvPath(String rootOfCsvPath) { this.rootOfCsvPath = rootOfCsvPath; }
	public String getRootOfCsvPath() { return rootOfCsvPath; }

	public void setRootOfHtmlPath(String rootOfHtmlPath) { this.rootOfHtmlPath = rootOfHtmlPath; }
	public String getRootOfHtmlPath() { return rootOfHtmlPath; }

	public void setMonthRainAll(float monthRainAll) { this.monthRainAll = monthRainAll; }
	public float getMonthRainAll() { return this.monthRainAll; }

	public void setYearRainAll(float yearRainAll) { this.yearRainAll = yearRainAll; }
	public float getYearRainAll() { return this.yearRainAll; }

	//---------------------------------------//
	// ------ For Debugging and Testing -----//
	//---------------------------------------//
	/**
	 * Print config data
	 */
	public String toString() {

		return (
				"Configuration parameters: " + STATION_CONFIG_FILENAME  + "\n" +

						"typeOfData: " + typeOfData  + "\n" +
						"Porta logger: " + loggerPort + "\n" +
						"Porta Davis: " + davisPort + "\n" +
						"deviceNumber: " + deviceNumber  + "\n" +

						"samplesOfDay: " + samplesOfDay  + "\n" +

						"minValues: " + minValues + "\n" +
						"maxValues: " + maxValues + "\n" +

						"offset: " + offset + "\n" +
						"factor: " + factor + "\n" +

						"stationId = " + stationId + "\n" +
						"meteoNetworkId = " + meteoNetworkId + "\n" +
						"latitude = " + latitude + "\n" +
						"longitude = " + longitude + "\n" +
						"altitude = " + altitude + "\n" +
						"coordinates = " + coordinates + "\n" +
						"ZoneId = " + zoneId + "\n" +
						"TimeZoneJS = " + timezoneJS + "\n" +

						"saveJs = " + saveJs + "\n" +
						"saveCsv = " + saveCsv + "\n" +
						"saveHtml = " + saveHtml + "\n" +

						"rootOfPath: " + rootOfPath + "\n" +
						"rootOfXmlPath: " + rootOfXmlPath + "\n" +
						"rootOfJsPath: " + rootOfJsPath + "\n" +
						"rootOfCsvPath: " + rootOfCsvPath + "\n" +

						"rootOfHtmlPath: " + rootOfHtmlPath  + "\n"
		);
	}
}

