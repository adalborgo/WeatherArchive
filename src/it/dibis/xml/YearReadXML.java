package it.dibis.xml;

import it.dibis.dataObjects.DataOfYear;

public class YearReadXML extends ReadXML {

    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: YearXMLRead.java,v 0.4 26/04/2013 23:59:59 adalborgo $";

	private static final String ROOT     = "DataOfYear";

    private static final String HEADER   = "Header";
    private static final String DATETIME = "DateTime";
    private static final String SUMMARY  = "Summary";
    private static final String ARRAYS   = "Arrays";

    private DataOfYear dataOfYear = new DataOfYear();

	/**
	 * @param int year
	 */
	public YearReadXML(int year) {
		dataOfYear = new DataOfYear();
		dataOfYear.init(year);
	}

	/**
	 * Read file .xml of DataOfYear
	 * @param String filename
	 * @return dataOfYear
	 */
    public DataOfYear getXmlDataOfYear2(String filename) {
 		readFile(filename);
		return this.dataOfYear;
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
			if (tag1stLevel==SUMMARY) {
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
				if (mainTag.equals("TemperatureMin")) {
					setDataArray(argument, TEMPERATURE_MIN_INDEX, lastTag);
				} else if (mainTag.equals("TemperatureMax")) {
					setDataArray(argument, TEMPERATURE_MAX_INDEX, lastTag);
				} else if (mainTag.equals("TemperatureMean")) {
					setDataArray(argument, TEMPERATURE_MEAN_INDEX, lastTag);

				} else if (mainTag.equals("HumidityMin")) {
					setDataArray(argument, HUMIDITY_MIN_INDEX, lastTag);
				} else if (mainTag.equals("HumidityMax")) {
					setDataArray(argument, HUMIDITY_MAX_INDEX, lastTag);
				} else if (mainTag.equals("HumidityMean")) {
					setDataArray(argument, HUMIDITY_MEAN_INDEX, lastTag);

				} else if (mainTag.equals("PressureMin")) {
					setDataArray(argument, PRESSURE_MIN_INDEX, lastTag);
				} else if (mainTag.equals("PressureMax")) {
					setDataArray(argument, PRESSURE_MAX_INDEX, lastTag);
				} else if (mainTag.equals("PressureMean")) {
					setDataArray(argument, PRESSURE_MEAN_INDEX, lastTag);

				} else if (mainTag.equals("WindSpeedMax")) {
					setDataArray(argument, WINDSPEED_MAX_INDEX, lastTag);
				} else if (mainTag.equals("WindSpeedMean")) {
					setDataArray(argument, WINDSPEED_MEAN_INDEX, lastTag);
				} else if (mainTag.equals("WindDirMean")) {
					setDataArray(argument, WINDDIR_MEAN_INDEX, lastTag);

				} else if (mainTag.equals("Rain")) {
					setDataArray(argument, RAINALL_INDEX, lastTag);

				} else if (mainTag.equals("SunradMean")) {
					setDataArray(argument, SUNRAD_MEAN_INDEX, lastTag);
				}
			}
		}
	}

	/**
	 *
	 * Set data of the array
	 * @param String element
	 * @param int type
	 * @param String tag
	 */
	private void setDataArray(String element, int type, String tag) {
		int index = 0;
		int dim = dataOfYear.getDataArrayLength();
		float value = 0;
		if (tag!=null && tag.length()>1 && tag.startsWith("I") ) {
			index = Integer.parseInt(tag.substring(1)); // Get index of the array
			if (index>=0 && index <dim) {
				value = Float.parseFloat(element);
				dataOfYear.setDataArray(value, type, index);
			}
		}
	}

	/**
	 * Set header
	 * @param String tag
	 * @param String element
	 */
	private void setHeader(String tag, String element) {
		if(tag.equals("stationId")) {
			dataOfYear.setStationId(element);
		}
	}

	/**
	 * Set date and time
	 * @param String tag
	 * @param String element
	 */
	private void setDateTime(String tag, String element) {
		if(tag.equals("month")) {
			dataOfYear.setYear(Integer.parseInt(element));
		} else if(tag.equals("year")) {
			dataOfYear.setYear(Integer.parseInt(element));
		}
	}

	/**
	 * Set temperature
	 * @param String tag
	 * @param String element
	 */
	private void setTemperature(String tag, String element) {
		if(tag.equals("min")) {
			dataOfYear.setTemperatureMin(Float.parseFloat(element));
		} else if(tag.equals("max")) {
			dataOfYear.setTemperatureMax(Float.parseFloat(element));
		} else if(tag.equals("mean")) {
			dataOfYear.setTemperatureMean(Float.parseFloat(element));
		}
	}

	/**
	 * Set humidity
	 * @param String tag
	 * @param String element
	 */
	private void setHumidity(String tag, String element) {
		if(tag.equals("min")) {
			dataOfYear.setHumidityMin(Float.parseFloat(element));
		} else if(tag.equals("max")) {
			dataOfYear.setHumidityMax(Float.parseFloat(element));
		} else if(tag.equals("mean")) {
			dataOfYear.setHumidityMean(Float.parseFloat(element));
		}
	}

	/**
	 * Set pressure
	 * @param String tag
	 * @param String element
	 */
	private void setPressure(String tag, String element) {
		if(tag.equals("min")) {
			dataOfYear.setPressureMin(Float.parseFloat(element));
		} else if(tag.equals("max")) {
			dataOfYear.setPressureMax(Float.parseFloat(element));
		} else if(tag.equals("mean")) {
			dataOfYear.setPressureMean(Float.parseFloat(element));
		}
	}

	/**
	 * Set wind
	 * @param String tag
	 * @param String element
	 */
	private void setWind(String tag, String element) {
		if(tag.equals("speedMax")) {
			dataOfYear.setWindSpeedMax(Float.parseFloat(element));
		} else if(tag.equals("speedMean")) {
			dataOfYear.setWindSpeedMean(Float.parseFloat(element));
		} else if(tag.equals("directionMean")) {
			dataOfYear.setWindDirectionMean(Integer.parseInt(element));
		}
	}

	/**
	 * Set rain
	 * @param tag
	 * @param element
	 */
	private void setRain(String tag, String element) {
		if(tag.equals("rainfall")) {
			dataOfYear.setRain_all(Float.parseFloat(element));
		} else if(tag.equals("rainRateMax")) {
			dataOfYear.setRainRateMax(Float.parseFloat(element));
		} else if(tag.equals("rainRateMaxMonth")) {
            dataOfYear.setRainRateMaxMonth(Integer.parseInt(element));
		} else if(tag.equals("rain02")) {
			dataOfYear.setRain02(Integer.parseInt(element));
		}else if(tag.equals("rain2")) {
			dataOfYear.setRain2(Integer.parseInt(element));
		}else if(tag.equals("rain20")) {
			dataOfYear.setRain20(Integer.parseInt(element));
		}	
	}

	/**
	 * Set sunrad
	 * @param String tag
	 * @param String element
	 */
	private void setSunrad(String tag, String element) {
		if(tag.equals("mean")) {
			dataOfYear.setSunradMean(Float.parseFloat(element));
		}
	}

	/**
	 * Only for testing and debugging
	 * @param args
	 */
	public static void main(String[] args) {

		int year = 2013;
		YearReadXML app = new YearReadXML(year);

		app.setDebug(true);
		app.readFile("" + year+".xml");

		app.dataOfYear.print();
		app.dataOfYear.printArray();
	}
}
