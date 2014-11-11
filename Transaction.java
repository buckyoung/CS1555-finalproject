/*
 * 	A simple Transaction class which all transaction must extend.
 */

public class Transaction {

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
	
	private String results;
	private boolean success;
}