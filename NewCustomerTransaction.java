import java.sql.*;

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
		
	}

	public void execute() {
	
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		
		System.out.println( "\nPlease enter your name: " );
		String name = br.readLine().trim();
		System.out.println( "Please enter your address: " );
		String address = br.readLine().trim();
		System.out.println( "\nPlease enter your email: " );
		String email = br.readLine().trim();
		while ( nameConfirmed() == false );
		System.out.println( "\nPlease enter your password: " );
		String password = br.readLine().trim();
		System.out.println( "\nUser is admin? (y/n): " );
		String userType = br.readLine().trim();
		
	 	boolean isAdmin = ( userType.toLowerCase().charAt(0) == 'y' ) true : false;
		
		try {
			statement = connection.createStatement();
			
			String query
			if ( isAdmin )
				query = "INSERT INTO admin () VALUES ()";
			else
				query = "INSERT INTO customer () VALUES ()";
				
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

	private String username;
	private Statement statement;
	private ResultSet resultSet;
}
