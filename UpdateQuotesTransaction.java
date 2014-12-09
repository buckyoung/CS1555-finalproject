import java.sql.*;
import java.text.ParseException;
import java.lang.Double;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UpdateQuotesTransaction extends Transaction {

	public UpdateQuotesTransaction( Connection connection ) {
		super( connection );
	}

	
	public void execute() {
		
		Statement statement = null;
		ResultSet resultSet = null;

		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		String symbol = "";
		String date = "";
		String price = "";
		
		try {
			statement = connection.createStatement();
			String query = "SELECT name, symbol FROM mutualfund";
			resultSet = statement.executeQuery( query );
			
			System.out.println();

			while ( resultSet.next() ) {
				System.out.println( resultSet.getString(1) + ": " + resultSet.getString(2) );
			}

			System.out.println();
		}
		catch ( SQLException e ) {
			//.
		}

		try {
			System.out.print( "Please select the mutual fund to be updated by it's symbol: " );
			symbol = br.readLine().trim().toUpperCase();
			
			try {
				statement = connection.createStatement();
				String query = "SELECT MAX(c_date) FROM mutualdate";
				resultSet = statement.executeQuery( query );
				resultSet.next();
				System.out.print( "\nPlease select the date (in yyyy-mm-dd format) that you wish to change the price for (today's mutual date is: " + resultSet.getDate(1) + "): " );
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
			
			System.out.print( "\nPlease enter the new price: " );
			price = br.readLine().trim();
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
		}
		
		double priceValue = 0.0;
		
		try {
			priceValue = Double.parseDouble( price );
		}
		catch ( NumberFormatException e ) {
			System.out.println( "Error: Price input is not a number. " + e.toString() );
			success = false;
			return;
		}

		try {
			statement = connection.createStatement();
			String query = "UPDATE closingprice SET price = " + priceValue + " WHERE p_date = ( select to_date( '" + date + "', 'yyyy-mm-dd' ) from dual ) AND symbol = '" + symbol + "'";
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

		success = true;
		results = "Update successful.";
		
	}

}
