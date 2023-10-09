package it.dibis.xml;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.XMLEvent;

import it.dibis.dataObjects.DataOfMonth;

public class MonthWriteXML extends WriteXML {

    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: MonthXMLWrite.java,v 0.4 26/04/2013 23:59:59 adalborgo $";

	static final String ROOT_ELEMENT = "DataOfMonth";

	XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
    final XMLEvent END = xmlEventFactory.createDTD("\n");
    final XMLEvent TAB = xmlEventFactory.createDTD("  "); // ("\t");

	XMLEventWriter xmlEventWriter = null;

	DataOfMonth dataOfMonth = new DataOfMonth();

	/**
	 * Write file .xml
	 * @param args String filename
	 * @param args DataOfMonth dataOfMonth
	 */
	public void writeFile(String filename, DataOfMonth dataOfMonth) {

		int lastSample = dataOfMonth.getLastDayOfMonth()-1;

		openXmlDocument(filename);

		openNode(ROOT_ELEMENT, true);

		// Header
		writeHeader(dataOfMonth.getStationId(), dataOfMonth.getLastDayOfMonth());

		// Date and time
		writeDateTime(dataOfMonth.getYear(), dataOfMonth.getMonth());

		// Summary
		openNode("Summary", true);

		writeData("Temperature",
			dataOfMonth.getTemperatureMin(), dataOfMonth.getTemperatureMax(),
				dataOfMonth.getTemperatureMean());

		writeData("Humidity",
			dataOfMonth.getHumidityMin(), dataOfMonth.getHumidityMax(),
				dataOfMonth.getHumidityMean());

		writeData("Pressure",
			dataOfMonth.getPressureMin(), dataOfMonth.getPressureMax(),
				dataOfMonth.getPressureMean());

		writeWind(
			dataOfMonth.getWindSpeedMax(), dataOfMonth.getWindSpeedMean(),
				(int)dataOfMonth.getWindDirectionMean());

		writeRain(
				dataOfMonth.getRain_all(), dataOfMonth.getRainRateMax(),
				dataOfMonth.getRainRateMaxDay(), dataOfMonth.getRain02(),
				dataOfMonth.getRain2(), dataOfMonth.getRain20());

		writeSunrad(dataOfMonth.getSunradMean());

		closeNode("Summary", true);

		// Arrays
		openNode("Arrays", true);

		writeFloatArray("TemperatureMin", dataOfMonth.getDataArray(), TEMPERATURE_MIN_INDEX, lastSample, TNODATA);
		writeFloatArray("TemperatureMax", dataOfMonth.getDataArray(), TEMPERATURE_MAX_INDEX, lastSample, TNODATA);
		writeFloatArray("TemperatureMean", dataOfMonth.getDataArray(), TEMPERATURE_MEAN_INDEX, lastSample, TNODATA);

		writeIntArray("HumidityMin", dataOfMonth.getDataArray(), HUMIDITY_MIN_INDEX, lastSample, 0);
		writeIntArray("HumidityMax", dataOfMonth.getDataArray(), HUMIDITY_MAX_INDEX, lastSample, 0);
		writeIntArray("HumidityMean", dataOfMonth.getDataArray(), HUMIDITY_MEAN_INDEX, lastSample, 0);
		
		writeFloatArray("PressureMin", dataOfMonth.getDataArray(), PRESSURE_MIN_INDEX, lastSample, 0);
		writeFloatArray("PressureMax", dataOfMonth.getDataArray(), PRESSURE_MAX_INDEX, lastSample, 0);
		writeFloatArray("PressureMean", dataOfMonth.getDataArray(), PRESSURE_MEAN_INDEX, lastSample, 0);

		writeFloatArray("WindSpeedMax", dataOfMonth.getDataArray(), WINDSPEED_MAX_INDEX, lastSample, -1);
		writeFloatArray("WindSpeedMean", dataOfMonth.getDataArray(), WINDSPEED_MEAN_INDEX, lastSample, -1);
		writeIntArray("WindDirMean", dataOfMonth.getDataArray(), WINDDIR_MEAN_INDEX, lastSample, -1);
		
		writeFloatArray("Rain", dataOfMonth.getDataArray(), RAINALL_INDEX, lastSample, -1);
		
		writeFloatArray("SunradMean", dataOfMonth.getDataArray(), SUNRAD_MEAN_INDEX, lastSample, -1);

		closeNode("Arrays", true);

		closeNode(ROOT_ELEMENT, true);

		closeXmlDocument();
	}

    private void writeHeader(String stationId, int lastDayOfMonth) {
		String node = "Header";
		openNode(node, true);
		writeElement("stationId", stationId);
		writeElement("lastDayOfMonth", Integer.toString(lastDayOfMonth));
		closeNode(node, true);
	}

    private void writeDateTime(int year, int month) {
		String node = "DateTime";
		openNode(node, true);
		writeElement("month", Integer.toString(month));
		writeElement("year", Integer.toString(year));
		closeNode(node, true);
	}

    private void writeData(String node, float min, float max, float mean) {
		openNode(node, true);
		writeElement("min", convertFormat("%.1f", min));
		writeElement("max", convertFormat("%.1f", max));
		writeElement("mean", convertFormat("%.1f", mean));
		closeNode(node, true);
	}

    private void writeWind(float speedMax, float speedMean, int directionMean) {
		String node = "Wind";
		openNode(node, true);
		writeElement("speedMax", convertFormat("%.1f", speedMax));
		writeElement("speedMean", convertFormat("%.1f", speedMean));
		writeElement("directionMean", Integer.toString(directionMean));
		closeNode(node, true);
	}

    private void writeRain(float rainfall, float rainRateMax, int rainRateMaxDay,
    		int rain02, int rain2, int rain20) {
		String node = "Rain";
		openNode(node, true);
		writeElement("rainfall", convertFormat("%.1f", rainfall));
		writeElement("rainRateMax", convertFormat("%.1f", rainRateMax));
		writeElement("rainRateMaxDay", Integer.toString(rainRateMaxDay));
		writeElement("rain02", Integer.toString(rain02));
		writeElement("rain2", Integer.toString(rain2));
		writeElement("rain20", Integer.toString(rain20));
		closeNode(node, true);
	}

    private void writeSunrad(float mean) {
		String node = "Sunrad";
		openNode(node, true);
		writeElement("mean", convertFormat("%.1f", mean));
		closeNode(node, true);
	}

}
