package it.dibis.tflogger;

/**
 * DecodeDumpFile.java
 * @author Antonio Dal Borgo adalborgo@gmail.com
 * @Release 0.1
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.StringTokenizer;
import java.text.DecimalFormat;

public class DecodeDumpFile {

	// TFCombilog: index of raw_data stored  
	public final static int TEMPERATURE_RAW_LOG		= 0;  // Temperatura istantanea
	public final static int TEMPERATURE_MIN_RAW_LOG = 1;  // Temperatura minima
	public final static int TEMPERATURE_MAX_RAW_LOG	= 2;  // Temperatura massima
	public final static int HUMIDITY_RAW_LOG		= 3;  // Umidita' istantanea
	public final static int HUMIDITY_MIN_RAW_LOG	= 4;  // Umidita' minima
	public final static int HUMIDITY_MAX_RAW_LOG	= 5;  // Umidita' massima
	public final static int PRESSURE_RAW_LOG	 	= 6;  // Pressione istantanea
	public final static int PRESSURE_MIN_RAW_LOG 	= 7;  // Pressione minima
	public final static int PRESSURE_MAX_RAW_LOG 	= 8;  // Pressione massima
	public final static int WINDSPEED_RAW_LOG		= 9;  // Vento velocita' istantanea
	public final static int WINDSPEED_MAX_RAW_LOG 	= 10; // Vento velocita' massimo
	public final static int WINDDIR_RAW_LOG 		= 11; // Vento direzione
	public final static int RAINALL_RAW_LOG			= 12; // Contatore pioggia
	public final static int SUNRAD_RAW_LOG			= 13; // Radiazione solare
	public final static int SUNRAD_MAX_RAW_LOG		= 14; // Radiazione solare massima

	DecimalFormat formD = new DecimalFormat();
	BufferedWriter outbuf = null;

	public DecodeDumpFile() {

	}

	/**
	 * Carica i dati globali
	 * 
	 * @param String pathname
     */
	public void dataLoad(String readPathname, String writePathName) {

        try {
            BufferedReader inbuf = new BufferedReader(new FileReader(readPathname));
			outbuf = new BufferedWriter(new FileWriter(writePathName, false));

			String strLine = inbuf.readLine(); // Prima riga: intestazione
			write(strLine + "\n\n", true);
			write(header() + "\n", true);

            // Read comma separated, line by line
           while( (strLine = inbuf.readLine()) != null) {
				//System.out.println(strLine);
				decodeRaw(strLine);
			}

			// Close file
			inbuf.close();
			outbuf.close();

        } catch(Exception e) {
			System.out.println("Exception while reading data file: " + e);                  
        }
	}

	public void decodeRaw(String line) {

		final int DATA_FIELD_LENGTH = 10; // Combilog 1020
		final int DATA_OFFSET = 16;		  // Combilog 1020

		int start = 1;

		StringBuffer dataField = new StringBuffer(DATA_FIELD_LENGTH);
		
		// The fields are separated by ';'
		StringTokenizer st = new StringTokenizer(line.toString(), ";");

		int nfield = st.countTokens()-1; // Data field numbers

		// System.out.println("nfield: " + nfield); // Sono 17

		// Date & time
		String yymmddhhmmss = st.nextToken().substring(1, start + 12).toString();
		String time = yymmddhhmmss.substring(6, 8) + ":" +
			yymmddhhmmss.substring(8, 10); // Secondi: yymmddhhmmss.substring(10, 12);

		write("|" + time + "|", true);

		int channel = 0; // Init channel
		float value = -999; // Init value

		while (st.hasMoreTokens()) {

				// Data in hex format
				float x = hexToFloat(st.nextToken());
				//System.out.print(x + "  ");

				switch(channel) {
					case TEMPERATURE_RAW_LOG:
						write(x, "0.0", 5, true);
						break;

					case TEMPERATURE_MIN_RAW_LOG:
						write(x, "0.0", 5, true);
						break;

					case TEMPERATURE_MAX_RAW_LOG:
						write(x, "0.0", 5, true);
						break;

					case HUMIDITY_RAW_LOG:
						write(x, "0", 3, true);
					break;

					case HUMIDITY_MIN_RAW_LOG:
						write(x, "0", 3, true);
						break;

					case HUMIDITY_MAX_RAW_LOG:
						write(x, "0", 3, true);
						break;

					case PRESSURE_RAW_LOG:
						write(x, "0.0", 6, true);
					break;

					case PRESSURE_MIN_RAW_LOG:
						write(x, "0.0", 6, true);
						break;

					case PRESSURE_MAX_RAW_LOG:
						write(x, "0.0", 6, true);
						break;

					case WINDSPEED_RAW_LOG:
						write(x, "0.0", 4, true);
					break;

					case WINDSPEED_MAX_RAW_LOG:
						write(x, "0.0", 4, true);
						break;

					case WINDDIR_RAW_LOG:
						write(x, "0", 3, true);
						break;

					case RAINALL_RAW_LOG:
						write(x, "0.0", 5, true);
						break;

					case SUNRAD_RAW_LOG:
						write(x, "0.0", 6, true);
						break;
						
					case SUNRAD_MAX_RAW_LOG:
						write(x, "0.0", 6, true);
						break;

					default: // Do nothing

				} // end switch

				++channel; // Update channel counter
		} // end while

		write("\n", true);
	}

	String header() {

		return
		"|------------------------------------------------------------------------------------------|\n" +
		"| Ora |   Temperatura   |  Umidit�  |      Pressione     |     Vento   |Prec.|  Radiazione |\n" +
		"|hh:mm|  �C | Tmin| Tmax| % |min|max|  hPa | Pmin | Pmax | m/s|Vmax|Dir|  mm | W/m^2| Rmax |\n" +
		"|------------------------------------------------------------------------------------------|";
		// "|00:00|+xx,x|+xx,x|+xx,x|100|100|100|xxxx,x|xxxx,x|xxxx,x|xx,x|xx,x|xxx|xxx.x|xxxx,x|xxxx,x|";

	}

	/**
	 * 
	 * @param String varStr
	 * @param double value
	 * @param String frmt
	 */
	void write(float value, String frmt, int len, boolean print) {
		formD.applyPattern(frmt);
		String str = right("      " + formD.format(value).replace(',', '.'), len) + "|";
		try {
			outbuf.write(str);
			if (print) System.out.print(str);
		} catch  (IOException e) {
			System.out.println("Error: " + e);
		}
	}

	void write(String s, boolean print) {
		try {
			outbuf.write(s);
			if (print) System.out.print(s);
		} catch  (IOException e) {
			System.out.println("Error: " + e);
		}
	}

	/**
	 * Converte un hex in float
	 * @param String hex
	 * @return float
	 */
	public float hexToFloat(String hex) {
		float x;
		try {
			Long i = Long.parseLong(hex, 16); // radix 16-bit (hex)
			x = Float.intBitsToFloat(i.intValue());
			} catch (NumberFormatException e) {
				System.out.println(e);
				x = Float.NaN;
			}
		return x;
	}

	/**
	 * Return a sub-string of n characters to the right
	 * 
	 * @param String s
	 * @param String n lenght of sub-string
	 * @return String
	 */
	public String right(String s, int n) {
		int l = s.length();
		int d = l - n;
	    if (d<0) {
			d = 0;
		} else if (d>l) {
			d = l;
		}

        return s.substring(d);
	}

	public static void main(String[] args) {

		DecodeDumpFile app = new DecodeDumpFile();
		app.dataLoad("01042013.txt", "out.txt");
	}
}
