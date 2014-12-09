import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class InvestTransaction extends Transaction {

	public InvestTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		// get relevant data here from user
		
		Statement statement = null;
		ResultSet resultSet = null;
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		boolean flag = false;
		float depositAmount = 0.0f;
		String deposit = "";

		//Ask for deposit amount
		while(!flag){
			System.out.println("\nHow much would you like to deposit for automatic investing? (Format: 123.45)");
			try {
				deposit = br.readLine().trim();
				depositAmount = Float.parseFloat(deposit);
				flag = true;
			}
			catch ( IOException e ) {
				System.out.println( "IOException: " + e.toString() );
			}
			catch ( NumberFormatException e ) {
				System.out.println( "Parsing error: " + e.toString() );
			}
		}

		//Perform balance query
		try {
			statement = connection.createStatement();

			String query = "UPDATE CUSTOMER SET balance = (balance + "+depositAmount+") WHERE login='"+BetterFutures.getCurrentUser()+"'";

			statement.executeQuery( query );

			System.out.println("Great! Those funds were invested according to your allocation preferences!");

		}
		catch ( SQLException e ) {
			System.out.println( "Error updating customer balance. Machine error: " + e.toString() );
		}
		finally {
			try {
				if (statement != null) statement.close();
			} catch (SQLException e) {
				System.out.println( "Cannot close Statement. Machine error: " + e.toString() );
			}
		}
		
	}

}