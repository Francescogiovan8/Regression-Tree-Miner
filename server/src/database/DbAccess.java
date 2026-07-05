package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbAccess {

	private final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

	private final String DBMS = "jdbc:mysql";
	private String SERVER = "localhost";
	private String DATABASE = "MapDB";
	private final int PORT = 3306;
	private String USER_ID = "MapUser";
	private String PASSWORD = "map";

	private Connection conn;

	public void initConnection() throws DatabaseConnectionException {

		try {
			Class.forName(DRIVER_CLASS_NAME);
		} catch (ClassNotFoundException e) {
			System.out.println("[!] Driver not found: " + e.getMessage());
			throw new DatabaseConnectionException(e.toString());
		}

		String connectionString = DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE + "?serverTimezone=UTC";

		try {
			conn = DriverManager.getConnection(connectionString, USER_ID, PASSWORD);
		} catch (SQLException e) {
			System.out.println("[!] SQLException: " + e.getMessage());
			System.out.println("[!] SQLState: " + e.getSQLState());
			System.out.println("[!] VendorError: " + e.getErrorCode());
			throw new DatabaseConnectionException(e.toString());
		}
	}

	public Connection getConnection() {
		return conn;
	}

	public void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.out.println("[!] Error closing connection: " + e.getMessage());
			}
		}
	}
}