package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

/**
 * Gestisce la comunicazione tra il server e un singolo client.
 *
 * <p>Ogni oggetto di questa classe viene eseguito in un thread separato e
 * permette al server di gestire più client contemporaneamente. Il client può
 * essere il client console oppure l'estensione Telegram Bot.</p>
 *
 * <p>La classe interpreta i codici operazione ricevuti tramite socket e richiama
 * le funzionalità del progetto Regression Tree Miner: caricamento del training
 * set, apprendimento dell'albero di regressione, caricamento da archivio e
 * predizione guidata.</p>
 */
class ServerOneClient extends Thread {

	/**
	 * Socket associata al client gestito dal thread corrente.
	 */
	private Socket socket;

	/**
	 * Stream di input usato per ricevere richieste e dati dal client.
	 */
	private ObjectInputStream in;

	/**
	 * Stream di output usato per inviare risposte e dati al client.
	 */
	private ObjectOutputStream out;

	/**
	 * Training set caricato dal database per la sessione corrente.
	 */
    private Data trainingSet;

	/**
	 * Albero di regressione appreso o caricato da archivio per la sessione corrente.
	 */
    private RegressionTree tree;

	/**
	 * Nome della tabella del database usata per caricare il training set.
	 *
	 * <p>Il nome viene utilizzato anche per salvare l'albero appreso in un file
	 * con estensione {@code .dmp}.</p>
	 */
    private String tableName;

	/**
	 * Crea un gestore per un client connesso al server.
	 *
	 * <p>Il costruttore inizializza la socket, crea gli stream di comunicazione
	 * e avvia il thread tramite {@link #start()}.</p>
	 *
	 * @param s socket del client accettata dal server
	 * @throws IOException se si verifica un errore durante la creazione degli stream
	 */
	ServerOneClient(Socket s) throws IOException {
		socket = s;
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(socket.getInputStream());
		start();
	}

	/**
	 * Esegue il ciclo principale di comunicazione con il client.
	 *
	 * <p>Il metodo resta in ascolto delle richieste inviate dal client e le
	 * gestisce in base al codice operazione ricevuto:</p>
	 *
	 * <ul>
	 *   <li>{@code 0}: caricamento del training set da una tabella del database;</li>
	 *   <li>{@code 1}: apprendimento di un nuovo albero di regressione;</li>
	 *   <li>{@code 2}: caricamento di un albero serializzato da archivio;</li>
	 *   <li>{@code 3}: esecuzione della predizione guidata.</li>
	 * </ul>
	 *
	 * <p>In caso di disconnessione del client, il thread termina e la socket viene
	 * chiusa nel blocco {@code finally}.</p>
	 */
	@Override
    public void run() {
        try {
            while (true) {
                int request = (Integer) in.readObject();

                switch (request) {
                    case 0:
                        tableName = (String) in.readObject();
                        trainingSet = null;
                        tree = null;

                        try {
                            trainingSet = new Data(tableName);
                            out.writeObject("OK");
                        } catch (TrainingDataException e) {
                            out.writeObject(e.getMessage());
                        }

                        out.flush();
                        break;

                    case 1:
                        if (trainingSet == null) {
                            out.writeObject("Nessun training set caricato. Carica prima una tabella dal database.");
                        } else {
                            try {
                                tree = new RegressionTree(trainingSet);
                                tree.salva(tableName + ".dmp");
                                out.writeObject("OK");
                            } catch (IOException e) {
                                out.writeObject(e.getMessage());
                            }
                        }

                        out.flush();
                        break;

                    case 2:
                        String fileName = (String) in.readObject();

                        try {
                            tree = RegressionTree.carica(fileName + ".dmp");
                            out.writeObject("OK");
                        } catch (IOException | ClassNotFoundException e) {
                            out.writeObject(e.getMessage());
                        }

                        out.flush();
                        break;

                    case 3:
                        if (tree == null) {
                            out.writeObject("Nessun albero di regressione caricato. Apprendi o carica prima un modello.");
                        } else {
                            try {
                                Double predictedClass = tree.predictClass(in, out);
                                out.writeObject("OK");
                                out.writeObject(predictedClass);
                            } catch (UnknownValueException e) {
                                out.writeObject(e.getMessage());
                            }
                        }

                        out.flush();
                        break;

                    default:
                        out.writeObject("Richiesta non valida.");
                        out.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnesso: " + socket);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Errore durante la chiusura del socket: " + e.getMessage());
            }
        }
    }
}