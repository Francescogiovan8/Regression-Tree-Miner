import data.Data;
import tree.RegressionTree;

import data.TrainingDataException;

class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws TrainingDataException{
		Data trainingSet= new Data("servo.dat");
		
		RegressionTree tree=new RegressionTree(trainingSet);
		
		tree.printRules();
		
		tree.printTree();
		
	}

}
