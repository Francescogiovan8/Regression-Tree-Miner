package data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import database.Column;
import database.DatabaseConnectionException;
import database.DbAccess;
import database.EmptySetException;
import database.Example;
import database.TableData;
import database.TableSchema;

/**
 * Rappresenta il training set usato per costruire l'albero di regressione.
 *
 * <p>La classe carica i dati da una tabella del database, costruisce l'insieme
 * degli attributi esplicativi e individua l'attributo di classe, cioè la variabile
 * target continua da predire.</p>
 *
 * <p>Gli esempi vengono memorizzati come lista di oggetti {@link Example}. Gli
 * attributi esplicativi possono essere discreti o continui, mentre l'attributo
 * target deve essere continuo.</p>
 */
public class Data {

	/**
	 * Lista degli esempi del training set.
	 */
	private List<Example> data = new ArrayList<Example>();

	/**
	 * Numero di esempi presenti nel training set.
	 */
	private int numberOfExamples;

	/**
	 * Insieme degli attributi esplicativi del training set.
	 */
	private List<Attribute> explanatorySet = new LinkedList<>();

	/**
	 * Attributo di classe continuo, cioè la variabile target da predire.
	 */
	private ContinuousAttribute classAttribute;

	/**
	 * Crea un training set caricando i dati dalla tabella indicata.
	 *
	 * <p>Il costruttore apre una connessione al database, legge lo schema della
	 * tabella, costruisce gli attributi esplicativi e carica gli esempi. L'ultima
	 * colonna della tabella viene considerata come variabile target e deve essere
	 * numerica.</p>
	 *
	 * @param tableName nome della tabella del database da cui caricare il training set
	 * @throws TrainingDataException se la tabella non esiste, se la struttura non è valida
	 * oppure se si verifica un errore durante il caricamento dei dati
	 */
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

	/**
	 * Restituisce il numero di esempi presenti nel training set.
	 *
	 * @return numero di esempi
	 */
	public int  getNumberOfExamples() {
		return numberOfExamples;
	}

	/**
	 * Restituisce il numero di attributi esplicativi.
	 *
	 * @return numero di attributi esplicativi
	 */
	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	/**
	 * Restituisce il valore della variabile target per l'esempio indicato.
	 *
	 * @param exampleIndex indice dell'esempio
	 * @return valore della classe dell'esempio
	 */
	public Double getClassValue(int exampleIndex) {
		return (Double) data.get(exampleIndex).get(explanatorySet.size());
	}

	/**
	 * Restituisce il valore di un attributo esplicativo per un esempio specifico.
	 *
	 * @param exampleIndex indice dell'esempio
	 * @param attributeIndex indice dell'attributo esplicativo
	 * @return valore dell'attributo esplicativo per l'esempio indicato
	 */
	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data.get(exampleIndex).get(attributeIndex);
	}

	/**
	 * Restituisce un attributo esplicativo dato il suo indice.
	 *
	 * @param index indice dell'attributo esplicativo
	 * @return attributo esplicativo corrispondente
	 */
	public Attribute getExplanatoryAttribute(int index) {
		return explanatorySet.get(index);
	}

	/**
	 * Restituisce l'attributo di classe del training set.
	 *
	 * @return attributo target continuo
	 */
	public ContinuousAttribute getClassAttribute() {
		return classAttribute;
	}

	/**
	 * Restituisce una rappresentazione testuale del training set.
	 *
	 * <p>Ogni riga contiene i valori degli attributi esplicativi seguiti dal valore
	 * della classe.</p>
	 *
	 * @return stringa che rappresenta il training set
	 */
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

	/**
	 * Ordina una porzione del training set rispetto all'attributo specificato.
	 *
	 * <p>Il metodo richiama l'algoritmo quicksort sulla porzione compresa tra
	 * {@code beginExampleIndex} ed {@code endExampleIndex}.</p>
	 *
	 * @param attribute attributo rispetto al quale ordinare gli esempi
	 * @param beginExampleIndex indice iniziale della porzione da ordinare
	 * @param endExampleIndex indice finale della porzione da ordinare
	 */
	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
		quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	/**
	 * Scambia due esempi del training set.
	 *
	 * @param i indice del primo esempio
	 * @param j indice del secondo esempio
	 */
	private void swap(int i, int j) {
		Example temp = data.get(i);
		data.set(i, data.get(j));
		data.set(j, temp);
	}

	/**
	 * Partiziona una porzione del training set rispetto a un attributo discreto.
	 *
	 * <p>Il metodo viene usato dall'algoritmo quicksort per ordinare gli esempi
	 * in base ai valori testuali dell'attributo discreto.</p>
	 *
	 * @param attribute attributo discreto rispetto al quale partizionare
	 * @param inf indice inferiore della porzione da partizionare
	 * @param sup indice superiore della porzione da partizionare
	 * @return posizione finale del pivot
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

	/**
	 * Partiziona una porzione del training set rispetto a un attributo continuo.
	 *
	 * <p>Il metodo viene usato dall'algoritmo quicksort per ordinare gli esempi
	 * in base ai valori numerici dell'attributo continuo.</p>
	 *
	 * @param attribute attributo continuo rispetto al quale partizionare
	 * @param inf indice inferiore della porzione da partizionare
	 * @param sup indice superiore della porzione da partizionare
	 * @return posizione finale del pivot
	 */
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

	/**
	 * Ordina una porzione del training set usando l'algoritmo quicksort.
	 *
	 * <p>Il metodo sceglie automaticamente la partizione corretta in base al tipo
	 * dell'attributo: discreto o continuo.</p>
	 *
	 * @param attribute attributo rispetto al quale ordinare gli esempi
	 * @param inf indice inferiore della porzione da ordinare
	 * @param sup indice superiore della porzione da ordinare
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

	/**
	 * Metodo di test per verificare il caricamento e l'ordinamento del training set.
	 *
	 * <p>Il metodo carica la tabella {@code provaC}, stampa il training set e poi
	 * ordina i dati rispetto a ciascun attributo esplicativo.</p>
	 *
	 * @param args argomenti passati da linea di comando, non utilizzati
	 * @throws TrainingDataException se si verifica un errore durante il caricamento
	 * del training set
	 */
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