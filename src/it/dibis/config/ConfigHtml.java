package it.dibis.config;

import java.util.Locale;

public class ConfigHtml {

	// Revision control id
	public static final String CVSID = "$Id: ConfigHtml.java,v 0.8 05/10/2023 23:53 adalborgo@gmail.com $";

	private static final int DATA_TYPE = it.dibis.common.Constants.DATA_TYPE;
	private static final String HTML_CONFIG_FILENAME = it.dibis.common.Constants.HTML_CONFIG_FILENAME;

	// Singleton
	private static ConfigHtml instance = null;

	// Locale
	private static Locale locale = Locale.getDefault();

	// Names of data
	private static String sTemperature = "Temperature";
	private static String sHumidity = "Humidity";
	private static String sPressure = "Pressure";
	private static String sWind = "Wind";
	private static String sRain = "Rainfall";
	private static String sPrecipitation = "Precipitation";
	private static String sPrec = "Prec.";
	private static String sSunrad = "Sun. Rad.";

	// Titles
	private static String dailyTitle = "Data of the day";
	private static String monthlyTitle = "Monthly summary";
	private static String yearlyTitle = "Annual summary";

	// Names
	private static String  sDay = "Day";
	private static String  sMonth = "Month";
	private static String  sYear = "Year";
	private static String  sTime = "Time";
	private static String  sDate = "Date";
	private static String  sMin = "Min";
	private static String  sMax = "Max";
	private static String  sAvg = "Avg";
	private static String  sOverall = "OverAll";

	// Wind
	private static String  sGust = "Gust";
	private static String  sWindAvg = "Avg speed";
	private static String  sDirAvg = "Avg Dir.";
	private static String  sSpeed = "Speed";
	private static String  sDirection = "Direction";

	// Rain
	private static String  sRainIntensity = "Intensity";
	private static String  sDaysRain1 = "Days of rain";
	private static String  sDaysRain2 = "h (mm)";

	// Headers
	private static String headerHtmlRaw1 = null;
	private static String headerHtmlRaw2 = null;

	private static float[] unitFactor = new float[DATA_TYPE];
	private static String[] unitSymbol = null;

	// ---------------------------------------------- //

	// Set locale
	public void setLocale(Locale locale) { this.locale = locale; }
	public Locale getLocale() { return locale; }

	// Names of data
	public void setTemperature(String s) { this.sTemperature = s; }
	public String getTemperature() { return sTemperature; }

	public void setHumidity(String s) { this.sHumidity = s; }
	public String getHumidity() { return sHumidity; }

	public void setPressure(String s) { this.sPressure = s; }
	public String getPressure() { return sPressure; }

	public void setWind(String s) { this.sWind = s; }
	public String getWind() { return sWind; }

	public void setRain(String s) { this.sRain = s; }
	public String getRain() { return sRain; }

	public void setSunrad(String s) { this.sSunrad = s; }
	public String getSunrad() { return sSunrad; }

	// Titles
	public void setDailyTitle(String s) { this.dailyTitle = s; }
	public String getDailyTitle() { return dailyTitle; }

	public void setMonthlyTitle(String s) { this.monthlyTitle = s; }
	public String getMonthlyTitle() { return monthlyTitle; }

	public void setYearlyTitle(String s) { this.yearlyTitle = s; }
	public String getYearlyTitle() { return yearlyTitle; }

	// Names
	public void setDay(String s) { this.sDay = s; }
	public String getDay() { return sDay; }

	public void setMonth(String s) { this.sMonth = s; }
	public String getMonth() { return sMonth; }

	public void setYear(String s) { this.sYear = s; }
	public String getYear() { return sYear; }

	public void setTime(String s) { this.sTime = s; }
	public String getTime() { return sTime; }

	public void setDate(String s) { this.sDate = s; }
	public String getDate() { return sDate; }

	public void setMin(String s) { this.sMin = s; }
	public String getMin() { return sMin; }

	public void setMax(String s) { this.sMax = s; }
	public String getMax() { return sMax; }

	public void setAvg(String s) { this.sAvg = s; }
	public String getAvg() { return sAvg; }

	// Wind
	public void setGust(String s) { this.sGust = s; }
	public String getGust() { return sGust; }

	public void setWindAvg(String s) { this.sWindAvg = s; }
	public String getWindAvg() { return sWindAvg; }

	public void setDirAvg(String s) { this.sDirAvg = s; }
	public String getDirAvg() { return sDirAvg; }

	public void setSpeed(String s) { this.sSpeed = s; }
	public String getSpeed() { return sSpeed; }

	public void setDirection(String s) { this.sDirection = s; }
	public String getDirection() { return sDirection; }

	// Rain
	public void setRainIntensity(String s) { this.sRainIntensity = s; }
	public String getRainIntensity() { return sRainIntensity; }

	public void setDaysRain1(String s) { this.sDaysRain1 = s; }
	public String getDaysRain1() { return sDaysRain1; }

	public void setDaysRain2(String s) { this.sDaysRain2 = s; }
	public String getDaysRain2() { return sDaysRain2; }

	// Others
	public void setOverall(String s) { this.sOverall = s; }
	public String getOverall() { return sOverall; }

	// Headers
	public void setHeaderHtmlRaw1(String headerHtmlRaw1) { this.headerHtmlRaw1 = headerHtmlRaw1; }
	public String getHeaderHtmlRaw1() { return headerHtmlRaw1; }

	public void setHeaderHtmlRaw2(String headerHtmlRaw2) { this.headerHtmlRaw2 = headerHtmlRaw2; }
	public String getHeaderHtmlRaw2() { return headerHtmlRaw2; }

	public void setUnitFactor(float[] unitFactor) { this.unitFactor = unitFactor; }
	public float[] getUnitFactor() { return this.unitFactor; }

	public void setUnitSymbol(String[] unitSymbol) { this.unitSymbol = unitSymbol; }
	public String[] getUnitSymbol() { return this.unitSymbol; }

	/*
	 * DO NOT instantiate with the operator new, use:
	 * ConfigHtml instance = getSingletonObject(); // Create the Singleton Object
	 */

	/**
	 * A private Constructor prevents any other class from instantiating
	 */
	private ConfigHtml() { /* Default constructor */ }

	/**
	 *
	 * @return
	 */
	public static synchronized ConfigHtml getInstance() {
		if (instance == null) instance = new ConfigHtml();
		return instance;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	//---------------------------------------//
	// --- For Debugging and Testing ---//
	//---------------------------------------//
	/**
	 * Print config data
	 */
	public String toString() {

		return (
				"Configuration parameters: " + HTML_CONFIG_FILENAME  + "\n" +

						"Locale: " + locale + "\n" +

						"sTemperature: " + sTemperature + "\n" +
						"sHumidity: " + sHumidity + "\n" +
						"sPressure: " + sPressure + "\n" +
						"sWind: " + sWind + "\n" +
						"sRain: " + sRain + "\n" +
						"sPrec: " + sPrec + "\n" +
						"sPrecipitation: " + sPrecipitation + "\n" +
						"sSunrad: " + sSunrad + "\n" +

						"dailyTitle: " + dailyTitle + "\n" +
						"montlyTitle: " + monthlyTitle + "\n" +
						"yearlyTitle: " + yearlyTitle + "\n" +

						"sDay: " + sDay + "\n" +
						"sMonth: " + sMonth + "\n" +
						"sYear: " + sYear + "\n" +
						"sTime: " + sTime + "\n" +
						"sDate: " + sDate + "\n" +

						"sMax: " + sMax + "\n" +
						"sMin: " + sMin + "\n" +
						"sAvg: " + sAvg + "\n" +

						"sWind: " + sWind + "\n" +
						"sWindAvg: " + sWindAvg + "\n" +
						"sWindDir: " + sDirAvg + "\n" +
						"sSpeed: " + sSpeed + "\n" +
						"sDirection: " + sDirection + "\n" +

						"sRainIntensity: " + sRainIntensity + "\n" +
						"sDaysRain1: " + sDaysRain1 + "\n" +
						"sDaysRain2: " + sDaysRain2 + "\n" +

						"sOverall: " + sOverall + "\n" +

						"unitFactor: " + unitFactor + "\n" +
						"unitSymbol: " + unitSymbol + "\n" +

						"headerLogoRaw1: " + headerHtmlRaw1 + "\n" +
						"headerLogoRaw2: " + headerHtmlRaw2 + "\n" +
						"\n"
		);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(new ConfigHtml());
	}
}
