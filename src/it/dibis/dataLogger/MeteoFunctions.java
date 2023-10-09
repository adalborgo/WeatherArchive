package it.dibis.dataLogger;

/**
 * MeteoFunctions.java
 * @author Antonio Dal Borgo adalborgo@gmail.com
 */

public class MeteoFunctions {

    /**
     *  Version info id
     */
    public static final String cvsId = "$Id: MeteoFunctions.java, Release 0.2 14/03/2011 23:59:59 adalborgo@gmail.com";

	/**
	 * Heat Index - Apparent Temperature
	 * 
	 * @param double temperature (celsius)
	 * @param double humidity (1..100)
	 * @return double heat index
	 * @see http://www.zunis.org/16element_heat_index_equation.htm
		hi =16.923+((1.85212E-1)*T)+(5.37941*RH)-((1.00254E-1)*T*RH)
		+((9.41695E-3)*T^2)+((7.28898E-3)*RH^2)+((3.45372E-4)*T^2*RH)
		-((8.14971E-4)*T*RH^2)+((1.02102E-5)*T^2*RH^2)-((3.8646E-5)*T^3)
		+((2.91583E-5)*RH^3)+((1.42721E-6)*T^3*RH)
		+((1.97483E-7)*T*RH^3)-((2.18429E-8)*T^3*RH^2)
		+((8.43296E-10)*T^2*RH^3)-((4.81975E-11)*T^3*RH^3)
	 * 
	 * Il computo del valore dell'indice ha significato solo per temperature dell'aria
	 * pi� grandi di 80 �F (27 �C), temperature del punto di rugiada (dew point)
	 * pi� grandi di 65 �F (12 �C), e umidit� relativa (RH) pi� grande del 40%.		
	 * @see http://www.meteo.unina.it/html/modules/meteolab/met_cal.htm
	 */ 
    public double heatIndex(double temperature, double humidity) {

		final double C0 = 16.923;
		final double C1 = 1.85212E-1;
		final double C2 = 5.37941;
		final double C3 = 1.00254E-1;

		final double C4 = 9.41695E-3;
		final double C5 = 7.28898E-3;
		final double C6 = 3.45372E-4;

		final double C7 = 8.14971E-4;
		final double C8 = 1.02102E-5;
		final double C9 = 3.8646E-5;

		final double C10 = 2.91583E-5;
		final double C11 = 1.42721E-6;

		final double C12 = 1.97483E-7;
		final double C13 = 2.18429E-8;

		final double C14 = 8.43296E-10;
		final double C15 = 4.81975E-11;
		
		// Convert to fahrenheit
		double tF = celsiusToFahrenheit(temperature);

		// Check limit
		if (!(tF>=80 && humidity>=40)) return temperature;
			
		double rh = humidity;

		double tF2 = tF*tF;
		double tF3 = tF2*tF;
		double rh2 = rh*rh;
		double rh3 = rh2*rh;

		/*
		double hi0 =
			-42.379 + 2.04901523*tF + 10.14333127*rh - 0.22475541*tF*rh - 6.83783e-3*tF2 -
			5.481717e-2*rh2 + 1.22874e-3*tF2*rh + 8.5282e-4*tF*rh2 - 1.99e-6*tF2*rh2; 
		System.out.println("hi0 (�F): " + hi0);
		*/

		double hi =
			C0 + (C1*tF) + (C2*rh) + (-C3*tF*rh) +
			(C4*tF2) + (C5*rh2) + (C6*tF2*rh) +
			(-C7*tF*rh2) + (C8*tF2*rh2) + (-C9*tF3) +
			(C10*rh3) + (C11*tF3*rh) + (C12*tF*rh3) + 
			(-C13*tF3*rh2) + (C14*tF2*rh3) + (-C15*tF3*rh3);

		// Heat index must be greater of the air temperature
		if (hi<tF) hi = tF;

		// System.out.println("heatIndex(fahrenheit): " + hi);
		return fahrenheitToCelsius(hi);
	}

	/**
	 * 
	 * @param temperature (celsius)
	 * @param windSpeed (m/s)
	 * @return double windChill
	 * 
	 * @remark the wind speed is measured at 10 m
	 * @see http://www.3bmeteo.com/giornale-meteo/il+wind+chill-3853
	 */
	public double windChill(double temperature, double windSpeed) {

		double tF;
		double wMph;

		// Convert celsius and m/s to fahrenheit and mph
		tF = celsiusToFahrenheit(temperature);
		wMph = 2.23693f*windSpeed;

		// Check limit
		if(wMph==0 || tF>=93.2) return temperature;

		if(wMph>=55) wMph = 55; // Max wind speed

		double we = (Math.pow(wMph, 0.16f)); // wMph^0.16
		double wCh = 35.74f + 0.6215f*tF-35.75f*we + 0.4275f*tF*we;

		// Wind chill not must be greater of the air temperature
		if (wCh>tF) wCh = tF;

		return fahrenheitToCelsius(wCh); // --> celsius
	}

	/**
	 * 
	 * @param temperature
	 * @param humidity
	 * @return double dewPoint
	 * http://www.ge.infn.it/ATLAS/SQTF/Minutes/Minutes_05-01-13.htm
	 * DPD = (14.55 + 0.114*T) x + ((2.5 + 0.007*T) x)^3 + (15.9 + 0.117*T) x^14
	 * x = (1 � 0.01 RH)
	 * 
	 * http://www.hpc.ncep.noaa.gov/html/dewrh.shtml
	 * 
	 * http://en.wikipedia.org/wiki/Dew_point#cite_note-4
	 * http://en.wikipedia.org/wiki/Arden_Buck_equation
	 * August�Roche�Magnus approximation
	 * 0 �C < T < 60 �C
	 * 1% < RH < 100%
	 * 0 �C < Td < 50 �C 
	 */
	public double dewPoint0(double temperature, double humidity) {
		double x = Math.log(humidity*0.01*6.112*Math.exp((17.62*temperature)/(temperature+243.12)));
		return (243.12*x - 440.1)/(19.43 - x);
	}

	/**
	 * 
	 * @param temperature
	 * @param humidity
	 * @return double dewPoint
	 * August�Roche�Magnus approximation
	 * www.sensirion.com: Dewpoint_Calculation_Humidity_Sensor_E.pdf
	 */
	public double dewPoint(double temperature, double humidity) {

		if (humidity>100) humidity = 100;
		if (humidity<1) humidity = 1; // Protect log

		// Check data
		if (temperature<-80 || temperature>80) return Double.NEGATIVE_INFINITY;

		double h = Math.log(humidity)-2/0.4343 + (17.62*temperature)/(243.12+temperature);
		double d = 17.62-h;
		if (d==0) d = 0.001; // Protect division (?!)
		double dp = 243.12*h/d;

		return dp;
	}

	/**
	 * 
	 * @param tF
	 * @return double fahrenheit (from celsius) 
	 */
	public double fahrenheitToCelsius(double tF) {
		return (tF-32f)/1.8f;
	}

	/**
	 * 
	 * @param tC
	 * @return double celsius (from fahrenheit)
	 */
	public double celsiusToFahrenheit(double tC) {
		return 1.8f*tC+32f;
	}

	//---------------------------------------//
	// --- Only for Debugging and Testing ---//
	//---------------------------------------//
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MeteoFunctions app = new MeteoFunctions();

		double temperature = 25;
		int humidity = 100;
		double windSpeed = 80/3.6; // km/h / 3.6

		System.out.println("fahrenheit: " + app.celsiusToFahrenheit(temperature));
		System.out.println("wMph: " + 2.23693f*windSpeed);

		System.out.println("heatIndex: " + app.heatIndex(temperature, humidity)); //32.22222307581963
		//System.out.println("WindChill: " + app.windChill(temperature, windSpeed));
		//System.out.println("DewPoint: " + app.dewPoint(temperature, humidity));
		//System.out.println("DewPoint0: " + app.dewPoint0(temperature, humidity));
	}
}
