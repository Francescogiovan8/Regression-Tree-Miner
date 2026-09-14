package data;

/**
 * Modella un attributo continuo del training set.
 *
 * <p>Un attributo continuo rappresenta una variabile numerica usata nella
 * costruzione dell'albero di regressione. Nel progetto viene usato per
 * distinguere gli attributi numerici dagli attributi discreti.</p>
 *
 * <p>La classe estende {@link Attribute} e ne eredita il nome e l'indice
 * all'interno dello schema dei dati.</p>
 */
public class ContinuousAttribute extends Attribute{
    /**
     * Crea un nuovo attributo continuo.
     *
     * @param name nome dell'attributo continuo
     * @param index indice dell'attributo nello schema dei dati
     */
    public ContinuousAttribute(String name, int index){
        super(name, index);
    }
}