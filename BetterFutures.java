import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.sql.*;
import java.io.IOException;

public class BetterFutures {

	public static void main ( String[] args ) {
		
		System.out.println( "Welcome to Better Futures! When you are prompted to choose a selection, please type the number corresponding to the selection you wish to make. Thank you!" );
		
		boolean isAdmin = false;
		boolean flag = false;
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		Transaction action = null;
		String message = null;
		int selectionValue = 0;
		Connection connection = null;
		
		try{
			DriverManager.registerDriver ( new oracle.jdbc.driver.OracleDriver() );
			String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass"; 
			connection = DriverManager.getConnection(url, "bcy3", "xxxxxx"); 
		}
		catch( Exception e ) { System.out.println("Error connecting to database.  Machine Error: " + e.toString() ); }
		/*finally	{ 
			try {
				connection.close();
			}
			catch ( SQLException e ) {
				System.out.println( "SQLException: " + e.toString() );
			}
		}*/
		
		String selection = "";
		
		while ( !flag ) {
			
			if ( message != null ) {
				System.out.print( message );
			}
		
			message = null;
		
			System.out.print("Please select --\n1. User login\n2. Admin login\n\nYour selection: ");
			try {
				selection = br.readLine().trim();
			}
			catch ( IOException e ) {
				System.out.println( "IOException: " + e.toString() );
			}		
			
			if ( selection.compareTo("1") != 0 && selection.compareTo("2") != 0 ) {
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
		message = "";
		
		while ( !flag ) {
			
			System.out.print( message );	
			
			action = new LoginTransaction( connection, selectionValue );
			action.execute();
			
			flag = action.isSuccessful();
			System.out.println( action.toString() );
		}
		
		boolean exitFlag = false;

		String selectionMsg;
		
		if ( selectionValue == 1 ) {
			selectionMsg = "\nSelect an action:\n\n";
			selectionMsg += "1. Exit\n";
			selectionMsg += "2. Browse Mutual Funds\n";
			selectionMsg += "3. Search Mutual Fund Descriptions\n";
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
			
			try {
				selection = br.readLine().trim();
			}
			catch ( IOException e ) {
				System.out.println( "IOException: " + e.toString() );
			}
					
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
					selectionValue += 7;
					
				switch ( selectionValue ) {
					case 2:
						action = new BrowseFundsTransaction( connection );
						break;
					case 3:
						action = new SearchFundsTransaction( connection );
						break;
					case 4:
						action = new InvestTransaction( connection );
						break;
					case 5:
						action = new SellSharesTransaction( connection );
						break;
					case 6:
						action = new BuySharesTransaction( connection );
						break;
					case 7:
						action = new ChangeAllocationTransaction( connection );
						break;
					case 8:
						action = new ViewPortfolioTransaction( connection );
						break;
					case 9:
						action = new NewCustomerTransaction( connection );
						break;
					case 10:
						action = new UpdateQuotesTransaction( connection );
						break;
					case 11:
						action = new AddFundTransaction( connection );
						break;
					case 12:
						action = new UpdateDateTimeTransaction( connection );
						break;
					case 13:
						action = new ViewStatisticsTransaction( connection );
						break;
					default:
						break;
				}
			
				action.execute();
				System.out.println( action.toString() );
				
			}
			
		}
		// Wrap everything up
		try {
			br.close();
		}
		catch ( IOException e ) {}

		try {
			connection.close();
		}
		catch ( SQLException e ) {
			System.out.println( "SQLException: " + e.toString() );
		}

		System.out.println("Bye!");

		return;

	}

	public static String currentUser = ""; // save for TRXLOG insertions // set in LoginTransactions.java 

	//Helper function for all results
	public static void printRows(ResultSet resultSet){
		try{
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int numCols = rsmd.getColumnCount();
			while(resultSet.next()){
				for( int i=1; i <= numCols; i++ ){
					if (i > 1) System.out.print(",\t");
					System.out.print(resultSet.getString(i));
				}
				System.out.println();

			}
		} catch( SQLException e ) {
			System.out.println( "SQLException while printing rows: " + e.toString() );
		}
	}
	
}