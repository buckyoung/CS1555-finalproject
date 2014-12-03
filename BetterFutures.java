import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.sql.*;

public class BetterFutures {

	public static void main ( String[] args ) {
		
		System.out.println( "Welcome to Better Futures! When you are prompted to choose a selection, please type the number corresponding to the selection you wish to make. Thank you!" );
		
		boolean isAdmin = false;
		boolean flag = false;
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		Transaction action;
		String message = null;
		int selectionValue = 0;
		Connection connection;
		
		try{
			DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
			String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass"; 
			connection = DriverManager.getConnection(url, "ajc148", "########"); 
		}
		catch( Exception e ) { System.out.println("Error connecting to database.  Machine Error: " + e.toString() ); }
		finally	{ connection.close(); }
		
		while ( !flag ) {
			
			if ( message != null ) {
				System.out.print( message );
			}
		
			message = null;
		
			System.out.print("Please select --\n1. User login\n2. Admin login\n\nYour selection: ");
			String selection = br.readLine().trim();
			
			if ( selection.compareTo("1") != true && selection.compareTo("2") != true ) {
				message = "Selection invalid, please type the number corresponding to the action you wish to select\n\n";
			}
			else {
				selectionValue = Integer.parseInt( selection );
				
				if ( selectionValue == 2 )
					isAdmin = true;
				
				flag = true;
			}
		}
		
		flag = false;
		
		String loginType = ( selectionValue == 1 ) ? "Admin login --" : "User login --";
		
		while ( !flag ) {
			
			if ( message != null ) {
				System.out.print( message );
			}
			
			message = null;
			
			System.out.print("\n" + loginType + "\n\nUsername:\t");
			String username = br.readLine().trim();
			
			System.out.print("\nPassword:\t");
			String password = br.readLine().trim();
			
			action = new LoginTransaction( connection, username, password, selectionValue );
			action.execute();
			
			flag = action.isSuccessful();
			
			if ( !flag ) {
				message = "Login failed.\n\n";
			}
		}
		
		boolean exitFlag = false;

		String selectionMsg;
		
		if ( selectionValue == 1 ) {
			selectionMsg = "\nSelect an action:\n\n";
			selectionMsg += "1. Exit\n";
			selectionMsg += "2. Browse Mutual Funds\n";
			selectionMsg += "3. Search Mutual Funds by name\n";
			selectionMsg += "4. Invest\n";
			selectionMsg += "5. Selling Shares\n";
			selectionMsg += "6. Buying Shares\n";
			selectionMsg += "7. Change Allocation Preference\n";	
			selectionMsg += "8. View My Portfolio\n\n";
		}
		else {
			selectionMsg = "\nSelect an administrative action:\n\n";
			selectionMsg += "1. Exit\n";
			selectionMsg += "2. New Customer Registration\n";
			selectionMsg += "3. Updating Share Quotes\n";
			selectionMsg += "4. Add New Mutual Fund\n";
			selectionMsg += "5. Update Date And Time\n";
			selectionMsg += "6. View Statistics\n";
		}
		
		selectionMsg += "Your selection: ";
		
		while ( !exitFlag ) {
			
			System.out.print( selectionMsg );
			selection = br.readLine().trim();
			try {
				selectionValue = Integer.parseInt( selection );
			}
			catch ( NumberFormatException e ) {
				System.out.println( "Parsing error: " + e.toString() );
				selectionValue = -1;
			}
			
			if ( selectionValue == 1 )
				exitFlag = true;
			
			if ( selectionValue > 1 ) {
			
				// We want to use one switch statement to service all actions
				//		Each menu is numbered from 1
				//		Thus we need to add an offset to the selected value
				//		If they are an admin so user actions and admin actions don't overlap
				if ( isAdmin )
					selectionValue += 8;
					
				switch ( selectionValue ) {
					case 1:
						action = new BrowseFundsTransaction( connection );
						break;
					// so forth, so on... should be 12 cases
					default:
						break;
				}
			
				action.execute();
				
			}
			
		}

	}
	
}