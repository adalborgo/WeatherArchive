package it.dibis.port;

/*         
 * File  : Rs232Port.java
 * Autore: adalborgo@racine.ra.it
 * Data  : 20-06-2005 (27-07-2004; 01/01/2004; 05/09/2002)
 * Rev   : 0.0.5/0.0.6
 * Agg.to: 1) aggiunto String getBuffer(); 2) modifiche a Rs232Port()
 * Nota  : Da sistemare la documentazione
 *	       Aggiunto rilevamento CTS (27-07-2004)
 * 		   Aggiunti notifyCTSOn() notifyCTSOff() (09/08/2004)
 *         Modificato(18-10-2010): if (flowControlIn==0 && flowControlOut==0) (0.0.6/12-08-2006) <- CONTROLLARE!!!
 */

import java.io.*;
import java.util.*;
//import javax.comm.*;
import gnu.io.*;

public class Rs232Port implements SerialPortEventListener {

	// Lettura su transizione CTS = {0:tutte; 1: on2off; 2: off2on}
	int readMode = 0;	// default

	//Utilizzate da 'serialEvent()' per la temporizzazione
	long endTime;
	int waitNoData;

	private static Enumeration portList;
	private static CommPortIdentifier portId;
	private SerialPort serialPort = null;

	private static final int dimByteBuffer = 1024;
	private int readBufferPointer = 0;

	private boolean dataAvailable, writeBufferEmpty;

	InputStream inputStream = null;
	OutputStream outputStream = null;

	byte[] readBuffer  = new byte[dimByteBuffer];
	byte[] writeBuffer = new byte[dimByteBuffer];

	boolean portConnected = false;
	boolean openToRead = false;
	boolean openToWrite = false;
	boolean listenMode = false;

    /**
    Open serial port

	@param String appName Application name as reference for port open
    @param int port Port number = {1|2|3|4}, Windows and Linux
    @param int baudRate Baud rate
    @param int flowControlIn Type of flow control for receiving = {0: none; 1: RTS/CTS; 2: XON/XOFF}
    @param int flowControlOut Type of flow control for sending  = {0: none; 1: RTS/CTS; 2: XON/XOFF}
    @param int databit Data bits = {5|6|7|8}
    @param int stopbit Stop bits = {1|2|3}; 3 for 1,5 bits
    @param int parity Parity = { 0: none; 1: odd; 2: even; 3: mark; 4: space }
	@param int timeout Max time (ms) to wait for port open
    @param boolean listenMode = { false (polling) | true (interrupt) }
    */
	public Rs232Port(String appName, int port, int baud, int dataBit, int stopBit, int parity,
		int flowControlIn, int flowControlOut, int timeout, boolean listenMode) {

		this.portConnected = portConnected;
		this.listenMode = listenMode;

		//Carica i parametri della porta selezionata      
		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();

			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL)	{
				if ( portId.getName().equals(commDev(port)) ) {
					// --- Open port ---
					try {
						serialPort = (SerialPort) portId.open(appName, timeout);

						// --- Set Serial Port Params ---
						try {
							serialPort.setSerialPortParams(baud, dataBit, stopBit, parity);

							//--- Set flow control ---//
							switch (flowControlIn) {
								case 1: //1
								serialPort.setFlowControlMode(serialPort.FLOWCONTROL_RTSCTS_IN); 
								break;

								case 2: //4
								serialPort.setFlowControlMode(serialPort.FLOWCONTROL_XONXOFF_IN); 
								break;

								default:
								flowControlIn = 0;
							}

							switch (flowControlOut) {
								case 1: //2
								serialPort.setFlowControlMode(serialPort.FLOWCONTROL_RTSCTS_OUT);
								break;

								case 2: //8
								serialPort.setFlowControlMode(serialPort.FLOWCONTROL_XONXOFF_OUT);
								break;

							default: //0
								flowControlOut = 0;
							}

							if (flowControlIn==0 && flowControlOut==0) //None Flow Control
								serialPort.setFlowControlMode(serialPort.FLOWCONTROL_NONE);

							//--- End flow control ---//

							//--- Port open Ok ---
							portConnected = true;

						} catch (UnsupportedCommOperationException e) {
                           portConnected = false;
						} // end catch 'Set Serial Port Params'

					} catch (PortInUseException e) {
						portConnected = false;
                    } //end try/catch 'Open port'
				} // end if 'portId.getName().equals(commDev(port))'

			} //end if 'portId.getPortType()'
		} //end while

		if (portConnected && listenMode) {
            try { //Add event listener
				serialPort.addEventListener(this);

				//Notify when input data is available
				serialPort.notifyOnDataAvailable(true);

				//Notify when Output buffer is empty
				serialPort.notifyOnOutputEmpty(true);

            } catch (TooManyListenersException e) {
				portConnected = false;
			}
		}
      
	} //end 'Rs232Port'
   
	public void close() {
        openToRead = false;
		openToWrite = false;

		serialPort.close();
	}

   	//-------- Open for reading --------//

    /**
     * Open serial port
	 *
	 * @param int waitNoData: tempo (ms) di attesa dall'ultimo byte ricevuto prima
	 *	di considerare terminata la ricezione della stringa di dati:
	 *	utilizzata da 'serialEvent()'
    */
	public boolean openRead(int waitNoData) {
		//Open input stream
		this.waitNoData = waitNoData;	//Assegna alla variabile di classe

		dataAvailable = false;
		readBufferPointer = 0;

		openToRead = true;
		if (inputStream==null) {
			try {
				inputStream = serialPort.getInputStream();
			} catch (IOException e) {
				openToRead = false;
			}
		}

		return openToRead;
	}

	/**
	 * Restituisce la stringa contenente il nome della porta seriale
	 * corrispondente al numero commPort {1, 2} assegnato nei sistemi
	 * DOS/Windows e Linux
	 * @param int commPort Port number = {1|2|3|4}, Windows and Linux
	 */
	String commDev(int commPort) {
		if ( System.getProperty( "file.separator", "/" ).compareTo( "/" ) != 0 ) {
			return "COM" + String.valueOf(commPort); //No ":";
		} else {
			return "/dev/ttyS" + String.valueOf(commPort-1);
		}
	}

	public int lenReadBuffer() {
		return readBufferPointer;
	}

	public boolean checkDataAvailable() {
		return dataAvailable && (readBufferPointer > 0);
	}

	/**
	 * Converte readBuffer in una stringa
	 */	
	public String getBuffer() {
		int len = getBuffer(readBuffer, 0, lenReadBuffer());

		StringBuffer str = new StringBuffer(len);

		for (int i=0; i<len; i++) str.append((char) readBuffer[i]);

		return str.toString();
	}

	public int getBuffer(byte[] dest, int pnt, int len) {
		int lendata = 0;
      
        if (readBufferPointer<=len) {
			//Disable notify when input data is available
            serialPort.notifyOnDataAvailable(false);
         
            for (int i=0; i<readBufferPointer; i++) dest[pnt+i] = readBuffer[i];
            lendata = lenReadBuffer();
            resetReadPointer();
         
         	//Enable notify when input data is available
            serialPort.notifyOnDataAvailable(true);
         
            return lendata; //Ok
		} else { //Overflow
			resetReadPointer();
			return -1;
		}
	}

	public void resetReadPointer() {
		dataAvailable = false;
		readBufferPointer = 0;
	}

	/**
	 * 
	 * return < 0: IOException
	 */
	public int dataAvailableToRead() {
		try {
			return inputStream.available();
		} catch (IOException e) {
			return -2;
		}
	}

	/**
	 * 
	 * return < 0: overflow | IOException
	 */
	public int getBufferData(byte[] bfdata, int bfpnt, int dataToRead) {
		try {
			if (bfpnt+dataToRead < bfdata.length) {
				return inputStream.read(bfdata, bfpnt, dataToRead);
			} else {
				return -1;	// Overflow
			}
		} catch (IOException e) {
			return -2;	// IOException
		}
	}

	/**
	 * 
	 * Lettura su transizione CTS = {0:tutte; 1: on2off; 2: off2on}
	 */
	public void notifyCTSOn(int readMode) {
		//Notify when CTS change?
		serialPort.notifyOnCTS(true);

		this.readMode = readMode;
	}

	public void notifyCTSOff() {
		// Disable notify when CTS change
		serialPort.notifyOnCTS(false);
	}

	// Attivata quando arrivano dei dati
	public void serialEvent(SerialPortEvent event) {
		switch(event.getEventType()) {
			case SerialPortEvent.BI:
				break;
         
            case SerialPortEvent.OE:
               break;
         
            case SerialPortEvent.FE:
               break;
         
            case SerialPortEvent.PE:
               break;
         
            case SerialPortEvent.CD:
               break;
         
            case SerialPortEvent.CTS:
				changedCTS();
			break;
         
            case SerialPortEvent.DSR:
               break;
         
            case SerialPortEvent.RI:
               break;
         
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
               writeBufferEmpty = true;
               break;
         
            case SerialPortEvent.DATA_AVAILABLE:
				getDataAvailable();
				break;
		}
	}

	/**
	 * Mette i dati in lettura in readBuffer[] e aggiorna readBufferPointer
	 * Completa le operazioni quando non arrivano piu' dati da un intervallo
	 * di tempo uguale a waitNoData
	 */
	private void getDataAvailable() {
		int lendata = 0;
        boolean dataLoad = false;
		boolean wait = false;

		do {
			lendata = dataAvailableToRead();
            if (lendata > 0) {
				int len = getBufferData(readBuffer, readBufferPointer, lendata);
				lendata = 0;
				if (len>=0) {
					readBufferPointer += len;
					dataLoad = true;

					//Tempo massimo di attesa senza dati
					endTime = System.currentTimeMillis() + (long)waitNoData;
				} else { //Buffer overflow!!!
					readBufferPointer = 0;
					dataLoad = false;
					break;
				}
            }

			//Attendi la pausa (waitNoData) nella ricezione dei dati
			wait = (endTime-System.currentTimeMillis() > 0);

		} while(wait);

		//Non ci sono dati da caricare
		dataAvailable = dataLoad;
	}

   	//-------- Open for writing --------//
	/**
	 * Open output stream
	 */
	public boolean openWrite() {
        writeBufferEmpty = false;

		openToWrite = true;
		if (outputStream==null) {
			try {
				outputStream = serialPort.getOutputStream();
			}  catch (IOException e) {
               openToWrite = false;
			}
		}

		return openToWrite;      
	}

	/**
	 * Write string data 
	 */
	public boolean write(byte[] writeBuffer, int pnt, int len) {
		boolean errWrite = false;
      
        writeBufferEmpty = false;
        try {
            outputStream.write(writeBuffer, pnt, len);
        } catch (IOException e) {
			errWrite = true;
		}
        return errWrite;
	}

	/**
	 * Write string data 
	 */
	public boolean write(String Sbuf) {
        boolean errWrite = false;
        writeBufferEmpty = false;
        try {
            outputStream.write(Sbuf.getBytes());
        } catch (IOException e) {
            errWrite = true;
        }

        return errWrite;
	}

	/**
	 * 
	 */
	public boolean checkWriteBufferEmpty() {
        return writeBufferEmpty;
	}

	/**
	 * 
	 * 
	 */
	private void changedCTS() {
		boolean isOn = serialPort.isCTS();

		if (
			(readMode==0) || (readMode==1 && !isOn) || (readMode==2 && isOn)
		   )
		{
			/// System.out.println("CTS: " + isOn);
		}
	}

}
