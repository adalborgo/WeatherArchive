package it.dibis.refresh;

/**
 * DataOfMonthAndYearXmlHtmlConvert.java
 * Rigenera, a partire dai file ddmmyyyy.xml, i file mmyyyy.xml, yyyy.xml e tutti html
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 */

import it.dibis.common.Constants;
import it.dibis.common.Utils;
import it.dibis.config.ConfigHtml;
import it.dibis.config.GetConfig;
import it.dibis.dataObjects.SharedData;
import it.dibis.files.MonthlyFile;
import it.dibis.files.YearlyFile;
import it.dibis.html.DailyWriteHTML;

public class DataOfMonthAndYearXmlHtmlConvert implements Constants {

	int error = 0; // No error

	int year;
	int month;
	int day;

	//--- Path of file archive ---//
	SharedData shared = SharedData.getInstance();
	ConfigHtml configHtml = ConfigHtml.getInstance();

	YearlyFile yearlyFile = new YearlyFile();

	private String rootOfXmlPath  = null;
	private String rootOfHtmlPath = null;
	
	// Revision control id
	public static String cvsId = "$Id: DataOfMonthXmlFullConvert.java,v 0.13 28/09/2023 23:59:59 adalborgo $";

	public DataOfMonthAndYearXmlHtmlConvert() {

		getConfigData(); // Load the configuration data in object

		rootOfXmlPath  = shared.getRootOfXmlPath();  // Path of data files .xml
		rootOfHtmlPath = shared.getRootOfHtmlPath(); // Path of files .html

		// mkdir if not exists
		boolean dirXmlDataFound = Utils.makeDir(rootOfXmlPath);
		if (dirXmlDataFound)  {
			System.out.println("Make " + rootOfXmlPath + " directory!\n");
			System.exit(error); // >>> EXIT!!!
		}

		boolean dirHtmlFound = Utils.checkDir(rootOfHtmlPath);
		if (!dirHtmlFound)  {
			System.out.println(rootOfHtmlPath + " not found\n");
			System.exit(error); // >>> EXIT!!!
		}

		printPaths();
	}

	/**
	 *
	 * @param year
	 * @param lastMonth
	 */
	public void runYearAll(int year, int lastMonth) {
		MonthlyFile monthlyFile = new MonthlyFile();

		// Days
		for (int month = 1; month<=lastMonth; month++) {
			for (int day = 1; day<=Utils.daysOfMonth (month, year); day++) {
				DailyWriteHTML dailyWriteHTML = new DailyWriteHTML(shared, configHtml);
				dailyWriteHTML.writeFile(rootOfXmlPath, rootOfHtmlPath, year, month, day);
			}
		}

		// Months
		for (int month = 1; month<=lastMonth; month++) {
			monthlyFile.writeFile(rootOfXmlPath, rootOfXmlPath , rootOfHtmlPath, 0, year, month);
		}

		// Year
		yearlyFile.writeFile(rootOfXmlPath, rootOfXmlPath , rootOfHtmlPath, lastMonth, year);
	}

	/**
	 * Print the paths
	 */
	public void printPaths() {
		System.out.println("----- Paths -----");
		System.out.println("rootOfXmlPath: " + rootOfXmlPath);
		System.out.println("rootOfHtmlPath: " + rootOfHtmlPath);
		System.out.println();
	}

	/**
	 * @return error
	 */
	public int getConfigData() {
		GetConfig getConfig = new GetConfig();
		int error = getConfig.getError();
		if (error!=0) {
			System.out.println("Error = " + error);
			System.exit(error); // -->> Exit on error
		}

		return error;
	}

	/**
	 * For DEBUG
	 * @param args
	 */
    public static void main(String[] args) {

		DataOfMonthAndYearXmlHtmlConvert app = new DataOfMonthAndYearXmlHtmlConvert();

		int firstYear = 2002;
		int lastYear = 2012;

		int lastMonth = 12; //3;
		for (int year = firstYear; year<=lastYear; year++) {
			app.runYearAll(year, lastMonth);
		}
    }
}
