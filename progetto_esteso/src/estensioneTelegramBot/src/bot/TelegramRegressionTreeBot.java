package bot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Implementa il Bot Telegram dell'estensione del progetto Regression Tree Miner.
 *
 * <p>La classe riceve i messaggi inviati dagli utenti Telegram tramite long polling,
 * interpreta i comandi e inoltra le richieste al server del progetto attraverso una
 * {@link ClientSession}. Il bot non accede direttamente al database e non costruisce
 * direttamente l'albero di regressione: delega tali operazioni al server, mantenendo
 * l'architettura client/server del progetto.</p>
 *
 * <p>Per ogni chat Telegram viene mantenuta una sessione separata, in modo da permettere
 * a più utenti di interagire indipendentemente con il sistema.</p>
 */
class TelegramRegressionTreeBot implements LongPollingSingleThreadUpdateConsumer {
	/**
	 * Client Telegram usato per inviare messaggi agli utenti.
	 */
	private final TelegramClient telegramClient;

	/**
	 * Indirizzo del server Regression Tree Miner a cui collegare le sessioni.
	 */
	private final String serverAddress;

	/**
	 * Porta del server Regression Tree Miner a cui collegare le sessioni.
	 */
	private final int serverPort;

	/**
	 * Mappa delle sessioni attive, indicizzate tramite identificativo della chat Telegram.
	 */
	private final Map<Long, ClientSession> sessions = new HashMap<>();

	/**
	 * Crea un nuovo Bot Telegram collegato al server specificato.
	 *
	 * @param botToken token Telegram del bot
	 * @param serverAddress indirizzo del server Regression Tree Miner
	 * @param serverPort porta del server Regression Tree Miner
	 */
	TelegramRegressionTreeBot(String botToken, String serverAddress, int serverPort) {
		this.telegramClient = new OkHttpTelegramClient(botToken);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
	}

	/**
	 * Elabora un aggiornamento ricevuto da Telegram.
	 *
	 * <p>Il metodo considera solo i messaggi testuali. Ogni messaggio valido viene
	 * inoltrato al metodo di gestione della conversazione. In caso di errore, la
	 * sessione associata alla chat viene chiusa e l'utente riceve un messaggio di
	 * errore.</p>
	 *
	 * @param update aggiornamento ricevuto da Telegram
	 */
	@Override
	public void consume(Update update) {
		if(!update.hasMessage() || !update.getMessage().hasText())
			return;

		long chatId = update.getMessage().getChatId();
		String text = update.getMessage().getText().trim();

		try {
			handleMessage(chatId, text);
		} catch(Exception e) {
			e.printStackTrace();
			closeSession(chatId);
			sendMessage(chatId, "⚠️ Si è verificato un errore durante la comunicazione con il server.");
			sendMessage(chatId, getSessionClosedMessage());
		}
	}

	/**
	 * Gestisce un messaggio testuale ricevuto da una chat Telegram.
	 *
	 * <p>Il metodo riconosce prima i comandi globali del bot. Se il messaggio non è
	 * un comando globale, viene recuperata la sessione associata alla chat e il testo
	 * viene interpretato in base allo stato corrente della sessione.</p>
	 *
	 * @param chatId identificativo della chat Telegram
	 * @param text testo del messaggio ricevuto
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se un oggetto ricevuto dal server non può essere deserializzato
	 */
	private void handleMessage(long chatId, String text) throws IOException, ClassNotFoundException {
		if(text.equals("/start")) {
			startNewSession(chatId);
			return;
		}

		if(text.equals("/end")) {
			endSession(chatId);
			return;
		}

		if(text.equals("/help")) {
			sendMessage(chatId, getHelpMessage());
			return;
		}

		if(text.equals("/info")) {
			sendMessage(chatId, getInfoMessage());
			return;
		}

		ClientSession session = sessions.get(chatId);

		if(session == null) {
			sendMessage(chatId, getNoSessionMessage());
			return;
		}

		switch(session.getState()) {
			case ClientSession.MENU:
				handleMenuChoice(chatId, session, text);
				break;

			case ClientSession.WAITING_TABLE_NAME:
				handleTableName(chatId, session, text);
				break;

			case ClientSession.WAITING_ARCHIVE_NAME:
				handleArchiveName(chatId, session, text);
				break;

			case ClientSession.WAITING_PREDICTION_CHOICE:
				handlePredictionChoice(chatId, session, text);
				break;

			case ClientSession.WAITING_REPEAT_PREDICTION:
				handleRepeatPredictionChoice(chatId, session, text);
				break;

			default:
				closeSession(chatId);
				sendMessage(chatId, "⚠️ Stato della sessione non valido.");
				sendMessage(chatId, getSessionClosedMessage());
				break;
		}
	}

	/**
	 * Avvia una nuova sessione per la chat indicata.
	 *
	 * <p>Se esiste già una sessione associata alla chat, questa viene chiusa prima
	 * di crearne una nuova. La nuova sessione apre una connessione socket verso il
	 * server Regression Tree Miner.</p>
	 *
	 * @param chatId identificativo della chat Telegram
	 */
	private void startNewSession(long chatId) {
		closeSession(chatId);

		try {
			ClientSession session = new ClientSession(serverAddress, serverPort);
			sessions.put(chatId, session);
			sendMessage(chatId, getWelcomeMessage() + "\n\n" + getMenuMessage());
		} catch(IOException e) {
			sendMessage(chatId, "⚠️ Impossibile connettersi al server Regression Tree Miner.\n\nControlla che il server sia avviato e poi digita /start.");
		}
	}

	/**
	 * Termina la sessione associata alla chat indicata.
	 *
	 * @param chatId identificativo della chat Telegram
	 */
	private void endSession(long chatId) {
		ClientSession session = sessions.get(chatId);

		if(session == null) {
			sendMessage(chatId, getNoSessionMessage());
			return;
		}

		closeSession(chatId);
		sendMessage(chatId, getSessionClosedMessage());
	}

	/**
	 * Gestisce la scelta effettuata dall'utente nel menu principale.
	 *
	 * <p>La scelta {@code 1} avvia il caricamento di un training set da database.
	 * La scelta {@code 2} avvia il caricamento di un albero già serializzato.</p>
	 *
	 * @param chatId identificativo della chat Telegram
	 * @param session sessione corrente dell'utente
	 * @param text testo inserito dall'utente
	 */
	private void handleMenuChoice(long chatId, ClientSession session, String text) {
		if(text.equals("1")) {
			session.setState(ClientSession.WAITING_TABLE_NAME);
			sendMessage(chatId, "📋 Inserisci il nome della tabella del database da caricare.\n\nEsempi: prova, provaC, servo");
			return;
		}

		if(text.equals("2")) {
			session.setState(ClientSession.WAITING_ARCHIVE_NAME);
			sendMessage(chatId, "📂 Inserisci il nome dell'archivio senza estensione .dmp.\n\nEsempio: prova, provaC, servo");
			return;
		}

		sendMessage(chatId, "⚠️ Scelta non valida.\n\n" + getMenuMessage());
	}

	/**
	 * Gestisce il nome della tabella inserito dall'utente.
	 *
	 * <p>Il metodo richiede al server il caricamento del training set e, in caso di
	 * successo, avvia l'apprendimento dell'albero di regressione. Dopo l'apprendimento
	 * viene avviata automaticamente la predizione guidata.</p>
	 *
	 * @param chatId identificativo della chat Telegram
	 * @param session sessione corrente dell'utente
	 * @param tableName nome della tabella del database
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se un oggetto ricevuto dal server non può essere deserializzato
	 */
	private void handleTableName(long chatId, ClientSession session, String tableName) throws IOException, ClassNotFoundException {
		String loadingResponse = session.loadTrainingSet(tableName);

		if(!isOk(loadingResponse)) {
			sendMessage(chatId, "⚠️ Errore: " + loadingResponse + "\n\nInserisci di nuovo il nome della tabella del database:");
			return;
		}

		sendMessage(chatId, "✅ [1/3] Training set caricato dal database.");
		sendMessage(chatId, "✅ [2/3] Apprendimento dell'albero di regressione.");

		String learningResponse = session.learnTree();

		if(!isOk(learningResponse)) {
			closeSession(chatId);
			sendMessage(chatId, "⚠️ Errore durante l'apprendimento: " + learningResponse);
			sendMessage(chatId, getSessionClosedMessage());
			return;
		}

		sendMessage(chatId, "✅ [3/3] Albero appreso e salvato.\n");
		sendMessage(chatId, "🔎 Avvio della predizione guidata.");
		session.setState(ClientSession.WAITING_PREDICTION_CHOICE);
		handleServerResponse(chatId, session, session.startPrediction());
	}

	/**
	 * Gestisce il nome dell'archivio inserito dall'utente.
	 *
	 * <p>Il metodo richiede al server il caricamento di un albero serializzato da
	 * file {@code .dmp}. In caso di successo viene avviata automaticamente la
	 * predizione guidata.</p>
	 *
	 * @param chatId identificativo della chat Telegram
	 * @param session sessione corrente dell'utente
	 * @param archiveName nome dell'archivio senza estensione {@code .dmp}
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se un oggetto ricevuto dal server non può essere deserializzato
	 */
	private void handleArchiveName(long chatId, ClientSession session, String archiveName) throws IOException, ClassNotFoundException {
		String loadingResponse = session.loadTree(archiveName);

		if(!isOk(loadingResponse)) {
			sendMessage(chatId, "⚠️ Errore: " + loadingResponse + "\n\nInserisci di nuovo il nome dell'archivio senza estensione .dmp:");
			return;
		}

		sendMessage(chatId, "✅ Albero di regressione caricato dall'archivio.");
		sendMessage(chatId, "🔎 Avvio della predizione guidata.");
		session.setState(ClientSession.WAITING_PREDICTION_CHOICE);
		handleServerResponse(chatId, session, session.startPrediction());
	}

	/**
	 * Gestisce una scelta numerica inserita durante la predizione guidata.
	 *
	 * <p>Il testo viene convertito in un intero e inviato al server. Se il testo non
	 * rappresenta un numero intero, il bot ripropone l'ultima domanda ricevuta dal server.</p>
	 *
	 * @param chatId identificativo della chat Telegram
	 * @param session sessione corrente dell'utente
	 * @param text testo inserito dall'utente
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se un oggetto ricevuto dal server non può essere deserializzato
	 */
	private void handlePredictionChoice(long chatId, ClientSession session, String text) throws IOException, ClassNotFoundException {
		int choice;

		try {
			choice = Integer.parseInt(text);
		} catch(NumberFormatException e) {
			sendMessage(chatId, "⚠️ La risposta deve essere un numero intero.\n\n" + formatQuestion(session.getLastQuestion()));
			return;
		}

		handleServerResponse(chatId, session, session.sendPredictionChoice(choice));
	}

	/**
	 * Gestisce la risposta dell'utente alla richiesta di effettuare un'altra predizione.
	 *
	 * <p>Le risposte affermative avviano una nuova predizione guidata con lo stesso
	 * albero di regressione. Le risposte negative chiudono la sessione.</p>
	 *
	 * @param chatId identificativo della chat Telegram
	 * @param session sessione corrente dell'utente
	 * @param text testo inserito dall'utente
	 * @throws IOException se si verifica un errore di comunicazione con il server
	 * @throws ClassNotFoundException se un oggetto ricevuto dal server non può essere deserializzato
	 */
	private void handleRepeatPredictionChoice(long chatId, ClientSession session, String text) throws IOException, ClassNotFoundException {
		if(text.equalsIgnoreCase("s") || text.equalsIgnoreCase("si") || text.equalsIgnoreCase("sì") || text.equalsIgnoreCase("y") || text.equalsIgnoreCase("yes")) {
			sendMessage(chatId, "🔎 Avvio di una nuova predizione guidata.");
			session.setState(ClientSession.WAITING_PREDICTION_CHOICE);
			handleServerResponse(chatId, session, session.startPrediction());
			return;
		}

		if(text.equalsIgnoreCase("n") || text.equalsIgnoreCase("no")) {
			closeSession(chatId);
			sendMessage(chatId, getSessionClosedMessage());
			return;
		}

		sendMessage(chatId, "⚠️ Risposta non valida.\n\n" + getRepeatPredictionMessage());
	}

	/**
	 * Gestisce una risposta ricevuta dal server durante la predizione guidata.
	 *
	 * <p>La risposta può contenere una nuova domanda, una predizione finale oppure
	 * un errore. In base al tipo della risposta, il bot aggiorna lo stato della
	 * sessione e invia il messaggio opportuno all'utente.</p>
	 *
	 * @param chatId identificativo della chat Telegram
	 * @param session sessione corrente dell'utente
	 * @param response risposta ricevuta dal server
	 */
	private void handleServerResponse(long chatId, ClientSession session, ClientSession.ServerResponse response) {
		if(response.getType().equals(ClientSession.ServerResponse.QUERY)) {
			session.setState(ClientSession.WAITING_PREDICTION_CHOICE);
			sendMessage(chatId, formatQuestion(response.getMessage()));
			return;
		}

		if(response.getType().equals(ClientSession.ServerResponse.PREDICTION)) {
			session.setState(ClientSession.WAITING_REPEAT_PREDICTION);
			sendMessage(chatId, "🎯 Classe predetta: " + response.getMessage());
			sendMessage(chatId, getRepeatPredictionMessage());
			return;
		}

		if(session.getLastQuestion() != null) {
			session.setState(ClientSession.WAITING_PREDICTION_CHOICE);
			sendMessage(chatId, "⚠️ Errore: " + response.getMessage() + "\n\n" + formatQuestion(session.getLastQuestion()));
			return;
		}

		closeSession(chatId);
		sendMessage(chatId, "⚠️ Errore: " + response.getMessage());
		sendMessage(chatId, getSessionClosedMessage());
	}

	/**
	 * Restituisce il messaggio del menu principale.
	 *
	 * @return testo del menu principale
	 */
	private String getMenuMessage() {
		return "🧭 Scegli un'operazione:\n\n"
			+ "1) 📚 Apprendi un nuovo albero da una tabella del database\n"
			+ "2) 📂 Carica un albero già salvato da archivio\n\n"
			+ "✍️ Invia 1 oppure 2.";
	}

	/**
	 * Restituisce il messaggio di aiuto del bot.
	 *
	 * @return testo della guida del bot
	 */
	private String getHelpMessage() {
		return "🆘 Guida di Regression Tree Miner Bot\n\n"
			+ "Comandi disponibili:\n\n"
			+ "/start - Avvia una nuova sessione\n"
			+ "/end - Termina la sessione corrente\n"
			+ "/info - Mostra informazioni sul progetto\n"
			+ "/help - Mostra questo messaggio\n\n"
			+ "📌 Come funziona:\n"
			+ "1. Avvia una sessione con /start\n"
			+ "2. Scegli se apprendere un nuovo albero o caricarne uno salvato\n"
			+ "3. Rispondi alle domande della predizione guidata\n"
			+ "4. Il bot mostrerà la classe predetta\n"
			+ "5. Potrai scegliere se effettuare un'altra predizione con lo stesso albero oppure terminare la sessione\n\n"
			+ "Durante la predizione, rispondi digitando il numero del ramo scelto.";
	}

	/**
	 * Restituisce il messaggio informativo sul progetto e sull'architettura del bot.
	 *
	 * @return testo informativo del bot
	 */
	private String getInfoMessage() {
		return "ℹ️ Regression Tree Miner Bot\n\n"
			+ "Questo bot è un'estensione Telegram del progetto Regression Tree Miner.\n\n"
			+ "Il sistema permette di costruire e usare alberi di regressione a partire da dati memorizzati in un database MySQL.\n\n"
			+ "Architettura:\n"
			+ "Telegram → Bot Java → Server Java → Database MySQL\n\n"
			+ "Il bot non accede direttamente al database e non calcola direttamente l'albero: comunica con il server tramite socket, rispettando la struttura client/server del progetto.";
	}

	/**
	 * Restituisce il messaggio di benvenuto mostrato all'avvio di una sessione.
	 *
	 * @return testo di benvenuto del bot
	 */
	private String getWelcomeMessage() {
		return "🌳 Benvenuto in Regression Tree Miner Bot!\n\n"
			+ "Regression Tree Miner è un sistema di data mining progettato per costruire alberi di regressione e usarli per predire un valore numerico.\n\n"
			+ "📊 Data Mining\n"
			+ "Il data mining consiste nell'analisi di dati per individuare relazioni, schemi e informazioni utili.\n\n"
			+ "🌳 Albero di regressione\n"
			+ "Un albero di regressione è un modello predittivo ad albero. Ogni nodo interno rappresenta un test su un attributo, mentre ogni foglia contiene un valore numerico predetto.\n\n"
			+ "🎯 Predizione\n"
			+ "Durante la predizione, il bot ti guiderà passo dopo passo nella scelta dei rami dell'albero, fino ad arrivare alla classe predetta.\n\n"
			+ "📁 Tabelle di esempio:\n"
			+ "prova, provaC, servo";
	}

	/**
	 * Restituisce il messaggio mostrato quando non esiste una sessione attiva.
	 *
	 * @return testo di assenza sessione
	 */
	private String getNoSessionMessage() {
		return "⚠️ Nessuna sessione attiva.\n\n"
			+ "Per iniziare a usare Regression Tree Miner Bot, digita:\n\n"
			+ "/start";
	}

	/**
	 * Restituisce il messaggio mostrato quando una sessione viene terminata.
	 *
	 * @return testo di chiusura sessione
	 */
	private String getSessionClosedMessage() {
		return "✅ Sessione terminata.\n\n"
			+ "Digita /start per iniziare una nuova sessione.";
	}

	/**
	 * Restituisce il messaggio con cui il bot chiede se effettuare un'altra predizione.
	 *
	 * @return testo della richiesta di nuova predizione
	 */
	private String getRepeatPredictionMessage() {
		return "🔁 Vuoi effettuare un'altra predizione con lo stesso albero? (s/n)";
	}

	/**
	 * Formatta una domanda di predizione guidata ricevuta dal server.
	 *
	 * @param question domanda ricevuta dal server
	 * @return domanda formattata per l'utente Telegram
	 */
	private String formatQuestion(String question) {
		if(question == null)
			return "⚠️ Predizione guidata non disponibile.\nDigita /start per iniziare una nuova sessione.";

		return "🔎 Predizione guidata\n\n"
			+ question
			+ "\n\n"
			+ "✍️ Rispondi digitando il numero del ramo scelto.";
	}

	/**
	 * Verifica se una risposta testuale del server indica esito positivo.
	 *
	 * @param response risposta ricevuta dal server
	 * @return {@code true} se la risposta è uguale a {@code OK}, {@code false} altrimenti
	 */
	private boolean isOk(String response) {
		return response != null && response.equals("OK");
	}

	/**
	 * Chiude e rimuove la sessione associata alla chat indicata.
	 *
	 * @param chatId identificativo della chat Telegram
	 */
	private void closeSession(long chatId) {
		ClientSession session = sessions.remove(chatId);

		if(session != null)
			session.close();
	}

	/**
	 * Invia un messaggio testuale alla chat Telegram indicata.
	 *
	 * @param chatId identificativo della chat Telegram
	 * @param text testo del messaggio da inviare
	 */
	private void sendMessage(long chatId, String text) {
		SendMessage message = SendMessage.builder()
			.chatId(chatId)
			.text(text)
			.build();

		try {
			telegramClient.execute(message);
		} catch(TelegramApiException e) {
			e.printStackTrace();
		}
	}
}