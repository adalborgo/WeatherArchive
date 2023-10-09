package it.dibis.xml;

import it.dibis.dataObjects.DataOfMonth;

public class MonthReadXML extends ReadXML {

    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: MonthXMLRead.java,v 0.4 26/04/2013 23:59:59 adalborgo $";

    private static final String ROOT     = "DataOfMonth";

    private static final String HEADER   = "Header";
    private static final String DATETIME = "DateTime";
    private static final String SUMMARY  = "Summary";
    private static final String ARRAYS   = "Arrays";

    private DataOfMonth dataOfMonth = new DataOfMonth();

	/**
	 * @param year
	 * @param month
	 */
	public MonthReadXML(int year, int month) {
		dataOfMonth = new DataOfMonth();
		dataOfMonth.init(year, month);
	}

	/**
	 * Read file .xml of DataOfMonth
	 *
	 * @param filename
	 * @return
	 */
	public DataOfMonth getXmlDataOfMonth(String filename) {
		readFile(filename);
		return this.dataOfMonth;
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
	 * Set data of the array
	 * @param element
	 * @param type
	 * @param tag
	 */
	private void setDataArray(String element, int type, String tag) {
		int index = 0;
		int dim = dataOfMonth.getDataArrayLength();
		float value = 0;
		if (tag!=null && tag.length()>1 && tag.startsWith("I") ) {
			index = Integer.parseInt(tag.substring(1)); // Get index of the array
			if (index>=0 && index <dim) {
				value = Float.parseFloat(element);
				dataOfMonth.setDataArray(value, type, index);
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
			dataOfMonth.setStationId(element);
		} else if(tag.equals("sampleOfMonth")) {
            dataOfMonth.setLastDayOfMonth(Integer.parseInt(element));
		}
	}

	// Set date and time
	private void setDateTime(String tag, String element) {
		if(tag.equals("month")) {
            dataOfMonth.setMonth(Integer.parseInt(element));
		} else if(tag.equals("year")) {
            dataOfMonth.setYear(Integer.parseInt(element));
		}
	}

	/**
	 * Set temperature
	 * @param tag
	 * @param element
	 */
	private void setTemperature(String tag, String element) {
		if(tag.equals("min")) {
			dataOfMonth.setTemperatureMin(Float.parseFloat(element));
		} else if(tag.equals("max")) {
             dataOfMonth.setTemperatureMax(Float.parseFloat(element));
		} else if(tag.equals("mean")) {
			dataOfMonth.setTemperatureMean(Float.parseFloat(element));
		}
	}

	/**
	 * Set humidity
	 * @param tag
	 * @param element
	 */
	private void setHumidity(String tag, String element) {
		if(tag.equals("min")) {
			dataOfMonth.setHumidityMin(Float.parseFloat(element));
		} else if(tag.equals("max")) {
            dataOfMonth.setHumidityMax(Float.parseFloat(element));
		} else if(tag.equals("mean")) {
            dataOfMonth.setHumidityMean(Float.parseFloat(element));
		}
	}

	/**
	 * Set pressure
	 * @param tag
	 * @param element
	 */
	private void setPressure(String tag, String element) {
		if(tag.equals("min")) {
			dataOfMonth.setPressureMin(Float.parseFloat(element));
		} else if(tag.equals("max")) {
            dataOfMonth.setPressureMax(Float.parseFloat(element));
		} else if(tag.equals("mean")) {
            dataOfMonth.setPressureMean(Float.parseFloat(element));
		}
	}

	/**
	 * Set wind
	 * @param tag
	 * @param element
	 */
	private void setWind(String tag, String element) {
		if(tag.equals("speedMax")) {
			dataOfMonth.setWindSpeedMax(Float.parseFloat(element));
		} else if(tag.equals("speedMean")) {
            dataOfMonth.setWindSpeedMean(Float.parseFloat(element));
		} else if(tag.equals("directionMean")) {
            dataOfMonth.setWindDirectionMean(Integer.parseInt(element));
		}
	}

	/**
	 * Set rain
	 * @param tag
	 * @param element
	 */
	private void setRain(String tag, String element) {
		if(tag.equals("rainfall")) {
			dataOfMonth.setRain_all(Float.parseFloat(element));
		} else if(tag.equals("rainRateMax")) {
			dataOfMonth.setRainRateMax(Float.parseFloat(element));
		} else if(tag.equals("rainRateMaxDay")) {
            dataOfMonth.setRainRateMaxDay(Integer.parseInt(element));
		} else if(tag.equals("rain02")) {
			dataOfMonth.setRain02(Integer.parseInt(element));
		}else if(tag.equals("rain2")) {
			dataOfMonth.setRain2(Integer.parseInt(element));
		}else if(tag.equals("rain20")) {
			dataOfMonth.setRain20(Integer.parseInt(element));
		}
	}

	/**
	 * Set sunrad
	 * @param tag
	 * @param element
	 */
	private void setSunrad(String tag, String element) {
		if(tag.equals("mean")) {
             dataOfMonth.setSunradMean(Float.parseFloat(element));
		}
	}

    /**
     * @param args
     */
    public static void main(String[] args) {

		int year = 2013;
		int month = 2;
		MonthReadXML app = new MonthReadXML(year, month);
		// app.readFile("" + String.format("%02d%month", month) + year + ".xml");
 		app.readFile("0" + month + year + ".xml");

		app.dataOfMonth.print();
		app.dataOfMonth.printArray();

    }

}
