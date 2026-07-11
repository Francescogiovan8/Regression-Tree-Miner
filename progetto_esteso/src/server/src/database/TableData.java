package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.LinkedList;
import java.util.List;

import java.util.Set;
import java.util.TreeSet;

/**
 * Gestisce il recupero dei dati contenuti nelle tabelle del database.
 *
 * <p>La classe fornisce metodi per leggere le tuple di una tabella e per
 * recuperare i valori distinti di una colonna. I dati letti dal database
 * vengono convertiti in oggetti {@link Example}, usati successivamente dalla
 * classe {@code data.Data} per costruire il training set.</p>
 */
public class TableData {

	/**
	 * Tipo di query aggregata.
	 *
	 * <p>L'enumerazione rappresenta possibili operazioni di minimo e massimo.</p>
	 */
	public enum QUERY_TYPE {
		/**
		 * Query per il valore minimo.
		 */
		MIN,

		/**
		 * Query per il valore massimo.
		 */
		MAX
	}

	/**
	 * Oggetto usato per accedere alla connessione al database.
	 */
	private DbAccess db;
	
	/**
	 * Crea un oggetto per il recupero dei dati dal database.
	 *
	 * @param db oggetto che gestisce la connessione al database
	 */
	public TableData(DbAccess db) {
		this.db=db;
	}

	/**
	 * Recupera tutte le tuple presenti nella tabella indicata.
	 *
	 * <p>Il metodo costruisce una query {@code SELECT} usando lo schema della
	 * tabella, legge tutte le righe restituite dal database e crea un oggetto
	 * {@link Example} per ogni tupla. I valori numerici vengono letti come
	 * {@code Double}, mentre gli altri valori vengono letti come {@code String}.</p>
	 *
	 * @param table nome della tabella da cui leggere le tuple
	 * @return lista di esempi corrispondenti alle righe della tabella
	 * @throws SQLException se si verifica un errore SQL durante la lettura
	 * @throws EmptySetException se la tabella non contiene tuple
	 */
	public List<Example> getTransazioni(String table) throws SQLException, EmptySetException{
		LinkedList<Example> transSet = new LinkedList<Example>();
		Statement statement;
		TableSchema tSchema=new TableSchema(db,table);
		
		
		String query="select ";
		
		for(int i=0;i<tSchema.getNumberOfAttributes();i++){
			Column c=tSchema.getColumn(i);
			if(i>0)
				query+=",";
			query += c.getColumnName();
		}
		if(tSchema.getNumberOfAttributes()==0)
			throw new SQLException();
		query += (" FROM "+table);
		
		statement = db.getConnection().createStatement();
		ResultSet rs = statement.executeQuery(query);
		boolean empty=true;
		while (rs.next()) {
			empty=false;
			Example currentTuple=new Example();
			for(int i=0;i<tSchema.getNumberOfAttributes();i++)
				if(tSchema.getColumn(i).isNumber())
					currentTuple.add(rs.getDouble(i+1));
				else
					currentTuple.add(rs.getString(i+1));
			transSet.add(currentTuple);
		}
		rs.close();
		statement.close();
		if(empty) throw new EmptySetException();
		
		
		return transSet;

	}

	/**
	 * Recupera i valori distinti di una colonna di una tabella.
	 *
	 * <p>Il metodo esegue una query {@code SELECT DISTINCT} ordinata sul nome della
	 * colonna. I valori numerici vengono inseriti nell'insieme come {@code Double},
	 * mentre i valori non numerici vengono inseriti come {@code String}.</p>
	 *
	 * @param table nome della tabella da interrogare
	 * @param column colonna di cui recuperare i valori distinti
	 * @return insieme ordinato dei valori distinti della colonna
	 * @throws SQLException se si verifica un errore SQL durante l'esecuzione della query
	 */
	public Set<Object> getDistinctColumnValues(String table, Column column) throws SQLException {

		Set<Object> distinctValues = new TreeSet<>();

		String query = "SELECT DISTINCT " + column.getColumnName() + " FROM " + table + " ORDER BY " + column.getColumnName();

		Statement statement = db.getConnection().createStatement();

		ResultSet resultSet = statement.executeQuery(query);

		while (resultSet.next()) {
			if (column.isNumber()) {
				distinctValues.add(resultSet.getDouble(1));
			} else {
				distinctValues.add(resultSet.getString(1));
			}
		}

		resultSet.close();
		statement.close();

		return distinctValues;
	}

}