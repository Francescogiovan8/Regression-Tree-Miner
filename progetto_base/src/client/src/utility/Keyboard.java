//********************************************************************
//  Keyboard.java       Author: Lewis and Loftus
//
//  Facilitates keyboard input by abstracting details about input
//  parsing, conversions, and exception handling.
//********************************************************************

package utility;

import java.io.*;
import java.util.*;

/**
 * Classe di utilità per la lettura di dati da tastiera.
 *
 * <p>La classe astrae i dettagli relativi alla lettura da standard input,
 * alla suddivisione dell'input in token, alla conversione dei valori e alla
 * gestione degli errori di parsing.</p>
 *
 * <p>Tutti i metodi sono statici, quindi la classe viene usata senza creare
 * oggetti.</p>
 */
public class Keyboard {
	// ************* Error Handling Section **************************

	/**
	 * Flag che indica se gli errori di input devono essere stampati su standard output.
	 */
	private static boolean printErrors = true;

	/**
	 * Numero di errori di input rilevati durante le letture.
	 */
	private static int errorCount = 0;

	// -----------------------------------------------------------------
	// Returns the current error count.
	// -----------------------------------------------------------------

	/**
	 * Restituisce il numero corrente di errori rilevati.
	 *
	 * @return numero di errori di input
	 */
	public static int getErrorCount() {
		return errorCount;
	}

	// -----------------------------------------------------------------
	// Resets the current error count to zero.
	// -----------------------------------------------------------------

	/**
	 * Reimposta a zero il contatore degli errori.
	 *
	 * <p>Il parametro {@code count} è presente nella firma originale del metodo,
	 * ma nell'implementazione corrente non viene utilizzato.</p>
	 *
	 * @param count parametro non utilizzato
	 */
	public static void resetErrorCount(int count) {
		errorCount = 0;
	}

	// -----------------------------------------------------------------
	// Returns a boolean indicating whether input errors are
	// currently printed to standard output.
	// -----------------------------------------------------------------

	/**
	 * Indica se gli errori di input vengono stampati su standard output.
	 *
	 * @return {@code true} se gli errori vengono stampati, {@code false} altrimenti
	 */
	public static boolean getPrintErrors() {
		return printErrors;
	}

	// -----------------------------------------------------------------
	// Sets a boolean indicating whether input errors are to be
	// printed to standard output.
	// -----------------------------------------------------------------

	/**
	 * Imposta la stampa degli errori di input su standard output.
	 *
	 * @param flag {@code true} per stampare gli errori, {@code false} per non stamparli
	 */
	public static void setPrintErrors(boolean flag) {
		printErrors = flag;
	}

	// -----------------------------------------------------------------
	// Increments the error count and prints the error message if
	// appropriate.
	// -----------------------------------------------------------------

	/**
	 * Registra un errore di input e, se previsto, stampa il messaggio associato.
	 *
	 * @param str messaggio di errore da stampare
	 */
	private static void error(String str) {
		errorCount++;
		if (printErrors)
			System.out.println(str);
	}

	// ************* Tokenized Input Stream Section ******************

	/**
	 * Token corrente già letto ma non ancora consumato.
	 */
	private static String current_token = null;

	/**
	 * Tokenizer usato per suddividere le righe lette da standard input.
	 */
	private static StringTokenizer reader;

	/**
	 * Lettore bufferizzato collegato allo standard input.
	 */
	private static BufferedReader in = new BufferedReader(
			new InputStreamReader(System.in));

	// -----------------------------------------------------------------
	// Gets the next input token assuming it may be on subsequent
	// input lines.
	// -----------------------------------------------------------------

	/**
	 * Restituisce il prossimo token disponibile, cercandolo anche nelle righe successive.
	 *
	 * @return prossimo token letto da standard input
	 */
	private static String getNextToken() {
		return getNextToken(true);
	}

	// -----------------------------------------------------------------
	// Gets the next input token, which may already have been read.
	// -----------------------------------------------------------------

	/**
	 * Restituisce il prossimo token disponibile.
	 *
	 * <p>Se esiste un token già letto e salvato in {@link #current_token}, viene
	 * restituito quello. Altrimenti viene letto un nuovo token dallo standard input.</p>
	 *
	 * @param skip indica se saltare i delimitatori e passare alle righe successive
	 * @return prossimo token disponibile
	 */
	private static String getNextToken(boolean skip) {
		String token;

		if (current_token == null)
			token = getNextInputToken(skip);
		else {
			token = current_token;
			current_token = null;
		}

		return token;
	}

	// -----------------------------------------------------------------
	// Gets the next token from the input, which may come from the
	// current input line or a subsequent one. The parameter
	// determines if subsequent lines are used.
	// -----------------------------------------------------------------

	/**
	 * Legge il prossimo token dallo standard input.
	 *
	 * <p>Il token può appartenere alla riga corrente oppure a una riga successiva,
	 * in base al valore del parametro {@code skip}.</p>
	 *
	 * @param skip indica se saltare i delimitatori e continuare la lettura
	 * @return prossimo token letto oppure {@code null} in caso di errore
	 */
	private static String getNextInputToken(boolean skip) {
		final String delimiters = " \t\n\r\f";
		String token = null;

		try {
			if (reader == null)
				reader = new StringTokenizer(in.readLine(), delimiters, true);

			while (token == null || ((delimiters.indexOf(token) >= 0) && skip)) {
				while (!reader.hasMoreTokens())
					reader = new StringTokenizer(in.readLine(), delimiters,
							true);

				token = reader.nextToken();
			}
		} catch (Exception exception) {
			token = null;
		}

		return token;
	}

	// -----------------------------------------------------------------
	// Returns true if there are no more tokens to read on the
	// current input line.
	// -----------------------------------------------------------------

	/**
	 * Indica se non ci sono altri token disponibili nella riga corrente.
	 *
	 * @return {@code true} se la riga corrente non contiene altri token, {@code false} altrimenti
	 */
	public static boolean endOfLine() {
		return !reader.hasMoreTokens();
	}

	// ************* Reading Section *********************************

	// -----------------------------------------------------------------
	// Returns a string read from standard input.
	// -----------------------------------------------------------------

	/**
	 * Legge una stringa completa da standard input.
	 *
	 * <p>Il metodo legge il token corrente e concatena gli eventuali token rimanenti
	 * nella stessa riga.</p>
	 *
	 * @return stringa letta da standard input oppure {@code null} in caso di errore
	 */
	public static String readString() {
		String str;

		try {
			str = getNextToken(false);
			while (!endOfLine()) {
				str = str + getNextToken(false);
			}
		} catch (Exception exception) {
			error("Error reading String data, null value returned.");
			str = null;
		}
		return str;
	}

	// -----------------------------------------------------------------
	// Returns a space-delimited substring (a word) read from
	// standard input.
	// -----------------------------------------------------------------

	/**
	 * Legge una parola da standard input.
	 *
	 * <p>La parola viene individuata come token delimitato da spazi o altri
	 * delimitatori standard.</p>
	 *
	 * @return parola letta da standard input oppure {@code null} in caso di errore
	 */
	public static String readWord() {
		String token;
		try {
			token = getNextToken();
		} catch (Exception exception) {
			error("Error reading String data, null value returned.");
			token = null;
		}
		return token;
	}

	// -----------------------------------------------------------------
	// Returns a boolean read from standard input.
	// -----------------------------------------------------------------

	/**
	 * Legge un valore booleano da standard input.
	 *
	 * <p>Sono riconosciute le stringhe {@code true} e {@code false}, senza
	 * distinzione tra maiuscole e minuscole.</p>
	 *
	 * @return valore booleano letto oppure {@code false} in caso di errore
	 */
	public static boolean readBoolean() {
		String token = getNextToken();
		boolean bool;
		try {
			if (token.toLowerCase().equals("true")) {
				bool = true;
			} else if (token.toLowerCase().equals("false")) {
				bool = false;
			} else {
				error("Error reading boolean data, false value returned.");
				bool = false;
			}
		} catch (Exception exception) {
			error("Error reading boolean data, false value returned.");
			bool = false;
		}
		return bool;
	}

	// -----------------------------------------------------------------
	// Returns a character read from standard input.
	// -----------------------------------------------------------------

	/**
	 * Legge un carattere da standard input.
	 *
	 * <p>Se il token letto contiene più caratteri, il primo viene restituito e i
	 * caratteri rimanenti vengono conservati per la lettura successiva.</p>
	 *
	 * @return carattere letto oppure {@link Character#MIN_VALUE} in caso di errore
	 */
	public static char readChar() {
		String token = getNextToken(false);
		char value;
		try {
			if (token.length() > 1) {
				current_token = token.substring(1, token.length());
			} else {
				current_token = null;
			}
			value = token.charAt(0);
		} catch (Exception exception) {
			error("Error reading char data, MIN_VALUE value returned.");
			value = Character.MIN_VALUE;
		}

		return value;
	}

	// -----------------------------------------------------------------
	// Returns an integer read from standard input.
	// -----------------------------------------------------------------

	/**
	 * Legge un numero intero da standard input.
	 *
	 * @return intero letto oppure {@link Integer#MIN_VALUE} in caso di errore
	 */
	public static int readInt() {
		String token = getNextToken();
		int value;
		try {
			value = Integer.parseInt(token);
		} catch (Exception exception) {
			error("Error reading int data, MIN_VALUE value returned.");
			value = Integer.MIN_VALUE;
		}
		return value;
	}

	// -----------------------------------------------------------------
	// Returns a long integer read from standard input.
	// -----------------------------------------------------------------

	/**
	 * Legge un numero intero lungo da standard input.
	 *
	 * @return valore long letto oppure {@link Long#MIN_VALUE} in caso di errore
	 */
	public static long readLong() {
		String token = getNextToken();
		long value;
		try {
			value = Long.parseLong(token);
		} catch (Exception exception) {
			error("Error reading long data, MIN_VALUE value returned.");
			value = Long.MIN_VALUE;
		}
		return value;
	}

	// -----------------------------------------------------------------
	// Returns a float read from standard input.
	// -----------------------------------------------------------------

	/**
	 * Legge un numero reale a precisione singola da standard input.
	 *
	 * @return valore float letto oppure {@link Float#NaN} in caso di errore
	 */
	public static float readFloat() {
		String token = getNextToken();
		float value;
		try {
			value = Float.parseFloat(token);
		} catch (Exception exception) {
			error("Error reading float data, NaN value returned.");
			value = Float.NaN;
		}
		return value;
	}

	// -----------------------------------------------------------------
	// Returns a double read from standard input.
	// -----------------------------------------------------------------

	/**
	 * Legge un numero reale a precisione doppia da standard input.
	 *
	 * @return valore double letto oppure {@link Double#NaN} in caso di errore
	 */
	public static double readDouble() {
		String token = getNextToken();
		double value;
		try {
			value = Double.parseDouble(token);
		} catch (Exception exception) {
			error("Error reading double data, NaN value returned.");
			value = Double.NaN;
		}
		return value;
	}
}