package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.util.Set;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.List;

public class Data {

	private Object data [][];
	private int numberOfExamples;
	private List<Attribute> explanatorySet = new LinkedList<>();
	private ContinuousAttribute classAttribute;

	public Data(String fileName) throws TrainingDataException {

		File inFile = new File(fileName);

		Scanner sc;

		try {
			sc = new Scanner(inFile);
		} catch (FileNotFoundException e) {
			throw new TrainingDataException(e.toString());
		}

	    if (!sc.hasNextLine()) {
			sc.close();
			throw new TrainingDataException("Training set vuoto");
		}

		String line = sc.nextLine();

		if (!line.trim().startsWith("@schema")) {
			sc.close();
			throw new TrainingDataException("Schema mancante");
		}

	    String[] s = line.split(" ");

		//popolare explanatory Set
	    //@schema 4

		short iAttribute = 0;
		line = sc.nextLine();
		while (!line.contains("@data")) {
			s = line.trim().split("\\s+");
			if (s[0].equals("@desc")) {
				if (s.length == 3) {
					String[] discreteValues = s[2].split(",");
					Set<String> values = new TreeSet<>();
					for (String value : discreteValues) {
						values.add(value);
					}
					explanatorySet.add(new DiscreteAttribute(s[1], iAttribute, values));
				} else if (s.length == 2) {
					explanatorySet.add(new ContinuousAttribute(s[1], iAttribute));
				}
			} else if (s[0].equals("@target")) {
				classAttribute = new ContinuousAttribute(s[1], iAttribute);
			}

			iAttribute++;
			line = sc.nextLine();
		}

		if (classAttribute == null){
			sc.close();
			throw new TrainingDataException("Variabile target numerica mancante");
		}

		//avvalorare numero di esempi
	    //@data 167
	    numberOfExamples=Integer.parseInt(line.split(" ")[1]);

		if (numberOfExamples <= 0){
			sc.close();
			throw new TrainingDataException("Training set vuoto");
		}

	    //popolare data
	    data = new Object[numberOfExamples][explanatorySet.size() + 1];
	    short iRow = 0;

	    while (sc.hasNextLine()) {
	    	line = sc.nextLine();
			// acquisisco i valori degli attributi discreti e continui
			s = line.split(","); //E,E,5,4, 0.28125095
			for (short jColumn = 0; jColumn < s.length - 1; jColumn++) {
				Attribute attribute = explanatorySet.get(jColumn);
				try {
					if (attribute instanceof DiscreteAttribute) {
						data[iRow][jColumn] = s[jColumn].trim();
					} else {
						data[iRow][jColumn] = Double.parseDouble(s[jColumn].trim());
					}
				} catch (NumberFormatException e) {
					sc.close();
					throw new TrainingDataException("Gli attributi continui devono essere numerici");
				}
			}

			try {
				data[iRow][s.length - 1] = Double.parseDouble(s[s.length - 1].trim());
			} catch (NumberFormatException e) {
				sc.close();
				throw new TrainingDataException("La variabile target deve essere numerica");
			}

	    	iRow++;

	    }
		sc.close();

	}

	public int  getNumberOfExamples() {
		return numberOfExamples;
	}

	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	public Double getClassValue(int exampleIndex) {
		return (Double) data[exampleIndex][explanatorySet.size()];
	}

	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data[exampleIndex][attributeIndex];
	}

	public Attribute getExplanatoryAttribute(int index) {
		return explanatorySet.get(index);
	}

	public ContinuousAttribute getClassAttribute() {
		return classAttribute;
	}

	@Override
	public String toString() {
		String value = "";
		for (int i = 0; i < numberOfExamples; i++) {
			for (int j = 0; j < explanatorySet.size(); j++) {
				value += data[i][j] + ",";
			}

			value += data[i][explanatorySet.size()] + "\n";
		}
		return value;

	}


	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
		quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	// scambio esempio i con esempi oj
	private void swap(int i,int j) {
		Object temp;
		for (int k = 0; k < getNumberOfExplanatoryAttributes() + 1; k++) {
			temp = data[i][k];
			data[i][k] = data[j][k];
			data[j][k] = temp;
		}

	}

	/*
	 * Partiziona il vettore rispetto all'elemento x e restiutisce il punto di separazione
	 */
	private  int partition(DiscreteAttribute attribute, int inf, int sup) {
		int i;
		int j;

		i = inf;
		j = sup;
		int	med = (inf + sup) / 2;
		String x = (String) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);

		while (true) {
			while (i <= sup && ((String) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
				i++;
			}

			while (((String) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
				j--;
			}

			if (i < j) {
				swap(i, j);
			} else {
				break;
			}

		}

		swap(inf, j);
		return j;

	}

	private int partition(ContinuousAttribute attribute, int inf, int sup) {
		int i = inf;
		int j = sup;
		int med = (inf + sup) / 2;

		Double x = (Double) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);
		while (true) {
			while (i <= sup && ((Double) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
				i++;
			}

			while (((Double) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
				j--;
			}

			if (i < j) {
				swap(i, j);
			} else {
				break;
			}
		}

		swap(inf, j);
		return j;
	}

	/*
	 * Algoritmo quicksort per l'ordinamento di un array di interi A
	 * usando come relazione d'ordine totale "<="
	 * @param A
	 */
	private void quicksort(Attribute attribute, int inf, int sup) {
		if (sup >= inf) {
			int pos;

			if (attribute instanceof DiscreteAttribute) {
				pos = partition((DiscreteAttribute) attribute, inf, sup);
			} else {
				pos = partition((ContinuousAttribute) attribute, inf, sup);
			}

			if ((pos - inf) < (sup - pos + 1)) {
				quicksort(attribute, inf, pos - 1);
				quicksort(attribute, pos + 1, sup);
			}
			else
			{
				quicksort(attribute, pos + 1, sup);
				quicksort(attribute, inf, pos - 1);
			}

		}

	}

	public static void main(String args[]) throws TrainingDataException {
		Data trainingSet=new Data("servo.dat");
		System.out.println(trainingSet);
		for (int jColumn = 0; jColumn < trainingSet.getNumberOfExplanatoryAttributes(); jColumn++) {
			System.out.println("ORDER BY "+trainingSet.getExplanatoryAttribute(jColumn));
			trainingSet.quicksort(trainingSet.getExplanatoryAttribute(jColumn),0 , trainingSet.getNumberOfExamples()-1);
			System.out.println(trainingSet);
		}

	}

}
