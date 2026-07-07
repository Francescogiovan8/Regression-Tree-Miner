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

class TelegramRegressionTreeBot implements LongPollingSingleThreadUpdateConsumer {
	private final TelegramClient telegramClient;
	private final String serverAddress;
	private final int serverPort;
	private final Map<Long, ClientSession> sessions = new HashMap<>();

	TelegramRegressionTreeBot(String botToken, String serverAddress, int serverPort) {
		this.telegramClient = new OkHttpTelegramClient(botToken);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
	}

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

	private void endSession(long chatId) {
		ClientSession session = sessions.get(chatId);

		if(session == null) {
			sendMessage(chatId, getNoSessionMessage());
			return;
		}

		closeSession(chatId);
		sendMessage(chatId, getSessionClosedMessage());
	}

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

	private String getMenuMessage() {
		return "🧭 Scegli un'operazione:\n\n"
			+ "1) 📚 Apprendi un nuovo albero da una tabella del database\n"
			+ "2) 📂 Carica un albero già salvato da archivio\n\n"
			+ "✍️ Invia 1 oppure 2.";
	}

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

	private String getInfoMessage() {
		return "ℹ️ Regression Tree Miner Bot\n\n"
			+ "Questo bot è un'estensione Telegram del progetto Regression Tree Miner.\n\n"
			+ "Il sistema permette di costruire e usare alberi di regressione a partire da dati memorizzati in un database MySQL.\n\n"
			+ "Architettura:\n"
			+ "Telegram → Bot Java → Server Java → Database MySQL\n\n"
			+ "Il bot non accede direttamente al database e non calcola direttamente l'albero: comunica con il server tramite socket, rispettando la struttura client/server del progetto.";
	}

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

	private String getNoSessionMessage() {
		return "⚠️ Nessuna sessione attiva.\n\n"
			+ "Per iniziare a usare Regression Tree Miner Bot, digita:\n\n"
			+ "/start";
	}

	private String getSessionClosedMessage() {
		return "✅ Sessione terminata.\n\n"
			+ "Digita /start per iniziare una nuova sessione.";
	}

	private String getRepeatPredictionMessage() {
		return "🔁 Vuoi effettuare un'altra predizione con lo stesso albero? (s/n)";
	}

	private String formatQuestion(String question) {
		if(question == null)
			return "⚠️ Predizione guidata non disponibile.\nDigita /start per iniziare una nuova sessione.";

		return "🔎 Predizione guidata\n\n"
			+ question
			+ "\n\n"
			+ "✍️ Rispondi digitando il numero del ramo scelto.";
	}

	private boolean isOk(String response) {
		return response != null && response.equals("OK");
	}

	private void closeSession(long chatId) {
		ClientSession session = sessions.remove(chatId);

		if(session != null)
			session.close();
	}

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
