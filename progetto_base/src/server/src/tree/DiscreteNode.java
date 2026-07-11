package tree;

import data.Attribute;
import data.Data;
import data.DiscreteAttribute;

/**
 * Modella un nodo di split basato su un attributo discreto.
 *
 * <p>Il nodo suddivide gli esempi del training set in base ai valori distinti
 * assunti dall'attributo discreto selezionato. Ogni valore distinto genera un
 * ramo dello split.</p>
 */
class DiscreteNode extends SplitNode {
    /**
     * Crea un nuovo nodo di split discreto.
     *
     * <p>Il costruttore richiama il costruttore della classe {@link SplitNode},
     * che ordina il training set rispetto all'attributo e avvia la costruzione
     * delle informazioni di split.</p>
     *
     * @param trainingSet training set usato per costruire l'albero
     * @param beginExampleIndex indice del primo esempio coperto dal nodo
     * @param endExampleIndex indice dell'ultimo esempio coperto dal nodo
     * @param attribute attributo discreto usato per effettuare lo split
     */
    DiscreteNode(Data trainingSet,int beginExampleIndex, int endExampleIndex,DiscreteAttribute attribute){
        super(trainingSet,beginExampleIndex,endExampleIndex,attribute);
    }

    /**
     * Costruisce le informazioni di split per un attributo discreto.
     *
     * <p>Il training set è già ordinato rispetto all'attributo. Il metodo scorre
     * gli esempi compresi tra {@code beginExampleIndex} ed {@code endExampleIndex}
     * e crea un nuovo ramo ogni volta che incontra un valore diverso dell'attributo.</p>
     *
     * @param trainingSet training set usato per costruire lo split
     * @param beginExampleIndex indice del primo esempio coperto dal nodo
     * @param endExampleIndex indice dell'ultimo esempio coperto dal nodo
     * @param attribute attributo discreto usato per lo split
     */
    @Override
    void setSplitInfo(Data trainingSet,int beginExampleIndex, int endExampleIndex, Attribute attribute){
        int attributeIndex = attribute.getIndex();

        mapSplit.clear();

        int numberChild = 0;
        int beginSplitIndex = beginExampleIndex;

        Object currentSplitValue = trainingSet.getExplanatoryValue(beginExampleIndex, attributeIndex);

        for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {

            Object currentValue = trainingSet.getExplanatoryValue(i, attributeIndex);

            if (!currentValue.equals(currentSplitValue)) {
                mapSplit.add(new SplitInfo(currentSplitValue, beginSplitIndex, i - 1, numberChild));
                numberChild++;
                beginSplitIndex = i;
                currentSplitValue = currentValue;
            }
        }

        mapSplit.add(new SplitInfo(currentSplitValue, beginSplitIndex, endExampleIndex, numberChild));
    }

    /**
     * Determina quale ramo seguire durante la predizione.
     *
     * <p>Il metodo confronta il valore ricevuto con i valori di split presenti
     * nella lista dei figli. Se il valore viene trovato, restituisce l'indice del
     * figlio corrispondente; altrimenti restituisce {@code -1}.</p>
     *
     * @param value valore dell'attributo da testare
     * @return indice del ramo da seguire, oppure {@code -1} se il valore non è presente
     */
    @Override
    int testCondition (Object value){
        for (int i = 0; i < mapSplit.size(); i++) {
            if (mapSplit.get(i).getSplitValue().equals(value)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Restituisce una rappresentazione testuale del nodo discreto.
     *
     * @return stringa contenente le informazioni del nodo discreto
     */
    @Override
    public String toString(){
        return "DISCRETE "+super.toString();
    }
}