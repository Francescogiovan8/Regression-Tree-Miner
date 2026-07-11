package tree;

import data.Data;

/**
 * Modella un nodo foglia dell'albero di regressione.
 *
 * <p>Un nodo foglia non ha figli e contiene il valore numerico predetto
 * dall'albero. Tale valore viene calcolato come media dei valori della variabile
 * target degli esempi del training set coperti dalla foglia.</p>
 */
class LeafNode extends Node {
    /**
     * Valore della classe predetta dalla foglia.
     */
    private Double predictedClassValue=0.0;

    /**
     * Crea un nuovo nodo foglia.
     *
     * <p>Il costruttore inizializza il nodo tramite il costruttore della classe
     * {@link Node} e calcola il valore predetto come media dei valori target
     * degli esempi compresi tra {@code beginExampleIndex} ed
     * {@code endExampleIndex}.</p>
     *
     * @param trainingSet training set usato per costruire l'albero
     * @param beginExampleIndex indice del primo esempio coperto dalla foglia
     * @param endExampleIndex indice dell'ultimo esempio coperto dalla foglia
     */
    LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex){
        super(trainingSet, beginExampleIndex, endExampleIndex);

        for(int i=beginExampleIndex;i<=endExampleIndex;i++){
            predictedClassValue+=trainingSet.getClassValue(i);
        }
        predictedClassValue=predictedClassValue/(endExampleIndex-beginExampleIndex+1);
    }

    /**
     * Restituisce il valore numerico predetto dalla foglia.
     *
     * @return valore della classe predetta
     */
    Double getPredictedClassValue(){
        return predictedClassValue;
    }

    /**
     * Restituisce il numero di figli del nodo foglia.
     *
     * <p>Una foglia non ha figli, quindi il metodo restituisce sempre {@code 0}.</p>
     *
     * @return numero di figli, sempre {@code 0}
     */
    int getNumberOfChildren(){
        return 0;
    }

    /**
     * Restituisce una rappresentazione testuale della foglia.
     *
     * @return stringa contenente il valore predetto e le informazioni del nodo
     */
    @Override
    public String toString(){
        return "LEAF : class="+getPredictedClassValue()+" "+super.toString();
    }
}