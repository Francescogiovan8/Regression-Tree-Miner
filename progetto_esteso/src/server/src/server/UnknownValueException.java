package server;

/**
 * Eccezione lanciata quando durante la predizione viene fornito un valore non riconosciuto.
 *
 * <p>Nel progetto Regression Tree Miner questa eccezione viene usata quando il valore
 * ricevuto durante la predizione guidata non permette di selezionare correttamente
 * uno dei rami dell'albero di regressione.</p>
 */
public class UnknownValueException extends Exception {

	/**
	 * Crea una nuova eccezione senza messaggio descrittivo.
	 */
	public UnknownValueException() {}

	/**
	 * Crea una nuova eccezione con un messaggio descrittivo.
	 *
	 * @param message messaggio che descrive l'errore verificatosi
	 */
	public UnknownValueException(String message) {
		super(message);
	}

}