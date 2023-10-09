package it.dibis.common;

/**
 * Utils.java
 * Azione: Metodi pubblici di uso generale
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 */

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Utils implements Constants {

	// Revision control id
	public static String cvsId = "$Id: Utils.java,v 0.20 14/09/2023 23:59:59 adalborgo $";

	/**
	 * Read a string from file
	 *
 	 * @param filename
	 * @return
	 */
	public static String readFileToString(String filename) {
		String s = null;
		try {
			BufferedReader buf = new BufferedReader(new FileReader(filename));
			s = buf.readLine();

			// Close buffer
			buf.close();

		} catch (Exception e) {
			System.out.println(e);
		}

		return s;
	}

	/**
	 * Write a string to file
	 *
	 * @param filename
	 * @param s
	 */
	public static void writeStringToFile(String filename, String s) {
		try {
			BufferedWriter outbuf = new BufferedWriter(new FileWriter(filename, false));
			outbuf.write(s);
			outbuf.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Check directory rootPath + "year/mm" if not exists 
	 *
	 * @param  String rootPath
	 * @param  String ddmmyyyy
	 * @return boolean true: found; false: not found or mkdir error
	 */
	public static boolean checkDirOfMonth(String rootPath, String ddmmyyyy) {
		try {
			return (new File( (rootPath + ddmmyyyy.substring(4, 8)) + "/" + ddmmyyyy.substring(2, 4) +"/" ).exists() );
		} catch (Exception e) {
		}

		return false; 
	}

	/**
	 * Make directory rootPath + "year/mm" if not exists 
	 *
	 * @param  String rootPath
	 * @param  String ddmmyyyy
	 * @return boolean error (false: no error; true: mkdir error)
	 */
	public static boolean makeDirOfMonth(String rootPath, String ddmmyyyy) {
		try {
			if (!new File(rootPath + ddmmyyyy.substring(4, 8)).exists())
				new File(rootPath + ddmmyyyy.substring(4, 8)).mkdir(); // year

			if (!new File(rootPath + ddmmyyyy.substring(4, 8) + "/" + ddmmyyyy.substring(2, 4)).exists())
				new File(rootPath + ddmmyyyy.substring(4, 8) + "/" + ddmmyyyy.substring(2, 4)).mkdir();	// month
			return false; // Ok
		} catch (Exception e) {
			System.out.println("MkDir error. (" + e + ")");
			return true; // mkdir error
		}
	}

	/**
	 * Chech if dir rootPath + "year/" exists
	 *
	 * @param	String rootPath
	 * @param	int year
	 * @return	boolean true: found; false: not found
	 */
	public static boolean checkDirOfYear(String rootPath, int year) {
		String  yearPath = rootPath + dateStringConvert(1, 1, year).substring(4, 8);
		try {
			return (new File(yearPath).exists());
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Chech if dir rootPath + "year/" exists
	 *
	 * @param	String	rootPath
	 * @param	String	ddmmyyyy
	 * @return	boolean true: found; false: not found
	 */
	public static boolean checkDirOfYear(String rootPath, String ddmmyyyy) {
		try {
			return (new File(rootPath + ddmmyyyy.substring(4, 8)).exists());
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Make directory rootPath + "year/"
	 * @param  String rootPath
	 * @param  int year
	 * @return boolean error (false: no error; true: mkdir error)
	 */
	public static boolean makeDirOfYear(String rootPath, int year) {
		String  yearPath = rootPath + dateStringConvert(1, 1, year).substring(4, 8);
		try {
			if (!new File(yearPath).exists()) new File(yearPath).mkdir(); // Anno
			return false; // Ok
		} catch (Exception e) {
			System.out.println("YearMkDir error. (" + e + ")");
			return true; // mkdir error
		}
	}

	/**
	 * Make directory rootPath + "year/"
	 * @param  String path (root path)
	 * @param  String ddmmyyyy
	 * @return boolean error (false: no error; true: mkdir error)
	 */
	public static boolean makeDirOfYear(String rootPath, String ddmmyyyy) {
		String yearPath = rootPath + ddmmyyyy.substring(4, 8);
		try {
			if (!new File(yearPath).exists()) new File(yearPath).mkdir(); // Anno
			return false; // Ok
		} catch (Exception e) {
			System.out.println("YearMkDir error. (" + e + ")");
			return true; // mkdir error
		}
	}

	/**
	 * Make directory 'pathDir'
	 *
	 * @param  String path
	 * @return boolean error (false: no error; true: mkdir error)
	 */
	public static boolean makeDir(String pathDir) {
		try {
			if (!new File(pathDir).exists()) new File(pathDir).mkdir();
			return false; // Ok
		} catch (Exception e) {
			System.out.println("MkDir error. (" + e + ")");
			return true; // mkdir error
		}
	}

	/**
	 * Check | make directory
	 *
	 * @param  String path
	 * @return boolean true: found | make; false: not found | not make
	 */
	public static boolean checkMkDir(String pathDir) {
		try {
			if (new File(pathDir).exists()) {
				return true;
			} else {
				new File(pathDir).mkdir();
				return (new File(pathDir).exists());
			}
		} catch (Exception e) {
			System.out.println("CheckDir error. (" + e + ")");
			return false;
		}
	}

	/**
	 * Chech if directory 'pathDir' exists
	 *
	 * @param  String path
	 * @return boolean true: found; false: not found
	 */
	public static boolean checkDir(String pathDir) {
		try {
			return (new File(pathDir).exists());
		} catch (Exception e) {
			System.out.println("CheckDir error. (" + e + ")");
			return false;
		}
	}

	/**
	 * Return String rootPath + "yyyy/mm/"
	 * @param String ddmmyyyy
	 * @return String rootPath + "yyyy/mm/"
	 */
	public static String getFullPathOfMonth(String rootPath, String ddmmyyyy) {
		return rootPath + ddmmyyyy.substring(4,8) + "/" +
				ddmmyyyy.substring(2,4) + "/";
	}

	/**
	 * Convert day, month, year to a string rootPath + "mm/yyyy/"
	 * @param int day
	 * @param int month
	 * @param int year
	 * @return String rootPath + "yyyy/mm/"
	 */
	public static String getFullPathOfMonth(String rootPath, int day, int month, int year) {
		String ddmmyyyy = dateStringConvert(day, month, year);
		return rootPath + ddmmyyyy.substring(4,8) + "/" +
		ddmmyyyy.substring(2,4) + "/";
	}

	/**
	 * Convert day, month, year to a string rootPath + "mm/yyyy/"
	 * @param int day
	 * @param int month
	 * @param int year
	 * @return String rootPath + "yyyy/mm/mmyyyy"
	 */
	public static String getFullPathnameOfMonth(String rootPath, int day, int month, int year) {
		String ddmmyyyy = dateStringConvert(day, month, year);
		// return rootPath + ddmmyyyy.substring(4,8) + "/" + ddmmyyyy.substring(2,4) + "/" + ddmmyyyy.substring(2,8);
		return rootPath + ddmmyyyy.substring(4,8) + "/" + ddmmyyyy.substring(2,8);
	}

	/**
	 * Convert day, month, year to a string rootPath + "mm/yyyy/ddmmyyyy"
	 * @param int day
	 * @param int month
	 * @param int year
	 * @return String rootPath + "yyyy/mm/ddmmyyyy"
	 */
	public static String getFullPathnameOfDate(String rootPath, int day, int month, int year) {
		String ddmmyyyy = dateStringConvert(day, month, year);
		return rootPath + ddmmyyyy.substring(4,8) + "/" +
		ddmmyyyy.substring(2,4) + "/" + ddmmyyyy;
	}

	/**
	 * Return a string rootPath + "yyyy/"
	 * @param String rootPath
	 * @param int year
	 * @return String rootPath + "yyyy/"
	 */
	public static String getFullPathOfYear(String rootPath, int year) {
		String ddmmyyyy = dateStringConvert(1, 1, year);
		return rootPath + ddmmyyyy.substring(4,8) + "/";
	}

	/**
	 * Return a string rootPath + "yyyy/"
	 * @param String rootPath
	 * @param int year
	 * @return String rootPath + "yyyy/yyyy"
	 */
	public static String getFullPathnameOfYear(String rootPath, int year) {
		String ddmmyyyy = dateStringConvert(1, 1, year);
		return rootPath + ddmmyyyy.substring(4,8);
	}

	/**
	 * Return a string rootPath + "yyyy/"
	 * @param String ddmmyyyy
	 * @return String rootPath + "yyyy/"
	 */
	public static String getFullPathOfYear(String rootPath, String ddmmyyyy) {
		return rootPath + ddmmyyyy.substring(4,8) + "/";
	}

	/**
	 * Return a string of type 'month_year' from a string 'mmyear'
	 *
	 * @param	String	ddmmyyyy
	 * @return	String	mmyyyy
	 */
	public static String get_month_year(String filename, Locale locale) {
		String month_year = null;
		try {
			Date myDate = (new SimpleDateFormat("MMyyyy", locale)).parse(filename);
			month_year = new SimpleDateFormat("MMMM yyyy", locale).format(myDate);
		} catch (ParseException e) {
			System.out.println("Exception: " + e);
		}

		return month_year;
	}

	/**
	 * Return a string of type 'month year'
	 *
	 * @param	String	ddmmyyyy
	 * @return	String	mmyyyy
	 */
	public static String getNameOfMonth_year(int year, int month, Locale locale) {
		String month_year = null;
		String str = dateStringConvert(1, month, year).substring(2);
		try {
			Date myDate = (new SimpleDateFormat("MMyyyy", locale)).parse(str);
			month_year = new SimpleDateFormat("MMMM yyyy", locale).format(myDate);
		} catch (ParseException e) {
			System.out.println("Exception: " + e);
		}

		return month_year;
	}

	/**
	 * Convert day, month, year in a string "ddmmyyyy"
	 * @param int day
	 * @param int month
	 * @param int year
	 * @return String "ddmmyyyy"
	 */
	public static String dateStringConvert(int day, int month, int year) {

		String gg = null;
		String mm = null;
		String yyyy = null;

		// Giorno
		if (day>0 && day<=31) {
			if (day>9) gg = String.valueOf(day);
			else gg = "0" + String.valueOf(day);
		}

		// Mese
		if (month>0 && month<=12) {
			if (month>9) mm = String.valueOf(month);
			else mm = "0" + String.valueOf(month);
		}

		// Anno
		if (year>=0 && year<=9999) {
			if (year>=2000) yyyy = String.valueOf(year);
			else if (year>=100 && year<=999) yyyy = "2" + String.valueOf(year);
			else if (year>=10 && year<=99) yyyy = "20" + String.valueOf(year);
			else yyyy = "200" + String.valueOf(year);
		}

		if ((gg + mm + yyyy).length() == 8) 
			return gg + mm + yyyy;
		else 
			return "";
	}

	/**
	 * Return a string "ddmmyyyy" from system Calendar
	 */
	public static String dateToString() {
		Date now = new Date();
		return new SimpleDateFormat("ddMMyyyy").format( new Date(now.getTime()) );
	}

	/**
	 * Return a string "ddmmyyyy" from Calendar calendar
	 */
	public static String dateToString(Calendar calendar) {
		return new SimpleDateFormat("ddMMyyyy").format(calendar.getTime());
	}

	/**
	 * Format a string "ddmmyyyy" in "yyyy/mm/"
	 */
	public static String dateToFilePath(String filedate) {
		return (filedate.length()>=8) ? filedate.substring(4, 8) + "/" + filedate.substring(2, 4) + "/" : null;
	}

	/**
	 * Convert minutes of day in string "hh:mm"
	 * @param iTime (in minutes)
	 * @return
	 */
	public static String minutesToHHMM(int iTime) {
		int h, m;
		String s;

		if (iTime >= 0) {
			h = iTime/60; m = iTime%60;
			s = right("0" + String.valueOf(h), 2) + ":" + right("0" + String.valueOf(m%60), 2);
		} else s = "--:--";
		return s;
	}

	/**
	 * Utilizzato nel vecchio formato con la stazione SIAP
	 * @param iTime (in hh + 256* minutes)
	 * @return
	 */
	public static String getHrMn(int iTime) {
		int h, m;
		String s;

		if (iTime > 0) { //> MININT ???
			h = iTime/256; m = iTime%256;
			s = right("0" + String.valueOf(h), 2) + ":" + right("0" + String.valueOf(m%256), 2);
		} else s = "--:--";
		return s;
	}

	/**
	 * Return a sub-string of n characters to the right
	 * 
	 * @param String s
	 * @param String n lenght of sub-string
	 * @return String
	 */
	public static String right(String s, int n) {
		int l = s.length();
		int d = l - n;

		if (d<0) {
			d = 0;
		} else if (d>l) {
			d = l;
		}

		return s.substring(d);
	}

	/**
	 * Convert 1st char to uppercase 
	 * @param s
	 * @return
	 */
	public static String firstUppercase(String s) {
		if (s==null) return s;
		int l = s.length();
		if (l==0) return s;
		if (l==1) {
			return s.toUpperCase();
		} else {
			return s.substring(0, 1).toUpperCase() + s.substring(1);
		}
	}

	/**
	 * Days in a month
	 *
	 * @param	int	month
	 * @param	int	year
	 * @return	int	days
	 */
	public static int daysOfMonth(int month, int year) {
		int daysOfMonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		boolean isLeapYear = (year%4 == 0) && !((year%100 == 0) && (year%400 > 0));

		return (month==2 && isLeapYear) ? (daysOfMonth[month-1]+1) : daysOfMonth[month-1];
	}

	/**
	 *  Performs the conversion from string to int
	 *
	 * @param String str
	 * @return int
	 */
	public static int stringToInt(String str) {
		int n = 0;
		try {
			n = Integer.valueOf(str);
		} catch ( NumberFormatException e )	{
			System.out.println("No int!");
		}

		return n;
	}

	/**
	 *  Performs the conversion from string to long (return 0 on error
	 *
	 * @param str
	 * @return int
	 */
	public static long stringToLong(String str) {
		long n = 0;
		try {
			n = Integer.parseInt(str);
		} catch ( NumberFormatException e )	{
			System.out.println("No integer!");
		}

		return n;
	}

	/**
	 * Adjust iTime with Daylight Saving Time
	 * @param int iTime
	 * @param boolean dst
	 */
	public static int adjust_iTimeWithDST(int iTime, boolean dst) {
		if (iTime>=0 && dst) {
			iTime = (iTime+60)%1440; // If dst is true, add 60 minutes
		}
		return iTime;
	}

	/**
	 * @param day
	 * @param month
	 * @param year
	 * @param format
	 * @return
	 */
	public static String setDate(int day, int month, int year, String format, Locale locale) {
		GregorianCalendar calendar = new GregorianCalendar(year, month-1, day);
		Date date = new Date(calendar.getTime().getTime());
		return new SimpleDateFormat(format, locale).format(date);
	}

	/**
	 * @param month [1..12]
	 * @return
	 */
	public static String getNameOfMonth(int month, Locale locale) {
		if (month>=1 && month<= 12 )
			return new DateFormatSymbols(locale).getMonths()[month-1];
		else 
			return "-";
	}

	/**
	 * Non utilizzato (copiato come javascript in meteodata.htm)
	 * 
	 * @param dirWind
	 * @return
	 */
	public static String getWindName(int dirWind) {
		String windDirName = null;
		// windNames[(dirWind-23)/45]
		if (dirWind >= 23 && dirWind <68)
			windDirName = "Nord-Est (Grecale)";
		else if (dirWind >= 68 && dirWind < 113)
			windDirName = "Est (Levante)";
		else if (dirWind >=113 && dirWind <158)
			windDirName = "Sud-Est (Scirocco)";
		else if (dirWind >=158 && dirWind <203)
			windDirName = "Sud (Mezzogiorno)";
		else if (dirWind >=203 && dirWind <248)
			windDirName = "Sud-Ovest (Libeccio)";
		else if (dirWind >=248 && dirWind <293)
			windDirName = "Ovest (Ponente)";
		else if (dirWind >=293 && dirWind <338)
			windDirName = "Nord-Ovest (Maestrale)";
		else //(338..360) || (0..22)
			windDirName = "Nord (Tramontana)";

		return windDirName;
	}

	/**
	 * Get path of class
	 * 
	 * @return String
	 */
	String getClassPath() {
		return this.getClass().getClassLoader().getResource("").getPath();
	}

}
