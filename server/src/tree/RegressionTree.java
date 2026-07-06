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

public class RegressionTree implements Serializable{
	private Node root;
	private RegressionTree[] childTree;

	RegressionTree(){}

	public RegressionTree(Data trainingSet){
			
		learnTree(trainingSet,0,trainingSet.getNumberOfExamples()-1,trainingSet.getNumberOfExamples()*10/100);
	}
		
	boolean isLeaf(Data trainingSet,int begin, int end,int numberOfExamplesPerLeaf){
		int numberOfExamples = end - begin + 1;
    	return numberOfExamples <= numberOfExamplesPerLeaf;
	}

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

	void learnTree(Data trainingSet,int begin, int end,int numberOfExamplesPerLeaf){
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
					childTree[i].learnTree(trainingSet, ((SplitNode)root).getSplitInfo(i).beginIndex, ((SplitNode)root).getSplitInfo(i).endIndex, numberOfExamplesPerLeaf);
				}
			}
			else
				root=new LeafNode(trainingSet,begin,end);
				
		}
	}

	public void printTree(){
		System.out.println("********* TREE **********\n");
		System.out.println(toString());
		System.out.println("*************************\n");
	}
		
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

	public void printRules(){
    	System.out.println("********* RULES **********");
    	printRules("");
		System.out.println("*************************\n");
    }

    void printRules(String current){
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

	public void salva(String nomeFile) throws FileNotFoundException, IOException {
		FileOutputStream fileOut = new FileOutputStream(nomeFile);
		ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

		objectOut.writeObject(this);

		objectOut.close();
	}

	public static RegressionTree carica(String nomeFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(nomeFile);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);

		RegressionTree tree = (RegressionTree) objectIn.readObject();
		objectIn.close();

		return tree;
	}
}
		
