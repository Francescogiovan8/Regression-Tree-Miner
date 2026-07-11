package tree;

import java.io.Serializable;

import data.Data;

/**
 * Modella un nodo generico dell'albero di regressione.
 *
 * <p>La classe rappresenta l'astrazione comune ai nodi foglia e ai nodi di split.
 * Ogni nodo copre una porzione del training set, individuata dagli indici del primo
 * e dell'ultimo esempio coperto.</p>
 *
 * <p>Per ogni nodo viene calcolata la quantità indicata come varianza, corrispondente
 * alla SSE della porzione di training set coperta dal nodo.</p>
 */
abstract class Node implements Serializable {
    /**
     * Contatore statico usato per assegnare un identificativo progressivo ai nodi.
     */
    private static int idNodeCount=0; 

    /**
     * Identificativo numerico del nodo.
     */
    private int idNode; 

    /**
     * Indice del primo esempio del training set coperto dal nodo.
     */
    private int beginExampleIndex; 

    /**
     * Indice dell'ultimo esempio del training set coperto dal nodo.
     */
    private int endExampleIndex; 

    /**
     * Valore della SSE associata agli esempi coperti dal nodo.
     */
    private double variance; 

    /**
     * Crea un nuovo nodo dell'albero di regressione.
     *
     * <p>Il costruttore assegna gli indici degli esempi coperti dal nodo,
     * genera l'identificativo del nodo e calcola la SSE relativa alla porzione
     * del training set indicata.</p>
     *
     * @param trainingSet training set usato per costruire l'albero
     * @param beginExampleIndex indice del primo esempio coperto dal nodo
     * @param endExampleIndex indice dell'ultimo esempio coperto dal nodo
     */
    protected Node(Data trainingSet, int beginExampleIndex, int endExampleIndex){
        this.beginExampleIndex=beginExampleIndex;
        this.endExampleIndex=endExampleIndex;
        idNode=idNodeCount++;
        variance = calculateSSE(trainingSet);
    }

    /**
     * Calcola la SSE degli esempi coperti dal nodo.
     *
     * <p>Il metodo usa i valori della variabile target degli esempi compresi tra
     * {@link #beginExampleIndex} ed {@link #endExampleIndex}. La formula usata è
     * {@code sumSquare - (sum * sum / size)}.</p>
     *
     * @param trainingSet training set da cui leggere i valori della classe
     * @return SSE della porzione di training set coperta dal nodo
     */
    private double calculateSSE(Data trainingSet){
        int size=getEndExampleIndex()-getBeginExampleIndex()+1;
        double value;
        double sum=0;
        double sumSquare=0;

        for(int i=getBeginExampleIndex();i<=getEndExampleIndex();i++){
            value=trainingSet.getClassValue(i);
            sum+=value;
            sumSquare+=value*value;
        }

        return sumSquare-(sum*sum/size);
    }

    /**
     * Restituisce l'identificativo numerico del nodo.
     *
     * @return identificativo del nodo
     */
    int getIdNode(){
        return idNode;
    }

    /**
     * Restituisce l'indice del primo esempio coperto dal nodo.
     *
     * @return indice iniziale della porzione di training set
     */
    int getBeginExampleIndex(){
        return beginExampleIndex;
    }
    
    /**
     * Restituisce l'indice dell'ultimo esempio coperto dal nodo.
     *
     * @return indice finale della porzione di training set
     */
    int getEndExampleIndex(){
        return endExampleIndex;
    }

    /**
     * Restituisce la SSE associata al nodo.
     *
     * @return valore della SSE del nodo
     */
    double getVariance(){
        return variance;
    }

    /**
     * Restituisce il numero di figli del nodo.
     *
     * <p>Il metodo viene implementato dalle sottoclassi, perché un nodo foglia
     * non ha figli, mentre un nodo di split può avere uno o più figli.</p>
     *
     * @return numero di figli del nodo
     */
    abstract int getNumberOfChildren();

    /**
     * Restituisce una rappresentazione testuale del nodo.
     *
     * @return stringa contenente intervallo degli esempi coperti e SSE del nodo
     */
    @Override
    public String toString(){
        return "Nodo: [Examples:"+beginExampleIndex+"-"+endExampleIndex+"] variance:"+variance;
    }
}