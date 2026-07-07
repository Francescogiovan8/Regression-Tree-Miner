package bot;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
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

	private static void printUsage() {
		System.err.println("Uso:");
		System.err.println("java -jar dist/bot.jar TOKEN_TELEGRAM [SERVER_ADDRESS] [SERVER_PORT]");
		System.err.println();
		System.err.println("Esempi:");
		System.err.println("java -jar dist/bot.jar \"123456:ABCDEF\"");
		System.err.println("java -jar dist/bot.jar \"123456:ABCDEF\" 127.0.0.1 8080");
	}
}
