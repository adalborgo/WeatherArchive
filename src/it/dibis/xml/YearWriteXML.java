package it.dibis.xml;

import it.dibis.dataObjects.DataOfYear;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.XMLEvent;

public class YearWriteXML extends WriteXML {

	/**
	 *  Revision control id
	 */
	public static String cvsId = "$Id: YearXMLWrite.java,v 0.5 22/11/2013 23:59:59 adalborgo $";

	static final String ROOT_ELEMENT = "DataOfYear";

	XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
	final XMLEvent END = xmlEventFactory.createDTD("\n");
	final XMLEvent TAB = xmlEventFactory.createDTD("  "); // ("\t");

	XMLEventWriter xmlEventWriter = null;

	DataOfYear dataOfYear = new DataOfYear();

	/**
	 * @param filename
	 * @param dataOfYear
	 */
	public void writeFile(String filename, DataOfYear dataOfYear) {

		int lastSample = MONTH_OF_YEAR;

		openXmlDocument(filename);

		openNode(ROOT_ELEMENT, true);

		// Header
		writeHeader(dataOfYear.getStationId());

		// Date and time
		writeDateTime(dataOfYear.getYear());

		// Summary
		openNode("Summary", true);

		writeData("Temperature",
				dataOfYear.getTemperatureMin(), dataOfYear.getTemperatureMax(),
				dataOfYear.getTemperatureMean());

		writeData("Humidity",
				dataOfYear.getHumidityMin(), dataOfYear.getHumidityMax(),
				dataOfYear.getHumidityMean());

		writeData("Pressure",
				dataOfYear.getPressureMin(), dataOfYear.getPressureMax(),
				dataOfYear.getPressureMean());

		writeWind(
				dataOfYear.getWindSpeedMax(), dataOfYear.getWindSpeedMean(),
				(int)dataOfYear.getWindDirectionMean());

		writeRain(
				dataOfYear.getRain_all(), dataOfYear.getRainRateMax(),
				dataOfYear.getRainRateMaxMonth(), dataOfYear.getRain02(),
				dataOfYear.getRain2(), dataOfYear.getRain20());

		writeSunrad(dataOfYear.getSunradMean());

		closeNode("Summary", true);

		// Arrays
		openNode("Arrays", true);

		writeFloatArray("TemperatureMin", dataOfYear.getDataArray(), TEMPERATURE_MIN_INDEX, lastSample, TNODATA);
		writeFloatArray("TemperatureMax", dataOfYear.getDataArray(), TEMPERATURE_MAX_INDEX, lastSample, TNODATA);
		writeFloatArray("TemperatureMean", dataOfYear.getDataArray(), TEMPERATURE_MEAN_INDEX, lastSample, TNODATA);

		writeIntArray("HumidityMin", dataOfYear.getDataArray(), HUMIDITY_MIN_INDEX, lastSample, 0);
		writeIntArray("HumidityMax", dataOfYear.getDataArray(), HUMIDITY_MAX_INDEX, lastSample, 0);
		writeIntArray("HumidityMean", dataOfYear.getDataArray(), HUMIDITY_MEAN_INDEX, lastSample, 0);
		
		writeFloatArray("PressureMin", dataOfYear.getDataArray(), PRESSURE_MIN_INDEX, lastSample, 0);
		writeFloatArray("PressureMax", dataOfYear.getDataArray(), PRESSURE_MAX_INDEX, lastSample, 0);
		writeFloatArray("PressureMean", dataOfYear.getDataArray(), PRESSURE_MEAN_INDEX, lastSample, 0);

		writeFloatArray("WindSpeedMax", dataOfYear.getDataArray(), WINDSPEED_MAX_INDEX, lastSample, -1);
		writeFloatArray("WindSpeedMean", dataOfYear.getDataArray(), WINDSPEED_MEAN_INDEX, lastSample, -1);
		writeIntArray("WindDirMean", dataOfYear.getDataArray(), WINDDIR_MEAN_INDEX, lastSample, -1);
		
		writeFloatArray("Rain", dataOfYear.getDataArray(), RAINALL_INDEX, lastSample, -1);
		
		writeFloatArray("SunradMean", dataOfYear.getDataArray(), SUNRAD_MEAN_INDEX, lastSample, -1);

		closeNode("Arrays", true);

		closeNode(ROOT_ELEMENT, true);

		closeXmlDocument();
	}

	/**
	 * @param stationId
	 */
	private void writeHeader(String stationId) {
		String node = "Header";
		openNode(node, true);
		writeElement("stationId", stationId);
		closeNode(node, true);
	}

	/**
	 * @param year
	 */
	private void writeDateTime(int year) {
		String node = "DateTime";
		openNode(node, true);
		writeElement("year", Integer.toString(year));
		closeNode(node, true);
	}

	/**
	 * @param node
	 * @param min
	 * @param max
	 * @param mean
	 */
	private void writeData(String node, float min, float max, float mean) {
		openNode(node, true);
		writeElement("min", convertFormat("%.1f", min));
		writeElement("max", convertFormat("%.1f", max));
		writeElement("mean", convertFormat("%.1f", mean));
		closeNode(node, true);
	}

	/**
	 * @param speedMax
	 * @param speedMean
	 * @param directionMean
	 */
	private void writeWind(float speedMax, float speedMean, int directionMean) {
		String node = "Wind";
		openNode(node, true);
		writeElement("speedMax", convertFormat("%.1f", speedMax));
		writeElement("speedMean", convertFormat("%.1f", speedMean));
		writeElement("directionMean", Integer.toString(directionMean));
		closeNode(node, true);
	}

	/**
	 * @param rainfall
	 */
    private void writeRain(float rainfall, float rainRateMax, int rainRateMaxMonth,
    		int rain02, int rain2, int rain20) {
		String node = "Rain";
		openNode(node, true);
		writeElement("rainfall", convertFormat("%.1f", rainfall));
		writeElement("rainRateMax", convertFormat("%.1f", rainRateMax));
		writeElement("rainRateMaxMonth", Integer.toString(rainRateMaxMonth));
		writeElement("rain02", Integer.toString(rain02));
		writeElement("rain2", Integer.toString(rain2));
		writeElement("rain20", Integer.toString(rain20));
		closeNode(node, true);
	}

	/**
	 * @param mean
	 */
	private void writeSunrad(float mean) {
		String node = "Sunrad";
		openNode(node, true);
		writeElement("mean", convertFormat("%.1f", mean));
		closeNode(node, true);
	}

}
