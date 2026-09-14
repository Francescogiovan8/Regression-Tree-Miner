import server.MultiServer;

/**
 * Classe principale del server del progetto Regression Tree Miner.
 *
 * <p>La classe avvia il server multiclient sulla porta configurata.
 * Il server resta in ascolto di connessioni socket provenienti dal client
 * console o dall'estensione Telegram Bot.</p>
 */
public class Main {

    /**
     * Punto di ingresso dell'applicazione server.
     *
     * <p>Il metodo crea un'istanza di {@link MultiServer}, specificando la porta
     * su cui il server deve rimanere in ascolto.</p>
     *
     * @param args argomenti passati da linea di comando, non utilizzati
     */
    public static void main(String[] args) {

        int port = 8080;    //se si vuole cambiare porta, cambiare il valore di questa variabile

        new MultiServer(port);

    }
}