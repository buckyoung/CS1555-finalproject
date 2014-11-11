import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BetterFutures {

	public static void main ( String[] args ) {
		
		System.out.println( "Welcome to Better Futures! When you are prompted to choose a selection, please type the number corresponding to the selection you wish to make. Thank you!" );
		
		boolean flag = false;
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		Transaction login;
		String message = null;
		int selectionValue = 0;
		
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
			
			login = new LoginTransaction(username, login, selectionValue);
			login.execute();
			
			flag = login.isSuccessful();
			
			if ( !flag ) {
				message = "Login failed.\n\n";
			}
		}
		
		boolean exitFlag = false;

		if ( selectionValue == 1 ) {
			
			String selectionMsg = "";
			// Array of sub selection messages? i.e. prompts based on the action chosen
			
			while ( !exitFlag ) {
				
				
				// if selection = Exit, then exitFlag = true;
			}
			
		}
		else {
			
			while ( !exitFlag ) {
				
				
				
			}

		}
		
	}
	
}