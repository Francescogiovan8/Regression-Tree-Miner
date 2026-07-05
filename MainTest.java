import java.io.IOException;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;
import tree.UnknownValueException;
import utility.Keyboard;

public class MainTest {

	public static void main(String[] args) {
		int decision = 0;

		do {
			System.out.println("Learn Regression Tree from data [1]");
			System.out.println("Load Regression Tree from archive [2]");
			decision = Keyboard.readInt();
		} while (decision != 1 && decision != 2);

		RegressionTree tree = null;

		if (decision == 1) {
			System.out.println("Table name:");
			String tableName = Keyboard.readString();

			System.out.println("Starting data acquisition phase!");

			Data trainingSet;

			try {
				trainingSet = new Data(tableName);
			} catch (TrainingDataException e) {
				System.out.println(e);
				return;
			}

			System.out.println("Starting learning phase!");
			tree = new RegressionTree(trainingSet);

			try {
				tree.salva(tableName + ".dmp");
			} catch (IOException e) {
				System.out.println(e);
			}
		} else {
			System.out.println("File name:");
			String fileName = Keyboard.readString();

			try {
				tree = RegressionTree.carica(fileName + ".dmp");
			} catch (ClassNotFoundException | IOException e) {
				System.out.println(e);
				return;
			}
		}

		tree.printRules();

		char answer = 'y';

		do {
			System.out.println("Starting prediction phase!");

			try {
				System.out.println(tree.predictClass());
			} catch (UnknownValueException e) {
				System.out.println(e);
			}

			System.out.println("Would you repeat ? (y/n)");
			answer = Keyboard.readChar();
		} while (Character.toUpperCase(answer) == 'Y');
	}
}