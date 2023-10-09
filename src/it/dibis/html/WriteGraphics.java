package it.dibis.html;

import java.awt.*;
import java.text.DecimalFormat;
import javax.swing.border.*;

import it.dibis.config.ConfigHtml;
import it.dibis.dataObjects.DataOfDay;
import it.dibis.dataObjects.SharedData;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

import it.dibis.common.Constants;

public class WriteGraphics extends JPanel implements Constants {

	//--- COSTANTI ---//

	/**
	 *  Revision control id
	 */
	private static final String cvsId = "$Id: WriteGraphics.java,v 0.1 25/11/2013 23:59:59 adalborgo@gmail.com $";

	// DEBUG
	static final boolean DEBUG = false;

	static final String IMAGE_FORMAT = "gif";
	
	static final float X_BEGIN = 0;
	static final float X_END = 24;

	static final int LEFT_BORDER   = 0; // 20;
	static final int RIGHT_BORDER  = 0; // 20;
	static final int TOP_BORDER    = 0; // 20;
	static final int BOTTOM_BORDER = 0; // 20;

	static final int WIDTH_AREA_GRAPH = 600;
	static final int HEIGHT_AREA_GRAPH = 300;

	// Dimensioni della finestra fisica
	static final int ORIGIN_X = LEFT_BORDER + 40;
	static final int ORIGIN_Y = TOP_BORDER + 48;

	static final int WIDTH_AXIS = WIDTH_AREA_GRAPH-LEFT_BORDER - 80;
	static final int HEIGHT_AXIS = HEIGHT_AREA_GRAPH-TOP_BORDER - 100;

	// Image dimension (default values)
	static final int WIDTH_IMAGE  = LEFT_BORDER + WIDTH_AREA_GRAPH + RIGHT_BORDER;
	static final int HEIGHT_IMAGE = TOP_BORDER + HEIGHT_AREA_GRAPH + BOTTOM_BORDER;

	static final int STEP_AXIS  = 30; // Vertical step

	static final String[] NESW = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"}; // Direzione VENTO

	static final int XMARK_NUM = SAMPLES_OF_DAY/4; // Numero marche principali asse x
   
	// Titolo basso
	static final String TITLE_BOTTOM = "Ora solare";
   
	// Colors
	public final static java.awt.Color BLUE1 = new Color(8, 77, 175); //(5, 50, 158);

	static final Color COLOR_BOTTOM_TITLE = Color.black;
	static final Color COLOR_GRAPH_ROW = Color.lightGray;
	static final Color COLOR_GRAPH_BOX = Color.black;
	static final Color COLOR_IGRO = new Color(50, 100, 200); // ,100
	static final Color COLOR_PRES = BLUE1;
	static final Color COLOR_TERMO = Color.red; 
	static final Color COLOR_WIND = Color.blue;
	static final Color COLOR_WIND_TITLE = BLUE1;
	static final Color COLOR_WDIR = new Color(120, 120,120); // Color.lightGray;
	static final Color COLOR_WDIR_TITLE = new Color(100, 100,100);
	static final Color COLOR_RAIN = Color.blue;
	static final Color COLOR_RAIN_TITLE = BLUE1;

	// Graph index
	static final int GRAPH_TERMO = 1;
	static final int GRAPH_PRES  = 2;
	static final int GRAPH_WIND  = 3;
	static final int GRAPH_RAIN  = 4;
	static final int MAX_SEL_GRAPH = GRAPH_RAIN;

	// Estremi di scala
	static final float TERMO_MIN = -30f, TERMO_MAX = 50f;
	static final float IGRO_MIN = 0f,    IGRO_MAX = 100f;
	static final float PRES_MIN = 500f,  PRES_MAX = 1050f;
	static final float WIND_MIN = 0f,    WIND_MAX = 180f;
	static final float WDIR_MIN = 0f,    WDIR_MAX = 360f;
	static final float RAIN_MIN = 0f,    RAIN_MAX = 409.4f;
	static final float SRAD_MIN = 0f,    SRAD_MAX = 999.9f; // ????

	// Filename of graph
	static final String FILE_GIF_TERMO = "temp_humi";
	static final String FILE_GIF_PRES  = "pressure";
	static final String FILE_GIF_WIND  = "wind";
	static final String FILE_GIF_RAIN  = "rain";

	static final Color IMAGE_BACKGROUND_COLOR = Color.WHITE;

	// Font of axis
	static final Font FONT_AXIS  = new Font("Arial", Font.BOLD, 14);
	static final Font FONT14     = new Font("Arial", Font.BOLD, 14);
	static final Font FONT_TR24  = new Font("TimesRoman", Font.BOLD, 24);
	static final Font TITLE_FONT = new Font("Arial", Font.PLAIN, 20);
	static final Font DATA_FONT  = new Font("Arial", Font.PLAIN, 12);

	//--- Definizione di variabili ---//
	float yBeg1, yEnd1, yBeg2, yEnd2; // Estremi della finestra virtuale
	float xStep;  // Incremento punti asse x
	int yMarkNum; // Numero marche principali asse y

	// Indica se devono essere presentati 2 grafici contemporaneamente
	boolean data2graph = false;

	// Background color
	Color bgColor = null;

	// Graphics color
	Color graphColor1, graphColorTitle1; // 1^ grafico
	Color graphColor2, graphColorTitle2; // 2^ grafico

	String titleData1 = null;
	String titleData2 = null;

	// Arrays
	float[] data1 = new float[SAMPLES_OF_DAY]; // Prima serie di dati
	float[] data2 = new float[SAMPLES_OF_DAY]; // Seconda serie di dati

	int selGraph;

	float noData1, noData2;
	int dataTyp1, dataTyp2;

	boolean dataLoad = false;
	boolean noDataRead = false;
   
	Image imgBuffer = null;

	DecimalFormat formD = new DecimalFormat();

	float[] unitFactor = null;
	float windvelUnitFactor;
	String[] unitSymbol = null;

	// Graph area origin
	int graphAreaOriginX = LEFT_BORDER;
	int graphAreaOriginY = TOP_BORDER;

	BufferedImage imageBuffer;
    Graphics2D g;
    
	String imagePath;

	// static String [] windNames = new String[8];

	public WriteGraphics() { }

	/**
	 * Carica i dati
	 */
	public void writeAllGraphics(DataOfDay dataOfDay, SharedData configData,
			ConfigHtml configHtml, String rootOfHtmlPath) {

		this.imagePath = configData.getRootOfHtmlPath();
		
		this.unitFactor = configHtml.getUnitFactor();
		this.unitSymbol = getUnitSymbol(configHtml.getUnitSymbol());
		this.windvelUnitFactor = configHtml.getUnitFactor()[WINDSPEED_INDEX];

		// Set graphics environment
		Dimension sizePanel = new Dimension(WIDTH_IMAGE, HEIGHT_IMAGE);
		xStep = (X_END-X_BEGIN)/SAMPLES_OF_DAY;

		setPreferredSize(sizePanel);
		setBackground(bgColor);
		setBorder( BorderFactory.createCompoundBorder(
				new EmptyBorder(0, 0, 0, 0), new EmptyBorder(0, 0, 0, 0)) );

		setWindow();

		// Save all files
		selGraph = GRAPH_TERMO;
		loadData(dataOfDay, selGraph, false);
		plotGraph();
		saveImage(imagePath + FILE_GIF_TERMO, IMAGE_FORMAT);
		
		selGraph = GRAPH_PRES;
		loadData(dataOfDay, selGraph, false);
		plotGraph();
		saveImage(imagePath + FILE_GIF_PRES, IMAGE_FORMAT);

		selGraph = GRAPH_WIND;
		loadData(dataOfDay, selGraph, false);
		plotGraph();
		saveImage(imagePath + FILE_GIF_WIND, IMAGE_FORMAT);
		
		selGraph = GRAPH_RAIN;
		loadData(dataOfDay, selGraph, false);
		plotGraph();
		saveImage(imagePath + FILE_GIF_RAIN, IMAGE_FORMAT);
	}

	/**
	 * Convert html some special characters
	 * 
	 * @param htmlSymbol
	 * @return
	 */
	private String[] getUnitSymbol(String[] htmlSymbol) {
		int len = htmlSymbol.length;
		String[] unitSymbol = new String[len];
		for (int i=0; i<len; i++) {
			if (htmlSymbol[i].indexOf("&deg;")>=0) {
				unitSymbol[i] = htmlSymbol[i].replace("&deg;", "\u00B0");
			} else if (htmlSymbol[i].indexOf("<sup>2</sup>")>=0){
				unitSymbol[i] = htmlSymbol[i].replace("<sup>2</sup>", "\\u00b2");
			} else {
				unitSymbol[i] = htmlSymbol[i];
			}
		}

		return unitSymbol;
	}

	/**
	 * Carica e seleziona i dati
	 * @param dataArray
	 * @param selGraph
	 * @param noDataRead
	 */
	void loadData(DataOfDay dataOfDay, int selGraph, boolean noDataRead) {
 
		float[][] dataArray = dataOfDay.getDataArray();
		this.noDataRead = noDataRead;
		this.selGraph = selGraph; // Altrimenti diventa var locale

		// Copia i dati da dataArray[] a data1[] e data2[]
		float min1 = +4096f; // Inizializza min
		float max1 = -4096f; // Inizializza max
		float xVal;

		for (int i=0; i<SAMPLES_OF_DAY; i++) {
			switch (selGraph) {
			    case GRAPH_TERMO: // Temperatura & UMIDITA
				  xVal = dataArray[TEMPERATURE_INDEX][i];
				  if (xVal>=TERMO_MIN && xVal<=TERMO_MAX){
					 this.data1[i] = unitFactor[TEMPERATURE_INDEX]*xVal;
					 if (xVal<min1) min1 = xVal; // Min
					 if (xVal>max1) max1 = xVal; // Max
				  }
				  else this.data1[i] = TERMO_MIN-1;
			   
				  xVal = dataArray[HUMIDITY_INDEX][i];
				  if (xVal>=0 && xVal<=100f){ // min-max
					 this.data2[i] = unitFactor[HUMIDITY_INDEX]*xVal; // %
				  }
				  else this.data2[i] = IGRO_MIN-1;
				  break;
			
			    case GRAPH_PRES: // Pressione
				  xVal = dataArray[PRESSURE_INDEX][i];
				  if (xVal>=PRES_MIN && xVal<=PRES_MAX){
					 this.data1[i] = unitFactor[PRESSURE_INDEX]*xVal; // kPa
					 if (xVal<min1) min1 = xVal; // Min
					 if (xVal>max1) max1 = xVal; // Max
				  }
				  else this.data1[i] = PRES_MIN-1;
				  break;
			
			    case GRAPH_WIND: // Velocita' & direzione VENTO
				  xVal = dataArray[WINDSPEED_INDEX][i];
				  if (xVal>=WIND_MIN && xVal<=WIND_MAX){
					 this.data1[i] = unitFactor[WINDSPEED_INDEX]*xVal; // km/h
					 if (xVal<min1) min1 = xVal; // Min
					 if (xVal>max1) max1 = xVal; // Max
				  }
				  else this.data1[i] = WIND_MIN-1;
			   
				  xVal = dataArray[WINDDIR_INDEX][i];
				  if (xVal>=WDIR_MIN && xVal<=WDIR_MAX)
					 this.data2[i] = unitFactor[WINDDIR_INDEX]*xVal; // gradi
				  else this.data2[i] = WDIR_MIN-1;
				  break;
			
			    case GRAPH_RAIN: // Precipitazioni
				  xVal = dataArray[RAIN_INDEX][i];
				  if (xVal>=RAIN_MIN && xVal<=RAIN_MAX){
					 this.data1[i] = unitFactor[RAIN_INDEX]*xVal; // mm
					 if (xVal<min1) min1 = xVal; //Min
					 if (xVal>max1) max1 = xVal; //Max
				  }
				  else this.data1[i] = RAIN_MIN-1;
				  break;
			} // end switch
		} // end for

		switch (selGraph) {
			case GRAPH_TERMO: // T + U
				// Temperatura
			   dataTyp1 = TEMPERATURE_INDEX+1;
			   titleData1 = "Temperatura (" + unitSymbol[TEMPERATURE_INDEX] + ")";
			   graphColorTitle1 = COLOR_TERMO;
			   graphColor1 = COLOR_TERMO;
			   noData1 = TERMO_MIN-1;
			   yMarkNum = 8; // Numero righe asse y
			   if (min1>0) { // Scala alta
				  yBeg1 = 0f; yEnd1 = 40f;
			   } else { // Scala bassa
				  yBeg1 = -20f; yEnd1 = 20f;
			   }
			
				// Umidita'
			   data2graph = true;
			   dataTyp2 = HUMIDITY_INDEX+1;
			   titleData2 = "Umidit\u00E0 (%)";
			   noData2 = IGRO_MIN-1;
			   graphColorTitle2 = COLOR_IGRO;
			   graphColor2 = COLOR_IGRO;
			   yBeg2 = 20f; yEnd2 = 100f;
			   break;

			case GRAPH_PRES: // Pressione
			   data2graph = false;
			   dataTyp1 = WINDSPEED_INDEX+1;
			   titleData1 = "Pressione (hPa)";
			   graphColorTitle1 = COLOR_PRES;
			   graphColor1 = COLOR_PRES; // blue
			   noData1 =  PRES_MIN-1;
			   yMarkNum = 5; // Numero righe asse y

			   // Selezione scala asse y
			   final int MIN_STEP = 5;
			   int yRange = (max1-min1<=25) ? 25 : 50;
			   if (min1>=PRES_MIN && max1<=PRES_MAX) {
				   yBeg1 = MIN_STEP*(int)((min1-1)/MIN_STEP); yEnd1 = yBeg1+yRange;
			   } else {
				   yBeg1 = PRES_MIN; yEnd1 = PRES_MAX;
			   }
			   break;
		 
			case GRAPH_WIND: // Vento
			   dataTyp1 = WINDSPEED_INDEX+1;
			   titleData1 = "Velocit\u00E0 vento (km/h)";
			   graphColorTitle1 = COLOR_WIND_TITLE;
			   graphColor1 = COLOR_WIND;
			   noData1 = WIND_MIN-1;
			   yBeg1=0f; yEnd1 = 40f;
			   yMarkNum = 8; // Numero righe asse y

				// Direzione
			   data2graph = true;
			   dataTyp2 = WINDDIR_INDEX+1;
			   titleData2 = "Direzione vento (" + unitSymbol[WINDDIR_INDEX] + ")";
			   graphColorTitle2 = COLOR_WDIR_TITLE; //new Color(100, 100,100);
			   graphColor2 = COLOR_WDIR;
			   noData2 = WDIR_MIN-1;
			   yBeg2 = 0f; yEnd2 = 360f;
			   break;
		 
			case GRAPH_RAIN: // Precipitazioni
			   data2graph = false;
			   dataTyp1 = RAIN_INDEX+1;
			   titleData1 = "Precipitazioni (mm)";
			   graphColorTitle1 = COLOR_RAIN_TITLE;
			   graphColor1 = COLOR_RAIN;
			   noData1 =  0; // View.RAIN_MIN-1;
			   yMarkNum = 10; // Numero righe asse y
			   yBeg1 = 0f; yEnd1 = 50f;
			   break;
		} // end case
	  
		dataLoad = true; // Abilita 'paint' a visualizzare il grafico
	}

	/**
	 * Prepara la finestra del grafico e visualizza gli assi
	 * @param g
	 * @param bgColor
	 * @param xOrig
	 * @param yOrig
	 * @param width
	 * @param height
	 */
	void plotGraph() {

		// Clear graph area
		g.setColor(IMAGE_BACKGROUND_COLOR);
        g.fillRect(0, 0, WIDTH_IMAGE, HEIGHT_IMAGE);

		// Se i dati non sono validi esci
		if (noDataRead) {
			noDateMessage(g);
			return;
		}

		int y2 = (int) yEnd2;

		//--- Asse x ---//
		float dx = (X_END-X_BEGIN)/XMARK_NUM;
		for (float xm = X_BEGIN; xm <= X_END; xm += dx) {
			// Disegna righe asse x
			if (xm < X_END) {
			   g.setColor(COLOR_GRAPH_ROW);
			   g.drawLine (xPset(xm), yPset1(yBeg1), xPset(xm), yPset1(yEnd1));
			}
		 
			// Scrivi valori asse x
			int dl = g.getFontMetrics().stringWidth(String.valueOf((int)xm))/2 - 1;
			g.setFont(FONT_AXIS);
			g.setColor(Color.black);
			g.drawString("" + (int)xm, xPset(xm) - dl, yPset1(yBeg1) + 16);
		}

		//--- Asse y ---//
		// Titolo dati 1
		g.setFont(FONT14);
		g.setColor(graphColorTitle1); // Colore titolo dati1
		if (DEBUG) System.out.println("WriteGraphics 369: " + titleData1 + ", " + ORIGIN_X + ", " + (ORIGIN_Y - 20) + ", " + WIDTH_AXIS);
		printString(-1, titleData1, ORIGIN_X, ORIGIN_Y - 20, WIDTH_AXIS);

		// Titolo dati 2
		if (data2graph) {
			g.setColor(graphColorTitle2); // Assegna colore dati2
			printString(1, titleData2, ORIGIN_X, ORIGIN_Y - 20, WIDTH_AXIS);
		}
	  
		// Titolo basso (Ora solare)
		g.setFont(FONT_AXIS);
		g.setColor(COLOR_BOTTOM_TITLE); // Assegna colore titolo
		printString(0, TITLE_BOTTOM, ORIGIN_X, ORIGIN_Y + HEIGHT_AXIS + 36, WIDTH_AXIS);
	  
		// Font per valori asse y
		g.setFont(FONT_AXIS);

		// Disegna righe asse y e relativi valori
		float dy1 = (yEnd1-yBeg1)/yMarkNum;
		float dy2 = (yEnd2-yBeg2)/yMarkNum;
		for (float ym1 = yBeg1, ym2 = yBeg2; ym1<= yEnd1; ym1 += dy1, ym2 += dy2) {
			// Disegna righe asse y
			if (ym1< yEnd1) {
			   g.setColor(COLOR_GRAPH_ROW);
			   g.setColor(Color.lightGray);		
			   g.drawLine (xPset(X_BEGIN), yPset1(ym1), xPset(X_END), yPset1(ym1));
			}
		 
			// Scrivi valori 1 asse y
			g.setColor(graphColor1);
			printString(1, String.valueOf((int)ym1), xPset(X_BEGIN) - 22, yPset1(ym1) + 4, 18);
		 
			// Scrivi valori 2 asse y
			if (data2graph) {// Valori_2 asse y
			   g.setColor(graphColor2);
			   if (dataTyp2!=WINDDIR_INDEX+1)
				  printString(1, String.valueOf((int)ym2), xPset(X_END) + 10, yPset1(ym1) + 4, 18);
			   else {//Direzioni (N NE E SE S SW W NW)
				  int inx = ((int)ym2/45)%8;	//Indice vettori direzione VENTO (N..W)
				 printString(0, NESW[inx], xPset(X_END) + 6, yPset1(ym1) + 4, 18);
			   }
			}
			y2 += 10; // Incrementa y2 di 10
		 
		} // end for
	  
		// Disegna rettangolo area grafico (N.B. Lasciare qui', le righe x e y lo cancellano)
		g.setColor(COLOR_GRAPH_BOX);
		g.drawRect(xPset(X_BEGIN), yPset1(yEnd1), WIDTH_AXIS, HEIGHT_AXIS);
	  
		//--- Disegna grafico ---//
		float x0, ydata1, ydata2;
		float ylast1, ylast2; //Punto iniziale

		// Inizializzazioni
		x0 = 0;
		ylast1 = data1[0]; ylast2 = data2[0];
	  
		for (int i = 1; i < SAMPLES_OF_DAY; i++, x0 += xStep) {
			g.setColor(graphColor1); // Assegna colore del grafico_1
			ydata1 = (int) (data1[i]); // Valore i+1
			if (ylast1 <= noData1) ylast1 = ydata1; //Controllo dati non assegnati
		 
			if (ydata1>=yBeg1 && ydata1<=yEnd1) {
			   if (ylast1<yBeg1 || ylast1>yEnd1) ylast1 = yEnd1; // Controllo fuori_scala
			   g.drawLine(xPset(X_BEGIN+(i-1)*xStep),yPset1(ylast1), xPset(X_BEGIN+i*xStep), yPset1(ydata1));
			   ylast1=ydata1;
			}
		 
			if (data2graph) {// Disegna grafico 2
			   ydata2 = (int) (data2[i]);
			   g.setColor(graphColor2); // Assegna colore del grafico_2
				if (ylast2 <= noData2) ylast2 = ydata2;	// Controllo dati non assegnati
				if (ydata2>=yBeg2 && ydata2<=yEnd2) {
					if (ylast2<yBeg2 || ylast2>yEnd2) ylast2 = yEnd2;	// Controllo fuori_scala
					g.drawLine(xPset(X_BEGIN+(i-1)*xStep),yPset2(ylast2), xPset(X_BEGIN+i*xStep), yPset2(ydata2));
					ylast2=ydata2;
				}
			}
		} // end for
	}

	/**
	 * 
	 * @param Graphics g
	 */
	void noDateMessage(Graphics g) { // Messaggio 'Dati non disponibili!'
		int vtab;
		setBackground(Color.white);
		g.setFont(FONT_TR24);
		g.setColor(Color.red); // Assegna colore titolo principlale
		vtab = TOP_BORDER + 120;
		printString(0, "In attesa del collegamento dati ...", ORIGIN_X, vtab, WIDTH_AXIS);
	}

	//================== Conversione Coordinate ==================//
	/**
	 * Calcolo posizione X entro Viewport
	 */
	int xPset(float x) {
		float h1 = ORIGIN_X, h2 = ORIGIN_X + WIDTH_AXIS;
		float x1=X_BEGIN,  x2=X_END;
		float m = (h2 - h1) / (x2 - x1);
		return (int) (h1 + m * (x - x1));
	}

	/**
	 * Calcolo posizione y_1 entro Viewport
	 * @param y
	 * @return
	 */
	int yPset1 (float y) {
		float h1 = ORIGIN_Y, h2 = ORIGIN_Y + HEIGHT_AXIS;
		float y1=yBeg1, y2=yEnd1;
		float m = (h2 - h1) / (y2 - y1);
		return (int) (h2 - m * (y - y1));
	}

	/**
	 * 
	 * @param y
	 * @return
	 */
	int yPset2 (float y) { // Calcolo posizione y_2 entro Viewport
		float h1 = ORIGIN_Y, h2 = ORIGIN_Y + HEIGHT_AXIS;
		float y1=yBeg2, y2=yEnd2;
		float m = (h2 - h1) / (y2 - y1);
		return (int) (h2 - m * (y - y1));
	}

    /**
	 * Apre l'ambiente grafico (Image) con valori predefiniti
     */
    public void setWindow() {
		// Set image buffer
		imageBuffer = new BufferedImage(WIDTH_IMAGE, HEIGHT_IMAGE, BufferedImage.TYPE_INT_BGR);
        g = imageBuffer.createGraphics();
    }

	// Save image
	public void saveImage(String filename, String IMAGE_FORMAT) {

		File file = new File(filename + "." + IMAGE_FORMAT);
		try {
			javax.imageio.ImageIO.write(imageBuffer, IMAGE_FORMAT, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Scrittura formattata di una stringa
	 * 	mode = 0:centrata; 1:destra; altro:sinistra
	 * 	aggiorna xPos (posizione x del cursore)
	 */
	public int printString(int mode, String string, int xPos, int yPos, int xWidth){
		int lenPixString = 0;
		if (string==null || string.length()==0) {
			lenPixString = 0;
		} else {
			lenPixString = g.getFontMetrics().stringWidth(string);
			
		}
	  
		if (mode == 0) { // Allineamento al centro
			xPos += (xWidth - lenPixString)/2;
		} else if (mode == 1) { // Allineamento a destra
			xPos += xWidth - lenPixString;
		}
	  
		if (string!=null && string.length()>0)
			g.drawString(string, xPos, yPos);
	  
		return xPos += lenPixString; // Nuova posizione cursore x
	}

} // end class

