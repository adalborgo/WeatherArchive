package it.dibis.commands;

import it.dibis.common.Constants;
import it.dibis.common.Utils;

/**
 * End program
 * 
 * @author Antonio Dal Borgo adalborgo@gmail.com
 * @Release 0.2
 */
public class EndApp implements Constants {

	public EndApp() {
		Utils.writeStringToFile(WEATHER_STATUS, "end\n");
		System.out.print("Please, wait ...");
	}

	public static void main(String[] args) {
		new EndApp();
	}
}
