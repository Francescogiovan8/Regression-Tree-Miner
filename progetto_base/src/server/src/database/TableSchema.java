package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Rappresenta lo schema di una tabella del database.
 *
 * <p>La classe legge i metadati della tabella tramite JDBC e costruisce una lista
 * di oggetti {@link Column}, ognuno dei quali rappresenta una colonna della
 * tabella con il relativo tipo logico.</p>
 *
 * <p>I tipi SQL vengono convertiti in tipi logici usati dal progetto:
 * {@code "string"} per attributi discreti e {@code "number"} per attributi
 * numerici.</p>
 */
public class TableSchema implements Iterable<Column>{
	
	/**
	 * Lista delle colonne che compongono lo schema della tabella.
	 */
	private List<Column> tableSchema=new ArrayList<Column>();
	
	/**
	 * Costruisce lo schema della tabella indicata.
	 *
	 * <p>Il costruttore usa i metadati della connessione al database per recuperare
	 * le colonne della tabella e convertirne i tipi SQL nei tipi logici utilizzati
	 * dal progetto Regression Tree Miner.</p>
	 *
	 * @param db oggetto per l'accesso al database
	 * @param tableName nome della tabella di cui leggere lo schema
	 * @throws SQLException se si verifica un errore durante la lettura dei metadati
	 * della tabella
	 */
	public TableSchema(DbAccess db, String tableName) throws SQLException{
		
		HashMap<String,String> mapSQL_JAVATypes=new HashMap<String, String>();
	
		mapSQL_JAVATypes.put("CHAR","string");
		mapSQL_JAVATypes.put("VARCHAR","string");
		mapSQL_JAVATypes.put("LONGVARCHAR","string");
		mapSQL_JAVATypes.put("BIT","string");
		mapSQL_JAVATypes.put("SHORT","number");
		mapSQL_JAVATypes.put("INT","number");
		mapSQL_JAVATypes.put("LONG","number");
		mapSQL_JAVATypes.put("FLOAT","number");
		mapSQL_JAVATypes.put("DOUBLE","number");
		
		Connection con=db.getConnection();
		DatabaseMetaData meta = con.getMetaData();
		ResultSet res = meta.getColumns(null, null, tableName, null);
		   
		while (res.next()) {
			if(mapSQL_JAVATypes.containsKey(res.getString("TYPE_NAME")))
				tableSchema.add(new Column(
					res.getString("COLUMN_NAME"),
					mapSQL_JAVATypes.get(res.getString("TYPE_NAME")))
				);
		}
		res.close();
	}
	  
	/**
	 * Restituisce il numero di attributi, cioè il numero di colonne riconosciute
	 * nello schema della tabella.
	 *
	 * @return numero di colonne dello schema
	 */
	public int getNumberOfAttributes(){
		return tableSchema.size();
	}
		
	/**
	 * Restituisce la colonna nella posizione indicata.
	 *
	 * @param index indice della colonna
	 * @return colonna corrispondente all'indice specificato
	 */
	public Column getColumn(int index){
		return tableSchema.get(index);
	}

	/**
	 * Restituisce un iteratore sulle colonne dello schema.
	 *
	 * @return iteratore sulle colonne della tabella
	 */
	@Override
	public Iterator<Column> iterator() {
		return tableSchema.iterator();
	}
}