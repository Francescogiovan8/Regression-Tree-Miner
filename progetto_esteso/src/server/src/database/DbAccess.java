package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestisce l'accesso al database MySQL usato dal progetto Regression Tree Miner.
 *
 * <p>La classe incapsula i parametri di connessione al database e fornisce i
 * metodi per inizializzare, ottenere e chiudere una connessione JDBC.</p>
 *
 * <p>Il database previsto dal progetto è {@code MapDB}, accessibile localmente
 * tramite l'utente configurato nella classe.</p>
 */
public class DbAccess {

	/**
	 * Nome della classe del driver JDBC MySQL.
	 */
	private final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

	/**
	 * Protocollo JDBC usato per la connessione al DBMS.
	 */
	private final String DBMS = "jdbc:mysql";

	/**
	 * Indirizzo del server MySQL.
	 */
	private String SERVER = "localhost";

	/**
	 * Nome del database utilizzato dal progetto.
	 */
	private String DATABASE = "MapDB";

	/**
	 * Porta su cui è in ascolto il server MySQL.
	 */
	private final int PORT = 3306;

	/**
	 * Nome utente usato per accedere al database.
	 */
	private String USER_ID = "MapUser";

	/**
	 * Password usata per accedere al database.
	 */
	private String PASSWORD = "map";

	/**
	 * Connessione JDBC al database.
	 */
	private Connection conn;

	/**
	 * Inizializza la connessione al database.
	 *
	 * <p>Il metodo carica il driver JDBC MySQL, costruisce la stringa di
	 * connessione e apre una connessione verso il database configurato.</p>
	 *
	 * @throws DatabaseConnectionException se il driver JDBC non viene trovato
	 * oppure se si verifica un errore durante l'apertura della connessione
	 */
	public void initConnection() throws DatabaseConnectionException {

		try {
			Class.forName(DRIVER_CLASS_NAME);
		} catch (ClassNotFoundException e) {
			System.out.println("[!] Driver not found: " + e.getMessage());
			throw new DatabaseConnectionException(e.toString());
		}

		String connectionString = DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE + "?serverTimezone=UTC";

		try {
			conn = DriverManager.getConnection(connectionString, USER_ID, PASSWORD);
		} catch (SQLException e) {
			System.out.println("[!] SQLException: " + e.getMessage());
			System.out.println("[!] SQLState: " + e.getSQLState());
			System.out.println("[!] VendorError: " + e.getErrorCode());
			throw new DatabaseConnectionException(e.toString());
		}
	}

	/**
	 * Restituisce la connessione JDBC corrente.
	 *
	 * @return connessione al database
	 */
	public Connection getConnection() {
		return conn;
	}

	/**
	 * Chiude la connessione al database, se aperta.
	 *
	 * <p>Eventuali errori durante la chiusura vengono stampati su standard output
	 * senza propagare eccezioni al chiamante.</p>
	 */
	public void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.out.println("[!] Error closing connection: " + e.getMessage());
			}
		}
	}
}