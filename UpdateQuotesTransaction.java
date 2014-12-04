import java.sql.*;
import java.text.ParseException;
import java.lang.Integer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UpdateQuotesTransaction extends Transaction {

	public UpdateQuotesTransaction( Connection connection ) {
		super( connection );
	}

	
	public void execute() {
		
		// get relevant data here from user
		
		Statement statement = null;
		ResultSet resultSet = null;

		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		String symbol = "";
		String date = "";
		String price = "";
		
		try {
			System.out.print( "Please enter the symbol of the mutual fund to be changed: " );
			symbol = br.readLine().trim();
			
			try {
				statement = connection.createStatement();
				String query = "SELECT MAX(c_date) FROM mutualdate";
				resultSet = statement.executeQuery( query );
				resultSet.next();
				System.out.print( "\nPlease select the date (in DD-Mon-YYYY format) that you wish to change the price for (today's mutual date is: " + resultSet.getString(0) + "): " );
				date = br.readLine().trim();
			}
			catch ( SQLException e ) {
				System.out.println( "Error validating user. Machine error: " + e.toString() );
			}
			finally {
				try {
					if (statement != null) statement.close();
				} catch (SQLException e) {
					System.out.println( "Cannot close Statement. Machine error: " + e.toString() );
				}
			}
			
			System.out.print( "\nPlease enter the new price" );
			price = br.readLine().trim();
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
		}
		
		int priceValue = 0;
		
		try {
			priceValue = Integer.parseInt( price );
		}
		catch ( NumberFormatException e ) {
			System.out.println( "Error: Price input is not a number. " + e.toString() );
			success = false;
			return;
		}

		try {
			statement = connection.createStatement();
			String query = "UPDATE closingprice SET price = " + priceValue + " WHERE p_date = " + date + " AND symbol = " + symbol;
			resultSet = statement.executeQuery( query );
		}
		catch ( SQLException e ) {
			System.out.println( "Error validating user. Machine error: " + e.toString() );
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