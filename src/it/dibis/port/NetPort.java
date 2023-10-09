package it.dibis.port;

/**         
 * File  : NetPort.java
 * Autore: adalborgo@racine.ra.it
 * Nota1 : Su meteofa.it: porta 1111 per scaricare i dati, porta 23 per configurare
 * Nota2 : Con il convertitore blu la situazione � rovesciata: porta 23 per scaricare i dati, porta 1111 per configurare
 */

import java.io.*;
import java.net.*;

public class NetPort implements Runnable {
	//--- Costanti ---
    /**
     *  Revision control id
     */
    private static final String cvsId = "$Id: NetPort.java,v 0.12 01/04/2011 23:59:59 adalborgo $";

	final static boolean DEBUG = true;

	// TCP port
	// Nota1 : Su meteofa.it: porta 1111 per scaricare i dati, porta 23 per configurare
	// Nota2 : Con il convertitore blu la situazione � rovesciata: porta 23 per scaricare i dati, porta 1111 per configurare
	// static final int TELNET_PORT = 23; // SOLO per configurazione!!!
	// static final int SERVER_PORT = 1111; // <-- Sulla stazione di Meteofa
	static final int SERVER_PORT = 23; // <-- Convertitore blu

	final static int WAITNODATA = 0; 		// ms

	static final int ERR_OPEN = 1;
	//~ static final int ERR_NOT_READY = 2;
	//~ static final int ERR_ALREADY_OPEN = 4;
	//~ static final int ERR_READ = 8;
	static final int ERR_WRITE = 16;

	String strAddress = null; // COM-Server IP-Address
	int serverPort = -1;	  // COM-Server Port A (TCP)

	private Socket socket = null;	

	private DataInputStream dataInputStream = null;
	private DataOutputStream dataOutputStream = null;

	private StringBuffer stringBufferRead = new StringBuffer();

	private Thread runner = null;

	//--- Variabili ---//
	boolean portConnected = false;
	boolean readPortReady  = false;
	boolean writePortReady = false;

	private int error = 0; // No error

	// Costruttore
	public NetPort(String lineAddress) {
		// xxx.xxx.xxx.xxx:yyyy
		getaddress_port(lineAddress); // Get strAddress and serverPort from lineAddress
		if (serverPort<0) this.serverPort = SERVER_PORT;
		try {
            socket = new Socket(strAddress, serverPort);
			portConnected = true;
		} catch( IOException e ) {
			System.out.println("Error" + e);
			portConnected = false;
		}
	}

	public boolean open(long wait) {

		boolean opened = false;

		readPortReady = openRead(WAITNODATA);
		writePortReady = openWrite();

		if (readPortReady && writePortReady) {

			// new Thread(this).start();
			if (runner==null) {
				runner = new Thread(this);
				runner.start();
			}

			// Attesa eventuale messaggio 'wellcome'
			delay(wait);
			opened = true;
		}

		return opened;
	}

    /**
     * Close net port
	 *
    */
	public void close() {

		try {
			dataInputStream.close();
			dataOutputStream.close();
			//socket.close() non serve, entrambi chiudono la socket
		} catch(IOException e) {
			System.out.println("close() error!");
		}

        readPortReady = false;
		writePortReady = false;
	}

   	//-------- Open for reading --------//
    /**
     * Open net port
	 *
	 * @param int waitNoData: tempo (ms) di attesa dall'ultimo byte ricevuto prima
	 *	di considerare terminata la ricezione della stringa di dati:
	 *	utilizzata da 'serialEvent()'
    */
	public boolean openRead(int waitNoData) {
	
		boolean opened = false;

		clearBuffer();

		if (dataInputStream==null) {
			try {
				dataInputStream = new DataInputStream(socket.getInputStream());
				opened = true;
			} catch (IOException e) {
				opened = false;
			}
		}

		return opened;
	}

	public int lenReadBuffer() {
		return stringBufferRead.length();
	}

	public boolean checkDataAvailable() {
		return ( stringBufferRead.length()>0 );
	}

	/**
	 * Converte readBuffer in una stringa
	 */	
	public String getBuffer() {
		int len = stringBufferRead.length();
		return stringBufferRead.substring(0, len).toString();
	}

	/**
	 * Cancella i dati nel buffer
	 * Nota: resetReadPointer() in Rs232Port
	 */	
	public void clearBuffer() {
		int len = stringBufferRead.length();
		stringBufferRead.delete(0, len);
	}

	/**
	 * Status port check
	 * @return boolean
	 */
	public boolean getStatus(boolean openread) {
		if (openread) {
			return (readPortReady && writePortReady);
		} else {
			return (readPortReady || writePortReady);
		}
	}

   	//-------- Open for writing --------//
	/**
	 * Open output stream
	 */
	public boolean openWrite() {
		boolean opened = false;

		if (dataOutputStream==null) {
			try {
				dataOutputStream = new DataOutputStream(socket.getOutputStream());
			}  catch (IOException e) {
			}
		}

		if (dataOutputStream==null) {
			error += ERR_OPEN;
			opened = false;
		} else {
			opened = true;
		}

		return opened;      
	}

	/**
	 * Write string data 
	 */
	public boolean write(String s) {
		boolean errWrite = false; // Solo per compatibilita'

		if (dataOutputStream!=null) {
			try {
				for (int i=0; i<s.length(); i++) dataOutputStream.write(s.charAt(i));
			} catch (IOException e) {
				error += ERR_WRITE;
			}
		} else {
			error += ERR_OPEN;
		}
        return errWrite;
	}

	/**
	 * 
	 */
	public void resetError() {
		error = 0;
	}

	// 
	public int getError() {
		return error;
	}

	private void delay(long dtime){
		try {
			Thread.sleep(dtime);
		} catch (InterruptedException e) {
		}
	}

	// Da rivedere!!!
	public void run() {

		byte[] b = new byte[1];

		int datiLetti = 0;
		try {
			do { // Leggi un byte alla volta
				if (socket.isClosed()==false) datiLetti = dataInputStream.read(b, 0, 1); //>=0
				stringBufferRead.append((char) b[0]);
				if (DEBUG && socket.isClosed()) System.out.println("Chiuso");
			} while(datiLetti>=0);
		} catch(Exception e) {
			if (DEBUG) {
				System.out.println("Chiuso? " + socket.isClosed());
				System.out.println("Errore-run(): " + e);
			}
		}
	}

	/**
	 * Get strAddress and port from line
	 * @param line
	 */
	private void getaddress_port(String line) {
		int pnt = line.indexOf(":");
		if (pnt>8) {
			this.strAddress = line.substring(0, pnt);

			// String to int conversion
			String strServerPort = line.substring(pnt+1);
			try {
				this.serverPort = Integer.valueOf(strServerPort);
			} catch ( NumberFormatException e )	{
				System.out.println("Non e' un int!");
			}
		} else {
			this.strAddress = line;
		}
	}

} //end class
