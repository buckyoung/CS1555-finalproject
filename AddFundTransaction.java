import java.sql.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.lang.Integer;

public class AddFundTransaction extends Transaction {

	public AddFundTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		Statement statement = null;
		ResultSet resultSet = null;

		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		String symbol = "";
		String price = "";
		String fundName = "";
		String category = "";
		String description = "";
		Date date = null;
		
		try {
			System.out.print( "\nPlease enter the name of the new mutual fund: " );
			fundName = br.readLine().trim();
			System.out.print( "Please enter the symbol of the mutual fund to be added: " );
			symbol = br.readLine().trim();
			System.out.print( "Please enter the category of the new fund: " );
			category = br.readLine().trim();
			System.out.print( "Please enter the description fo the new fund: ");
			description = br.readLine().trim();
			System.out.print( "Please enter an initial price for the fund: ");
			price = br.readLine().trim();
			
			try {
				statement = connection.createStatement();
				String query = "SELECT MAX(c_date) FROM mutualdate";
				resultSet = statement.executeQuery( query );
				resultSet.next();
				date = resultSet.getDate(1);
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
			String query = "SELECT COUNT(*) FROM mutualfund WHERE symbol = '" + symbol + "'";
			resultSet = statement.executeQuery( query );
			
			if ( resultSet.next() ) {
				if ( resultSet.getInt(1) == 1 ) {
					success = false;
					results = "Add mutual fund failed. Fund with symbol '" + symbol + "' already exists.";
					return;
				}
			}
			
			query = "INSERT INTO mutualfund (symbol, name, description, category, c_date) VALUES ('" + symbol + "','" + fundName + "','" + description + "','" + category + "', ( select to_date( '" + date.toString() + "', 'yyyy-mm-dd' ) from dual ) )";
			statement.executeQuery( query );
			query = "INSERT INTO closingprice (symbol, price, p_date) VALUES ('" + symbol + "'," + priceValue + ", ( select to_date( '" + date.toString() + "', 'yyyy-mm-dd' ) from dual ) )";
			statement.executeQuery( query );			
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
		results = "Fund successfully added!";

	}

}
