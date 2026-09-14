package bot;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

/**
 * Classe principale dell'estensione Bot Telegram del progetto Regression Tree Miner.
 *
 * <p>La classe legge i parametri di avvio da linea di comando, configura
 * l'indirizzo e la porta del server e registra il bot Telegram tramite
 * long polling.</p>
 *
 * <p>Il token Telegram deve essere passato come primo argomento. L'indirizzo
 * e la porta del server sono opzionali e, se omessi, vengono usati i valori
 * predefiniti {@code 127.0.0.1} e {@code 8080}.</p>
 */
public class Main {

	/**
	 * Punto di ingresso dell'applicazione Bot Telegram.
	 *
	 * <p>Gli argomenti accettati sono:</p>
	 * <ul>
	 *   <li>{@code args[0]}: token Telegram del bot;</li>
	 *   <li>{@code args[1]}: indirizzo del server, opzionale;</li>
	 *   <li>{@code args[2]}: porta del server, opzionale.</li>
	 * </ul>
	 *
	 * <p>Il metodo crea un'istanza di {@link TelegramBotsLongPollingApplication},
	 * registra il bot e mantiene attivo il processo fino all'interruzione manuale.</p>
	 *
	 * @param args argomenti passati da linea di comando
	 */
	public static void main(String[] args) {
		String botToken = null;
		String serverAddress = "127.0.0.1";
		int serverPort = 8080;

		if(args.length >= 1)
			botToken = args[0];

		if(args.length >= 2)
			serverAddress = args[1];

		if(args.length >= 3) {
			try {
				serverPort = Integer.parseInt(args[2]);
			} catch(NumberFormatException e) {
				System.err.println("Errore: la porta del server deve essere un numero intero.");
				printUsage();
				return;
			}
		}

		if(botToken == null || botToken.isBlank()) {
			System.err.println("Errore: token Telegram non specificato.");
			printUsage();
			return;
		}

		try(TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			botsApplication.registerBot(botToken, new TelegramRegressionTreeBot(botToken, serverAddress, serverPort));

			System.out.println("Regression Tree Miner Bot avviato.");
			System.out.println("Server configurato: " + serverAddress + ":" + serverPort);
			System.out.println("Premi CTRL+C per terminare.");

			Thread.currentThread().join();
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Bot interrotto.");
		} catch(Exception e) {
			System.err.println("Errore durante l'avvio del bot.");
			e.printStackTrace();
		}
	}

	/**
	 * Stampa le istruzioni d'uso per avviare correttamente il Bot Telegram.
	 *
	 * <p>Il metodo viene invocato quando il token Telegram non viene fornito
	 * oppure quando la porta del server non è un numero intero valido.</p>
	 */
	private static void printUsage() {
		System.err.println("Uso:");
		System.err.println("java -jar dist/bot.jar TOKEN_TELEGRAM [SERVER_ADDRESS] [SERVER_PORT]");
		System.err.println();
		System.err.println("Esempi:");
		System.err.println("java -jar dist/bot.jar \"123456:ABCDEF\"");
		System.err.println("java -jar dist/bot.jar \"123456:ABCDEF\" 127.0.0.1 8080");
	}
}