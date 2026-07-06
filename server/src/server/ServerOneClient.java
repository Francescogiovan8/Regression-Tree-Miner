package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

class ServerOneClient extends Thread {

	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;

    private Data trainingSet;
    private RegressionTree tree;
    private String tableName;

	public ServerOneClient(Socket s) throws IOException {
		socket = s;
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(socket.getInputStream());
		start();
	}

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