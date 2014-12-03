import java.sql.*;

public class SearchFundsTransaction extends Transaction {

	public SearchFundsTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		// get relevant data here from user
		
		Statement statement;
		ResultSet resultSet;

		try {
			statement = connection.createStatement();
			//String query = "";
			//resultSet = statement.executeQuery( query );
			
			//resultSet.next();
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