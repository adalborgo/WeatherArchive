package it.dibis.tflogger;

/*
 * Combilog.java
 * API per la stazione meteo Combilog 1020
 * @author: adalborgo@gmail.com
 * Note  : Utilizza Port come interfaccia verso CombiLog
 *		   WAIT = 200L (prima 100L), MAX_TRIES = 5 (prima 3)
 */

import it.dibis.port.*;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Combilog {

	boolean debug = false;

	// --- Costanti --- //

    /**
     *  Revision control id
     */
	private static final String cvsId = "$Id: Combilog.java,v 0.0.2 30/11/2010 23:59:59 adalborgo $";

	//--- Definizioni di COMBILOG 1020 ---//
	final static char SD = '#';
	final static char READ_CMD	= 'R';	// Read command

	final long WAIT = 200L;	// 0.2000 s
	final int MAX_TRIES = 5;

	final static char CR	= 13;
	final static char ACK	= 6;
	final static char NAK	= 21;

	//---------------------------------------//

	static final int ERR_CMD = -1;
	static final int NOERR = 0;
	static final int ERR_OPEN = 1;
	static final int ERR_NOT_READY = 2;
	static final int ERR_ALREADY_OPEN = 4;
	static final int ERR_READ = 8;
	static final int ERR_WRITE = 16;
	static final int ERR_FCS = 32;

	static final int DIM_BYTE_BUFFER = 1024;
	byte[] readBuffer = new byte[DIM_BYTE_BUFFER];

	//--- Variabili ---//
	String deviceNumber = null;
	String loggerPort = null;

	boolean readPortReady  = false;
	boolean writePortReady = false;

	private Port port = new Port();

	private int error = 0; // No error

	//-----------------------------------//

	/**
	 * Costruttore
	 */
	public Combilog(String deviceNumber, String loggerPort) {
		this.deviceNumber = (deviceNumber.length()==1) ? " " +deviceNumber : deviceNumber; // 2-char
		this.loggerPort = loggerPort;
	}

	/**
	 * Invia un comando e restituisce la risposta compreso in carattere di coda (CR | ACK | NAK)
	 * @param	String reqCmd
	 * @return	String 	stringa restituita
	 */
	private String sendCmd(String reqCmd) {

		resetError();

		// Check port open
		if(!getStatus()) {
			error = ERR_OPEN;
			return null;
		}

		StringBuffer line = new StringBuffer();
		int len;
		do { //Check buffer empty
			len = lenReadBuffer();
			if (len>0) resetReadPointer();
		} while(len>0);

		// Send request code
		boolean err = write(reqCmd + "\r");

		int tries = MAX_TRIES;
		if (!err) { 
			boolean waitAns = true; // Wait CR
			do {
				if (lenReadBuffer()>0) { //(checkDataAvailable()) {
					line.append(getBuffer());
					clearBuffer(); // Cancella il buffer
					char endChar = line.charAt(line.length()-1);
					waitAns = !(endChar==CR || endChar==ACK || endChar==NAK);
				}

				if (waitAns) {
					delay(WAIT);
					--tries;
				}

			} while(waitAns && tries>0);
			
			if (tries==0) {
				error += ERR_NOT_READY;
				if (debug) System.out.println("Errore(sendCmd): Combilog non risponde!");
			}

		} else {
			error += ERR_WRITE;	// Errore di scrittura
			if (debug) System.out.println("Errore su richiesta dati!");
		}

		if (error==0) {
			return line.toString();
		} else {
			return null;
		}
	}

	/**
	 * Encode command
	 * @param String device, String channel, char cmd
	 * @return String cmdReq + fcs(cmdReq)
	 */
	private String encodeCksCmd(String device, String channel, char cmd) {
		// String reqCmd = "#01R01xx";
		String cmdReq = String.valueOf(SD) + device + String.valueOf(cmd) + channel;

		return cmdReq + fcs(cmdReq);
	}

	/**
	 * Encode command
	 * @param String device, String channel, String cmd
	 * @return String cmdReq + fcs(cmdReq)
	 */
	private String encodeCksCmd(String device, String channel, String cmd) {
		// String reqCmd = "#01R01xx";
		String cmdReq = String.valueOf(SD) + device + cmd + channel;

		return cmdReq + fcs(cmdReq);
	}

	/**
	 * Genera il checksum
	 * @param	String data		stringa da cui calcolare il checksum
	 * @return	checksum (caratteri alfabetici in maiuscolo)
	 */
	private String fcs(String data) {

		int sum = 0;
		for (int i=0; i<data.length(); i++) {
			char c = data.charAt(i);
			sum += c;
		}

		String s = Integer.toHexString(sum%256).toUpperCase();
		// Sempre 2 caratteri
		return "00".concat(s).substring(s.length());
	}

	/**
	 * Rimuove l'intestazione, controlla e rimuove il carattere di controllo,
	 * rimuove eventuali spazi in testa e coda
	 * @param String lineRead
	 * @return String data field
	 */
	private String getDataField(String line) {

		int dataBegin = 1; // Inizio campo dati
		int fcsPnt = 0;

		boolean check = false;

		if (line!=null && error==0) {

			fcsPnt = line.length()-3; // Posizione fcs
			if (fcsPnt>=0) {
				//~ fcsRead = line.substring(fcsPnt, fcsPnt+2); //fcs(hex_2)
				//~ fcsCalc = fcs(line.substring(0, fcsPnt));
				check = line.substring(fcsPnt, fcsPnt+2).equals(fcs(line.substring(0, fcsPnt)));
			}

		} else {
			error += ERR_READ;
			return null;
		}

		if (check)
			return line.substring(dataBegin, fcsPnt).trim();
		else {
			if (debug) System.out.println("Errore: getDataField()!");
			error += ERR_FCS;
			return null;
		}
	}

	//------------------------------------------------//
	//--------------- Comandi CombiLog ---------------//
	//------------------------------------------------//
	/**
	 * Lettura di un dato dal canale assegnato
	 * @param String channel	canale di lettura
	 * @return double
	 */
	public float getChannelData(String channel) {

		float data = Float.NEGATIVE_INFINITY;	// Default error
		String lineRead = null;

		if (debug) System.out.println("deviceNumber: " + deviceNumber + ", Command: " + READ_CMD + ", channel: " + channel);

		// Invia la richiesta della temperatura istantanea
		lineRead = sendCmd(encodeCksCmd(deviceNumber, channel, READ_CMD));
		if (lineRead!=null && error==0) {
			//// if (debug) System.out.println("\nStringa letta: " + lineRead);
			data = getFloatDataField(lineRead);
		} else {
			if (debug) System.out.println("Errore: getChannelData() - Combilog non risponde!");
		}

		return data;
	}

	/**
	 * Rimuove l'intestazione, controlla e rimuove il carattere di controllo,
	 * converte il campo dati in un double
	 * @param String lineRead
	 * @return double data
	 */
	public float getFloatDataField(String lineRead) {
		String dataField = getDataField(lineRead);
		float x = Float.NEGATIVE_INFINITY; // Default: errore
		if (dataField!=null && error==0) {
			try {
				x = Float.parseFloat(dataField.trim());
			} catch(NumberFormatException e) {
				System.out.println("Combilog.getFloatDataField: " + e);
			}
			return x;
		} else {
			if (debug) System.out.println("Errore: getFloatDataField()!");
			error += ERR_READ;
			return -99999.9F; // Errore
		}
	}

	/**
	 * Lettura dati di identificazione NetCombiLog
	 * @return String
	 */
	public String getDeviceIdentification() {
		String lineRead = getDataField( sendCmd(encodeCksCmd(deviceNumber, "", 'V')) );
		if (lineRead!=null && error==0) {
		} else {
			if (debug) System.out.println("Errore: getDeviceIdentification()!");
		}

		return lineRead;
	}

	/**
	 * Lettura dati di informazione del NetCombiLog
	 * @return String
	 */
	public String getDeviceInfo() {
		String lineRead = getDataField( sendCmd(encodeCksCmd(deviceNumber, "", 'S')) );
		if (lineRead!=null && error==0) {
		} else {
			if (debug) System.out.println("Errore: getDeviceInfo()!");
		}

		return lineRead;
	}

	/**
	 * Lettura dei parametri di stato
	 * @return String
	 */
	public String getStatusInfo() {
		String lineRead = getDataField( sendCmd(encodeCksCmd(deviceNumber, "", 'Z')) );
		if (lineRead!=null && error==0) {
		} else {
			if (debug) System.out.println("Errore: getStatusInfo()!");
		}

		return lineRead;
	}

	/**
	 * Lettura di informazioni dal canale assegnato
	 * @param String channel	canale di lettura
	 * @return String
	 */
	public String getChannelInfo(String channel) {
		String lineRead = getDataField( sendCmd(encodeCksCmd(deviceNumber, channel, 'B')) );
		if (lineRead!=null && error==0) {
		} else {
			if (debug) System.out.println("Errore: getChannelInfo!");
		}

		return lineRead;
	}

	/**
	 * Lettura Data e Ora
	 * @return String  aammgghhmmss
	 */
	public String getDataTime() {
		String lineRead = getDataField( sendCmd(encodeCksCmd(deviceNumber, "", 'H')) );
		if (lineRead!=null && error==0) {
		} else {
			if (debug) System.out.println("Errore: getDataTime()!");
		}

		return lineRead;
	}

	/**
	 * Sync timestamp with PC
	 * Nota: duplicato in CombiLog
	 * @param String  dateTime (aammgghhmmss)
	 * @return int error
	 */
	public int synchDataTime() {
		// dateTime format = yyMMddHHmmss
		String dateTime = new SimpleDateFormat ("yyMMddHHmmss").format(new Date());

		String lineRead = sendCmd(encodeCksCmd(deviceNumber, dateTime, 'G'));
		if (lineRead!=null && error==NOERR) {
			return (lineRead.equals(String.valueOf(ACK))) ? NOERR : ERR_CMD;
		} else {
			if (debug) System.out.println("Errore: setDataTime()!");
			return ERR_CMD;
		}
	}

	//--------------- Nuovi Comandi ---------------//
	/**
	 * Delete all data in memory
	 * @return boolean
	 */
	public boolean deleteDataMemory() {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, "", "C.ALL"));
		if (lineRead!=null && error==0) {
			return (lineRead.equals(String.valueOf(ACK)));
		} else {
			if (debug) System.out.println("Errore: deleteDataMemory()!");
			return false;
		}
	}

	/**
	 * Reset a channel (Non funziona!!!)
	 * N.B. Il manuale riporta la lettera 'D' ma funziona solo con 'W'
	 *
	 * @param String channel	canale di lettura
	 * @return boolean
	 */
	public boolean resetChannel(String channel) {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, channel, 'W'));
		if (lineRead!=null && error==0) {
			return (lineRead.equals(String.valueOf(ACK)));
		} else {
			if (debug) System.out.println("Errore: resetChannel()!");
			return false;
		}
	}

	//--- Puntatore #1 ---//
	/**
	 * Legge l'evento indirizzato dal puntatore #1 e fa avanzare il puntatore
	 *
	 * @return String
	 */
	public String readIncEventFromPointer1() {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, "", 'E'));
		if (lineRead!=null && error==0) {
			return getDataField(lineRead);
		} else {
			if (debug) System.out.println("Errore: readIncEventFromPointer1()!");
			//return null;
			return "";
		}
	}

	/**
	 * Legge l'evento indirizzato dal puntatore #1 senza fare avanzare il puntatore
	 * 
	 * @return String
	 */
	public String readEventFromPointer1() {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, "", 'F'));
		if (lineRead!=null && error==0) {
			return getDataField(lineRead);
		} else {
			if (debug) System.out.println("Errore: readEventFromPointer1()!");
			return null;
		}
	}

	/**
	 * Imposta il puntatore #1 all'inizio dell'area di memoria
	 * 
	 * @return boolean
	 */
	public boolean setTopMemoryPointer1() {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, "", 'C'));
		if (lineRead!=null && error==0) {
			return (lineRead.equals(String.valueOf(ACK)));
		} else {
			if (debug) System.out.println("Errore: setTopMemoryPointer1()!");
			return false;
		}
	}

	/**
	 * Imposta il puntatore #1 ad una determinata data
	 *
	 * @param String dateTime
	 * @return boolean
	 */
	public boolean setDateMemoryPointer1(String dateTime) {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, dateTime, 'C'));
		if (lineRead!=null && error==0) {
			return (lineRead.equals(String.valueOf(ACK)));
		} else {
			if (debug) System.out.println("Errore: setDateMemoryPointer1()!");
			return false;
		}
	}

	/**
	 * Imposta il puntatore #1 ad una determinata data_ora
	 * NOTA: Funziona SOLO con il puntatore #2 !!!
	 *
	 * @param int pointer
	 * @return boolean
	 */
	//~ boolean setDateMemoryPointer1(int pointer) {
		//~ String lineRead = sendCmd(encodeCksCmd(deviceNumber, String.valueOf(pointer), 'C'));
		//~ if (lineRead!=null && error==0) {
			//~ return (lineRead.equals(String.valueOf(ACK)));
		//~ } else {
			//~ if (debug) System.out.println("Errore: setDateMemoryPointer1()!");
			//~ return false;
		//~ }
	//~ }

	//--- Puntatore #2 ---//
	/**
	 * Legge l'evento indirizzato dal puntatore #2 e fa avanzare il puntatore
	 *
	 * @return String
	 */
	public String readIncEventFromPointer2() {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, "", 'e'));
		if (lineRead!=null && error==0) {
			return getDataField(lineRead);
		} else {
			if (debug) System.out.println("Errore: readIncEventFromPointer2()!");
			return null;
		}
	}

	/**
	 * Legge l'evento indirizzato dal puntatore #2 senza fare avanzare il puntatore
	 * 
	 * @return String
	 */
	public String readEventFromPointer2() {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, "", 'f'));
		if (lineRead!=null && error==0) {
			return getDataField(lineRead);
		} else {
			if (debug) System.out.println("Errore: readEventFromPointer2()!");
			return null;
		}
	}

	/**
	 * Imposta il puntatore #2 all'inizio dell'area di memoria
	 * 
	 * @return boolean
	 */
	public boolean setTopMemoryPointer2() {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, "", 'c'));
		if (lineRead!=null && error==0) {
			return (lineRead.equals(String.valueOf(ACK)));
		} else {
			if (debug) System.out.println("Errore: setTopMemoryPointer2()!");
			return false;
		}
	}

	/**
	 * Imposta il puntatore #2 ad una determinata data_ora
	 * NOTA: Funziona SOLO con il puntatore #2
	 *       Per il n. di eventi successivi usare SOLO readEventNumber2()
	 *
	 * @param String dateTime
	 * @return boolean
	 */
	public boolean setDateMemoryPointer2(String dateTime) {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, dateTime, 'c'));
		if (lineRead!=null && error==0) {
			return (lineRead.equals(String.valueOf(ACK)));
		} else {
			if (debug) System.out.println("Errore: setDateMemoryPointer2()!");
			return false;
		}
	}

	/**
	 * Imposta il puntatore #2 ad una determinata posizione
	 * 
	 * @param int pointer
	 * @return boolean
	 */
	public boolean setDateMemoryPointer2(int pointer) {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, String.valueOf(pointer), 'c'));
		if (lineRead!=null && error==0) {
			return (lineRead.equals(String.valueOf(ACK)));
		} else {
			if (debug) System.out.println("Errore: setDateMemoryPointer2()!");
			return false;
		}
	}

	/**
	 * Legge il numero di eventi (SOLO dall'inizio della memoria = TOTALE)
	 * 
	 * @return int
	 */
	public int readEventNumber1() {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, "", 'N'));
		if (lineRead!=null && error==0) {
			return (int) getFloatDataField(lineRead);
		} else {
			if (debug) System.out.println("Errore: readEventNumber2()!");
			return -1;
		}
	}

	/**
	 * Legge il numero di eventi
	 * 
	 * @return int
	 */
	public int readEventNumber2() {
		String lineRead = sendCmd(encodeCksCmd(deviceNumber, "", 'n'));
		if (lineRead!=null && error==0) {
			return (int) getFloatDataField(lineRead);
		} else {
			if (debug) System.out.println("Errore: readEventNumber2()!");
			return -1;
		}
	}

	//---------------------------------------//
	//--- Gestione porta di comunicazione ---//
	//---------------------------------------//
	/**
	 * Open port
	 * 
	 * @return int port.err
	 */
	public int open() {
		return port.open(loggerPort);
	}

	/**
	 * Status port check
	 * @return boolean
	 */
	public boolean getStatus() {
		return port.getStatus();
	}

	/**
	 * Close port
	 */
	public void close() {
		port.close();
	}

	/**
	 * 
	 */
	public String getBuffer() { //???
		return port.getBuffer();
	}

	/**
	 * 
	 */
	public int lenReadBuffer() {
		return port.lenReadBuffer();
	}

	/**
	 * 
	 */
	public void resetReadPointer() { //???
		port.clearBuffer();
	}

	/**
	 * 
	 */
	public void clearBuffer() { //???
		port.clearBuffer();
	}

	/**
	 * 
	 */
	public boolean write(String s) {
		return port.write(s);
	}

	/**
	 * 
	 */
	public boolean checkDataAvailable() { //???
		return port.checkDataAvailable();
	}

	//---------------------------------------//
	/**
	 * 
	 */
	public void resetError() {
		error = 0;
	}

	/**
	 * 
	 */
	public int getError() {
		return error;
	}

	/**
	 * 
	 */
	private void delay(long dtime){
		try { Thread.sleep(dtime); } catch (InterruptedException e) { }
	}

	public static String getCvsid() {
		return cvsId;
	}

}
