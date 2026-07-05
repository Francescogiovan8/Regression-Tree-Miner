import server.MultiServer;

/**
 * Classe main del Server.
 */
public class Main {

    /**
     * Main del server.
     * @param args argomenti passati da terminale
     */
    public static void main(String[] args) {

        int port = 8080;    //se si vuole cambiare porta, cambiare il valore di questa variabile

        new MultiServer(port);

    }
}
