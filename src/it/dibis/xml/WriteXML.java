package it.dibis.xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Locale;
 
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.dibis.common.Constants;

public class WriteXML implements Constants {

    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: XMLWrite.java,v 0.3 20/04/2013 23:59:59 adalborgo $";

	XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
    final XMLEvent END = xmlEventFactory.createDTD("\n");
    final XMLEvent TAB = xmlEventFactory.createDTD("  "); // ("\t");

	XMLEventWriter xmlEventWriter = null;

	private int level = 0; // Nested level for tab

    public void openXmlDocument(String fileName) {
		try {
			XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
			xmlEventWriter = xmlOutputFactory.createXMLEventWriter(new FileOutputStream(fileName), "UTF-8");

			StartDocument startDocument = xmlEventFactory.createStartDocument();
			xmlEventWriter.add(startDocument);
			xmlEventWriter.add(END);
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
	}

    public void closeXmlDocument() {
		try {
			xmlEventWriter.add(xmlEventFactory.createEndDocument());
            xmlEventWriter.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
	}

    public void writeFloatArray(String node, float[][] x, int typeIndex, int maxIndex, float nodata) {
		openNode(node, true);

		for (int dataIndex = 0; dataIndex<=maxIndex; dataIndex++) {
			float value = (x[typeIndex][dataIndex]);
			if (value>nodata)
				writeElement("I" + Integer.toString(dataIndex), convertFormat("%.1f", value));
		}

		closeNode(node, true);
	}

    public void writeIntArray(String node, float[][] x, int typeIndex, int maxIndex, int nodata) {
		openNode(node, true);

		for (int dataIndex = 0; dataIndex<=maxIndex; dataIndex++) {
			int value = (int)(x[typeIndex][dataIndex]);
			if (value>nodata)
				writeElement("I" + Integer.toString(dataIndex), Integer.toString(value));
		}

		closeNode(node, true);
	}

	public void writeElement(String element, String value) {
		Characters characters = xmlEventFactory.createCharacters(value);
		openNode(element, false);
		try {
			xmlEventWriter.add(characters); // Create Content
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

		closeNode(element, false);
	}

    public void openNode(String rootElement, boolean newline) {
		try {
			writeTab(level);
			StartElement startElement = xmlEventFactory.createStartElement("", "", rootElement);
			xmlEventWriter.add(startElement);
			if (newline) {
				xmlEventWriter.add(END);
				++level;
			}
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }			
	}

    public void closeNode(String rootElement, boolean tab) {
		try {
			if (tab) {
				--level;
				writeTab(level);
			}
			xmlEventWriter.add(xmlEventFactory.createEndElement("", "", rootElement));
			xmlEventWriter.add(END);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }			
	}

    public void writeTab(int tabLevel) {
		try {
			for (int i=0; i<tabLevel; i++) xmlEventWriter.add(TAB);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
	}

    /**
     * @param frmt
     * @param x
     * @return
     */
    public String convertFormat(String frmt, float x) {
	   		return String.format(Locale.ENGLISH,frmt, x);
	}
}
