package it.dibis.xml;

import java.io.BufferedInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import it.dibis.common.Constants;

public abstract class ReadXML implements Constants {

    /**
     *  Revision control id
     */
    public static String cvsId = "$Id: XMLRead.java,v 0.3 06/05/2013 23:59:59 adalborgo $";

    String root = null;

	boolean openTag = false;
	boolean dataTag = false;

	String mainTag = null;
	String lastTag = null;
	String argument = null;
	String tag1stLevel = null;

	private int level; // Nested level for tab

	private boolean debug = false;

	void setDebug(boolean debug) { this.debug = debug; }

	/**
	 * Get DataOfDay object from xml file
	 * @param String filename | url
	 */
    public void readFile(String filename) {
		URL url = null;
		
		// Is an url?
		boolean isUrl = filename.indexOf("://")>0;
		if (isUrl) {
			try {
				url = new URL(filename);
			} catch (
					MalformedURLException e) {
				System.out.println("Bad URL: " + e.getMessage());
			}
		}

    	level = 0; // Reset level

    	XMLEventReader eventReader = null;
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			if (isUrl) // Url
				eventReader = factory.createXMLEventReader(new BufferedInputStream(url.openStream()));
			else // File
				eventReader = factory.createXMLEventReader(new FileReader(filename));

			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();

				// Start element
				if (event.isStartElement()) {
					openTag = true;
					StartElement element = (StartElement) event;
					String startTag = element.getName().toString();

					lastTag = startTag;

					if (level==0) { // Root level
						if(startTag==null || !checkDocumentRoot(startTag)) {
							System.out.println("Documento errato!");
							System.exit(1);
						}
					} else if (level==1) {
						tag1stLevel = startTag;
					}

					if (debug) {
						writeTab(level);
						System.out.print("<" + startTag + "(" + level + ")>");
					}
				}

				// End element
				if (event.isEndElement()) {
					if (!openTag) --level;
					openTag = false;
					EndElement element = (EndElement) event;
					String endTag = element.getName().toString();
					if (!dataTag && debug) writeTab(level);
					if (debug) System.out.println("</" + endTag + ">");
					dataTag = false;
				}

				// Argument element
				if (event.isCharacters()) {
					argument = event.asCharacters().getData().trim();
					if (argument.length()>0) {
						dataTag = true;
						if (debug) System.out.print(argument);
						decodeNode(level, mainTag, tag1stLevel, lastTag, argument);
					} else {
						if (openTag) {
							mainTag = lastTag;
							++level;
							if (debug) System.out.println();
						}
					}	
				}
			}

        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
	}

    /**
     * @param level
     * @param mainTag
     * @param tag1stLevel
     * @param lastTag
     * @param argument
     */
    protected abstract void decodeNode(int level, String mainTag, String tag1stLevel, String lastTag, String argument);

	/**
	 * @param root
	 * @return
	 */
    protected abstract boolean checkDocumentRoot(String root);

    /**
     * @param tabLevel
     */
    protected void writeTab(int tabLevel) {
		for (int i=0; i<tabLevel; i++) System.out.print("  ");
	}

}
