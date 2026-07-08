package tree;

import server.UnknownValueException;

import data.Data;
import data.Attribute;
import data.ContinuousAttribute;
import data.DiscreteAttribute;

import java.util.TreeSet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Modella un albero di regressione.
 *
 * <p>La classe implementa la costruzione ricorsiva dell'albero a partire da un
 * training set, la stampa dell'albero, la stampa delle regole, la predizione
 * guidata e la serializzazione su file.</p>
 *
 * <p>L'albero può essere composto da nodi foglia, rappresentati da
 * {@link LeafNode}, e da nodi di split, rappresentati da sottoclassi di
 * {@link SplitNode}. Il server usa questa classe per apprendere un modello,
 * salvarlo, caricarlo e usarlo per effettuare predizioni.</p>
 */
public class RegressionTree implements Serializable{
	/**
	 * Radice dell'albero di regressione.
	 */
	private Node root;

	/**
	 * Sottoalberi figli associati ai rami della radice, se la radice è un nodo di split.
	 */
	private RegressionTree[] childTree;

	/**
	 * Crea un albero di regressione vuoto.
	 *
	 * <p>Il costruttore ha visibilità di package perché viene usato internamente
	 * durante la costruzione ricorsiva dei sottoalberi.</p>
	 */
	RegressionTree(){}

	/**
	 * Crea e apprende un albero di regressione a partire dal training set specificato.
	 *
	 * <p>Il numero minimo di esempi per foglia viene fissato al 10% del numero
	 * totale di esempi del training set.</p>
	 *
	 * @param trainingSet training set usato per apprendere l'albero
	 */
	public RegressionTree(Data trainingSet){
		learnTree(trainingSet,0,trainingSet.getNumberOfExamples()-1,trainingSet.getNumberOfExamples()*10/100);
	}
		
	/**
	 * Verifica se una porzione del training set deve diventare una foglia.
	 *
	 * @param trainingSet training set usato per costruire l'albero
	 * @param begin indice iniziale della porzione di training set
	 * @param end indice finale della porzione di training set
	 * @param numberOfExamplesPerLeaf numero massimo di esempi ammesso per creare una foglia
	 * @return {@code true} se la porzione deve diventare una foglia, {@code false} altrimenti
	 */
	private boolean isLeaf(Data trainingSet,int begin, int end,int numberOfExamplesPerLeaf){
		int numberOfExamples = end - begin + 1;
    	return numberOfExamples <= numberOfExamplesPerLeaf;
	}

	/**
	 * Determina il miglior nodo di split per una porzione del training set.
	 *
	 * <p>Il metodo crea un possibile nodo di split per ogni attributo esplicativo,
	 * confronta gli split prodotti tramite la loro varianza/SSE e seleziona quello
	 * migliore, cioè quello con valore minore.</p>
	 *
	 * @param trainingSet training set usato per valutare gli split
	 * @param begin indice iniziale della porzione di training set
	 * @param end indice finale della porzione di training set
	 * @return miglior nodo di split individuato
	 */
	private SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
		TreeSet<SplitNode> splitNodes = new TreeSet<>();

		for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
			Attribute attribute = trainingSet.getExplanatoryAttribute(i);
			SplitNode currentSplitNode;

			if (attribute instanceof DiscreteAttribute) {
				currentSplitNode = new DiscreteNode(trainingSet, begin, end, (DiscreteAttribute) attribute);
			} else {
				currentSplitNode = new ContinuousNode(trainingSet, begin, end, (ContinuousAttribute) attribute);
			}
			splitNodes.add(currentSplitNode);
		}

		SplitNode bestSplitNode = splitNodes.first();
		trainingSet.sort(bestSplitNode.getAttribute(), begin, end);
		return bestSplitNode;
	}

	/**
	 * Costruisce ricorsivamente l'albero di regressione.
	 *
	 * <p>Se la porzione corrente del training set soddisfa la condizione di foglia,
	 * viene creato un {@link LeafNode}. Altrimenti viene scelto il miglior
	 * {@link SplitNode} e vengono costruiti ricorsivamente i sottoalberi figli.</p>
	 *
	 * @param trainingSet training set usato per apprendere l'albero
	 * @param begin indice iniziale della porzione corrente
	 * @param end indice finale della porzione corrente
	 * @param numberOfExamplesPerLeaf numero massimo di esempi ammesso per creare una foglia
	 */
	private void learnTree(Data trainingSet,int begin, int end,int numberOfExamplesPerLeaf){
		if( isLeaf(trainingSet, begin, end, numberOfExamplesPerLeaf)){
			//determina la classe che compare più frequentemente nella partizione corrente
			root=new LeafNode(trainingSet,begin,end);
		}
		else //split node
		{
			root=determineBestSplitNode(trainingSet, begin, end);
			
			if(root.getNumberOfChildren()>1){
				childTree=new RegressionTree[root.getNumberOfChildren()];
				for(int i=0;i<root.getNumberOfChildren();i++){
					childTree[i]=new RegressionTree();
					childTree[i].learnTree(trainingSet, ((SplitNode)root).getSplitInfo(i).getBeginindex(), ((SplitNode)root).getSplitInfo(i).getEndIndex(), numberOfExamplesPerLeaf);
				}
			}
			else
				root=new LeafNode(trainingSet,begin,end);
				
		}
	}

	/**
	 * Stampa su standard output la rappresentazione testuale dell'albero.
	 */
	public void printTree(){
		System.out.println("********* TREE **********\n");
		System.out.println(toString());
		System.out.println("*************************\n");
	}
		
	/**
	 * Restituisce una rappresentazione testuale dell'albero di regressione.
	 *
	 * @return stringa che rappresenta l'albero e i suoi sottoalberi
	 */
	@Override
	public String toString(){
		String tree=root.toString()+"\n";
			
		if( root instanceof LeafNode){
			
		}
		else //split node
		{
			for(int i=0;i<childTree.length;i++)
				tree +=childTree[i];
		}
		return tree;
	}

	/**
	 * Stampa su standard output le regole estratte dall'albero di regressione.
	 */
	private void printRules(){
    	System.out.println("********* RULES **********");
    	printRules("");
		System.out.println("*************************\n");
    }

	/**
	 * Stampa ricorsivamente le regole dell'albero.
	 *
	 * <p>Il parametro {@code current} contiene la condizione costruita lungo il
	 * percorso dalla radice fino al nodo corrente.</p>
	 *
	 * @param current condizione corrente della regola in costruzione
	 */
    private void printRules(String current){
		if (root instanceof LeafNode) {
			Double predictedClassValue=((LeafNode) root).getPredictedClassValue();
			if (current.isEmpty()){
				System.out.println("Class=" + predictedClassValue);
			}
			else{
				System.out.println(current + " ==> Class=" + predictedClassValue);
			}
		} 
		else{

			SplitNode splitNode=(SplitNode) root;

			for (int i = 0; i < splitNode.getNumberOfChildren(); i++) {

				String condition = splitNode.getAttribute().getName() + splitNode.getSplitInfo(i).getComparator() + splitNode.getSplitInfo(i).getSplitValue();
				String newCurrent;

				if (current.isEmpty()){
					newCurrent = condition;
				} 
				else{
					newCurrent =current + " AND " + condition;
				}
				childTree[i].printRules(newCurrent);
			}
    	}
    }

	/**
	 * Esegue una predizione guidata usando l'albero di regressione.
	 *
	 * <p>Se la radice è una foglia, viene restituito direttamente il valore
	 * predetto. Se la radice è un nodo di split, il metodo invia al client una
	 * query contenente i rami disponibili, legge la risposta dell'utente e prosegue
	 * ricorsivamente nel sottoalbero selezionato.</p>
	 *
	 * @param in stream di input da cui leggere la scelta del client
	 * @param out stream di output su cui inviare le query al client
	 * @return valore numerico predetto dall'albero
	 * @throws UnknownValueException se la scelta ricevuta non corrisponde a un ramo valido
	 * @throws IOException se si verifica un errore di comunicazione sugli stream
	 * @throws ClassNotFoundException se l'oggetto ricevuto dallo stream non può essere deserializzato
	 */
	public Double predictClass(ObjectInputStream in, ObjectOutputStream out) throws UnknownValueException, IOException, ClassNotFoundException {
		if (root instanceof LeafNode) {
			return ((LeafNode) root).getPredictedClassValue();
		}

		out.writeObject("QUERY");
		out.writeObject(((SplitNode) root).formulateQuery());
		out.flush();

		int answer = (Integer) in.readObject();

		if (answer < 0 || answer >= root.getNumberOfChildren()) {
			throw new UnknownValueException("La risposta deve essere un numero intero compreso tra 0 e " + (root.getNumberOfChildren() - 1) + "!");
		}

		return childTree[answer].predictClass(in, out);
	}

	/**
	 * Salva l'albero di regressione in un file tramite serializzazione.
	 *
	 * @param nomeFile nome del file su cui salvare l'albero
	 * @throws FileNotFoundException se il file non può essere creato o aperto
	 * @throws IOException se si verifica un errore durante la scrittura sul file
	 */
	public void salva(String nomeFile) throws FileNotFoundException, IOException {
		FileOutputStream fileOut = new FileOutputStream(nomeFile);
		ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

		objectOut.writeObject(this);

		objectOut.close();
	}

	/**
	 * Carica un albero di regressione da un file serializzato.
	 *
	 * @param nomeFile nome del file da cui caricare l'albero
	 * @return albero di regressione caricato dal file
	 * @throws FileNotFoundException se il file non viene trovato
	 * @throws IOException se si verifica un errore durante la lettura del file
	 * @throws ClassNotFoundException se l'oggetto letto dal file non può essere deserializzato
	 */
	public static RegressionTree carica(String nomeFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(nomeFile);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);

		RegressionTree tree = (RegressionTree) objectIn.readObject();
		objectIn.close();

		return tree;
	}
}