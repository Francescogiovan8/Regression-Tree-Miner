package database;

public class EmptySetException extends Exception {

	public EmptySetException() {
	}

    public EmptySetException(String message){
        super(message);
    }
}