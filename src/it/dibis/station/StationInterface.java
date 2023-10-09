package it.dibis.station;

import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import it.dibis.common.Constants;
import it.dibis.dataObjects.SharedData;

/**
 * StationInterface.java
 * API per l'accesso alle risorse della stazione meteo
 * @author Antonio Dal Borgo adalborgo@gmail.com
 *
 * TEST: java TestDebug -test_station
 */

public abstract class StationInterface implements Constants {

	//--- Constants ---//
	// Revision control id
	public static final String CVSID = "$Id: StationInterface.java,v 0.9 03/10/2023 23:59:59 adalborgo@gmail.com $";

	protected final static int DEBUG = 0;

	// --- Variables ---//
	protected int error = 0; // No error

	protected SharedData shared = SharedData.getInstance();

	protected String loggerPort = shared.getLoggerPort();
	protected String deviceNumber = shared.getDeviceNumber() ;

	// All stations use and can share the stationData object
	protected StationData stationData = StationData.getInstance(); // Static object

	/**
	 * @return int 
	 */
	public int getError() {
		return error;
	}

	public abstract void getDataFromAllChannels();

	/**
	 * Clear StationData
	 */
	public synchronized void clearStationData() {
		stationData.clear();
	}

	/**
	 * Open communication port with weather station (for TFCombilog)
	 * @return int error
     */
	public abstract int open();

	/**
	 * Update date and time of station with PC (not used)
	 * @return int error
	 */
	public abstract int synchDataTime();

	public abstract StationData getNextStoredData(String yymmdd);
	public abstract int getNumberOfStoredData(String yymmdd);

	// ----- Utils -----//

	/**
	 * Load raw of data from url
	 * 
	 * @param urlToRead
	 * @return String 
     */
	protected synchronized String dataLoad(String urlToRead) {

		URL url = null;
		try {
			url = new URL(urlToRead);
		} catch (
			MalformedURLException e) {
			System.out.println("Bad URL: " + e.getMessage());
			return null;
		}

		StringBuffer raw = new StringBuffer();
		final int lenBuf = 1024;
		byte[] data = new byte[lenBuf];
		try {
			java.io.BufferedInputStream in = new BufferedInputStream(url.openStream());
			int x=0;
			while((x = in.read(data, 0, lenBuf))>=0) {
					raw.append(getBuffer(data, x));
			}

			in.close();
		} catch (Exception e) {
			System.err.println("Exception error: " + e.toString());
		}

		return raw.toString();
	}

	/**
	 * Converte readBuffer in una stringa
	 */	
	protected String getBuffer(byte[] readBuffer, int lenReadBuffer) {
		StringBuffer str = new StringBuffer(lenReadBuffer);
		for (int i=0; i<lenReadBuffer; i++) str.append((char) readBuffer[i]);
		return str.toString();
	}

	/**
	 * Convert hh.mm into (int) minutes
	 * @param str
	 * @return int  (-1: on error)
	 */
	protected int getTime(String str) {
		// Time (hh.mm)
		int pnt = str.indexOf(":");
		if (pnt<1) return -1;
		int hours = parseInt(str.substring(0, pnt));
		int minutes = (hours>=0) ? parseInt(str.substring(pnt+1)) : -1;
		return ((hours>=0 && hours<=24) && (minutes>=0 && minutes<60)) ? 60*hours + minutes : -1;
	}

	/**
	 * @param str
	 * @return int  (nodata = -1, on error) 
	 */
	protected int parseInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			System.out.println(e);
			return -1;
		}
	}

	/**
	 * @param str
	 * @return long  (-1: on error)
	 */
	protected long parseLong(String str) {
		try {
			return Long.parseLong(str);
		} catch (NumberFormatException e) {
			System.out.println(e);
			return -1;
		}
	}

	/**
	 * @param str
	 * @return float (nodata: on error)
	 */
	protected float parseFloat(String str, float nodata) {
		if (str==null || str.isEmpty()) return nodata;
		try {
			return Float.parseFloat(str);
		} catch (NumberFormatException e) {
			System.out.println(e);
			return nodata;
		}
	}
}
