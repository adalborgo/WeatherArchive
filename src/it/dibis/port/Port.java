package it.dibis.port;

/**
 * Nome  Port.java
 * Azione: Interfacciamento verso NetPort e Rs232Port
 * @author: Antonio Dal Borgo
 * Ver.  : 0.0.1 (29-12-2005?)
 * Data  : 18-10-2010 (29-12-2005?)
 */

public class Port {

	//--- Gestione Porta seriale ---
	static final int OPEN_PORT_TIMEOUT = 500;	// ms
	static final int WAITNODATA = 0; 			// ms

	// --- Porta seriale ---//
	private Rs232Port rs232Port = null;

	// --- Porta di rete ---//
	private NetPort netPort = null;

	//-----------------------------------//

	static final int ERR_OPEN = 1;
	static final int ERR_ALREADY_OPEN = 4;
 
	//--- Variabili ---//
	private int comPort = 0; // Net Port
	private int error = 0; // No error

	private boolean readPortReady  = false;
	private boolean writePortReady = false;

	//-----------------------------------//

	public Port() {
	}

	/**
	 * Open port loggerPort
	 * @param String loggerPort (porta seriale = {"1"|"2"}; porta di rete = "192.168.x.x"	
	 * @return int err
	 */
	public int open(String loggerPort) {

		int err = 0;

System.out.println("Port: " + loggerPort);

		//--- Connessione ---//
		// String loggerPort:
		// 	porta seriale = {"1"|"2"}; porta di rete = "192.168.0.99"	

     	// Controllo dei parametri assegnati
		if ( loggerPort.equals("1") || loggerPort.equals("2")
				|| loggerPort.equals("3") || loggerPort.equals("4") ) {
			comPort = Integer.valueOf(loggerPort).intValue();
			err = serialConnect(comPort);
		} else {
			comPort = 0;
			err = netConnect(loggerPort);
		}

		//--- Apertura ---//
		if (err==0) {
			if (comPort==0) {
				err = openNet();
			} else {
				err = openSerial();
			}
		} else {
			err = ERR_OPEN;
		}

		return err;
	}

	/**
	 * Close port
	 */
	public void close() {
		if (comPort==0) {
			closeNet();
		} else {
			closeSerial();
		}
	}

	/**
	 * Status port check
	 * @return boolean
	 */
	public boolean getStatus() {
		if (comPort==0) {
			return netPort.getStatus(true);
		} else {
			return (readPortReady || writePortReady);
		}
	}

	/**
	 * 
	 * @return String
	 */
	public String getBuffer() {
		if (comPort==0) {
			return netPort.getBuffer();
		} else {
			return rs232Port.getBuffer();
		}
	}

	/**
	 * 
	 * @return int 
	 */
	public int lenReadBuffer() {
		if (comPort==0) {
			return netPort.lenReadBuffer();
		} else {
			return rs232Port.lenReadBuffer();
		}
	}

	/**
	 * 
	 */
	public void clearBuffer() {
		if (comPort==0) {
			netPort.clearBuffer();
		} else {
			// Non implementato
		}
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean write(String s) {
		if (comPort==0) {
			return netPort.write(s);
		} else {
			return rs232Port.write(s);
		}
	}

	/**
	 * 
	 */
	public boolean checkDataAvailable() {
		if (comPort==0) {
			return netPort.checkDataAvailable();
		} else {
			return rs232Port.checkDataAvailable();
		}
	}

	/**
	 * Reset Error
	 */
	public void resetError() {
		error = 0;
	}

	/**
	 * 
	 * @return int error
	 */
	public int getError() {
		return error;
	}

	//------------------- Serial Port -------------------//
    /**
     * Apre la porta seriale solo se rs232Port==null 
     * @param  int port Port number = {1|2|3|4} for Windows and Linux
	 * Note: (rs232Port is non static!!!)
	 * 		 !!! Bisogna modificare RS232 per dare la possibilita' di chiudere e riaprire la porta
     */
	public int serialConnect(int comPort) {

		String portName		= "rs232Port";
		int baudRate		= 19200;
		int databit			= 8;
		int stopbit			= 1;
		int parity			= 0;
		int flowControlIn	= 0;
		int flowControlOut	= 0;

		resetError();

		if (rs232Port==null) {
			rs232Port = new Rs232Port(portName, comPort, baudRate,
				databit, stopbit, parity, flowControlIn, flowControlOut, OPEN_PORT_TIMEOUT, true);
		} else {
			error = ERR_ALREADY_OPEN; // Porta gia' aperta
		}

		return error;
    }

	/**
	 * Open serial port
	 * @return int
	 */
	public int openSerial() {

		if (rs232Port.portConnected) {
			readPortReady = rs232Port.openRead(WAITNODATA);
			writePortReady = rs232Port.openWrite();

			if ( !readPortReady || !writePortReady ) {	//Errore in apertura o chiusura
				rs232Port.close();    //Chiusura porta seriale
				error = ERR_OPEN;
			}
		} else {
			error = ERR_OPEN; // La porta non e' aperta
			System.out.println("La porta non e' aperta!");
		}

		return error;
	}

	/**
	 * Close serial port
	 */
	public void closeSerial() {
		//Close serial port
		if (rs232Port.portConnected) {	//Check buffer empty
			int len = rs232Port.lenReadBuffer();;
			while (len>0) {
				rs232Port.resetReadPointer();
				len = rs232Port.lenReadBuffer();
			}

			rs232Port.close();	//Close
		}
	}

	//------------------- Net Port -------------------//
    /**
     * Apre la porta di rete se netPort==null
	 * Note: netPort = 0
	 *
     * @param  int port Port number = {0|1|2|3|4} for Windows and Linux
     */
	public int netConnect(String strAddress) {
		resetError();
		if (netPort==null) {
			netPort = new NetPort(strAddress);
		} else {
			error = ERR_ALREADY_OPEN; // Porta gia' aperta
		}

		return error;
    }

	/**
	 * Open net port
	 * @return int
	 */
	public int openNet() {
		if (netPort.portConnected) {
			boolean opened = netPort.open(100);
			if (opened) {
				// Deve cancellare dal buffer il messaggio di connessione!!!
				readPortReady = writePortReady = (getBuffer().length()>0);
				clearBuffer(); // Cancella il buffer
			} else { //Errore in apertura o chiusura
				netPort.close();    //Chiusura porta seriale
				error = ERR_OPEN;
			}

		} else {
			error = ERR_OPEN; // La porta non e' aperta
			System.out.println("La porta non e' aperta!");
		}

		return error;
	}
	/**
	 * Close net port
	 */
	public void closeNet() {
		netPort.close();
	}
} // end class
