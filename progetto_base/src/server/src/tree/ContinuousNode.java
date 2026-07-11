package tree;

import java.util.ArrayList;
import java.util.List;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;

/**
 * Modella un nodo di split basato su un attributo continuo.
 *
 * <p>Il nodo suddivide gli esempi del training set in due partizioni in base
 * a una soglia numerica. La soglia scelta è quella che produce la minore
 * varianza/SSE complessiva tra le partizioni generate.</p>
 */
class ContinuousNode extends SplitNode {

	/**
	 * Crea un nuovo nodo di split continuo.
	 *
	 * <p>Il costruttore richiama il costruttore della classe {@link SplitNode},
	 * che ordina il training set rispetto all'attributo e avvia la costruzione
	 * delle informazioni di split.</p>
	 *
	 * @param trainingSet training set usato per costruire l'albero
	 * @param beginExampleIndex indice del primo esempio coperto dal nodo
	 * @param endExampleIndex indice dell'ultimo esempio coperto dal nodo
	 * @param attribute attributo continuo usato per effettuare lo split
	 */
	ContinuousNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, ContinuousAttribute attribute) {
		super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
	}

	/**
	 * Costruisce le informazioni di split per un attributo continuo.
	 *
	 * <p>Il training set è già ordinato rispetto all'attributo. Il metodo valuta
	 * le possibili soglie di separazione tra valori consecutivi diversi e sceglie
	 * quella che minimizza la somma delle varianze/SSE delle due partizioni.</p>
	 *
	 * @param trainingSet training set usato per costruire lo split
	 * @param beginExampleIndex indice del primo esempio coperto dal nodo
	 * @param endExampleIndex indice dell'ultimo esempio coperto dal nodo
	 * @param attribute attributo continuo usato per lo split
	 */
	@Override
	void setSplitInfo(Data trainingSet,int beginExampleIndex, int endExampleIndex, Attribute attribute) {
		//Update mapSplit defined in SplitNode -- contiene gli indici del partizionamento
		Double currentSplitValue= (Double)trainingSet.getExplanatoryValue(beginExampleIndex,attribute.getIndex());
		double bestInfoVariance=0;
		List <SplitInfo> bestMapSplit=null;

		for(int i=beginExampleIndex+1;i<=endExampleIndex;i++){
			Double value=(Double)trainingSet.getExplanatoryValue(i,attribute.getIndex());
			if(value.doubleValue()!=currentSplitValue.doubleValue()){
			//	System.out.print(currentSplitValue +" var ");
				double localVariance=new LeafNode(trainingSet, beginExampleIndex,i-1).getVariance();
				double candidateSplitVariance=localVariance;
				localVariance=new LeafNode(trainingSet, i,endExampleIndex).getVariance();
				candidateSplitVariance+=localVariance;
				//System.out.println(candidateSplitVariance);
				if(bestMapSplit==null){
					bestMapSplit=new ArrayList<SplitInfo>();
					bestMapSplit.add(new SplitInfo(currentSplitValue, beginExampleIndex, i-1,0,"<="));
					bestMapSplit.add(new SplitInfo(currentSplitValue, i, endExampleIndex,1,">"));
					bestInfoVariance=candidateSplitVariance;
				}
				else{
					if(candidateSplitVariance<bestInfoVariance){
						bestInfoVariance=candidateSplitVariance;
						bestMapSplit.set(0, new SplitInfo(currentSplitValue, beginExampleIndex, i-1,0,"<="));
						bestMapSplit.set(1, new SplitInfo(currentSplitValue, i, endExampleIndex,1,">"));
					}
				}
				currentSplitValue=value;
			}
		}
		mapSplit=bestMapSplit;

		//rimuovo split inutili (che includono tutti gli esempi nella stessa partizione)
		if((mapSplit.get(1).getBeginindex()==mapSplit.get(1).getEndIndex())){
			mapSplit.remove(1);
		}
	}

	/**
	 * Determina quale ramo seguire durante la predizione.
	 *
	 * <p>Il valore ricevuto viene confrontato con la soglia dello split. Se il
	 * valore è minore o uguale alla soglia, viene scelto il primo ramo. Se è
	 * maggiore e il secondo ramo esiste, viene scelto il secondo ramo.</p>
	 *
	 * @param value valore continuo dell'attributo da testare
	 * @return indice del ramo da seguire, oppure {@code -1} se non esiste un ramo valido
	 */
	@Override
	int testCondition(Object value) {
		Double continuousValue = (Double) value;
		Double splitValue = (Double) mapSplit.get(0).getSplitValue();

		if (continuousValue <= splitValue) {
			return 0;
		}

		if (mapSplit.size() > 1) {
			return 1;
		}

		return -1;
	}

	/**
	 * Restituisce una rappresentazione testuale del nodo continuo.
	 *
	 * @return stringa contenente le informazioni del nodo continuo
	 */
	@Override
	public String toString() {
		return "CONTINUOUS " + super.toString();
	}
}