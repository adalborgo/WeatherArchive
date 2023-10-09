package it.dibis.xml;

import it.dibis.dataObjects.DataOfDay;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.XMLEvent;

public class DayWriteXML extends WriteXML {

    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: DayXmlWrite.java,v 0.4 26/04/2013 23:59:59 adalborgo $";

	static final String ROOT_ELEMENT = "DataOfDay";

	XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
    final XMLEvent END = xmlEventFactory.createDTD("\n");
    final XMLEvent TAB = xmlEventFactory.createDTD("  "); // ("\t");

	XMLEventWriter xmlEventWriter = null;

	DataOfDay dataOfDay = new DataOfDay();

	/**
	 * Write file .xml
	 * @param args String filename
	 * @param args DataOfDay dataOfDay
	 */
	public void writeFile(String filename, DataOfDay dataOfDay) {

		int lastSample = dataOfDay.getLastSample();

		openXmlDocument(filename);

		openNode(ROOT_ELEMENT, true);

		// Header
		writeHeader(dataOfDay.getStationId(), dataOfDay.getSampleOfDay(), dataOfDay.getLastSample());

		// Date and time
		writeDateTime(dataOfDay.getDay(), dataOfDay.getMonth(), dataOfDay.getYear(),
			dataOfDay.getHour(), dataOfDay.getMinute()
		);

		// Last data
		openNode("LastData", true);

		writeData("Temperature",
			dataOfDay.getTemperatureMin(), dataOfDay.getTemperatureMinTime(),
			dataOfDay.getTemperatureMax(), dataOfDay.getTemperatureMaxTime());

		writeData("Humidity",
			dataOfDay.getHumidityMin(), dataOfDay.getHumidityMinTime(),
			dataOfDay.getHumidityMax(), dataOfDay.getHumidityMaxTime());

		writeData("Pressure",
			dataOfDay.getPressureMin(), dataOfDay.getPressureMinTime(),
			dataOfDay.getPressureMax(), dataOfDay.getPressureMaxTime());

		writeWind(
			dataOfDay.getWindSpeedMax(), dataOfDay.getWindSpeedMaxTime(),
			(int)dataOfDay.getWindDirectionOfMaxSpeed());

		writeRain(dataOfDay.getRain_all(), dataOfDay.getRainRateMax(),
				dataOfDay.getRainRateMaxTime());

		writeSunrad(
			dataOfDay.getSunradMax(), dataOfDay.getSunradMaxTime());

		closeNode("LastData", true);

		// Arrays
		openNode("Arrays", true);

		writeFloatArray("Temperature", dataOfDay.getDataArray(), TEMPERATURE_INDEX, lastSample, TNODATA);
		writeIntArray("Humidity", dataOfDay.getDataArray(), HUMIDITY_INDEX, lastSample, 0);
		writeFloatArray("Pressure", dataOfDay.getDataArray(), PRESSURE_INDEX, lastSample, 0);
		writeFloatArray("WindSpeed", dataOfDay.getDataArray(), WINDSPEED_INDEX, lastSample, -1);
		writeIntArray("WindDir", dataOfDay.getDataArray(), WINDDIR_INDEX, lastSample, -1);
		writeFloatArray("Rain", dataOfDay.getDataArray(), RAIN_INDEX, lastSample, -1);
		writeFloatArray("Sunrad", dataOfDay.getDataArray(), SUNRAD_INDEX, lastSample, -1);
		closeNode("Arrays", true);

		closeNode(ROOT_ELEMENT, true);

		closeXmlDocument();
	}

    /**
     * @param stationId
     * @param sampleOfDay
     */
    private void writeHeader(String stationId, int sampleOfDay, int lastSample) {
		String node = "Header";
		openNode(node, true);
		writeElement("stationId", stationId);
		writeElement("sampleOfDay", Integer.toString(sampleOfDay));
		writeElement("lastSample", Integer.toString(lastSample));
		closeNode(node, true);
	}

    /**
     * @param day
     * @param month
     * @param year
     * @param hour
     * @param minute
     */
    private void writeDateTime(int day, int month, int year, int hour, int minute) {
		String node = "DateTime";
		openNode(node, true);
		writeElement("day", Integer.toString(day));
		writeElement("month", Integer.toString(month));
		writeElement("year", Integer.toString(year));
		writeElement("hour", Integer.toString(hour));
		writeElement("minute", Integer.toString(minute));
		closeNode(node, true);
	}

    /**
     * @param node
     * @param min
     * @param minTime
     * @param max
     * @param maxTime
     */
    private void writeData(String node, float min, int minTime, float max, int maxTime) {
		openNode(node, true);
		writeElement("min", convertFormat("%.1f", min));
		writeElement("minTime", Integer.toString(minTime));
		writeElement("max", convertFormat("%.1f", max));
		writeElement("maxTime", Integer.toString(maxTime));
		closeNode(node, true);
	}

    /**
     * @param speedMax
     * @param speedMaxTime
     * @param directionOfMaxSpeed
     */
    private void writeWind(float speedMax, int speedMaxTime, int directionOfMaxSpeed) {
		String node = "Wind";
		openNode(node, true);
		writeElement("speedMax", convertFormat("%.1f", speedMax));
		writeElement("speedMaxTime", Integer.toString(speedMaxTime));
		writeElement("directionOfMaxSpeed", Integer.toString(directionOfMaxSpeed));
		closeNode(node, true);
	}

    /**
     * @param rainfall
     */
    private void writeRain(float rainfall, float rainRateMax, int rainRateMaxTime) {
		String node = "Rain";
		openNode(node, true);
		writeElement("rainfall", convertFormat("%.1f", rainfall));
		writeElement("rainRateMax", convertFormat("%.1f", rainRateMax));
		writeElement("rainRateMaxTime", Integer.toString(rainRateMaxTime));
		closeNode(node, true);
	}

    /**
     * @param max
     * @param maxTime
     */
    private void writeSunrad(float max, int maxTime) {
		String node = "Sunrad";
		openNode(node, true);
		writeElement("max", convertFormat("%.1f", max));
		writeElement("maxTime", Integer.toString(maxTime));
		closeNode(node, true);
	}

}
