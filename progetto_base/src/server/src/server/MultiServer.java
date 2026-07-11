package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server multiclient del progetto Regression Tree Miner.
 *
 * <p>La classe apre una {@link ServerSocket} sulla porta specificata e resta
 * in attesa di connessioni da parte dei client. Per ogni connessione accettata,
 * crea un nuovo oggetto {@link ServerOneClient}, incaricato di gestire la
 * comunicazione con il singolo client.</p>
 *
 * <p>Il server può ricevere connessioni sia dal client console sia
 * dall'estensione Telegram Bot.</p>
 */
public class MultiServer {

	/**
	 * Porta su cui il server resta in ascolto.
	 */
	private int PORT = 8080;

	/**
	 * Crea e avvia un server multiclient sulla porta specificata.
	 *
	 * <p>Il costruttore imposta la porta del server e avvia immediatamente
	 * il ciclo di ascolto delle connessioni.</p>
	 *
	 * @param port porta su cui avviare il server
	 */
	public MultiServer(int port) {
		PORT = port;
		run();
	}

	/**
	 * Avvia il ciclo principale del server.
	 *
	 * <p>Il metodo crea una {@link ServerSocket}, attende nuove connessioni
	 * tramite {@code accept()} e, per ogni client connesso, delega la gestione
	 * della comunicazione a un nuovo {@link ServerOneClient}.</p>
	 *
	 * <p>In caso di errore nella creazione del gestore del client, la socket
	 * accettata viene chiusa.</p>
	 */
	private void run() {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("Server avviato sulla porta " + PORT);

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("Connessione accettata: " + socket);

				try {
					new ServerOneClient(socket);
				} catch (IOException e) {
					System.out.println("Errore nella gestione del client: " + e.getMessage());
					socket.close();
				}
			}
		} catch (IOException e) {
			System.out.println("Errore del server: " + e.getMessage());
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					System.out.println("Errore nella chiusura del server: " + e.getMessage());
				}
			}
		}
	}
}