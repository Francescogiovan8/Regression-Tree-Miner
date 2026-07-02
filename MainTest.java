import data.Data;
import tree.RegressionTree;

import data.TrainingDataException;
import tree.UnknownValueException;

class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args){
		Data trainingSet;

		try{
			trainingSet= new Data("servo.dat");
		} catch (TrainingDataException e) {
			System.out.println(e);
			return;
		}

		RegressionTree tree=new RegressionTree(trainingSet);
		
		tree.printRules();
		
		tree.printTree();
		
		try {
			System.out.println("Predicted class: " + tree.predictClass());
		} catch (UnknownValueException e) {
			System.out.println(e);
		}
	}
}
