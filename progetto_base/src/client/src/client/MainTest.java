package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import utility.Keyboard;

/**
 * Classe principale del client console del progetto Regression Tree Miner.
 *
 * <p>Il client permette all'utente di comunicare con il server tramite socket.
 * Attraverso il menu testuale è possibile apprendere un nuovo albero di regressione
 * da una tabella del database oppure caricare un albero già serializzato da archivio.
 * Dopo il caricamento o l'apprendimento dell'albero, il client avvia una predizione
 * guidata interagendo con il server.</p>
 *
 * <p>La classe rappresenta il punto di ingresso del client da terminale.</p>
 */
public class MainTest {

	/**
	 * Punto di ingresso del client console.
	 *
	 * <p>Il metodo riceve da linea di comando l'indirizzo e la porta del server,
	 * apre una connessione socket e gestisce l'interazione testuale con l'utente.
	 * Il client invia al server i codici operazione previsti dal protocollo
	 * dell'applicazione:</p>
	 *
	 * <ul>
	 *   <li>{@code 0}: caricamento del training set da database;</li>
	 *   <li>{@code 1}: apprendimento dell'albero di regressione;</li>
	 *   <li>{@code 2}: caricamento di un albero da archivio;</li>
	 *   <li>{@code 3}: avvio della predizione guidata.</li>
	 * </ul>
	 *
	 * <p>Durante la predizione, il client riceve dal server le domande relative
	 * ai nodi dell'albero e invia la scelta del ramo selezionato dall'utente.</p>
	 *
	 * @param args argomenti da linea di comando; {@code args[0]} contiene l'indirizzo
	 * del server e {@code args[1]} contiene la porta del server
	 */
	public static void main(String[] args){
		
		InetAddress addr;

		try {
			addr = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e) {
			System.out.println(e.toString());
			return;
		}

		Socket socket=null;
		ObjectOutputStream out=null;
		ObjectInputStream in=null;

		try {
			socket = new Socket(args[0], Integer.parseInt(args[1]));
			System.out.println(socket);		
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream()); // stream con richieste del client
			
		}  catch (IOException e) {
			System.out.println(e.toString());
			return;
		}

		

		
		String answer="";
		
		int decision=0;
		do{
			System.out.println("\n===== Regression Tree Miner =====");
			System.out.println("1) Apprendi un nuovo albero da una tabella del database");
			System.out.println("2) Carica un albero già salvato da archivio");
			System.out.print("Scelta: ");
			decision=Keyboard.readInt();

			if (decision != 1 && decision != 2) {
				System.out.println("Scelta non valida. Inserire 1 oppure 2.");
			}
		}while(!(decision==1) && !(decision ==2));
		
		String tableName="";
		if(decision==1)
			System.out.println("\nInserisci il nome della tabella del database:");
		else
			System.out.println("\nInserisci il nome dell'archivio senza estensione .dmp:");
		tableName=Keyboard.readString();

		try{
		
			if(decision==1) {
				System.out.println("\n[1/3] Caricamento del training set dal database...");
				
				
				
				out.writeObject(0);
				out.writeObject(tableName);
				answer=in.readObject().toString();
				if(!answer.equals("OK")){
					System.out.println(answer);
					return;
				}
					
				
				
			
				System.out.println("[2/3] Apprendimento dell'albero di regressione...");
				out.writeObject(1);
				
			
			}
			else
			{
				System.out.println("\nCaricamento dell'albero di regressione dall'archivio...");
				out.writeObject(2);
				out.writeObject(tableName);
				
			}
			
			answer=in.readObject().toString();
			if(!answer.equals("OK")){
				System.out.println(answer);
				return;
			}
			
			if(decision==1) {
				System.out.println("[3/3] Albero appreso e salvato.");
			}
			else {
				System.out.println("Albero caricato correttamente.");
			}


			char risp='y';
			
			do{
				out.writeObject(3);
				
				System.out.println("\n--- Predizione guidata ---");
				System.out.println("Scegli il ramo digitando il numero corrispondente:");
				answer=in.readObject().toString();
			
				
				while(answer.equals("QUERY")){
					// Formualting query, reading answer
					answer=in.readObject().toString();
					System.out.println(answer);
					System.out.print("Scelta: ");
					int path=Keyboard.readInt();
					out.writeObject(path);
					answer=in.readObject().toString();
				}
			
				if(answer.equals("OK"))
				{ // Reading prediction
					answer=in.readObject().toString();
					System.out.println("Classe predetta: "+ answer);
					
				}
				else //Printing error message
					System.out.println(answer);
				
			
				System.out.println("\nVuoi effettuare un'altra predizione? (s/n)");
				risp=Keyboard.readChar();
								
			}while (Character.toUpperCase(risp)=='S' || Character.toUpperCase(risp)=='Y');
		
		}
		catch(IOException | ClassNotFoundException e){
			System.out.println(e.toString());
			
		}
	}

}