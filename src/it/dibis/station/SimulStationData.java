package it.dibis.station;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

public class SimulStationData {

    protected static Calendar calendar; // Added 01-09-2023
    protected static int day, month, year;
    protected static int hour, minute, second;

    // "hh:mm;Tempe;Hum;Pressu;WiSp;WiD;RainA;SunRad;",
    public static final String[] CSVDATA = { // 11092023.csv
            "00:00;+20.8;046;1013.8;04.0;192;000.0;0000.0;",
            "00:15;+20.7;046;1013.9;03.8;198;000.0;0000.0;",
            "00:30;+20.4;047;1013.8;01.2;185;000.0;0000.0;",
            "00:45;+20.2;048;1013.8;01.2;226;000.0;0000.0;",
            "01:00;+20.0;049;1013.7;01.4;200;000.0;0000.0;",
            "01:15;+19.8;049;1013.5;02.2;177;000.0;0000.0;",
            "01:30;+19.7;050;1013.5;01.8;202;000.0;0000.0;",
            "01:45;+19.6;050;1013.4;02.0;211;000.0;0000.0;",
            "02:00;+19.6;050;1013.5;02.4;204;000.0;0000.0;",
            "02:15;+19.6;049;1013.4;02.0;196;000.0;0000.0;",
            "02:30;+19.5;050;1013.4;01.4;191;000.0;0000.0;",
            "02:45;+19.3;050;1013.3;01.4;180;000.0;0000.0;",
            "03:00;+19.1;051;1013.3;00.6;216;000.0;0000.0;",
            "03:15;+19.0;052;1013.3;01.0;200;000.0;0000.0;",
            "03:30;+18.8;052;1013.2;01.2;189;000.0;0000.0;",
            "03:45;+18.7;053;1013.2;01.0;202;000.0;0000.0;",
            "04:00;+18.6;053;1013.2;01.2;210;000.0;0000.0;",
            "04:15;+18.4;054;1013.1;02.0;194;000.0;0000.0;",
            "04:30;+18.4;054;1013.0;01.8;198;000.0;0000.0;",
            "04:45;+18.4;055;1013.0;01.2;189;000.0;0000.0;",
            "05:00;+18.5;054;1012.9;02.6;200;000.0;0000.0;",
            "05:15;+18.4;055;1012.9;02.6;202;000.0;0000.0;",
            "05:30;+18.4;055;1013.0;01.8;195;000.0;0000.0;",
            "05:45;+18.3;055;1012.9;00.8;180;000.0;0001.4;",
            "06:00;+18.2;055;1013.0;01.4;203;000.0;0011.3;",
            "06:15;+18.2;056;1013.1;02.6;208;000.0;0023.0;",
            "06:30;+18.2;057;1013.1;00.8;233;000.0;0034.7;",
            "06:45;+18.3;058;1013.2;01.4;177;000.0;0060.3;",
            "07:00;+18.6;057;1013.3;00.4;214;000.0;0068.2;",
            "07:15;+19.0;056;1013.3;00.4;210;000.0;0224.2;",
            "07:30;+19.4;055;1013.2;00.4;213;000.0;0270.8;",
            "07:45;+19.9;054;1013.3;01.0;223;000.0;0315.1;",
            "08:00;+20.6;053;1013.4;00.0;250;000.0;0360.5;",
            "08:15;+21.1;052;1013.5;00.0;250;000.0;0404.6;",
            "08:30;+21.7;050;1013.4;00.0;250;000.0;0445.9;",
            "08:45;+22.5;048;1013.4;00.0;027;000.0;0485.4;",
            "09:00;+23.4;044;1013.4;00.0;305;000.0;0523.6;",
            "09:15;+24.2;040;1013.4;00.4;305;000.0;0560.7;",
            "09:30;+25.0;037;1013.4;00.6;323;000.0;0592.4;",
            "09:45;+26.0;035;1013.3;00.6;286;000.0;0620.4;",
            "10:00;+27.3;032;1013.2;00.8;319;000.0;0647.1;",
            "10:15;+28.1;030;1013.0;01.4;308;000.0;0671.6;",
            "10:30;+28.2;029;1013.0;01.2;314;000.0;0692.3;",
            "10:45;+29.4;027;1012.8;00.8;313;000.0;0709.6;",
            "11:00;+29.8;025;1012.6;01.4;349;000.0;0722.8;",
            "11:15;+30.6;024;1012.5;01.4;275;000.0;0731.4;",
            "11:30;+30.3;023;1012.3;00.8;346;000.0;0737.4;",
            "11:45;+31.0;023;1012.2;00.6;323;000.0;0741.1;",
            "12:00;+31.0;021;1012.0;04.0;330;000.0;0741.7;",
            "12:15;+31.9;020;1011.9;02.2;310;000.0;0740.3;",
            "12:30;+31.4;019;1011.8;01.4;312;000.0;0734.2;",
            "12:45;+31.7;019;1011.7;00.0;012;000.0;0724.5;",
            "13:00;+32.3;018;1011.7;01.4;352;000.0;0714.5;",
            "13:15;+32.0;017;1011.6;00.6;320;000.0;0696.2;",
            "13:30;+31.7;018;1011.5;02.6;302;000.0;0678.1;",
            "13:45;+31.5;018;1011.3;00.0;275;000.0;0652.5;",
            "14:00;+31.3;018;1011.2;00.0;016;000.0;0627.9;",
            "14:15;+31.1;019;1011.0;00.6;073;000.0;0597.0;",
            "14:30;+30.9;019;1010.9;01.8;323;000.0;0567.6;",
            "14:45;+30.8;019;1010.8;01.6;319;000.0;0536.7;",
            "15:00;+30.8;020;1010.6;00.8;312;000.2;0496.4;",
            "15:15;+30.9;019;1010.5;00.0;077;000.4;0459.8;",
            "15:30;+30.8;019;1010.5;00.4;014;000.6;0423.1;",
            "15:45;+30.7;019;1010.3;01.4;331;000.8;0382.8;",
            "16:00;+30.6;022;1010.2;00.4;152;001.0;0336.9;",
            "16:15;+30.3;021;1010.2;01.2;045;001.2;0295.9;",
            "16:30;+30.0;022;1010.2;01.0;133;001.4;0254.1;",
            "16:45;+29.7;022;1010.1;01.0;126;001.4;0213.0;",
            "17:00;+29.3;029;1010.1;01.0;117;001.4;0167.4;",
            "17:15;+28.9;030;1010.2;01.2;150;001.4;0122.7;",
            "17:30;+28.4;033;1010.1;01.2;130;001.4;0041.0;",
            "17:45;+27.9;034;1010.2;01.6;079;001.4;0031.0;",
            "18:00;+27.4;036;1010.2;00.8;073;001.4;0020.6;",
            "18:15;+26.7;037;1010.2;00.4;102;001.4;0008.1;",
            "18:30;+26.0;039;1010.2;00.6;110;001.4;0000.0;",
            "18:45;+25.4;040;1010.3;00.0;092;001.4;0000.0;",
            "19:00;+24.9;041;1010.3;00.2;126;001.4;0000.0;",
            "19:15;+24.4;042;1010.4;00.8;113;001.4;0000.0;",
            "19:30;+24.0;043;1010.5;00.0;110;001.4;0000.0;",
            "19:45;+23.6;044;1010.6;00.0;129;001.4;0000.0;",
            "20:00;+23.2;046;1010.7;00.0;129;001.4;0000.0;",
            "20:15;+22.9;048;1010.8;00.4;146;001.4;0000.0;",
            "20:30;+22.8;049;1010.9;00.0;173;001.4;0000.0;",
            "20:45;+22.6;049;1011.0;00.2;168;001.4;0000.0;",
            "21:00;+22.6;048;1010.9;00.8;216;001.4;0000.0;",
            "21:15;+22.3;049;1010.9;00.0;183;001.4;0000.0;",
            "21:30;+22.1;049;1010.9;00.8;206;001.4;0000.0;",
            "21:45;+22.3;047;1010.8;01.8;212;001.4;0000.0;",
            "22:00;+22.3;045;1011.0;01.2;219;001.6;0000.0;",
            "22:15;+22.3;044;1011.0;01.4;181;001.8;0000.0;",
            "22:30;+22.2;044;1011.0;01.6;203;002.0;0000.0;",
            "22:45;+22.2;043;1011.0;01.6;219;002.2;0000.0;",
            "23:00;+22.1;043;1011.0;01.4;208;003.4;0000.0;",
            "23:15;+22.0;043;1011.0;02.0;203;004.6;0000.0;",
            "23:30;+21.7;043;1010.9;01.2;188;004.6;0000.0;",
            "23:45;+21.6;043;1010.9;01.2;200;004.6;0000.0;"
    };

    // hh:mm;Tempe;Hum;Pressu;WiSp;WiD;RainA;SunRad;
    // "00:00;+20.8;046;1013.8;04.0;192;000.0;0000.0;",
    public static StationData getNextStoredData(int sample, String yymmdd) {
        StationData stationData = StationData.getInstance();
        stationData.clear();

        if (sample > CSVDATA.length) return stationData; // All cleared values

        String csvdata = CSVDATA[sample];

        String[] values = csvdata.split(";");

        // hh:mm;Tempe;Hum;Pressu;WiSp;WiD;RainA;SunRad;
        // Time (mm.ss)
        if (values[0].length() == 5) {
            stationData.setHour(parseInt(values[0].substring(0, 2)));
            stationData.setMinute(parseInt(values[0].substring(3, 5)));
            stationData.setSecond(0); // Not used!
        }

        stationData.setTemperature(parseFloat(values[1], -100.0f));
        stationData.setHumidity(parseInt(values[2]));
        stationData.setPressure(parseFloat(values[3], -1));
        stationData.setWindSpeed(parseFloat(values[4], -1));

        // !!!Nel Combilog il sensore di direzione vento è guasto!!!
        stationData.setWindDirection(parseFloat(values[5], -1));

        // Nel Combilog il pluviometro non è collegato!!!
        stationData.setRain(parseFloat(values[6], -1));

        stationData.setSunrad(parseFloat(values[7], -1));

        // Dummy data: yymmdd
        int day = parseInt(yymmdd.substring(4, 6));
        int month = parseInt(yymmdd.substring(2, 4));
        int year = parseInt(yymmdd.substring(0, 2));

        stationData.setDay(day);
        stationData.setMonth(month);
        stationData.setYear(year);

        stationData.setTemperatureMin(16.4f);
        stationData.setTemperatureMax(30.7f);
        stationData.setTemperatureMinTime(6 * 60 + 34);
        stationData.setTemperatureMaxTime(14 * 60 + 47);

        stationData.setHumidityMin(27f);
        stationData.setHumidityMax(86f);
        stationData.setHumidityMinTime(60 * 15 + 8);
        stationData.setHumidityMaxTime(60 * 5 + 19);

        stationData.setPressureMin(1014f);
        stationData.setPressureMax(1019f);
        stationData.setPressureMinTime(1 * 60 + 0);
        stationData.setPressureMaxTime(10 * 60 + 14);

        stationData.setWindSpeedMax(21f);
        stationData.setWindSpeedMaxTime(12 * 60 + 50);
        stationData.setWindDirectionOfMaxSpeed(123);

        stationData.setSunradMax(712.4f);
        stationData.setSunradMaxTime(-1);

        // stationData.print(); // For Debug

        return stationData;
    }

    /*
        Carica da un file dati .csv
        poi copia e incolla in String[] CSVDATA
     */
    public static void getCsvData() {
        String filename = "11092023.csv";
        // hh:mm;Tempe;Hum;Pressu;WiSp;WiD;RainA;SunRad;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                int m = parseInt(line.substring(3, 5)); // // 00:00;
                if (m == 0 || m == 15 || m == 30 || m == 45) {
                    System.out.println("\"" + line + "\",");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert hh.mm into (int) minutes
     *
     * @param str
     * @return int  (-1: on error)
     */
    public static int getTime(String str) {
        // Time (hh.mm)
        if (str.length() != 5) return -1;
        int hours = parseInt(str.substring(0, 2));
        int minutes = (hours >= 0) ? parseInt(str.substring(3, 5)) : -1;
        return ((hours >= 0 && hours <= 24) && (minutes >= 0 && minutes < 60)) ? 60 * hours + minutes : -1;
    }

    /**
     * @param str
     * @return int  (nodata = -1, on error)
     */
    public static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            System.out.println(e);
            return -1;
        }
    }

    /**
     * @param str
     * @return long  (-1: on error)
     */
    public static long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            System.out.println(e);
            return -1;
        }
    }

    /**
     * @param str
     * @return float (nodata: on error)
     */
    public static float parseFloat(String str, float nodata) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            System.out.println(e);
            return nodata;
        }
    }

    // FOR Debug
    public static void main(String[] args) {
        new SimulStationData().getCsvData();
        //new SimulStationData().getNextStoredData(0);
    }
}
