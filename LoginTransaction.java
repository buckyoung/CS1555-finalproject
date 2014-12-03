public class LoginTransaction extends Transaction {

	public LoginTransaction( Connection connection, String username, String password, int loginType ) {
		super( connection );
		this.username = username;
		this.password = password;
		this.loginType = loginType;
	}

	public void execute() {
		
		Statement statement;
		ResultSet resultSet;

		try {
			statement = connection.createStatement();
			String query = "SELECT password FROM customer WHERE login = " + username + ";";
			resultSet = statement.executeQuery( query );
			
			resultSet.next();
			
			int compare = password.compareTo( resultSet.getString( 1 ) );
			
			success = ( compare == 0 ) ? true : false;
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
		
		results = ( success ) ? "Logged in successfully!" : "Login attempt failed.";
	}
	
	private String username;
	private String password;
	private int loginType;
}