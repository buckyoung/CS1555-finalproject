import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class LoginTransaction extends Transaction {

	public LoginTransaction( Connection connection, int loginType ) {
		super( connection );
		this.loginType = loginType;
	}

	public void execute() {
		
		Statement statement = null;
		ResultSet resultSet = null;
		
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		String username = "";
		String password = "";	
		String loginPrompt = ( loginType == 2 ) ? "Admin login --" : "User login --";
		
		try {
			System.out.print("\n" + loginPrompt + "\n\nUsername:\t");
			username = br.readLine().trim();
			System.out.print("\nPassword:\t");
			password = br.readLine().trim();
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
			try { 
				br.close();
			} catch ( IOException ex ){}
			System.exit(0);
		}

		try {
			statement = connection.createStatement();
			String query = ( loginType == 2 )?"SELECT password FROM administrator WHERE login = '" + username + "'":"SELECT password FROM customer WHERE login = '" + username + "'";
			resultSet = statement.executeQuery( query );
			
			resultSet.next();
			
			int compare = password.compareTo( resultSet.getString( 1 ) );
			
			success = ( compare == 0 ) ? true : false;
		}
		catch ( SQLException e ) {
			results = "Error validating user. Machine error: " + e.toString();
			success = false;
			return;
		}
		finally {
			try {
				if (statement != null) statement.close();
			} catch (SQLException e) {
				results = "Cannot close Statement. Machine error: " + e.toString();
			}
		}
		
		results = ( success ) ? "Logged in successfully!" : "Login attempt failed.";
	}
	
	private int loginType;
}