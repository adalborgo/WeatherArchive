package it.dibis.files;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import it.dibis.common.Constants;
import it.dibis.dataObjects.DataOfDay;
import it.dibis.dataLogger.MeteoFunctions;

/**
 * Genera un file dati per la rete MeteoNetwork
 *
 * @author adalborgo@gmail.com
 */
/*
Specifiche del file da inviare dalla stazione al server MeteoNetwork
 formato del file con campi separati da ";"

1.	Station - codice Stazione
2.	Date - Data rilevamento (gg/mm/aa)
3.	Time - Ora rilevamento (hh:mm)
4.	TempOut - Temperatura esterna (°C)
5.	Pres - Pressione (Hpa)
6.	HumOut Umidita' relativa esterna (%)
7.	Wind - Velocita' del vento (km/h)
8.	Dir - Direzione del vento (°)
9.	Gust - Massima raffica (km/h)
10.	RainRate - Rain rate (mm/h)
11.	Rain - Pioggia giornaliera (mm)
12.	DewPoint - DewPoint (°C)
13.	Software - Nome del software (un nome generico)
14.	Versione - Versione del software
15.	TempIn - Temperatura interna (°C)
16.	HumIn - Umidita' interna (%)
17.	UVI - Radiazione solare (UVI)
18.	Radiazione Solare W/m2

Dato non presente indicare -99999. Ultima riga in basso l'ultimo dato.
Se non si puo' mandare tutto l'archivio della giornata basta mandare
	solo l'ultimo dato, una riga sola.
Di norma il file contiene i dati degli ultimi 2 giorni.

Esempio di file:
vnt214;18/08/15;00:05;20.4;1010.2;90;0.0;180;3.2;0.0;0.0;18.7;WL02;0.1;-99999;-99999;-99999;-99999;
vnt214;18/08/15;00:10;20.4;1010.2;90;1.6;180;3.2;0.0;0.0;18.7;WL02;0.1;-99999;-99999;-99999;-99999;
 */

public class FileMeteoNetwork implements Constants {

    // Revision control id
    public static final String cvsId = "$Id: FileMeteoNetwork.java,v 0.3 28/12/2020 23:59:59 adalborgo $";

    //--- Internal data ---//
    final static String MNW_FILENAME = "ero317.txt";
    final static String SOFTWARE = "Meteofa";
    final static String SOFTWARE_RELEASE = "0.1";
    final static String DATA_VOID = "-99999";

    DecimalFormat formD = new DecimalFormat();
    BufferedWriter outbuf = null;

    public FileMeteoNetwork() { }

    /**
     *
     * Scrive il file dati per la rete MeteoNetwork
     */
    public void writeFile(DataOfDay dataOfDay, String mnwId, float monthRainAll, float yearRainAll, String writePathName) {
        float value;
        try {
            Boolean append = false;
            outbuf = new BufferedWriter(new FileWriter(writePathName+ MNW_FILENAME, append));

            // Write date
            String date = doubleToString(dataOfDay.getDay(), "00") + "/" +
                    doubleToString(dataOfDay.getMonth(), "00") + "/" +
                    doubleToString(dataOfDay.getYear(), "00").substring(2,4);

            for (int i = 0; i < dataOfDay.getLastSample(); i++) {

                int hour = i / 4;
                int minute = i % 4 * 15;

                // 1. Station ID
                outbuf.write(mnwId + ";");

                // 2. Date - Data rilevamento (gg/mm/aa)
                outbuf.write(date + ";");

                // 3. Time - Ora rilevamento (hh:mm)
                outbuf.write(doubleToString(hour, "00") + ":" + doubleToString(minute, "00") + ";");

                // 4. TempOut - Temperatura esterna (Â°C)
                value = dataOfDay.getDataArray(TEMPERATURE_INDEX, i);
                writeField(value, "0.0");

                // 5. Pres - Pressione (Hpa)
                value = dataOfDay.getDataArray(PRESSURE_INDEX, i);
                writeField(value, "0.0");

                // 6. HumOut Umidita' relativa esterna (%)
                value = dataOfDay.getDataArray(HUMIDITY_INDEX, i);
                writeField(value, "0");

                // 7. Wind - Velocita' del vento (km/h)
                value = (3.6f*dataOfDay.getDataArray(WINDSPEED_INDEX, i));
                writeField(value, "0.0");

                // 8. Dir - Direzione del vento (Â°)
                value = dataOfDay.getDataArray(WINDDIR_INDEX, i);
                writeField(value, "0");

                // 9. Gust - Massima raffica (km/h)
                value = (float) (3.6d*dataOfDay.getWindSpeedMax());
                writeField(value, "0.0");

                // 10. RainRate - Rain rate (mm/h)
                // dataOfDay.getRainRateMax() -->> Vedi: calcRainRateMax()
                // writeField(dataOfDay.getRainRateMax(), "000.0");
                outbuf.write(DATA_VOID + ";");

                // 11. Rain - Pioggia giornaliera (mm)
                value = dataOfDay.getDataArray(RAIN_INDEX, i);
                writeField(value, "0.0");

                // 12. DewPoint - DewPoint (Â°C)
                double tx = (double) dataOfDay.getDataArray(TEMPERATURE_INDEX, i);
                double hx = (double) dataOfDay.getDataArray(HUMIDITY_INDEX, i);
                value = (float) new MeteoFunctions().dewPoint(tx, hx);
                writeField(value, "0.0");

                // 13. Software - Nome del software
                outbuf.write(SOFTWARE + ";");

                // 14. Versione - Versione del software
                outbuf.write(SOFTWARE_RELEASE + ";");

                // 15. TempIn - Temperatura interna (Â°C)
                outbuf.write(DATA_VOID + ";");

                // 16. HumIn - Umidita' interna (%)
                outbuf.write(DATA_VOID + ";");

                // 17. UVI - Radiazione solare (UVI)
                outbuf.write(DATA_VOID + ";");

                // 18. Radiazione Solare W/m2
                value = dataOfDay.getDataArray(SUNRAD_INDEX, i);
                writeField(value, "0.0");

                // New line
                outbuf.write("\n");
            }

            // Close file
            outbuf.close();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     *
     * @param value
     * @param pattern
     * @return
     */
    private String doubleToString(double value, String pattern) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     *
     * @param value
     * @param frmt
     * Note: change "," to "."
     */
    private void writeField(double value, String frmt) {
        formD.applyPattern(frmt);
        try {
            if (value > -999.0) outbuf.write(formD.format(value).replace(",", "."));
            outbuf.write(";");
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

}

