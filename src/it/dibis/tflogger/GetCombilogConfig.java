package it.dibis.tflogger;

/*
 * GetCombilogConfig.java
 * Read Combilog config data
 * Tab = 4
 */

// import it.dibis.Common.Constants;

import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;

import it.dibis.common.Constants;

/**
 *
 *
 */
public class GetCombilogConfig implements Constants {
	
	// --- Costanti --- //

	/**
	 *  Revision control id
	 */
	public static final String cvsId = "$Id: GetCombilogConfig.java,,v 0.7 11/02/2013 23:53 adalborgo@gmail.com $";

	// Name of files
	public static final String CONFIG_FILENAME = "Combilog.config";

	private Properties properties = new Properties();

	//--- Variables ---//
	public int[] channelsIndex	= new int[UPPER_STATION_CHANNEL];
	public int[] channelsRawIndex = new int[UPPER_STATION_CHANNEL];

	private int error = 0;

	//-----------------------------------------------------//

	/**
	 * Constructor
	 */
	public GetCombilogConfig() {
		loadConfig();
		// printConfig();
		if (getError()>0) System.out.println("Error = " + getError());
	}

	/**
	 * Load configuration parameters
	 */
	public int loadConfig() {

		boolean isOpen = open(CONFIG_FILENAME, false);
		if (isOpen) {
			try {

				// Get channelsIndex
				String[] chnInx = getValue("channelsIndex").split(",");
				int maxInx = chnInx.length;
				for (int i=0; i<maxInx; i++)
					if (i<channelsIndex.length)
						channelsIndex[i] = Integer.parseInt(chnInx[i].trim());

				// Get channelsRawIndex
				String[] chnRawInx = getValue("channelsRawIndex").split(",");
				int maxChnRawInx = chnRawInx.length;
				for (int i=0; i<maxChnRawInx; i++)
					if (i<channelsRawIndex.length)
						channelsRawIndex[i] = Integer.parseInt(chnRawInx[i].trim());

			} catch (NullPointerException e) {
				error = ERROR_CONFIG_SEVERE;
				System.out.println("Exception: " + e);
			}

		} else {
			error = ERROR_CONFIG_FILENAME_NOT_FOUND;
		}

		// System.out.println("Error: " + error);
		return error;
	}

	/**
	 * 
	 * @return
	 */
	public int getError() {
		return error;
	}

	/**
	 * Apre il file di configurazione
	 * 
	 * @param String configFile	config filename
	 * @param boolean jar	true if config file in jar file
	 *
	 * @return boolean false if file not found
	 */
	public boolean open(String configFile, boolean jar) {
		boolean fileOpen = false;
		
		try {
			if (jar) {
				properties.load(getClass().getResourceAsStream(configFile));
			} else {
				properties.load(new FileInputStream(configFile));
			}

			fileOpen = true;

		} catch (IOException e) {
			System.out.println("Exception: " + e);
		}

		return fileOpen;
	}
	
	/**
	 * Restituisce una stringa col valore corrispondente alla chiave fornita
	 * Elimina eventuali commenti seguiti dal carattere # anche sulla linea dati
	 */
	public String getValue(String key) {
		String keyValue = null;
		try {
			// String getProperty(String key, String defaultValue)
			keyValue = properties.getProperty(key);
			if (keyValue!=null) {
				int pntRem = keyValue.indexOf("#");
				if (pntRem>=0) {
					keyValue = keyValue.substring(0, pntRem).trim();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return keyValue;
	}

	//----------------------------------//
	// --- Only for Debugging and Testing ---//
	//----------------------------------//
	/**
	 * Print config data
	 */
	public void printConfig() {
			System.out.println(
					"Configuration parameters: " + CONFIG_FILENAME  + "\n" +
					"channelsIndex: " + channelsIndex + "\n" +
					"channelsRawIndex: " + channelsRawIndex + "\n"
				);
	}

	public static void main(String args[]) {
		GetCombilogConfig app = new GetCombilogConfig();

		app.loadConfig();
		app.printConfig();
		System.out.println("Error = " + app.getError());
	}

	public static String getCvsid() {
		return cvsId;
	}
	
} // end class
