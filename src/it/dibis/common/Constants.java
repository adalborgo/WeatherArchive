package it.dibis.common;

/**
 * Common constants
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 *
 */
public interface Constants {

	// Revision control id
	public static final String cvsId = "$Id: Constants.java,v 0.18 14/09/2023 23:59:59 adalborgo $";

    final int DAY_SECONDS = 86400;

    final double SOLAR_CONSTANT = 1353; // W/m� (http://en.wikipedia.org/wiki/Sunlight#Solar_constant)

	// Error codes (error_code <0 is fatal --> System.exit(error);
	public final static int ERROR_CONFIG_SEVERE = 1;
	public final static int ERROR_CONFIG_FILENAME_NOT_FOUND = 2;
	public final static int ERROR_PATH_NOT_FOUND = 4;
	public final static int ERROR_MKDIR = 8;
	public final static int ERROR_LOGGER = 16;
	public final static int ERROR_MINMAX = 32;
	public final static int ERROR_CALIBRATION = 64;
	public final static int ERROR_UNIT = 128;

	// Name of files
	public static final String STATION_CONFIG_FILENAME = "station.config";
	public static final String HTML_CONFIG_FILENAME = "html.config";
	public static final String MSGLOGGER_FILENAME = "WeatherHistoryLogger.xml";
	public static final String FLASH_JS_NAME = "flash.js";
	public static final String DATA_NAME = "data";

	public static final float TNODATA = -100.0f; // Data not available for temperature

	public final static int SAMPLES_OF_DAY = 4*24; // Daily samples = 96

	public final static int MINUTES_OF_DAY = 1440;
	public final static int DELTA_TIME = MINUTES_OF_DAY/(SAMPLES_OF_DAY); // 15 minutes

	public static final int TEMPERATURE_INDEX = 0;
	public static final int HUMIDITY_INDEX	  = 1;
	public static final int PRESSURE_INDEX	  = 2;
	public static final int WINDSPEED_INDEX	  = 3;
	public static final int WINDDIR_INDEX	  = 4;
	public static final int RAIN_INDEX		  = 5;
	public static final int SUNRAD_INDEX	  = 6;

	// Types of data provided by station (temperature, humidity, ...)
	public final static int DATA_TYPE = 7; // (= SUNRAD_INDEX + 1)

	// For class DataOfMonth and DataOfYear
	public static final int MONTH_OF_YEAR = 12;

	public static final int ARRAY_DATA_TYPE = 14;

	// Index of the data stored in the arrays of DataOfMonth and DataOfYear
	public static final int TEMPERATURE_MIN_INDEX	= 0;
	public static final int TEMPERATURE_MAX_INDEX	= 1;
	public static final int TEMPERATURE_MEAN_INDEX	= 2;
	public static final int HUMIDITY_MIN_INDEX		= 3;
	public static final int HUMIDITY_MAX_INDEX		= 4;
	public static final int HUMIDITY_MEAN_INDEX		= 5;
	public static final int PRESSURE_MIN_INDEX		= 6;
	public static final int PRESSURE_MAX_INDEX		= 7;
	public static final int PRESSURE_MEAN_INDEX		= 8;
	public static final int WINDSPEED_MAX_INDEX		= 9;
	public static final int WINDSPEED_MEAN_INDEX	= 10;
	public static final int WINDDIR_MEAN_INDEX		= 11;
	public static final int RAINALL_INDEX			= 12;
	public static final int SUNRAD_MEAN_INDEX		= 13;

	// For array dimension (SUNRAD_MAX+1)
	public final static int UPPER_STATION_CHANNEL = 15;

	// Name of the status file
	String WEATHER_STATUS = "status";
}
