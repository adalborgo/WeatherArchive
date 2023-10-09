package it.dibis.station;

/**
 * StationDavisPhp.java
 * API per l'accesso alle risorse della stazione meteo
 * @author Antonio Dal Borgo adalborgo@gmail.com
 *
 * TEST: java TestDebug -test_station
 */

import java.util.StringTokenizer;

public class StationDavisPhp extends StationInterface {

	//--- Constants ---//

	// Revision control id
	public static final String CVSID = "$Id: StationDavisPhp.java,v 0.8 16/04/2013 23:59:59 adalborgo@gmail.com $";

	// Pavullo nel Frignano: https://www.meteosystem.com/wlip/pavullo/pavullo.php
	// Monte Cimone: https://www.meteosystem.com/wlip/piancavallaro/piancavallaro.php

	/**
	 * Carica i dati istantanei da ogni singolo canale del logger e li carica nell'oggetto stationData
	 */
	public void getDataFromAllChannels() {

		final String TOKEN = "|";

		final int TIME_CHANNEL = 0;  // Current time
		final int DATE_CHANNEL = 1;  // Current date

		final int TEMPERATURE_CHANNEL          = 2;  // Temperatura istantanea
		final int TEMPERATURE_MIN_CHANNEL      = 11; // Temperatura minima
		final int TEMPERATURE_MAX_CHANNEL      = 14; // Temperatura massima
		final int TEMPERATURE_MIN_TIME_CHANNEL = 12;
		final int TEMPERATURE_MAX_TIME_CHANNEL = 15;
		final int HUMIDITY_CHANNEL             = 3;  // Umidita' istantanea
		final int HUMIDITY_MIN_CHANNEL         = 16; // Umidita' minima
		final int HUMIDITY_MAX_CHANNEL         = 18; // Umidita' massima
		final int HUMIDITY_MIN_TIME_CHANNEL    = 17;  // 
		final int HUMIDITY_MAX_TIME_CHANNEL    = 19;  // 
		final int PRESSURE_CHANNEL             = 7;  // Pressione istantanea
		final int PRESSURE_MIN_CHANNEL         = 29; // Pressione minima
		final int PRESSURE_MAX_CHANNEL         = 31; // Pressione massima
		final int PRESSURE_MIN_TIME_CHANNEL    = 30;  // 
		final int PRESSURE_MAX_TIME_CHANNEL    = 32;  // 
		final int WINDSPEED_CHANNEL            = 44; // Vento velocita' istantanea
		final int WINDSPEED_MAX_CHANNEL        = 47; // Vento velocita' massimo
		final int WINDDIR_CHANNEL              = 45; // Vento direzione
		final int WINDSPEED_MAX_TIME_CHANNEL   = 48; // 
		final int RAINALL_CHANNEL              = 54; // Contatore pioggia
		final int SUNRAD_CHANNEL               = 8;  // Radiazione solare
		final int SUNRAD_MAX_CHANNEL           = 33; // Radiazione solare massima
		final int SUNRAD_MAX_TIME_CHANNEL      = 34; // 

		String line = dataLoad(loggerPort); // URL_TO_READ

		stationData.clear(); // ClearAllData stationData

		StringBuffer dataField = new StringBuffer();

		if ( line!=null && (line.length() >0) ) {

			// I campi sono separati dal carattere TOKEN
			StringTokenizer st = new StringTokenizer(line.toString(), TOKEN);

			int nfield = st.countTokens(); // Data field numbers

			if (DEBUG==2) System.out.println("nfield: " + nfield);

			int meteoDataToken = 0; // Init meteoDataToken

			while (meteoDataToken < nfield && st.hasMoreTokens()) {

				if (meteoDataToken>=nfield) { break; } // Check ArrayIndexOutOfBoundsException

				// More simple replace(int start, String str), with no end!
				dataField.delete(0, dataField.length()); // Empty buffer
				dataField.append(st.nextToken());
				if (DEBUG==2) System.out.println("" + meteoDataToken + ", dataField: " + dataField);

				// Check if meteoDataToken is active
				int channelIndex; // = searchChannel(meteoDataToken);
				channelIndex = meteoDataToken;

				if (channelIndex>=0) { // Channel is active
					if (DEBUG==2) System.out.println("channelIndex, meteoDataToken: "
							+ channelIndex + "\t" + meteoDataToken);

					switch(channelIndex) {
					case TIME_CHANNEL:
						// Time (mm.ss)
						stationData.setHour(parseInt(dataField.toString().substring(0, 2)));
						stationData.setMinute(parseInt(dataField.toString().substring(3, 5)));
						stationData.setSecond(0); // Not used!
						break;

					case DATE_CHANNEL:
						// Date (dd/mm/yy)
						stationData.setDay(parseInt(dataField.toString().substring(0, 2)));
						stationData.setMonth(parseInt(dataField.toString().substring(3, 5)));
						stationData.setYear(2000 + parseInt(dataField.toString().substring(6, 8)));
						break;

					case TEMPERATURE_CHANNEL:
						stationData.setTemperature(parseFloat(dataField.toString(), TNODATA));
						break;

					case TEMPERATURE_MIN_CHANNEL:
						stationData.setTemperatureMin(parseFloat(dataField.toString(), TNODATA));
						break;

					case TEMPERATURE_MAX_CHANNEL:
						stationData.setTemperatureMax(parseFloat(dataField.toString(), TNODATA));
						break;

					case TEMPERATURE_MIN_TIME_CHANNEL:
						stationData.setTemperatureMinTime(getTime(dataField.toString()));
						break;

					case TEMPERATURE_MAX_TIME_CHANNEL:
						stationData.setTemperatureMaxTime(getTime(dataField.toString()));
						break;

					//---------------------//
					case HUMIDITY_CHANNEL:
						stationData.setHumidity(parseFloat(dataField.toString(), -1));
					break;

					case HUMIDITY_MIN_CHANNEL:
						stationData.setHumidityMin(parseFloat(dataField.toString(), -1));
						break;

					case HUMIDITY_MAX_CHANNEL:
						stationData.setHumidityMax(parseFloat(dataField.toString(), -1));
						break;

					case HUMIDITY_MIN_TIME_CHANNEL:
						stationData.setHumidityMinTime(getTime(dataField.toString()));
						break;

					case HUMIDITY_MAX_TIME_CHANNEL:
						stationData.setHumidityMaxTime(getTime(dataField.toString()));
						break;

					//---------------------//
					case PRESSURE_CHANNEL:
						stationData.setPressure(parseFloat(dataField.toString(), -1));
					break;

					case PRESSURE_MIN_CHANNEL:
						stationData.setPressureMin(parseFloat(dataField.toString(), -1));
						break;

					case PRESSURE_MAX_CHANNEL:
						stationData.setPressureMax(parseFloat(dataField.toString(), -1));
						break;

					case PRESSURE_MIN_TIME_CHANNEL:
						stationData.setPressureMinTime(getTime(dataField.toString()));
						break;

					case PRESSURE_MAX_TIME_CHANNEL:
						stationData.setPressureMaxTime(getTime(dataField.toString()));
						break;

					//---------------------//
					case WINDSPEED_CHANNEL:
						stationData.setWindSpeed(parseFloat(dataField.toString(), -1));
					break;

					case WINDSPEED_MAX_CHANNEL:
						stationData.setWindSpeedMax(parseFloat(dataField.toString(), -1));
						break;

					case WINDDIR_CHANNEL:
						stationData.setWindDirection(parseFloat(dataField.toString(), -1));
						break;

					case WINDSPEED_MAX_TIME_CHANNEL:
						stationData.setWindSpeedMaxTime(getTime(dataField.toString()));
						break;

					//---------------------//
					case RAINALL_CHANNEL:
						stationData.setRain(parseFloat(dataField.toString(), -1));
						break;

					case SUNRAD_CHANNEL:
						stationData.setSunrad(parseFloat(dataField.toString(), -1));
						break;
						
					case SUNRAD_MAX_CHANNEL:
						stationData.setSunradMax(parseFloat(dataField.toString(), -1));
						break;

					case SUNRAD_MAX_TIME_CHANNEL:
						stationData.setSunradMaxTime(getTime(dataField.toString()));
						break;

					default: // Do nothing
					} // end switch

				} // end if (check if meteoDataToken is active)

				++meteoDataToken; // Update meteoDataToken counter
			} // end while

		} // end if

		// DEBUG
		if (DEBUG>0) {
			System.out.println("\n-------------------------\n");
			stationData.print();
		}
	}

	//------------------ Private methods ------------------//

	/**
	 * Convert simbol direction to float
	 * @param str
	 * @return float
	 */
	private float convertWindDir(String str) {
		final String [] SWIN_DIR = {"N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W","WNW","NW","NNW"};
		final float  [] WIN_DIR  = {0f,22.5f,45f,67.5f,90f,112.5f,135f,157.5f,180f,202.5f,225f,247.5f,270f,292.5f,315f,337.5f};

		int pnt = -1;
		if ( str!=null && (str.length()>=0) ) {
			for (int i=0; i<SWIN_DIR.length; i++) {
				if (SWIN_DIR[i].equals(str)) {
					pnt = i; break;
				}
			}
		}
		return (pnt>=0)? WIN_DIR[pnt] : -1;
	}

	// --- Method abstract in StationInterface, here not used ---//
	public int open() { return 0; }
	public int synchDataTime() { return 0; }
	public StationData getNextStoredData(String yymmdd) { return null; }  // Not used
	public int getNumberOfStoredData(String yymmdd) { return 0; }  // Not used
}
