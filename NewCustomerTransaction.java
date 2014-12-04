import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class NewCustomerTransaction extends Transaction {

	public NewCustomerTransaction( Connection connection ) {
		super( connection );
	}

	private boolean nameConfirmed() {
	
		boolean nameAvailable = false;
	
		try {
			statement = connection.createStatement();
			String query = "SELECT COUNT(*) FROM customer WHERE name";
			resultSet = statement.executeQuery( query );
			resultSet.next();
			
			if ( resultSet.getInt( 1 ) == 0 )
				nameAvailable = true;
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
		
		return nameAvailable;
	}

	public void execute() {
	
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		String name = "";
		String address = "";
		String email = "";
		String password = "";
		String userType = "";
		
		try {
			System.out.println( "\nPlease enter your name: " );
			name = br.readLine().trim();
			System.out.println( "Please enter your address: " );
			address = br.readLine().trim();
			System.out.println( "\nPlease enter your email: " );
			email = br.readLine().trim();
			while ( nameConfirmed() == false );
			System.out.println( "\nPlease enter your password: " );
			password = br.readLine().trim();
			System.out.println( "\nUser is admin? (y/n): " );
			userType = br.readLine().trim();
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
		}
		
	 	boolean isAdmin = ( userType.toLowerCase().charAt(0) == 'y' ) ? true : false;
		
		try {
			statement = connection.createStatement();
			
			String query;
			
			if ( isAdmin )
				query = "INSERT INTO administrator (login, name, email, address, password) VALUES ('" + username + "','" + name + "','" + email + "','" + address + "','" + password + "')";
			else
				query = "INSERT INTO customer (login, name, email, address, password, balance) VALUES ('" + username + "','" + name + "','" + email + "','" + address + "','" + password + "',0)";
				
			statement.executeQuery( query );
			success = true;
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

	private String username;
	private Statement statement;
	private ResultSet resultSet;
}
