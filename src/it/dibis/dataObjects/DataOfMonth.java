package it.dibis.dataObjects;

import it.dibis.common.Constants;
import it.dibis.common.Utils;

/**
 * Object of monthly data
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 *
 */
public class DataOfMonth implements Constants {

	// --- Costanti --- //
    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: DataOfMonth.java,v 0.12 26/04/2013 23:59:59 adalborgo $";

	//--- Variables ---//
	private String stationId;

	private int month;
	private int year;

	private int lastDayOfMonth = 0;
	
	// Used internally by calculateAndSetSummaryData() and
	// externally by MonthlyFile.java
	private int currentLastDayOfMonth = 0;  

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
	private int rainRateMaxDay; // Minutes

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

	private float[][] dataArray = null;

	/**
	 * Constructor
	 */
	public DataOfMonth() { /* Default constructor */ }

	public void init(int year, int month) {
		this.year = year;
		this.month = month;
		setStationId("");

		// Index = 0 is for summary data
		setLastDayOfMonth(Utils.daysOfMonth (month, year));
		dataArray = new float[ARRAY_DATA_TYPE][lastDayOfMonth+1];
		clearDataArray(); // Set temperature[] = TNODATA
	}

	// ---------------------------------------------- //

	// Id
	public synchronized void setStationId(String stationId) { this.stationId = stationId; }
	public synchronized String getStationId() { return this.stationId; }

	// lastDayOfMonth
	public synchronized void setLastDayOfMonth(int lastDayOfMonth) {this.lastDayOfMonth = lastDayOfMonth; }
	public synchronized int getLastDayOfMonth() { return this.lastDayOfMonth; }

	// Used only by MonthlyFile.java
	public void setCurrentLastDayOfMonth(int currentLastDayOfMonth) {this.currentLastDayOfMonth = currentLastDayOfMonth; }

	// Month and year
	public synchronized void setMonth(int month) { this.month = month; }
	public synchronized void setYear(int year) { this.year = year; }

	public synchronized int  getMonth() { return this.month; }
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
	public synchronized void setRainRateMaxDay(int rainRateMaxDay) { this.rainRateMaxDay = rainRateMaxDay; }
	public synchronized void setRain02(int rain02) { this.rain02 = rain02; }
	public synchronized void setRain2(int rain2) { this.rain2 = rain2; }
	public synchronized void setRain20(int rain20) { this.rain20 = rain20; }

	public synchronized float getRain_all() { return this.rain_all; }
	public synchronized float getRainRateMax() { return this.rainRateMax; }
	public synchronized int getRainRateMaxDay() { return this.rainRateMaxDay; }
	public synchronized int getRain02() { return this.rain02; }
	public synchronized int getRain2() { return this.rain2; }
	public synchronized int getRain20() { return this.rain20; }

	// Solar Radiation
	public synchronized void setSunradMean(float sunradMean) { this.sunradMean = sunradMean; }
	public synchronized float getSunradMean() { return this.sunradMean; }

	//--- Array of monthly data for every day ---//

	/**
	 * Update dataArray[]
	 * @param int index index of array
	 */
	public synchronized void setDataArray(int index) {
		if (index<lastDayOfMonth) {
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
			dataArray[WINDSPEED_MEAN_INDEX][index]  = windSpeedMean;
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
	 * @param int type, int day
	 * @return float
     */
	public synchronized float getDataArray(int type, int day) {
		return this.dataArray[type][day];
	}

	/**
	 * Extract a raw from dataArray
	 * @param int sample of day
	 */
	public synchronized void getRawOfDataArray(int day) {
		if (day<lastDayOfMonth) {
			temperatureMinBuffer = dataArray[TEMPERATURE_MIN_INDEX][day];
			temperatureMaxBuffer = dataArray[TEMPERATURE_MAX_INDEX][day];
			temperatureMeanBuffer = dataArray[TEMPERATURE_MEAN_INDEX][day];
			
			humidityMinBuffer = dataArray[HUMIDITY_MIN_INDEX][day];
			humidityMaxBuffer = dataArray[HUMIDITY_MAX_INDEX][day];
			humidityMeanBuffer = dataArray[HUMIDITY_MEAN_INDEX][day];
			
			pressureMinBuffer = dataArray[PRESSURE_MIN_INDEX][day];
			pressureMaxBuffer = dataArray[PRESSURE_MAX_INDEX][day];
			pressureMeanBuffer = dataArray[PRESSURE_MEAN_INDEX][day];
			
			windSpeedMaxBuffer = dataArray[WINDSPEED_MAX_INDEX][day];
			windSpeedMeanBuffer = dataArray[WINDSPEED_MEAN_INDEX][day];
			windDirectionMeanBuffer = dataArray[WINDDIR_MEAN_INDEX][day];

			rain_allBuffer = dataArray[RAINALL_INDEX][day];
			sunradBuffer = dataArray[SUNRAD_MEAN_INDEX][day];
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

		lastDayOfMonth = 0;
		currentLastDayOfMonth = 0;

		temperatureMin = temperatureMax = temperatureMean = TNODATA;
		humidityMin = humidityMax = humidityMean = -1;
		pressureMin = pressureMax = pressureMean = -1;
		windSpeedMax = windSpeedMean = -1;
		windDirectionMean = -1;
		rain_all = rainRateMax = -1;
		rain02 = rain2 = rain20 = -1;
		rainRateMaxDay = -1;

		sunradMean = -1;
    }

	/**
	 * Reset all data
	 */
    public synchronized void clearAllData() {
		clearSummaryData();   
        clearDataArray();
    }

	/**
     * Get statistics for one day
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
		float umidMin = TNODATA;
		float umidMax = -1;
		float presMin = -1;
		float presMax = -1;
		float windvelMax = -1;
		float xMaxSpeed = -1;

		float rain_all = 0;

		float value = 0;

		for (int day=0; day<currentLastDayOfMonth; day++) {

			// Extract a raw from dataArray
			getRawOfDataArray(day);

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
			value = getDataArray(TEMPERATURE_MIN_INDEX, day);
			if (tempMin<=TNODATA || value>TNODATA && value<tempMin) tempMin = value;

			value = getDataArray(TEMPERATURE_MAX_INDEX, day);
			if (tempMax<=TNODATA || value>TNODATA && value>tempMax) tempMax = value;

			// Humidity
			value = getDataArray(HUMIDITY_MIN_INDEX, day);
			if (umidMin<0 || (value>0 && value<umidMin)) umidMin = value;

			value = getDataArray(HUMIDITY_MAX_INDEX, day);
			if (umidMax<0 || (value<=100 && value>umidMax)) umidMax = value;

			// Pressure
			value = getDataArray(PRESSURE_MIN_INDEX, day);
			if (presMin<0 || (value>0 && value<presMin)) presMin = value;

			value = getDataArray(PRESSURE_MAX_INDEX, day);
			if (presMax<0 || (value>0 && value>presMax)) presMax = value; 

			// Wind max speed
			xMaxSpeed = getDataArray(WINDSPEED_MAX_INDEX, day);
			if (windvelMax<0 || (xMaxSpeed>=0 && xMaxSpeed>windvelMax)) windvelMax = xMaxSpeed;

			// Overall rainfall
			value = getDataArray(RAINALL_INDEX, day);
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
		// setWindSpeedMin(windvelMin);
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
		System.out.println("\tlastDayOfMonth: " + lastDayOfMonth);
		System.out.println("currentLastDayOfMonth: " + currentLastDayOfMonth);

		System.out.println("month: "  + month);
		System.out.println("year: "  + year);

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
        System.out.println("rainRateMaxDay:" + rainRateMaxDay);
        System.out.println("DaysOfRain (0.2, 2.0, 20): " + rain02 + ", " + rain2 + ", " +rain2);
	}

	// Print DataArray (0..lastDayOfMonth-1)
	public void printArray() {
		System.out.println("lastDayOfMonth: " + lastDayOfMonth);
		for (int dataIndex=0; dataIndex<lastDayOfMonth; dataIndex++) {
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
