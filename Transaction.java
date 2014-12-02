/*
 * 	A simple Transaction class which all transaction must extend.
 */

import java.sql.Connection;
 
public class Transaction {

	/*
	 *		Inheriting classes can call super( Connection var ) in constructor
	 */
	public Transaction( Connection connection ) {
		success = false;
		results = "";
		this.connection = connection;
	}
	
	public Transaction() {
		success = false;
		results = "";
	}
	
	public boolean isSuccessful() {
		return success;
	}
	
	public String toString() {
		return results;
	}

	/*
	 *		Inheriting classes must set success inside execute
	 */
	public void execute() {}
	
	protected Connection connection;
	protected String results;
	protected boolean success;
}