package data;

import java.io.Serializable;

/**
 * Modella un attributo generico del training set.
 *
 * <p>La classe rappresenta l'astrazione comune agli attributi discreti e continui.
 * Ogni attributo è identificato da un nome e da un indice, che rappresenta la sua
 * posizione all'interno dello schema dei dati.</p>
 *
 * <p>La classe è astratta perché non viene istanziata direttamente, ma viene
 * specializzata dalle sottoclassi {@link DiscreteAttribute} e
 * {@link ContinuousAttribute}.</p>
 */
public abstract class Attribute implements Serializable {
    /**
     * Nome dell'attributo.
     */
    private String name;

    /**
     * Indice dell'attributo all'interno dello schema dei dati.
     */
    private int index;

    /**
     * Crea un nuovo attributo con il nome e l'indice specificati.
     *
     * @param name nome dell'attributo
     * @param index indice dell'attributo nello schema dei dati
     */
    protected Attribute(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * Restituisce il nome dell'attributo.
     *
     * @return nome dell'attributo
     */
    public String getName() {
        return name;
    }

    /**
     * Restituisce l'indice dell'attributo.
     *
     * @return indice dell'attributo nello schema dei dati
     */
    public int getIndex() {
        return index;
    }

    /**
     * Restituisce una rappresentazione testuale dell'attributo.
     *
     * <p>La rappresentazione coincide con il nome dell'attributo.</p>
     *
     * @return nome dell'attributo
     */
    @Override
    public String toString() {
        return name;
    }
}