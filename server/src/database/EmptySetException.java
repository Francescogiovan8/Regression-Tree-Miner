package database;

/**
 * Eccezione lanciata quando una query al database restituisce un insieme vuoto.
 *
 * <p>Nel progetto Regression Tree Miner questa eccezione viene usata quando non
 * vengono trovati esempi nella tabella richiesta o quando non sono disponibili
 * valori da elaborare.</p>
 */
public class EmptySetException extends Exception {

	/**
	 * Crea una nuova eccezione senza messaggio descrittivo.
	 */
	public EmptySetException() {
	}

    /**
     * Crea una nuova eccezione con un messaggio descrittivo.
     *
     * @param message messaggio che descrive l'errore verificatosi
     */
    public EmptySetException(String message){
        super(message);
    }
}