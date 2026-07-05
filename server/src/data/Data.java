package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import database.Column;
import database.DatabaseConnectionException;
import database.DbAccess;
import database.EmptySetException;
import database.Example;
import database.TableData;
import database.TableSchema;

public class Data {

	private List<Example> data = new ArrayList<Example>();
	private int numberOfExamples;
	private List<Attribute> explanatorySet = new LinkedList<>();
	private ContinuousAttribute classAttribute;

	public Data(String tableName) throws TrainingDataException {
		DbAccess db = new DbAccess();

		try {
			db.initConnection();

			TableSchema tableSchema = new TableSchema(db, tableName);

			int numberOfAttributes = tableSchema.getNumberOfAttributes();

			if (numberOfAttributes == 0) {
				throw new TrainingDataException("La tabella " + tableName + " non esiste");
			}

			if (numberOfAttributes < 2) {
				throw new TrainingDataException("La tabella deve contenere almeno due colonne");
			}

			TableData tableData = new TableData(db);

			for (int i = 0; i < numberOfAttributes - 1; i++) {
				Column column = tableSchema.getColumn(i);

				if (column.isNumber()) {
					explanatorySet.add(new ContinuousAttribute(column.getColumnName(), i));
				} else {
					Set<Object> distinctValues = tableData.getDistinctColumnValues(tableName, column);

					Set<String> values = new TreeSet<>();

					for (Object value : distinctValues) {
						values.add((String) value);
					}

					explanatorySet.add(new DiscreteAttribute(column.getColumnName(), i, values));
				}
			}

			Column targetColumn = tableSchema.getColumn(numberOfAttributes - 1);

			if (!targetColumn.isNumber()) {
				throw new TrainingDataException("La variabile target deve essere numerica");
			}

			classAttribute = new ContinuousAttribute(targetColumn.getColumnName(), numberOfAttributes - 1);

			data.addAll(tableData.getTransazioni(tableName));

			numberOfExamples = data.size();

		} catch (DatabaseConnectionException e) {
			throw new TrainingDataException(e.toString());
		} catch (SQLException e) {
			throw new TrainingDataException(e.toString());
		} catch (EmptySetException e) {
			throw new TrainingDataException(e.toString());
		} finally {
			db.closeConnection();
		}
	}

	public int  getNumberOfExamples() {
		return numberOfExamples;
	}

	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	public Double getClassValue(int exampleIndex) {
		return (Double) data.get(exampleIndex).get(explanatorySet.size());
	}

	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data.get(exampleIndex).get(attributeIndex);
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
				value += data.get(i).get(j) + ",";
			}

			value += data.get(i).get(explanatorySet.size()) + "\n";
		}
		return value;

	}


	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
		quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	// scambio esempio i con esempi oj
	private void swap(int i, int j) {
		Example temp = data.get(i);
		data.set(i, data.get(j));
		data.set(j, temp);
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
		Data trainingSet=new Data("provaC");
		System.out.println(trainingSet);
		for (int jColumn = 0; jColumn < trainingSet.getNumberOfExplanatoryAttributes(); jColumn++) {
			System.out.println("ORDER BY "+trainingSet.getExplanatoryAttribute(jColumn));
			trainingSet.quicksort(trainingSet.getExplanatoryAttribute(jColumn),0 , trainingSet.getNumberOfExamples()-1);
			System.out.println(trainingSet);
		}

	}

}
