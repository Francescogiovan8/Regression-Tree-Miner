import data.Data;
import tree.RegressionTree;
import utility.Keyboard;

import data.TrainingDataException;
import tree.UnknownValueException;

class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args){
		Data trainingSet;

		System.out.println("Training set:");
		String fileName = Keyboard.readString();

		System.out.println("Starting data acquisition phase!");

		try {
			trainingSet = new Data(fileName);
		} catch (TrainingDataException e) {
			System.out.println(e);
			return;
		}

		System.out.println("Starting learning phase!");

		RegressionTree tree=new RegressionTree(trainingSet);
		
		tree.printRules();
		
		tree.printTree();
		
		char answer;

		do {
			System.out.println("Starting prediction phase!");

			try {
				System.out.println("Predicted class: " + tree.predictClass());
			} catch (UnknownValueException e) {
				System.out.println(e);
			}

			System.out.println("Would you repeat ? (y/n)");
			answer = Keyboard.readChar();

		} while (answer == 'y');
	}
}
