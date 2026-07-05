package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {

	private int PORT = 8080;

	public MultiServer(int port) {
		PORT = port;
		run();
	}

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