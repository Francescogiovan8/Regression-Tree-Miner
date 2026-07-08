package bot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce una singola sessione di comunicazione tra il Bot Telegram e il server
 * del progetto Regression Tree Miner.
 *
 * <p>La classe incapsula la socket, gli stream di oggetti e lo stato della
 * conversazione dell'utente Telegram. Attraverso questa classe il bot può
 * richiedere al server il caricamento di un training set, l'apprendimento di un
 * albero di regressione, il caricamento di un albero già serializzato e
 * l'esecuzione di una predizione guidata.</p>
 *
 * <p>La classe ha visibilità di package perché viene utilizzata solo dalle
 * classi del package {@code bot}.</p>
 */
class ClientSession {

	/**
	 * Stato della conversazione in cui il bot mostra il menu principale.
	 */
	static final String MENU = "MENU";

	/**
	 * Stato della conversazione in cui il bot attende il nome della tabella del database.
	 */
	static final String WAITING_TABLE_NAME = "WAITING_TABLE_NAME";

	/**
	 * Stato della conversazione in cui il bot attende il nome dell'archivio contenente
	 * un albero di regressione serializzato.
	 */
	static final String WAITING_ARCHIVE_NAME = "WAITING_ARCHIVE_NAME";

	/**
	 * Stato della conversazione in cui il bot attende una scelta numerica durante la
	 * predizione guidata.
	 */
	static final String WAITING_PREDICTION_CHOICE = "WAITING_PREDICTION_CHOICE";

	/**
	 * Stato della conversazione in cui il bot chiede all'utente se vuole effettuare
	 * un'altra predizione con lo stesso albero.
	 */
	static final String WAITING_REPEAT_PREDICTION = "WAITING_REPEAT_PREDICTION";

	/**
	 * Socket usata per comunicare con il server.
	 */
	private Socket socket;

	/**
	 * Stream di output usato per inviare oggetti e comandi al server.
	 */
	private ObjectOutputStream out;

	/**
	 * Stream di input usato per ricevere oggetti e risposte dal server.
	 */
	private ObjectInputStream in;

	/**
	 * Stato corrente della conversazione associata alla sessione.
	 */
	private String state;

	/**
	 * Ultima domanda ricevuta dal server durante la predizione guidata.
	 */
	private String lastQuestion;

	/**
	 * Lista delle scelte numeriche già inviate al server durante la predizione corrente.
	 *
	 * <p>La lista viene usata per ricostruire il percorso già effettuato nel caso in cui
	 * l'utente inserisca un valore non valido e il server restituisca un errore.</p>
	 */
	private List<Integer> predictionChoices = new ArrayList<>();

	/**
	 * Crea una nuova sessione aprendo una connessione socket verso il server.
	 *
	 * @param serverAddress indirizzo del server a cui connettersi
	 * @param serverPort porta del server a cui connettersi
	 * @throws IOException se si verifica un errore durante l'apertura della socket o degli stream
	 */
	ClientSession(String serverAddress, int serverPort) throws IOException {
		socket = new Socket(serverAddress, serverPort);
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(socket.getInputStream());
		state = MENU;
	}

	/**
	 * Restituisce lo stato corrente della conversazione.
	 *
	 * @return stato corrente della sessione
	 */
	String getState() {
		return state;
	}

	/**
	 * Imposta lo stato corrente della conversazione.
	 *
	 * @param state nuovo stato della sessione
	 */
	void setState(String state) {
		this.state = state;
	}

	/**
	 * Restituisce l'ultima domanda ricevuta dal server durante la predizione guidata.
	 *
	 * @return ultima domanda ricevuta dal server
	 */
	String getLastQuestion() {
		return lastQuestion;
	}

	/**
	 * Richiede al server il caricamento del training set da una tabella del database.
	 *
	 * <p>Il metodo invia al server il codice operazione {@code 0}, seguito dal nome
	 * della tabella.</p>
	 *
	 * @param tableName nome della tabella del database da caricare
	 * @return risposta testuale restituita dal server
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se l'oggetto ricevuto dallo stream non può essere deserializzato
	 */
	String loadTrainingSet(String tableName) throws IOException, ClassNotFoundException {
		out.writeObject(0);
		out.writeObject(tableName);
		out.flush();
		return in.readObject().toString();
	}

	/**
	 * Richiede al server l'apprendimento di un nuovo albero di regressione.
	 *
	 * <p>Il metodo invia al server il codice operazione {@code 1}. Il server usa il
	 * training set precedentemente caricato per costruire l'albero.</p>
	 *
	 * @return risposta testuale restituita dal server
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se l'oggetto ricevuto dallo stream non può essere deserializzato
	 */
	String learnTree() throws IOException, ClassNotFoundException {
		out.writeObject(1);
		out.flush();
		return in.readObject().toString();
	}

	/**
	 * Richiede al server il caricamento di un albero di regressione da archivio.
	 *
	 * <p>Il metodo invia al server il codice operazione {@code 2}, seguito dal nome
	 * dell'archivio da cui caricare l'albero serializzato.</p>
	 *
	 * @param archiveName nome dell'archivio contenente l'albero serializzato
	 * @return risposta testuale restituita dal server
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se l'oggetto ricevuto dallo stream non può essere deserializzato
	 */
	String loadTree(String archiveName) throws IOException, ClassNotFoundException {
		out.writeObject(2);
		out.writeObject(archiveName);
		out.flush();
		return in.readObject().toString();
	}

	/**
	 * Avvia una nuova predizione guidata.
	 *
	 * <p>Il metodo azzera le scelte della predizione precedente, invia al server il
	 * codice operazione {@code 3} e legge la prima risposta prodotta dal server.</p>
	 *
	 * @return risposta del server relativa alla predizione
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se l'oggetto ricevuto dallo stream non può essere deserializzato
	 */
	ServerResponse startPrediction() throws IOException, ClassNotFoundException {
		predictionChoices.clear();
		out.writeObject(3);
		out.flush();
		return readPredictionResponse();
	}

	/**
	 * Invia al server una scelta numerica durante la predizione guidata.
	 *
	 * <p>Se il server restituisce un errore dovuto a una scelta non valida, il metodo
	 * tenta di recuperare la predizione ricostruendo il percorso già effettuato con le
	 * scelte precedenti.</p>
	 *
	 * @param choice scelta numerica inserita dall'utente
	 * @return risposta del server dopo l'invio della scelta
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se l'oggetto ricevuto dallo stream non può essere deserializzato
	 */
	ServerResponse sendPredictionChoice(int choice) throws IOException, ClassNotFoundException {
		out.writeObject(choice);
		out.flush();

		ServerResponse response = readPredictionResponse();

		if(response.getType().equals(ServerResponse.ERROR) && lastQuestion != null)
			return recoverPrediction();

		if(!response.getType().equals(ServerResponse.ERROR))
			predictionChoices.add(choice);

		return response;
	}

	/**
	 * Recupera la predizione guidata dopo l'inserimento di una scelta non valida.
	 *
	 * <p>Il metodo riavvia la predizione sul server e reinvia tutte le scelte valide
	 * già effettuate dall'utente, in modo da tornare all'ultima domanda corretta.</p>
	 *
	 * @return risposta del server dopo il recupero del percorso di predizione
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se l'oggetto ricevuto dallo stream non può essere deserializzato
	 */
	private ServerResponse recoverPrediction() throws IOException, ClassNotFoundException {
		out.writeObject(3);
		out.flush();

		ServerResponse response = readPredictionResponse();

		for(Integer choice : predictionChoices) {
			if(!response.getType().equals(ServerResponse.QUERY))
				return response;

			out.writeObject(choice);
			out.flush();
			response = readPredictionResponse();
		}

		return response;
	}

	/**
	 * Legge e interpreta una risposta del server durante la predizione guidata.
	 *
	 * <p>Il server può restituire una richiesta di scelta, una predizione finale
	 * oppure un messaggio di errore.</p>
	 *
	 * @return risposta del server convertita in un oggetto {@link ServerResponse}
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se l'oggetto ricevuto dallo stream non può essere deserializzato
	 */
	private ServerResponse readPredictionResponse() throws IOException, ClassNotFoundException {
		String answer = in.readObject().toString();

		if(answer.equals("QUERY")) {
			String question = in.readObject().toString();
			lastQuestion = question;
			return ServerResponse.query(question);
		}

		if(answer.equals("OK")) {
			String predictedClass = in.readObject().toString();
			return ServerResponse.prediction(predictedClass);
		}

		return ServerResponse.error(answer);
	}

	/**
	 * Chiude la connessione socket associata alla sessione.
	 *
	 * <p>Eventuali eccezioni in fase di chiusura vengono ignorate perché la sessione
	 * deve essere terminata senza interrompere l'esecuzione del bot.</p>
	 */
	void close() {
		try {
			if(socket != null)
				socket.close();
		} catch(IOException ignored) {
		}
	}

	/**
	 * Rappresenta una risposta del server durante la predizione guidata.
	 *
	 * <p>La risposta può indicare una nuova domanda da mostrare all'utente, una
	 * predizione finale oppure un errore.</p>
	 */
	static class ServerResponse {
		/**
		 * Tipo di risposta che indica una domanda da mostrare all'utente.
		 */
		static final String QUERY = "QUERY";

		/**
		 * Tipo di risposta che indica una predizione finale.
		 */
		static final String PREDICTION = "PREDICTION";

		/**
		 * Tipo di risposta che indica un errore restituito dal server.
		 */
		static final String ERROR = "ERROR";

		/**
		 * Tipo della risposta.
		 */
		private String type;

		/**
		 * Messaggio associato alla risposta.
		 */
		private String message;

		/**
		 * Crea una nuova risposta del server.
		 *
		 * @param type tipo della risposta
		 * @param message messaggio associato alla risposta
		 */
		private ServerResponse(String type, String message) {
			this.type = type;
			this.message = message;
		}

		/**
		 * Crea una risposta contenente una domanda da mostrare all'utente.
		 *
		 * @param question domanda ricevuta dal server
		 * @return risposta di tipo {@link #QUERY}
		 */
		static ServerResponse query(String question) {
			return new ServerResponse(QUERY, question);
		}

		/**
		 * Crea una risposta contenente la classe predetta.
		 *
		 * @param predictedClass valore numerico predetto dal modello
		 * @return risposta di tipo {@link #PREDICTION}
		 */
		static ServerResponse prediction(String predictedClass) {
			return new ServerResponse(PREDICTION, predictedClass);
		}

		/**
		 * Crea una risposta contenente un messaggio di errore.
		 *
		 * @param errorMessage messaggio di errore restituito dal server
		 * @return risposta di tipo {@link #ERROR}
		 */
		static ServerResponse error(String errorMessage) {
			return new ServerResponse(ERROR, errorMessage);
		}

		/**
		 * Restituisce il tipo della risposta.
		 *
		 * @return tipo della risposta
		 */
		String getType() {
			return type;
		}

		/**
		 * Restituisce il messaggio associato alla risposta.
		 *
		 * @return messaggio della risposta
		 */
		String getMessage() {
			return message;
		}
	}
}