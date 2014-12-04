import java.sql.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UpdateDateTimeTransaction extends Transaction {

	public UpdateDateTimeTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		// get relevant data here from user
		
		Statement statement = null;
		ResultSet resultSet = null;

		String date = "";
		BufferedReader br = new BufferedReader( new InputStreamReader ( System.in ) );
		
		try {
			System.out.print( "Enter the new date in YYYY-MM-DD format: " );
			date = br.readLine().toLowerCase().trim();
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
		}
		
		try {
			statement = connection.createStatement();
			String query = "UPDATE mutualdate SET c_date = " + date;
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
		
	}

}