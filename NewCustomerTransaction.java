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
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );

		try {
			System.out.print( "\nEnter their username: " );
			username = br.readLine().trim();
			statement = connection.createStatement();
			String query = "SELECT COUNT(*) FROM customer WHERE name = '" + username + "'";
			resultSet = statement.executeQuery( query );
			resultSet.next();
			
			if ( resultSet.getInt( 1 ) == 0 )
				nameAvailable = true;
		}
		catch ( SQLException e ) {
			System.out.println( "Error validating user. Machine error: " + e.toString() );
		}
		catch ( IOException e ) {
			System.out.println(e.toString());
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
			System.out.print( "\nPlease enter the new user's name: " );
			name = br.readLine().trim();
			System.out.print( "Please enter their address: " );
			address = br.readLine().trim();
			System.out.print( "\nPlease enter their email: " );
			email = br.readLine().trim();
			while ( nameConfirmed() == false );
			System.out.print( "\nPlease enter their password: " );
			password = br.readLine().trim();
			System.out.print( "\nUser is admin? (y/n): " );
			userType = br.readLine().trim();
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
		}
		finally {
			try {
				br.close();
			} catch ( IOException e ) {
				System.out.println(e.toString());
			}
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
