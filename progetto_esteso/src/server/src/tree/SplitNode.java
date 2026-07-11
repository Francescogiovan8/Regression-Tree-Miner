package tree;

import data.Attribute;
import data.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Modella un nodo di split dell'albero di regressione.
 *
 * <p>Un nodo di split è un nodo interno dell'albero che suddivide gli esempi
 * coperti dal nodo in più sottoinsiemi, in base ai valori di un attributo
 * esplicativo.</p>
 *
 * <p>La classe contiene la struttura comune ai nodi di split discreti e continui.
 * Le sottoclassi devono implementare la costruzione delle informazioni di split
 * e il test della condizione durante la predizione.</p>
 */
abstract class SplitNode extends Node implements Comparable<SplitNode> {

	/**
	 * Contiene le informazioni descrittive relative a un singolo ramo dello split.
	 *
	 * <p>Ogni oggetto {@code SplitInfo} associa un valore di split, o una condizione
	 * di split, all'intervallo di esempi del training set coperto dal figlio
	 * corrispondente.</p>
	 */
	class SplitInfo implements Serializable {
		/**
		 * Valore usato per descrivere la condizione di split.
		 */
		private Object splitValue;

		/**
		 * Indice del primo esempio coperto dal figlio.
		 */
		private int beginIndex;

		/**
		 * Indice dell'ultimo esempio coperto dal figlio.
		 */
		private int endIndex;

		/**
		 * Numero identificativo del figlio associato allo split.
		 */
		private int numberChild;

		/**
		 * Comparatore testuale usato nella formulazione della condizione.
		 *
		 * <p>Il valore predefinito è {@code "="}, usato per gli attributi discreti.</p>
		 */
		private String comparator = "=";

		/**
		 * Crea una nuova informazione di split usando il comparatore predefinito.
		 *
		 * @param splitValue valore dello split
		 * @param beginIndex indice del primo esempio coperto dal figlio
		 * @param endIndex indice dell'ultimo esempio coperto dal figlio
		 * @param numberChild numero identificativo del figlio
		 */
		SplitInfo(Object splitValue, int beginIndex, int endIndex, int numberChild) {
			this.splitValue = splitValue;
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.numberChild = numberChild;
		}

		/**
		 * Crea una nuova informazione di split specificando anche il comparatore.
		 *
		 * @param splitValue valore dello split
		 * @param beginIndex indice del primo esempio coperto dal figlio
		 * @param endIndex indice dell'ultimo esempio coperto dal figlio
		 * @param numberChild numero identificativo del figlio
		 * @param comparator comparatore associato alla condizione di split
		 */
		SplitInfo(Object splitValue, int beginIndex, int endIndex, int numberChild, String comparator) {
			this.splitValue = splitValue;
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.numberChild = numberChild;
			this.comparator = comparator;
		}

		/**
		 * Restituisce l'indice del primo esempio coperto dal figlio.
		 *
		 * @return indice iniziale degli esempi coperti dal figlio
		 */
		int getBeginindex() {
			return beginIndex;
		}

		/**
		 * Restituisce l'indice dell'ultimo esempio coperto dal figlio.
		 *
		 * @return indice finale degli esempi coperti dal figlio
		 */
		int getEndIndex() {
			return endIndex;
		}

		/**
		 * Restituisce il valore associato allo split.
		 *
		 * @return valore dello split
		 */
		 Object getSplitValue() {
			return splitValue;
		}

		/**
		 * Restituisce una rappresentazione testuale dell'informazione di split.
		 *
		 * @return stringa contenente figlio, condizione e intervallo di esempi
		 */
		public String toString() {
			return "child " + numberChild + " split value" + comparator + splitValue + "[Examples:" + beginIndex + "-" + endIndex + "]";
		}

		/**
		 * Restituisce il comparatore associato allo split.
		 *
		 * @return comparatore della condizione di split
		 */
		String getComparator() {
			return comparator;
		}

	}

	/**
	 * Attributo esplicativo usato per effettuare lo split del nodo.
	 */
	private Attribute attribute;

	/**
	 * Lista delle informazioni di split associate ai figli del nodo.
	 */
	protected List<SplitInfo> mapSplit = new ArrayList<>();

	/**
	 * Valore della varianza/SSE totale ottenuta dopo lo split.
	 */
	private double splitVariance;

	/**
	 * Costruisce le informazioni di split del nodo.
	 *
	 * <p>Il metodo viene implementato dalle sottoclassi, perché la costruzione
	 * degli split cambia tra attributi discreti e attributi continui.</p>
	 *
	 * @param trainingSet training set usato per costruire l'albero
	 * @param beginExampelIndex indice del primo esempio coperto dal nodo
	 * @param endExampleIndex indice dell'ultimo esempio coperto dal nodo
	 * @param attribute attributo usato per lo split
	 */
	abstract void setSplitInfo(Data trainingSet, int beginExampelIndex, int endExampleIndex, Attribute attribute);

	/**
	 * Verifica quale ramo dello split deve essere seguito per un valore specifico.
	 *
	 * @param value valore dell'attributo da testare
	 * @return indice del figlio da seguire
	 */
	abstract int testCondition(Object value);

	/**
	 * Crea un nuovo nodo di split.
	 *
	 * <p>Il costruttore inizializza il nodo, ordina il training set rispetto
	 * all'attributo di split, costruisce le informazioni dei figli e calcola la
	 * varianza/SSE complessiva dello split.</p>
	 *
	 * @param trainingSet training set usato per costruire l'albero
	 * @param beginExampleIndex indice del primo esempio coperto dal nodo
	 * @param endExampleIndex indice dell'ultimo esempio coperto dal nodo
	 * @param attribute attributo usato per effettuare lo split
	 */
	SplitNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
			super(trainingSet, beginExampleIndex, endExampleIndex);
			this.attribute = attribute;
			trainingSet.sort(attribute, beginExampleIndex, endExampleIndex); // order by attribute
			setSplitInfo(trainingSet, beginExampleIndex, endExampleIndex, attribute);

			//compute variance
			splitVariance = 0;
			for (int i = 0; i < mapSplit.size(); i++) {
				double localVariance = new LeafNode(trainingSet, mapSplit.get(i).getBeginindex(), mapSplit.get(i).getEndIndex()).getVariance();
				splitVariance += (localVariance);
			}
	}

	/**
	 * Restituisce l'attributo usato per lo split.
	 *
	 * @return attributo di split
	 */
	Attribute getAttribute() {
		return attribute;
	}

	/**
	 * Restituisce la varianza/SSE dello split.
	 *
	 * @return valore della varianza/SSE dello split
	 */
	@Override
	double getVariance() {
		return splitVariance;
	}

	/**
	 * Restituisce il numero di figli generati dallo split.
	 *
	 * @return numero di figli del nodo di split
	 */
	@Override
	int getNumberOfChildren() {
		return mapSplit.size();
	}

	/**
	 * Restituisce le informazioni di split relative a un figlio.
	 *
	 * @param child indice del figlio
	 * @return informazioni di split del figlio indicato
	 */
	SplitInfo getSplitInfo(int child) {
		return mapSplit.get(child);
	}

	/**
	 * Formula la domanda da mostrare durante la predizione guidata.
	 *
	 * <p>Il metodo costruisce una stringa contenente tutti i rami disponibili,
	 * numerati e descritti tramite attributo, comparatore e valore di split.</p>
	 *
	 * @return stringa contenente le alternative di split
	 */
	String formulateQuery() {
		String query = "";
		for (int i = 0; i < mapSplit.size(); i++) {
			query += (i + ":" + attribute + mapSplit.get(i).getComparator() + mapSplit.get(i).getSplitValue()) + "\n";
		}
		return query;
	}

	/**
	 * Restituisce una rappresentazione testuale del nodo di split.
	 *
	 * @return stringa contenente attributo, varianza/SSE e informazioni dei figli
	 */
	@Override
	public String toString() {
		String v = "SPLIT : attribute=" + attribute + " " + super.toString() +  " Split Variance: " + getVariance() + "\n";

		for (int i = 0; i < mapSplit.size(); i++) {
			v += "\t" + mapSplit.get(i) + "\n";
		}

		return v;
	}

	/**
	 * Confronta due nodi di split in base alla loro varianza/SSE.
	 *
	 * <p>Il confronto viene usato per scegliere lo split migliore, cioè quello
	 * con varianza/SSE minore.</p>
	 *
	 * @param o nodo di split con cui confrontare il nodo corrente
	 * @return valore negativo se il nodo corrente ha varianza minore, positivo se
	 * ha varianza maggiore, zero se le varianze sono uguali
	 */
	@Override
	public int compareTo(SplitNode o) {
		if (splitVariance < o.splitVariance) {
			return -1;
		} else if (splitVariance > o.splitVariance) {
			return 1;
		} else {
			return 0;
		}
	}
}