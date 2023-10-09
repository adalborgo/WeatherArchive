package it.dibis.xml;

import it.dibis.dataObjects.DataOfDay;

public class DayReadXML extends ReadXML {

    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: DayXMLRead.java,v 0.5 24/08/2017 23:59:59 adalborgo $";

	static final String ROOT     = "DataOfDay";

	static final String HEADER   = "Header";
	static final String DATETIME = "DateTime";
	static final String LASTDATA = "LastData";
	static final String ARRAYS   = "Arrays";

	DataOfDay dataOfDay = null;

	public DayReadXML() {
		dataOfDay = new DataOfDay();
		dataOfDay.init(SAMPLES_OF_DAY);
	}

	/**
	 * Read .xml of DataOfDay from file or url 
	 * @param args String filename
	 * @return dataOfDay
	 */
    public DataOfDay getXmlDataOfDay(String filename, boolean copy) {
		readFile(filename);

		if (copy) {
			// Copy last sample to current data
			int ls = dataOfDay.getLastSample()-1; // last_sample-1
			if (ls>=0 && ls<SAMPLES_OF_DAY) {
				dataOfDay.setTemperature(dataOfDay.getDataArray(TEMPERATURE_INDEX, ls));
				dataOfDay.setHumidity(dataOfDay.getDataArray(HUMIDITY_INDEX, ls));
				dataOfDay.setPressure(dataOfDay.getDataArray(PRESSURE_INDEX, ls));
				dataOfDay.setWindSpeed(dataOfDay.getDataArray(WINDSPEED_INDEX, ls));
				dataOfDay.setWindDirection(dataOfDay.getDataArray(WINDDIR_INDEX, ls));
				dataOfDay.setRain_all(dataOfDay.getDataArray(RAIN_INDEX, ls));
				dataOfDay.setSunrad(dataOfDay.getDataArray(SUNRAD_INDEX, ls));
			}
		}

		return dataOfDay;
    }

	/**
	 * Check root document
	 * @param String startTag
	 */
	protected boolean checkDocumentRoot(String startTag) {
		return startTag.equals(ROOT);
	}

    /**
     * @param String level
     * @param String mainTag
     * @param String tag1stLevel
     * @param String lastTag
     * @param String argument
     */
	protected void decodeNode(int level, String mainTag, String tag1stLevel, String lastTag, String argument) {
		if (level<2 || mainTag==null ) {
			return;

		} else if (level==2) {
			if (mainTag.equals(HEADER)) {
				setHeader(lastTag, argument);
			} else if (mainTag.equals(DATETIME)) {
				setDateTime(lastTag, argument);
			}

		} else if (level==3) {
			if (tag1stLevel==LASTDATA) {
				if (mainTag.equals("Temperature")) {
					setTemperature(lastTag, argument);
				} else if (mainTag.equals("Humidity")) {
					setHumidity(lastTag, argument);
				} else if (mainTag.equals("Pressure")) {
					setPressure(lastTag, argument);
				} else if (mainTag.equals("Wind")) {
					setWind(lastTag, argument);
				} else if (mainTag.equals("Rain")) {
					setRain(lastTag, argument);
				} else if (mainTag.equals("Sunrad")) {
					setSunrad(lastTag, argument);
				}
			} else if (tag1stLevel==ARRAYS) {
				if (mainTag.equals("Temperature")) {
					setDataArray(argument, TEMPERATURE_INDEX, lastTag);
				} else if (mainTag.equals("Humidity")) {
					setDataArray(argument, HUMIDITY_INDEX, lastTag);
				} else if (mainTag.equals("Pressure")) {
					setDataArray(argument, PRESSURE_INDEX, lastTag);
				} else if (mainTag.equals("WindSpeed")) {
					setDataArray(argument, WINDSPEED_INDEX, lastTag);
				} else if (mainTag.equals("WindDir")) {
					setDataArray(argument, WINDDIR_INDEX, lastTag);
				} else if (mainTag.equals("Rain")) {
					setDataArray(argument, RAIN_INDEX, lastTag);
				} else if (mainTag.equals("Sunrad")) {
					setDataArray(argument, SUNRAD_INDEX, lastTag);
				}
			}
		}
	}

	/**
	 * Set data of the array
	 * @param element
	 * @param type
	 * @param tag
	 */
	private void setDataArray(String element, int type, String tag) {
		int index = 0;
		int dim = dataOfDay.getDataArrayLength();
		float value = 0;
		if (tag!=null && tag.length()>1 && tag.startsWith("I") ) {
			index = Integer.parseInt(tag.substring(1)); // Get index of the array
			if (index>=0 && index <dim) {
				value = Float.parseFloat(element);
				dataOfDay.setDataArray(value, type, index);
			}
		}
	}

	/**
	 * Set header
	 * @param tag
	 * @param element
	 */
	private void setHeader(String tag, String element) {
		if(tag.equals("stationId")) {
			dataOfDay.setStationId(element);
		} else if(tag.equals("sampleOfDay")) {
            dataOfDay.setSampleOfDay(Integer.parseInt(element));
		} else if(tag.equals("lastSample")) {
            dataOfDay.setLastSample(Integer.parseInt(element));
		}
	}

	// Set date and time
	private void setDateTime(String tag, String element) {
		if(tag.equals("day")) {
			dataOfDay.setDay(Integer.parseInt(element));
		} else if(tag.equals("month")) {
            dataOfDay.setMonth(Integer.parseInt(element));
		} else if(tag.equals("year")) {
            dataOfDay.setYear(Integer.parseInt(element));
		} else if(tag.equals("hour")) {
             dataOfDay.setHour(Integer.parseInt(element));
		} else if(tag.equals("minute")) {
            dataOfDay.setMinute(Integer.parseInt(element));
		}
	}

	/**
	 * Set temperature
	 * @param tag
	 * @param element
	 */
	private void setTemperature(String tag, String element) {
		if(tag.equals("min")) {
			dataOfDay.setTemperatureMin(Float.parseFloat(element));
		} else if(tag.equals("minTime")) {
            dataOfDay.setTemperatureMinTime(Integer.parseInt(element));
		} else if(tag.equals("max")) {
             dataOfDay.setTemperatureMax(Float.parseFloat(element));
		} else if(tag.equals("maxTime")) {
            dataOfDay.setTemperatureMaxTime(Integer.parseInt(element));
		}
	}

	/**
	 * Set humidity
	 * @param tag
	 * @param element
	 */
	private void setHumidity(String tag, String element) {
		if(tag.equals("min")) {
			dataOfDay.setHumidityMin(Float.parseFloat(element));
		} else if(tag.equals("minTime")) {
            dataOfDay.setHumidityMinTime(Integer.parseInt(element));
		} else if(tag.equals("max")) {
            dataOfDay.setHumidityMax(Float.parseFloat(element));
		} else if(tag.equals("maxTime")) {
            dataOfDay.setHumidityMaxTime(Integer.parseInt(element));
		}
	}

	/**
	 * Set pressure
	 * @param tag
	 * @param element
	 */
	private void setPressure(String tag, String element) {
		if(tag.equals("min")) {
			dataOfDay.setPressureMin(Float.parseFloat(element));
		} else if(tag.equals("minTime")) {
            dataOfDay.setPressureMinTime(Integer.parseInt(element));
		} else if(tag.equals("max")) {
            dataOfDay.setPressureMax(Float.parseFloat(element));
		} else if(tag.equals("maxTime")) {
            dataOfDay.setPressureMaxTime(Integer.parseInt(element));
		}
	}

	/**
	 * Set wind
	 * @param tag
	 * @param element
	 */
	private void setWind(String tag, String element) {
		if(tag.equals("speedMax")) {
			dataOfDay.setWindSpeedMax(Float.parseFloat(element));
		} else if(tag.equals("speedMaxTime")) {
            dataOfDay.setWindSpeedMaxTime(Integer.parseInt(element));
		} else if(tag.equals("directionOfMaxSpeed")) {
            dataOfDay.setWindDirectionOfMaxSpeed(Integer.parseInt(element));
		}
	}

	/**
	 * Set rain
	 * @param tag
	 * @param element
	 */
	private void setRain(String tag, String element) {
		if(tag.equals("rainfall")) {
			dataOfDay.setRain_all(Float.parseFloat(element));
		} else if(tag.equals("rainRateMax")) {
            dataOfDay.setRainRateMax(Float.parseFloat(element));
		} else if(tag.equals("rainRateMaxTime")) {
            dataOfDay.setRainRateMaxTime(Integer.parseInt(element));
		}
	}

	/**
	 * Set sunrad
	 * @param tag
	 * @param element
	 */
	private void setSunrad(String tag, String element) {
		if(tag.equals("max")) {
             dataOfDay.setSunradMax(Float.parseFloat(element));
		} else if(tag.equals("maxTime")) {
            dataOfDay.setSunradMaxTime(Integer.parseInt(element));
		}
	}

    /**
     * @param args
     */
    public static void main(String[] args) {

		String filename =
		// "data2.xml";
		// "C:/Users/admin/MeteoStazione/meteofa/new/xml/2001/05/02052001.xml";
		"http://82.189.219.226/meteofa/data2/xml/data.xml";
		
		DayReadXML app = new DayReadXML();

 		app.readFile(filename);

		app.dataOfDay.printHeader();
		app.dataOfDay.printFlash();
		app.dataOfDay.printArray();
    }
}
