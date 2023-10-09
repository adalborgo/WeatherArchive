package it.dibis.station;

/**
 * StationMeteofaJs.java
 * API per l'accesso alle risorse della stazione meteo
 * @author Antonio Dal Borgo adalborgo@gmail.com
 *
 * TEST: java TestDebug -test_station
 */

import java.util.Calendar;
import java.util.StringTokenizer;

public class StationMeteofaJs extends StationInterface {

	// Revision control id
	public static final String CVSID = "$Id: StationMeteofaJs.java,v 0.10 02/08/2013 23:59:59 adalborgo@gmail.com $";

	Calendar stationCalendar = Calendar.getInstance(); // Initialize calendar

	float x = 0.0f;

	/**
	 * Load data into stationData object from all channel 
	 * 
	 * error = (+1 | +2 | +4 | -1)
	 */
	public void getDataFromAllChannels() {
		final int TIME_CHANNEL                 =  0;
		final int TEMPERATURE_CHANNEL          =  1;
		final int TEMPERATURE_MIN_CHANNEL      =  2;
		final int TEMPERATURE_MAX_CHANNEL      =  3;
		final int TEMPERATURE_MIN_TIME_CHANNEL =  4;
		final int TEMPERATURE_MAX_TIME_CHANNEL =  5;
		final int HUMIDITY_CHANNEL             =  6;
		final int HUMIDITY_MIN_CHANNEL         =  7;
		final int HUMIDITY_MAX_CHANNEL         =  8;
		final int HUMIDITY_MIN_TIME_CHANNEL    =  9;
		final int HUMIDITY_MAX_TIME_CHANNEL    = 10;
		final int PRESSURE_CHANNEL             = 11;
		final int PRESSURE_MIN_CHANNEL         = 12;
		final int PRESSURE_MAX_CHANNEL         = 13;
		final int PRESSURE_MIN_TIME_CHANNEL    = 14;
		final int PRESSURE_MAX_TIME_CHANNEL    = 15;
		final int WINDSPEED_CHANNEL            = 16;
		final int WINDSPEED_MAX_CHANNEL        = 17;
		final int WINDDIR_CHANNEL              = 18;
		final int WINDSPEED_MAX_TIME_CHANNEL   = 19;
		final int WINDDIR_MAX_SPEED            = 20;
		final int RAINALL_CHANNEL              = 21;
		final int SUNRAD_CHANNEL               = 22;
		final int SUNRAD_MAX_CHANNEL           = 23;
		final int SUNRAD_MAX_TIME_CHANNEL      = 24;

		final int[] ID_DATA = { TIME_CHANNEL,
			TEMPERATURE_CHANNEL, TEMPERATURE_MIN_CHANNEL, TEMPERATURE_MIN_TIME_CHANNEL,
			TEMPERATURE_MAX_CHANNEL, TEMPERATURE_MAX_TIME_CHANNEL,
			HUMIDITY_CHANNEL, HUMIDITY_MIN_CHANNEL, HUMIDITY_MIN_TIME_CHANNEL,
			HUMIDITY_MAX_CHANNEL, HUMIDITY_MAX_TIME_CHANNEL,
			PRESSURE_CHANNEL, PRESSURE_MIN_CHANNEL, PRESSURE_MIN_TIME_CHANNEL,
			PRESSURE_MAX_CHANNEL, PRESSURE_MAX_TIME_CHANNEL,
			WINDSPEED_CHANNEL, WINDDIR_CHANNEL, WINDSPEED_MAX_CHANNEL,
			WINDDIR_MAX_SPEED, WINDSPEED_MAX_TIME_CHANNEL,
			RAINALL_CHANNEL, SUNRAD_CHANNEL
		};

		final String[] ID_NAME = { "currentTimeMillis",
			"temperature", "temperatureMin", "temperatureMinTime",
			"temperatureMax", "temperatureMaxTime",
			"humidity", "humidityMin", "humidityMinTime",
			"humidityMax", "humidityMaxTime", 
			"pressure", "pressureMin", "pressureMinTime",
			"pressureMax", "pressureMaxTime",
			"windSpeed", "windDirection", "windSpeedMax",
			"windDirectionOfMaxSpeed", "windSpeedMaxTime",
			"rainfall", "radiation"
		};

		stationData.clear(); // ClearAllData stationData

		String dataBuffer = dataLoad(loggerPort); // URL_TO_READ
		if (dataBuffer==null || (dataBuffer.length()<=0)) return;

		// Each data line is terminated with a semicolon
		StringTokenizer st = new StringTokenizer(dataBuffer.toString(), ";");

		int nfield = st.countTokens(); // Data fields

		if (DEBUG==2) System.out.println("nfield: " + nfield);

		String varName = null;
		String argument = null;
		int channelIndex = -1; // Channel not found

		for (int i=0; i<nfield; i++) {
				
			String line = st.nextToken(); // Single line of data

			// Set pointer of 'var' and '='
			int pntVar = line.toUpperCase().indexOf("VAR");
			int pntEqu = line.indexOf("=");

			if (pntVar>=0 || pntEqu>pntVar) {
				// Split the line into name and argument
				varName = line.substring(pntVar + "VAR".length() + 1, pntEqu).trim();
				argument = line.substring(pntEqu+1).trim();

				// Strip single quote from argument
				int pntBeg = argument.indexOf("'");
				int pntEnd = argument.indexOf("'", pntBeg+1);
				if (pntBeg>=0 && pntEnd>0) {
					argument = argument.substring(pntBeg+1, pntEnd).trim();
				}

				if (varName.length()>0 && argument.length()>0) {
					// Parse name of variable
					channelIndex = -1; // Channel not found
					for (int ii=0; ii<ID_NAME.length; ii++) {
						if (varName.equals(ID_NAME[ii])) {
							channelIndex = ID_DATA[ii];
							break;
						}
					}
				}
			} // end if (pntVar<0 || pntEqu<0)

			if (DEBUG>0) System.out.println(channelIndex + ": " + varName+"  " + argument);

			// Get data
			switch(channelIndex) {

				case -1: // Not found or no data
						break;

				case TIME_CHANNEL: // get current time in millis
					long timeInMillis = parseLong(argument); // stationData.setTimeInMillis(parseLong(argument));
					stationCalendar.setTimeInMillis(timeInMillis);
					stationData.setDay(stationCalendar.get(Calendar.DAY_OF_MONTH));
					stationData.setMonth(stationCalendar.get(Calendar.MONTH)+1);
					stationData.setYear(stationCalendar.get(Calendar.YEAR));
					stationData.setHour(stationCalendar.get(Calendar.HOUR_OF_DAY));
					stationData.setMinute(stationCalendar.get(Calendar.MINUTE));
					break;

					//---------------------//
					case TEMPERATURE_CHANNEL:
						stationData.setTemperature(parseFloat(argument, TNODATA));
						break;

					case TEMPERATURE_MIN_CHANNEL:
						stationData.setTemperatureMin(parseFloat(argument, TNODATA));
						break;

					case TEMPERATURE_MAX_CHANNEL:
						stationData.setTemperatureMax(parseFloat(argument, TNODATA));
						break;

					case TEMPERATURE_MIN_TIME_CHANNEL:
						stationData.setTemperatureMinTime(getTime(argument));
						break;

					case TEMPERATURE_MAX_TIME_CHANNEL:
						stationData.setTemperatureMaxTime(getTime(argument));
						break;

					//---------------------//
					case HUMIDITY_CHANNEL:
						stationData.setHumidity(parseFloat(argument, -1));
					break;

					case HUMIDITY_MIN_CHANNEL:
						stationData.setHumidityMin(parseFloat(argument, -1));
						break;

					case HUMIDITY_MAX_CHANNEL:
						stationData.setHumidityMax(parseFloat(argument, -1));
						break;

					case HUMIDITY_MIN_TIME_CHANNEL:
						stationData.setHumidityMinTime(getTime(argument));
						break;

					case HUMIDITY_MAX_TIME_CHANNEL:
						stationData.setHumidityMaxTime(getTime(argument));
						break;

					//---------------------//
					case PRESSURE_CHANNEL:
						stationData.setPressure(parseFloat(argument, -1));
					break;

					case PRESSURE_MIN_CHANNEL:
						stationData.setPressureMin(parseFloat(argument, -1));
						break;

					case PRESSURE_MAX_CHANNEL:
						stationData.setPressureMax(parseFloat(argument, -1));
						break;

					case PRESSURE_MIN_TIME_CHANNEL:
						stationData.setPressureMinTime(getTime(argument));
						break;

					case PRESSURE_MAX_TIME_CHANNEL:
						stationData.setPressureMaxTime(getTime(argument));
						break;

					//---------------------//
					case WINDSPEED_CHANNEL: // km/h (dividere per 3.6)
						x = parseFloat(argument, -1)/3.6f;
						stationData.setWindSpeed(x);
					break;

					case WINDSPEED_MAX_CHANNEL:
						x = parseFloat(argument, -1)/3.6f;
						stationData.setWindSpeedMax(x);
						break;

					case WINDDIR_MAX_SPEED:
						stationData.setWindDirectionOfMaxSpeed(parseFloat(argument, -1));
						break;

					case WINDDIR_CHANNEL:
						stationData.setWindDirection(parseFloat(argument, -1));
						break;

					case WINDSPEED_MAX_TIME_CHANNEL:
						stationData.setWindSpeedMaxTime(getTime(argument));
						break;

					//---------------------//
					case RAINALL_CHANNEL:
						stationData.setRain(parseFloat(argument, -1));
						break;

					case SUNRAD_CHANNEL:
						stationData.setSunrad(parseFloat(argument, -1));
						break;
						
					case SUNRAD_MAX_CHANNEL:
						stationData.setSunradMax(parseFloat(argument, -1));
						break;

					case SUNRAD_MAX_TIME_CHANNEL:
						stationData.setSunradMaxTime(getTime(argument));
						break;

					default: // Do nothing

			} // end switch

		} // end for (int i=0; i<nfield; i++)

		// DEBUG
		///stationData.setTemperatureMinTime(-1);
		///stationData.setTemperatureMaxTime(-1);
		if (DEBUG>0) {
			System.out.println("\n--- 258 StatioMeteofaJs.java ---\n");
			stationData.print();
		}

	}

	// --- Method abstract in StationInterface, here not used ---//
	public int open() { return 0; }
	public int synchDataTime() { return 0; }
	public StationData getNextStoredData(String yymmdd) { return null; }  // Not used
	public int getNumberOfStoredData(String yymmdd) { return 0; }  // Not used

	//---------------------------------------//
	// --- Only for Debugging and Testing ---//
	//---------------------------------------//
	/**
	 * Demo lettura dati per DEBUG
	 * Get also date e time 
	 * 
	 * @return StationData
	 */
	public StationData getDemoData() {
		stationData.demoData();
		return stationData;
	}
	
	/**
	 * Print all data channel of the raw
	 * ONLY FOR DEBUG
	 * @param str
	 * @param token
	 */
	private void scanPrintString(String str, String token) {
		if ( str!=null && (str.length()>=0) ) {
			StringTokenizer st = new StringTokenizer(str.toString(), token);

			int nfield = st.countTokens(); // Data fields
			System.out.println("nfield: " + nfield);

			int tokenCounter = 0; // Init
			while (st.hasMoreTokens()) {
				System.out.println("" + tokenCounter + ", " + st.nextToken());
				++tokenCounter;
			}
		}
	}

}
