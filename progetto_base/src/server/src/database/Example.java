package database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Rappresenta un esempio, cioè una riga di una tabella del database.
 *
 * <p>Ogni esempio è costituito da una lista ordinata di valori. I primi valori
 * rappresentano gli attributi esplicativi, mentre l'ultimo valore rappresenta
 * la variabile target del training set.</p>
 *
 * <p>La classe implementa {@link Iterable} per permettere di iterare sui valori
 * dell'esempio e {@link Comparable} per confrontare due esempi in base ai valori
 * contenuti.</p>
 */
public class Example implements Comparable<Example>, Iterable<Object>{

	/**
	 * Lista dei valori che compongono l'esempio.
	 */
	private List<Object> example=new ArrayList<Object>();

	/**
	 * Aggiunge un nuovo valore all'esempio.
	 *
	 * @param o valore da aggiungere
	 */
	public void add(Object o){
		example.add(o);
	}
	
	/**
	 * Restituisce il valore presente nella posizione indicata.
	 *
	 * @param i indice del valore da recuperare
	 * @return valore presente nella posizione indicata
	 */
	public Object get(int i){
		return example.get(i);
	}

	/**
	 * Confronta l'esempio corrente con un altro esempio.
	 *
	 * <p>Il confronto viene effettuato scorrendo i valori dell'esempio ricevuto
	 * come parametro e confrontandoli con i valori corrispondenti dell'esempio
	 * corrente.</p>
	 *
	 * @param ex esempio con cui confrontare l'oggetto corrente
	 * @return un valore negativo, nullo o positivo in base all'ordine tra i due esempi
	 */
	@Override
	public int compareTo(Example ex) {
		
		int i=0;
		for(Object o:ex.example){
			if(!o.equals(this.example.get(i)))
				return ((Comparable)o).compareTo(example.get(i));
			i++;
		}
		return 0;
	}

	/**
	 * Restituisce una rappresentazione testuale dell'esempio.
	 *
	 * <p>I valori vengono concatenati e separati da uno spazio.</p>
	 *
	 * @return stringa contenente i valori dell'esempio
	 */
	@Override
	public String toString(){
		String str="";
		for(Object o:example)
			str+=o.toString()+ " ";
		return str;
	}

	/**
	 * Restituisce un iteratore sui valori dell'esempio.
	 *
	 * @return iteratore sui valori dell'esempio
	 */
	@Override
	public Iterator<Object> iterator() {
		return example.iterator();
	}
	
}