package database;

/**
 * Rappresenta una colonna di una tabella del database.
 *
 * <p>La classe memorizza il nome della colonna e il suo tipo logico.
 * Nel progetto Regression Tree Miner il tipo viene usato per distinguere
 * gli attributi numerici dagli attributi discreti durante la costruzione
 * del training set.</p>
 */
public class Column{
	/**
	 * Nome della colonna.
	 */
	private String name;

	/**
	 * Tipo logico della colonna.
	 *
	 * <p>Il valore {@code "number"} identifica una colonna numerica.</p>
	 */
	private String type;

	/**
	 * Crea una nuova colonna con nome e tipo specificati.
	 *
	 * <p>Il costruttore ha visibilità di package perché le colonne vengono
	 * create e gestite all'interno del package {@code database}.</p>
	 *
	 * @param name nome della colonna
	 * @param type tipo logico della colonna
	 */
	Column(String name,String type){
		this.name=name;
		this.type=type;
	}

	/**
	 * Restituisce il nome della colonna.
	 *
	 * @return nome della colonna
	 */
	public String getColumnName(){
		return name;
	}

	/**
	 * Indica se la colonna contiene valori numerici.
	 *
	 * @return {@code true} se il tipo della colonna è {@code "number"},
	 * {@code false} altrimenti
	 */
	public boolean isNumber(){
		return type.equals("number");
	}

	/**
	 * Restituisce una rappresentazione testuale della colonna.
	 *
	 * @return stringa nel formato {@code nome:tipo}
	 */
	public String toString(){
		return name+":"+type;
	}
}