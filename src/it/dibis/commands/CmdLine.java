package it.dibis.commands;

import it.dibis.common.Constants;
import it.dibis.common.Utils;
import it.dibis.config.ConfigHtml;
import it.dibis.config.GetConfig;
import it.dibis.dataObjects.SharedData;
import it.dibis.files.MonthlyFile;
import it.dibis.files.YearlyFile;
import it.dibis.html.DailyWriteHTML;

/**
 * @author adalborgo@gmail.com
 *
 */
public class CmdLine implements Constants {

	/**
	 *  Revision control id
	 */
	public static String cvsId = "$Id: CmdLine.java,v 0.14 04/05/2013 23:59:59 adalborgo $";

	int error = 0; // No error

	int year;
	int month;

	private SharedData shared = SharedData.getInstance();
	ConfigHtml configHtml = ConfigHtml.getInstance();

	MonthlyFile monthlyFile = new MonthlyFile();
	YearlyFile yearlyFile = new YearlyFile();
	
	//--- Path of file archive ---//
	private String rootOfXmlPath  = null;
	private String rootOfHtmlPath = null;

	public CmdLine() {
		error = getConfigData();
		if (error!=0) {
			System.out.println("Error = " + error);
			System.exit(error); // -->> Exit on error
		}

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
	}

	/**
	 * 
	 * @param args
	 */
	public void runYearly(String[] args) {
		int lastMonth = 0;
		if (args.length==2 || args.length==3) {
			try {
				year = Integer.parseInt(args[1]);
				if (args.length==3) {
					lastMonth = Integer.parseInt(args[2]);
				} else {
					lastMonth = 12;
				}
			} catch (NumberFormatException e) {
				System.err.println("Please, only integer number!");
				System.err.println("Your entry: year = " + args[1] + "  lastMonth = " + args[2]);
				System.exit(1);
			}
		} else {
			System.out.println("Syntax: -year year [lastMonth]");
			System.exit(1);
		}

		System.out.println("year: " + year + "  lastMonth: " + lastMonth);
		// -> Write file yyyy.htm to rootOfPublicPath + 'yyyy.htm'
		yearlyFile.writeFile(rootOfXmlPath, rootOfXmlPath , rootOfHtmlPath, lastMonth, year);
	}

	/**
	 * 
	 * @param args
	 */
	public void runMonthly(String[] args) {
		int beginMonth = 0;
		int endMonth = 0;
		if (args.length==3 || args.length==4) {
			try {
				year = Integer.parseInt(args[1]);
				beginMonth= Integer.parseInt(args[2]);
				if (args.length==4) {
					endMonth = Integer.parseInt(args[3]);
				} else {
					endMonth = beginMonth;
				}
			} catch (NumberFormatException e) {
				System.err.println("Please, only integer number!");
				System.err.println("Your entry: year = " + args[1] +
						"  beginMonth = " + args[2] + "  endMonth = " + args[3]);
				System.exit(1);
			}
		} else {
			System.out.println("Syntax: -month year beginMonth [endMonth]");
			System.exit(1);
		}

		System.out.println("year: " + year + "  beginMonth: " + beginMonth + "  endMonth: " + endMonth);

		for (int month = beginMonth; month<=endMonth; month++) {
			MonthlyFile monthlyFile = new MonthlyFile();
			monthlyFile.writeFile(rootOfXmlPath, rootOfXmlPath , rootOfHtmlPath, 0, year, month);
		}
	}

	/**
	 * 
	 * @param args
	 */
	public void runDaily(String[] args) {

		int beginDay = 0;
		int endDay = 0;

		if (args.length==4 || args.length==5) {
			try {
				year = Integer.parseInt(args[1]);
				month = Integer.parseInt(args[2]);
				beginDay = Integer.parseInt(args[3]);
				if (args.length==5) {
					endDay = Integer.parseInt(args[4]);
				} else {
					endDay = beginDay;
				}
			} catch (NumberFormatException e) {
				System.err.println("Please, only integer number!");
				System.err.println("Your entry:" +
						" year = " + args[1] + "  month = " + args[2] +
						"  beginDay = " + args[3] + "  endDay = " + args[4]);
				System.exit(1);
			}
		} else {
			System.out.println("Syntax: -day year month beginDay [endDay]");
			System.exit(1);
		}

		for (int day = beginDay; day<=endDay; day++) {
			DailyWriteHTML dailyWriteHTML = new DailyWriteHTML(shared, configHtml);
			dailyWriteHTML.writeFile(rootOfXmlPath, rootOfHtmlPath, year, month, day);
		}
	}

	/**
	 * 
	 * @param args
	 */
	public void runYearAll(String[] args) {

		int lastMonth = 0;
		if (args.length==2 || args.length==3) {
			try {
				year = Integer.parseInt(args[1]);
				if (args.length==3) {
					lastMonth = Integer.parseInt(args[2]);
				} else {
					lastMonth = 12;
				}
			} catch (NumberFormatException e) {
				System.err.println("Please, only integer number!");
				System.err.println("Your entry: allDayYear = " + args[1] + "  lastMonth = " + args[2]);
				System.exit(1);
			}
		} else {
			System.out.println("Syntax: -yearAll year [lastMonth]");
			System.exit(1);
		}

		System.out.println("year: " + year + "  lastMonth: " + lastMonth);

		// Days
		DailyWriteHTML dailyWriteHTML = new DailyWriteHTML(shared, configHtml);
		for (int month = 1; month<=lastMonth; month++) {
			for (int day = 1; day<=Utils.daysOfMonth (month, year); day++) {
				dailyWriteHTML.writeFile(rootOfXmlPath, rootOfHtmlPath, year, month, day);
			}
		}

		// Months
		for (int month = 1; month<=lastMonth; month++) {
			monthlyFile.writeFile(rootOfXmlPath, rootOfXmlPath, rootOfHtmlPath, 0, year, month);
		}

		// Year
		yearlyFile.writeFile(rootOfXmlPath, rootOfXmlPath , rootOfHtmlPath, lastMonth, year);
	}

	/**
	 * @return error
	 */
	public int getConfigData() {
		GetConfig getConfig = new GetConfig();
		int error = getConfig.getError();
		if (error!=0) {
			System.out.println("Error = " + error);
			return error;
		}

		return error;
	}

	//------------------------------------------------------------------------//
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
	 * Parses the commmand line arguments
	 *
	 * @param args the main method arguments
	 * @return -1 on error, 1 if version or help argument found,  0 otherwise
	 */
	public String[] parseCmdLineArgs(String[] args) {

		System.out.print("Cmd: ");
		for (int i=0; i<args.length; i++) System.out.print(args[i] + " ");

		System.out.println("\n--------------------");

		if (args.length==0) {
			printHelp();
		} else {
			String cmd = args[0].toLowerCase();
			if (cmd.equals("-v")) { // Version
				System.out.println(cvsId);

			} else if (cmd.equals("-h")) { // Help
				printHelp();
				args[0] = "HELP";

			} else if (cmd.equals("-yearAll") ||
					cmd.equals("-yearall")) { // Recupero dati di tutti i giorni dell'anno
				runYearAll(args);

			} else if (cmd.equals("-year")) { // Recupero dati dell'anno
				runYearly(args);

			} else if (cmd.equals("-month")) { // Recupero dati del mese
				runMonthly(args);

			} else if (cmd.equals("-day")) { // Recupero dati del giorno
				runDaily(args);

			} else { // Command not found
				printHelp();
			}
		}
		return(args);
	}

	/**
	 * Stampa i comandi di linea
	 */
	public void printHelp() {
		System.out.println(" ### Guida dei comandi ###\n"
				+ " -v   Versione programma\n"
				+ " -h   Questo testo di aiuto\n"
				+ " -yearAll year [lastMonth]  Recupero file per tutti i giorni dell'anno\n"
				+ " -year year [lastMonth]  Recupero file dell'anno\n"
				+ " -month year beginMonth [endMonth]  Recupero file del mese\n"
				+ " -day year month day  Recupero file del giorno\n"
				+ "\n");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CmdLine().parseCmdLineArgs(args);
	}

}

