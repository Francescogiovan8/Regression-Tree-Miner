package data;

/**
 * Eccezione lanciata quando il training set non può essere caricato o non è valido.
 *
 * <p>Nel progetto Regression Tree Miner questa eccezione viene usata per segnalare
 * errori relativi ai dati di addestramento, come una tabella inesistente, una
 * tabella vuota, una struttura non valida o una variabile target non numerica.</p>
 */
public class TrainingDataException extends Exception {

    /**
     * Crea una nuova eccezione senza messaggio descrittivo.
     */
    public TrainingDataException(){}

    /**
     * Crea una nuova eccezione con un messaggio descrittivo.
     *
     * @param message messaggio che descrive l'errore verificatosi
     */
    public TrainingDataException(String message) {
        super(message);
    }

}
