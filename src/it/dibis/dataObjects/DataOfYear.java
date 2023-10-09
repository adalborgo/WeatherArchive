package it.dibis.dataObjects;

import it.dibis.common.Constants;

/**
 * @author: Antonio Dal Borgo (adalborgo@gmail.com)
 *
 */
public class DataOfYear implements Constants {

	// --- Constants --- //
    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: DataOfYear.java,v 0.12 26/04/2013 23:59:59 adalborgo $";

	//--- Variables ---//
	private String stationId;

	private int year;

	private float temperatureMin = TNODATA;
	private float temperatureMax = TNODATA;
	private float temperatureMean= TNODATA;

	private float humidityMin = -1;
	private float humidityMax = -1;
	private float humidityMean	= -1;

	private float pressureMin = -1;
	private float pressureMax = -1;
	private float pressureMean	= -1;

	private float windSpeedMax = -1;
	private float windSpeedMean = -1;
	private float windDirectionMean = -1;

	private float rain_all = -1;
	private float rainRateMax = -1; // mm/h
	private int rainRateMaxMonth; // Minutes
	private int rain02 = -1;
	private int rain2 = -1;
	private int rain20 = -1;

	private float sunradMean = -1;

	// Temporary data for getDataArray(int sample) and calcMean
	private float temperatureMinBuffer;
	private float temperatureMaxBuffer;
	private float temperatureMeanBuffer;
	private float humidityMinBuffer;
	private float humidityMaxBuffer;
	private float humidityMeanBuffer;
	private float pressureMinBuffer;
	private float pressureMaxBuffer;
	private float pressureMeanBuffer;
	private float windSpeedMaxBuffer;
	private float windSpeedMeanBuffer;
	private float windDirectionMeanBuffer;
	private float rain_allBuffer;
	private float sunradBuffer;

	// Index = 0 is for summary data
	private float[][] dataArray = new float[ARRAY_DATA_TYPE][MONTH_OF_YEAR+1];

	/**
	 * Constructor
	 */
	public DataOfYear() { /* Default constructor */}

	public void init(int year) {
		clearDataArray(); // Set temperature[] = TNODATA

		this.year = year;
		setYear(year);
		setStationId("");
	}

	// ---------------------------------------------- //

	// Id
	public synchronized void setStationId(String stationId) { this.stationId = stationId; }
	public synchronized String getStationId() { return this.stationId; }

	// Year
	public synchronized void setYear(int year) { this.year = year; }
	public synchronized int  getYear() { return this.year; }

	// Temperature
	public synchronized void  setTemperatureMin(float temperatureMin) { this.temperatureMin = temperatureMin; }
	public synchronized void  setTemperatureMax(float temperatureMax) { this.temperatureMax = temperatureMax; }
	public synchronized void  setTemperatureMean(float temperatureMean) { this.temperatureMean = temperatureMean; }

	public synchronized float getTemperatureMin() { return this.temperatureMin; }
	public synchronized float getTemperatureMax() { return this.temperatureMax; }
	public synchronized float getTemperatureMean() { return this.temperatureMean; }

	// Humidity
	public synchronized void  setHumidityMin(float humidityMin) { this.humidityMin = humidityMin; }
	public synchronized void  setHumidityMax(float humidityMax) { this.humidityMax = humidityMax; }
	public synchronized void  setHumidityMean(float humidityMean) { this.humidityMean = humidityMean; }

	public synchronized float getHumidityMin() { return this.humidityMin; }
	public synchronized float getHumidityMax() { return this.humidityMax; }
	public synchronized float getHumidityMean() { return this.humidityMean; }

	// Pressure
	public synchronized void  setPressureMin(float pressureMin) { this.pressureMin = pressureMin; }
	public synchronized void  setPressureMax(float pressureMax) { this.pressureMax = pressureMax; }
	public synchronized void  setPressureMean(float pressureMean) { this.pressureMean = pressureMean; }

	public synchronized float getPressureMin() { return this.pressureMin; }
	public synchronized float getPressureMax() { return this.pressureMax; }
	public synchronized float getPressureMean() { return this.pressureMean; }

	// Wind
	public synchronized void  setWindSpeedMax(float windSpeedMax) { this.windSpeedMax = windSpeedMax; }
	public synchronized void  setWindSpeedMean(float windSpeedMean) { this.windSpeedMean = windSpeedMean; }
	public synchronized void  setWindDirectionMean(float windDirectionMean) { this.windDirectionMean = windDirectionMean; }

	public synchronized float getWindSpeedMax() { return this.windSpeedMax; }
	public synchronized float getWindSpeedMean() { return this.windSpeedMean; }
	public synchronized float getWindDirectionMean() { return this.windDirectionMean; }

	// Rain
	public synchronized void setRain_all(float rain_all) { this.rain_all = rain_all; }
	public synchronized void setRainRateMax(float rainRateMax) { this.rainRateMax = rainRateMax; }
	public synchronized void setRainRateMaxMonth(int rainRateMaxMonth) { this.rainRateMaxMonth = rainRateMaxMonth; }
	public synchronized void setRain02(int rain02) { this.rain02 = rain02; }
	public synchronized void setRain2(int rain2) { this.rain2 = rain2; }
	public synchronized void setRain20(int rain20) { this.rain20 = rain20; }

	public synchronized float getRain_all() { return this.rain_all; }
	public synchronized float getRainRateMax() { return this.rainRateMax; }
	public synchronized int getRainRateMaxMonth() { return this.rainRateMaxMonth; }
	public synchronized int getRain02() { return this.rain02; }
	public synchronized int getRain2() { return this.rain2; }
	public synchronized int getRain20() { return this.rain20; }

	// Solar Radiation
	public synchronized void setSunradMean(float sunradMean) { this.sunradMean = sunradMean; }
	public synchronized float getSunradMean() { return this.sunradMean; }

	//--- Array of monthly data for every month ---//

	/**
	 * Update dataArray[]
	 * @param int index index of array
	 */
	public synchronized void setDataArray(int index) {
		if (index<MONTH_OF_YEAR) {
			dataArray[TEMPERATURE_MIN_INDEX][index] = temperatureMin;
			dataArray[TEMPERATURE_MAX_INDEX][index] = temperatureMax;
			dataArray[TEMPERATURE_MEAN_INDEX][index]= temperatureMean;
			dataArray[HUMIDITY_MIN_INDEX][index]    = humidityMin;
			dataArray[HUMIDITY_MAX_INDEX][index]    = humidityMax;
			dataArray[HUMIDITY_MEAN_INDEX][index]   = humidityMean;
			dataArray[PRESSURE_MIN_INDEX][index]    = pressureMin;
			dataArray[PRESSURE_MAX_INDEX][index]    = pressureMax;
			dataArray[PRESSURE_MEAN_INDEX][index]   = pressureMean;
			dataArray[WINDSPEED_MAX_INDEX][index]	= windSpeedMax;
			dataArray[WINDSPEED_MEAN_INDEX][index]	= windSpeedMean;
			dataArray[WINDDIR_MEAN_INDEX][index]    = windDirectionMean;
			dataArray[RAINALL_INDEX][index]        	= rain_all;
			dataArray[SUNRAD_MEAN_INDEX][index]     = sunradMean;
		}
	}

	/**
	 * Update dataArray[]
	 * Important! Used by XMLEncoder
	 * @param float[][] dataArray
	 */
	public synchronized void setDataArray(float[][] dataArray) {
		this.dataArray = dataArray;
	}

	/**
	 * Update single element of the array
	 * @param value
	 * @param type
	 * @param sample
	 */
	public synchronized void setDataArray(float value, int type, int sample) {
		this.dataArray[type][sample] = value;
	}

	/**
	 * Important! Used by XMLEncoder
	 */
	public synchronized float[][] getDataArray() {
		return this.dataArray;
	}

	/**
	 * Return data of array
	 * @param int type, int month
	 * @return float
     */
	public synchronized float getDataArray(int type, int month) {
		return this.dataArray[type][month];
	}

	/**
	 * Extract a raw from dataArray
	 * @param int sample of month
	 */
	public synchronized void getRawOfDataArray(int month) {
		if (month<MONTH_OF_YEAR) {
			temperatureMinBuffer = dataArray[TEMPERATURE_MIN_INDEX][month];
			temperatureMaxBuffer = dataArray[TEMPERATURE_MAX_INDEX][month];
			temperatureMeanBuffer = dataArray[TEMPERATURE_MEAN_INDEX][month];
			
			humidityMinBuffer = dataArray[HUMIDITY_MIN_INDEX][month];
			humidityMaxBuffer = dataArray[HUMIDITY_MAX_INDEX][month];
			humidityMeanBuffer = dataArray[HUMIDITY_MEAN_INDEX][month];
			
			pressureMinBuffer = dataArray[PRESSURE_MIN_INDEX][month];
			pressureMaxBuffer = dataArray[PRESSURE_MAX_INDEX][month];
			pressureMeanBuffer = dataArray[PRESSURE_MEAN_INDEX][month];
			
			windSpeedMaxBuffer = dataArray[WINDSPEED_INDEX][month];
			windSpeedMeanBuffer = dataArray[WINDSPEED_MEAN_INDEX][month];
			windDirectionMeanBuffer = dataArray[WINDDIR_MEAN_INDEX][month];

			rain_allBuffer = dataArray[RAINALL_INDEX][month];
			sunradBuffer = dataArray[SUNRAD_MEAN_INDEX][month];
		}
	}

	/**
	 * Return lenght of array
	 * @return int
     */
	public synchronized int getDataArrayLength() {
		return this.dataArray[0].length;
	}

	/**
	 * Clear all data of array 
	 */
	public synchronized void clearDataArray() {
		for (int dataIndex = 0; dataIndex<getDataArrayLength(); dataIndex++) {
			for (int typeIndex = 0; typeIndex<ARRAY_DATA_TYPE; typeIndex++) {
				if (typeIndex==TEMPERATURE_MIN_INDEX || typeIndex==TEMPERATURE_MAX_INDEX || typeIndex==TEMPERATURE_MEAN_INDEX) {
					dataArray[typeIndex][dataIndex] = TNODATA;
				} else {
					dataArray[typeIndex][dataIndex] = -1;
				}
			}
		}
    }

	// ---------------------------------------------- //
	/**
	 * Clear current data 
	 */
	public synchronized void clearSummaryData() {

		this.stationId = null; // Data not yet assigned

		temperatureMin = temperatureMax = temperatureMean = TNODATA;
		humidityMin = humidityMax = humidityMean = -1;
		pressureMin = pressureMax = pressureMean = -1;
		windSpeedMax = windSpeedMean = -1;
		windDirectionMean = -1;
		rain_all = rainRateMax = -1;
		rain02 = rain2 = rain20 = -1;
		rainRateMaxMonth = -1;
		sunradMean = -1;
    }

	/**
	 * Clear all data
	 */
    public synchronized void clearAllData() {
		clearSummaryData();   
        clearDataArray();
    }

	/**
     * Get statistics for one month
     */
    public void calculateAndSetSummaryData() {

		float tempMeanSum	= 0;
		float umidMeanSum	= 0;
		float wvelMeanSum	= 0;
		float wdirMeanSum	= 0;
		float presMeanSum	= 0;
		float sunradMeanSum = 0;

		int sampleTemp   = 0;
		int sampleUmid   = 0;
		int samplePres	 = 0;
		int sampleWinDir = 0;
		int sampleWinVel = 0;
		int sampleSunrad = 0;

		float tempMin = TNODATA;
		float tempMax = TNODATA;
		float umidMin = -1;
		float umidMax = -1;
		float presMin = -1;
		float presMax = -1;
		float windvelMax = -1;
		float xMaxSpeed = -1;

		float rain_all = 0; // Must be 0

		float value = 0;

		for (int month=0; month<MONTH_OF_YEAR; month++) {

			// Extract a raw from dataArray
			getRawOfDataArray(month);

			// --- Get mean values ---

			// Temperature
			if (temperatureMeanBuffer>TNODATA) {
				tempMeanSum += temperatureMeanBuffer; ++sampleTemp;
			}

			// Humidity
			if (humidityMeanBuffer>0) {
				umidMeanSum += humidityMeanBuffer; ++sampleUmid;
			}

			// Pressure
			if (pressureMeanBuffer>0) {
				presMeanSum += pressureMeanBuffer; ++samplePres;
			}

			// Wind speed
			if (windSpeedMeanBuffer>=0) { // Wind speed
				wvelMeanSum += windSpeedMeanBuffer; ++sampleWinVel;
			}

			// Wind direction
			if ((windSpeedMeanBuffer>0) &&
					(windDirectionMeanBuffer>=0 && windDirectionMeanBuffer<=360)) {
				wdirMeanSum += windDirectionMeanBuffer; ++sampleWinDir;
			}

			// Sunrad
			if (sunradBuffer>0) {
				sunradMeanSum += sunradBuffer; ++sampleSunrad;
			}

			// --- Get min/max ---
			// Temperature
			value = getDataArray(TEMPERATURE_MIN_INDEX, month);
			if (tempMin<=TNODATA || value>TNODATA && value<tempMin) tempMin = value;

			value = getDataArray(TEMPERATURE_MAX_INDEX, month);
			if (tempMax<=TNODATA || value>TNODATA && value>tempMax) tempMax = value;

			// Humidity
			value = getDataArray(HUMIDITY_MIN_INDEX, month);
			if (umidMin<0 || (value>0 && value<umidMin))umidMin = value;

			value = getDataArray(HUMIDITY_MAX_INDEX, month);
			if (umidMax<0 || (value<=100 && value>umidMax)) umidMax = value;

			// Pressure
			value = getDataArray(PRESSURE_MIN_INDEX, month);
			if (presMin<0 || (value>0 && value<presMin)) presMin = value;

			value = getDataArray(PRESSURE_MAX_INDEX, month);
			if (presMax<0 || (value>0 && value>presMax)) presMax = value; 

			// Wind max speed
			xMaxSpeed = getDataArray(WINDSPEED_MAX_INDEX, month);
			if (windvelMax<0 || (xMaxSpeed>=0 && xMaxSpeed>windvelMax)) windvelMax = xMaxSpeed;

			// Overall rainfall
			value = getDataArray(RAINALL_INDEX, month);
			if (value>0) rain_all += value;

		} // end for

		// --- Set mean, min, max value ---//
		// Temperature
		setTemperatureMin(tempMin);
		setTemperatureMax(tempMax);
		if (sampleTemp>0) temperatureMean = tempMeanSum/sampleTemp;

		// Humidity
		setHumidityMin(umidMin);
		setHumidityMax(umidMax);
		if (sampleUmid>0) humidityMean = umidMeanSum/sampleUmid;

		// Pressure
		setPressureMin(presMin);
		setPressureMax(presMax);
		if (samplePres>0) pressureMean = presMeanSum/samplePres;

		// Wind
		setWindSpeedMax(windvelMax);
		if (sampleWinVel>0) windSpeedMean = wvelMeanSum/sampleWinVel;
		if (sampleWinDir>0) windDirectionMean = wdirMeanSum/sampleWinDir;

		// Overall rainfall
		if (rain_all>=0) {
			setRain_all(rain_all);	
		} else {
			setRain_all(-1);
		}

		// Solar radiation
		if (sampleSunrad>0) sunradMean = sunradMeanSum/sampleUmid;
	}

    //------------------- Only for Debug --------------------//
	public void print() {
		java.text.DecimalFormat formD = new java.text.DecimalFormat();
		java.text.DecimalFormat formD1 = new java.text.DecimalFormat();

		formD.applyPattern("0");
		formD1.applyPattern("0.0");

        System.out.print("stationId: " + stationId);

		System.out.println("year: "  + formD1.format(year));

		System.out.println("tmean (celsius): "  + formD1.format(temperatureMean));
		System.out.println("tmin (celsius): "   + formD1.format(temperatureMin));
		System.out.println("tmax (celsius): "   + formD1.format(temperatureMax));

		System.out.println("umean (%): "   + formD.format(humidityMean));
		System.out.println("umin (%): "    + formD1.format(humidityMin));
		System.out.println("umax (%): "	   + formD1.format(humidityMax));

		System.out.println("pmean (hPa): " + formD.format(pressureMean));
		System.out.println("pmin (hPa): "  + formD1.format(pressureMin));
		System.out.println("pmax (hPa): "  + formD1.format(pressureMax));

		System.out.println("wvelMean (m/s): " + formD.format(windSpeedMean));
		System.out.println("wvelmax  (m/s): " + formD1.format(windSpeedMax));
		System.out.println("wdirMean (deg): " + formD.format(windDirectionMean));

		System.out.println("sunradMean (W/m^2): " + formD.format(sunradMean));

		System.out.println("Rain_all (mm): "	+ formD1.format(rain_all));
        System.out.println("rainRateMax: " + rainRateMax + " mm/h");
        System.out.println("rainRateMaxMonth:" + rainRateMaxMonth);
        System.out.println("DaysOfRain (0.2, 2.0, 20): " + rain02 + ", " + rain2 + ", " +rain2);
	}

	// Print DataArray (0..MONTH_OF_YEAR-1)
	public void printArray() {
		for (int dataIndex=0; dataIndex<MONTH_OF_YEAR; dataIndex++) {
			System.out.println();
			System.out.print((dataIndex) +"\t"); 
			for (int typeIndex = 0; typeIndex<ARRAY_DATA_TYPE; typeIndex++) {
				System.out.print(dataArray[typeIndex][dataIndex]);
				if (typeIndex>=0 && typeIndex<ARRAY_DATA_TYPE) {
					System.out.print("\t"); 
				} else {
					System.out.println();
				}
			}
		}

		System.out.println();
	}


}
