package database;

/**
 * Eccezione lanciata quando si verifica un errore nella connessione al database.
 *
 * <p>Nel progetto Regression Tree Miner questa eccezione viene usata dalla classe
 * {@link DbAccess} per segnalare problemi durante l'apertura della connessione
 * JDBC al database MySQL.</p>
 */
public class DatabaseConnectionException extends Exception {

    /**
     * Crea una nuova eccezione senza messaggio descrittivo.
     */
    public DatabaseConnectionException() {
	}

	/**
	 * Crea una nuova eccezione con un messaggio descrittivo.
	 *
	 * @param message messaggio che descrive l'errore di connessione
	 */
	public DatabaseConnectionException(String message) {
		super(message);
	}
}