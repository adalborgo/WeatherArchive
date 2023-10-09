package it.dibis.common;

/*
 * 
 * @author adalborgo@gmail.com
 * Test: http://www.esrl.noaa.gov/gmd/grad/solcalc/
 */

import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class SunData {

 /**
     *  Revision control id
     */
 public static final String cvsId = "$Id: SunData.java,v 0.17 15/06/2014 23:59:59 adalborgo $";

	private final double PI = Math.PI;
	private final double PI2 = 2.0*PI;
	private final double TO_DEGREES = 180.0/PI; // Math.toDegrees()
	private final double TO_RADIANS = PI/180.0; // Math.toRadians()
	private final long MILLIS_OF_HOUR = 3600000L;

	/*
	 * Per il caso speciale dell'sunrise o del sunset lo zenit e' posto a 90.833
	 * (la correzione approssimata per la rifrazione atmosferica all'sunrise e al sunset)
	*/
	final double COS_REFRACTION = Math.cos(90.833*TO_RADIANS);

	//--- Variabili ---
///	private int configOfsUTC = 0; // time zone + daylight saving time (DST) in hours

	private double latitude_rad;
	private double longitude_deg;

	private double declination;
	private double equationTime;
	private double trueSolarTime;
	private double azimuth;
	private double altitude;
	private double sunrise;
	private double noon;
	private double sunset;

	private Calendar calendar = null;
	private Calendar calendarUTC = null;

	private DecimalFormat formD = new DecimalFormat();

	/**
	 * Constructor
	 * 
	 * @param double latitude
	 * @param double longitude
	 * @param String timeZone
	 * @param Calendar calendar
	 *
	 * calendar = new GregorianCalendar(year, month-1, day, hour, minute, second);
	 * calendar = Calendar.getInstance();
	 */
	public SunData(double latitude, double longitude, Calendar calendar) {
		this.latitude_rad = latitude*TO_RADIANS; // North: plus, South: negative 
		this.longitude_deg = longitude; // West: plus, Est: negative
		this.calendar = calendar;

		TimeZone tz = calendar.getTimeZone();

		int systemOfsUTC = (tz.getRawOffset() + tz.getDSTSavings())/3600000;

		// calendar in UTC
		this.calendarUTC = (Calendar) calendar.clone(); // Clone current calendar for internal use

		// Shift calendar with UTC offset
		long millis = calendarUTC.getTime().getTime() - systemOfsUTC*MILLIS_OF_HOUR;
		this.calendarUTC.setTimeInMillis(millis);

		// Process the data
		equationTime();
		computeDeclination();
		computeSunriseSunsetNoon();
		sunPosition();
	}

	// ------------------ Get computed data ------------------//
	/**
	 * Get equation time (in minuti)
	 */
	public double getEquationTime() {
		return equationTime;
	}

	/**
	 * Restituisce l'ora vera del sole (in minuti)
	 */
	public double getSolarTimeUTC() {
		return (trueSolarTime);
	}

	/**
     * Restituisce l'orario dell'alba (in minuti)
     */
	public double getSunriseUTC() {
		return sunrise;
	}

	/**
     * Restituisce l'orario del mezzogiorno solare (in minuti)
     */
	public double getSunNoonUTC() {
		return noon;
	}

	/**
	 * Restituisce l'orario del tramonto (in minuti)
     */
	public double getSunsetUTC() {
		return sunset;
	}

	/**
	 * Return solar azimut
	 * @return double azimuth
	 */
	public double getAzimuth() {
		return (azimuth*TO_DEGREES);
	}

	/**
	 * Return solar azimut
	 * @param pattern
	 * @return
	 */
	public String getAzimuth(String pattern) {
		formD.applyPattern(pattern);
		return formD.format(azimuth*TO_DEGREES);
	}

	/**
	 * Return solar altitude
	 * @return double altitude
	 */
	public double getAltitude() {
		return altitude*TO_DEGREES;
	}

	// -------------- Get string formatted data --------------//
	/**
	 * Restituisce l'ora vera del sole (hh:mm)
     */
	public String getSolarTime() {
		return formatHHMM(trueSolarTime);
	}

	/**
     * Restituisce l'orario dell'alba (hh:mm)
     */
	public String getSunrise(int offset) {
		return formatHHMM(sunrise + offset);
	}

	/**
     * Restituisce l'orario del mezzogiorno solare (hh:mm)
     */
	public String getSunNoon(int offset) {
		return formatHHMM(noon + offset);
	}

	/**
     * Restituisce l'orario del tramonto (hh:mm)
     */
	public String getSunset(int offset) {
		return formatHHMM(sunset + offset);
	}

	/**
     * Restituisce le ore di luce del giorno
     */
	public String getLight() {
		return formatHHMM(sunset-sunrise);
	}

	/**
     * Restituisce le ore di buio del giorno
     */
	public String getDark() {
		return formatHHMM(1440 - (sunset-sunrise));
	}

	/**
	 * Return solar altitude
	 * @param String pattern
	 * @return String altitude
	 */
	public String getAltitude(String pattern) {
		formD.applyPattern(pattern); //Cifre decimali
		return formD.format(altitude*TO_DEGREES);
	}

	//---------- Private ----------//
	/**
	 Calcola l'equazione del tempo (in minuti)
     (dalle ore del giorno bisogna togliere 1 per l'ora solare???)
     da +16 minuti e 33 secondi (tra il 31 ottobre ed il 1^ novembre) e
     -14 minuti e 6 secondi (tra l'11 e il 12 febbraio)

	 The equation of time (EOT) is a formula used in the process of converting between
	 solar time and clock time to compensate for the earth's elliptical orbit around
	 the sun and its axial tilt. Essentially, the earth does
	 not move perfectly smoothly in a perfectly circular orbit,
	 so the EOT adjusts for that.
	 The EOT can be approximated by the following formula
     */
	private void equationTime() {
		double numberOfDays = (double)(calendarUTC.get(calendar.DAY_OF_YEAR)) +
			(double)(calendarUTC.get(Calendar.HOUR_OF_DAY)-12)/24.0;

		double b = PI2*(numberOfDays-81)/364;

		this.equationTime = 9.87*Math.sin(2*b) - 7.53*Math.cos(b) - 1.5*Math.sin(b);
	}

	// Calcolo angolo di declinazione solare (in radianti)
	private void computeDeclination() {
		// Orario
		int hour      = calendarUTC.get(Calendar.HOUR_OF_DAY);
		int minute    = calendarUTC.get(Calendar.MINUTE);
		int second    = calendarUTC.get(Calendar.SECOND);
		int dayOfYear = calendarUTC.get(Calendar.DAY_OF_YEAR);

		// Calcolo gamma e frazione di anno (in radianti) ...
		double gamma = PI2*( dayOfYear-1 + (hour-12)/24 + minute/1440 + second/86400 ) / 365;

		// ... dal valore di gamma all'angolo di declinazione solare (in radianti)
		this.declination = 0.006918 - 0.399912*Math.cos(gamma) + 0.070257*Math.sin(gamma) - 0.006758 * Math.cos(2*gamma)
			+ 0.000907*Math.sin(2*gamma) - 0.002697*Math.cos(3*gamma) + 0.00148*Math.sin(3*gamma);
	}

	private void computeSunriseSunsetNoon() {

		// Funzioni goniometriche di latitudine (radians) e declinazione 
		double cos_lat  = Math.cos(latitude_rad);
		double tan_lat  = Math.tan(latitude_rad);
		double cos_decl = Math.cos(declination);
		double tan_decl = Math.tan(declination);

		// ----- Calculate sunrise and sunset time -----//
		double cos_ha = ( COS_REFRACTION/(cos_lat*cos_decl) ) -	tan_lat*tan_decl ;
		double ha = Math.acos(cos_ha) * TO_DEGREES; // In gradi

		/*
		 * Dove il numero positivo corrisponde a sunrise, quello negativo a sunset.
		 * Orario UTC time di sunrise (o di sunset) in minuti:
		 */
		this.sunrise = 4*(180 + longitude_deg - ha) - equationTime; // in minuti UTC
		this.sunset = 4*(180 + longitude_deg + ha) - equationTime;  // in minuti UTC

		/*
		 * la longitudine, l'angolo orario sono espressi in gradi e l'equazione del tempo in minuti
		 * Il mezzogiorno solare per una data localita' puo' essere ricavato dalla longitudine
		 * (in gradi) e dall'equazione del tempo (in minuti):
		 */
		this.noon = 4*(180 + longitude_deg) - equationTime; // in minuti UTC
	}

	private void sunPosition() {

		// Funzioni goniometriche di latitudine e declinazione (radians)
		double sin_decl = Math.sin(declination);
		double cos_decl = Math.cos(declination);
		double sin_lat = Math.sin(latitude_rad);
		double cos_lat = Math.cos(latitude_rad);

		/*
			il fuso orario (timezone) in ore rispetto al meridiano fondamentale
			(-E; +W: l'Italia e' UTC+1 in inverno, UTC+2 con l'ora legale)
		*/

		//---------------------------------------
		// Orario
		int hour      = calendarUTC.get(Calendar.HOUR_OF_DAY);
		int minute    = calendarUTC.get(Calendar.MINUTE);
		int second    = calendarUTC.get(Calendar.SECOND);
		int dayOfYear = calendarUTC.get(Calendar.DAY_OF_YEAR);

		// Calcolo del tempo solare vero (in minuti)
		double tempoMedioFuso = hour*60 + minute + second/60; // tempo civile

		// (latitudine_locale - latitudine_fuso_Italia)*4 // latitudine_fuso_Italia = 15
		double correzioneLongitudine = - 4*(longitude_deg); // delta negativo a est di quello centrale

		// Tempo solare vero (in minuti)
		trueSolarTime = tempoMedioFuso + correzioneLongitudine + equationTime;

		// Angolo orario solare (0 a mezzogiorno)
		double ha = (180 - trueSolarTime/4)*TO_RADIANS; // In radianti

		// Trasformazione da ECF (Earth Center Fixed) a coordinate locali
		convertECFtoLocal(ha);
	}

	// Trasformazione da ECF (Earth Center Fixed) a coordinate locali
	private void convertECFtoLocal(double ha) {

		// Funzioni goniometriche di latitudine e declinazione
		double sin_decl = Math.sin(declination);
		double cos_decl = Math.cos(declination);
		double tan_decl = Math.tan(declination);
		double sin_lat  = Math.sin(latitude_rad);
		double cos_lat  = Math.cos(latitude_rad);
		double sin_ha   = Math.sin(ha);
		double cos_ha   = Math.cos(ha);

		// Altezza del sole
		double cos_phi = sin_lat*sin_decl + cos_lat*cos_decl*Math.cos(ha);
		double phi = Math.acos(cos_phi); // In radianti
		altitude = PI/2-phi;

		// azimuth
		// double tan_theta2 = Math.sin(ha)/( Math.cos(ha)*Math.sin(lat) - Math.tan(decl)*Math.cos(lat) );
		double theta = Math.atan2(sin_ha, (cos_ha*sin_lat-tan_decl*cos_lat));
		this.azimuth = PI-theta;
	}

	// ------------------------ Utils ------------------------//

	/**
	 * Set calendar based on the current time in the given time zone
	 *
	 * @param TimeZone timeZone
	 * @return Calendar
	 */
	public static Calendar setCalendar(TimeZone timeZone) {
		Calendar calendar = new GregorianCalendar(timeZone);
		return calendar;
	}

	/**
	 * Set calendar with the given date and time set for the default time zone
	 *
	 * @param TimeZone timeZone
	 * @return Calendar
	 */
	public static Calendar setCalendar(int year, int month, int day,
			int hourOfDay, int minute, int second, TimeZone timeZone) {
		Calendar calendar = new GregorianCalendar(timeZone);
		calendar.set(year, month-1, day, hourOfDay, minute, second);
		return calendar;
	}

	/**
	 * Set calendar with the given date and time set for the default time zone
	 *
	 * @param TimeZone timeZone
	 * @return Calendar
	 */
	public static Calendar setCalendar(int year, int month, int day,
			int hourOfDay, int minute, int second) {
		Calendar calendar = new GregorianCalendar();
		calendar.set(year, month-1, day, hourOfDay, minute, second);
		return calendar;
	}

	/**
	 * Get offset from calendarUTC in minutes
	 */
	public int getOffsetFromUTC(TimeZone tz) {
		// TimeZone tz = TimeZone.getTimeZone(timeZoneID);
		// TimeZone configTZ = configHtml.getTimeZone();
		Calendar cal = (Calendar) calendarUTC.clone(); 
		cal.setTimeZone(tz);
		int offsetDST = getDST(cal) ? 60 : 0; // In minutes

		// System.out.println("offsetDST: " + offsetDST);
		// System.out.println("tz.getRawOffset()/60000: " + tz.getRawOffset()/60000);

		return tz.getRawOffset()/60000 + offsetDST;
	}

	/**
	 * Check if calendar is in daylight saving time
	 */
	public boolean getDST(Calendar calendar) {
		TimeZone tz = calendar.getTimeZone();
		return tz.inDaylightTime(calendar.getTime());
	}

	public void printDatestamp() {
		SimpleDateFormat dayformat = new SimpleDateFormat ("dd.MM.yyyy HH:mm zzz");
		System.out.println(dayformat.format(calendar.getTime()));
	}

	public void printDatestamp(Calendar calendar) {
		SimpleDateFormat dayformat = new SimpleDateFormat ("dd.MM.yyyy HH:mm zzz");
		System.out.println(dayformat.format(calendar.getTime()));
	}

	/**
	 * Converts minutes in hh:mm
	 * @param double minutes
	 */
	private String formatHHMM(double minutes) {
		if (minutes>1440) return "Overflow";
		int mint = (int)Math.round(minutes);
		int hh = mint/60;
		int mm = mint-hh*60;
		String hs = (hh<10) ? "0" + hh : "" + hh;
		String ms = (mm<10) ? ":0" + mm : ":" + mm;
		return (hs + ms);
	}

	// ------------------------ DEBUG ------------------------//
	/**
	 * Print computed data
	 */
	public void printComputeData(String timeZoneID) {
		// TimeZone tz = TimeZone.getTimeZone(timeZoneID);
		// TimeZone configTZ = configHtml.getTimeZone();

		int configOfsUTC = getOffsetFromUTC(TimeZone.getTimeZone(timeZoneID));


		System.out.println("----------------------");

		System.out.print("calendar ----->> ");
		printDatestamp(calendar);

		System.out.print("calendarUTC -->> ");
		printDatestamp(calendarUTC);

		System.out.println("configOfsUTC: " + configOfsUTC);

		System.out.println("getEquationTime(): " + getEquationTime());
		System.out.println("trueSolarTime: " + getSolarTime());
		System.out.println("getSolarTimeUTC: " + getSolarTimeUTC()/60);

		System.out.println("sunrise: " + getSunrise(configOfsUTC));
		System.out.println("getSunriseUTC: " + getSunriseUTC()/60);
		System.out.println("getSunrise2: " + ((configOfsUTC+getSunriseUTC())/60));
		System.out.println("getSunrise2S: " + formatHHMM(configOfsUTC+getSunriseUTC()));

		System.out.println("noon: " + getSunNoon(configOfsUTC));
		System.out.println("getSunNoonUTC(): " + getSunNoonUTC()/60);

		System.out.println("sunset: " + getSunset(configOfsUTC));
		System.out.println("light: " + getLight() );
		System.out.println("dark: " + getDark() );
		System.out.println("Azimut: " + getAzimuth("#.##"));
		System.out.println("Altitude: " + getAltitude("#.##"));
		System.out.println();
	}

	public static void main(String[] args) {

		// Coordinate geografiche di Faenza (Ra)
		double latitude = 44.28639;   // Nord: positivo; Sud: negativo 
		double longitude = -11.86993; // Ovest: positivo; Est: negativo; 

		SunData app = null;
		
		// Altra data
		int year   = 2014;
		int month  = 6; // Range: 1..12
		int day	   = 14;
		int hour   = 13;
		int minute = 12;
		int second = 0;
		
		boolean now = true;
		Calendar calendar = null;
		String timeZoneID = "Europe/Rome";
		// timeZoneID = "Africa/Algiers";  // TimeZone.getDefault().getID()
		if (now) { // Adesso
			// timeZoneID = "Africa/Algiers";
			// timeZoneID = "Europe/Rome"; // TimeZone.getDefault().getID()
			// systemTimeZoneID = TimeZone.getDefault().getID();
			// tz = TimeZone.getTimeZone(systemTimeZoneID);
			// calendar = setCalendar(TimeZone.getDefault());
			calendar = Calendar.getInstance(); // setCalendar(TimeZone.getDefault());
			app = new SunData(latitude, longitude, calendar);
			app.printComputeData(timeZoneID);
			System.out.println("DST: " + app.getDST(app.calendar));

		} else { // Altra data
			calendar = setCalendar(year, month, day, hour, minute, second);
			app = new SunData(latitude, longitude, calendar);
			app.printComputeData(timeZoneID);
			System.out.println("DST: " + app.getDST(app.calendar));
		}
	}

}
