package data;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Modella un attributo discreto del training set.
 *
 * <p>Un attributo discreto rappresenta una variabile che può assumere un numero
 * finito di valori distinti. Nel progetto Regression Tree Miner questi valori
 * vengono usati per costruire gli split dei nodi discreti dell'albero di
 * regressione.</p>
 *
 * <p>La classe implementa {@link Iterable} per permettere di iterare direttamente
 * sui valori distinti dell'attributo.</p>
 */
public class DiscreteAttribute extends Attribute implements Iterable<String> {

	/**
	 * Insieme ordinato dei valori distinti assunti dall'attributo discreto.
	 *
	 * <p>Viene usato un {@link TreeSet} per mantenere i valori ordinati in modo
	 * crescente.</p>
	 */
	private Set<String> values = new TreeSet<>(); // order by asc

	/**
	 * Crea un nuovo attributo discreto.
	 *
	 * @param name nome dell'attributo discreto
	 * @param index indice dell'attributo nello schema dei dati
	 * @param values insieme dei valori distinti assunti dall'attributo
	 */
	public DiscreteAttribute(String name, int index, Set<String> values) {
		super(name, index);
		this.values = values;
	}

	/**
	 * Restituisce il numero di valori distinti dell'attributo discreto.
	 *
	 * @return numero di valori distinti
	 */
	public int getNumberOfDistinctValues() {
		return values.size();
	}

	/**
	 * Restituisce un iteratore sui valori distinti dell'attributo.
	 *
	 * @return iteratore sui valori distinti
	 */
	@Override
	public Iterator<String> iterator() {
		return values.iterator();
	}

}