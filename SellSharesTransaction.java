import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class SellSharesTransaction extends Transaction {

	public SellSharesTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		// get relevant data here from user
		
		Statement statement = null;
		ResultSet resultSet = null;
		String query = "";
		int nextTRXid = 0;
		String symbol = "";
		String numShares = "";
		int numSharesValue = 0;
		int priceValue = 0;
		String amount = "";
		float amountValue = 0.0f;
		String mutualDate = "";
		boolean flag = false;
		BufferedReader br = new BufferedReader( new InputStreamReader ( System.in ) );


		// Print what the user owns
		try {
			statement = connection.createStatement();

			query = "SELECT symbol, shares FROM owns WHERE login='"+BetterFutures.getCurrentUser()+"' ";
			resultSet = statement.executeQuery( query );
			
			System.out.println("\n= = == ====  LIST OF OWNED FUNDS ==== == = =");

			while ( resultSet.next() ) {
				System.out.println( resultSet.getString(1) + ": " + resultSet.getString(2) + " shares");
			}

			System.out.println("= = == ============================== == = =");
		}
		catch ( SQLException e ) {
			System.out.println( "Error retrieving reference symbols and shares. Machine error: " + e.toString() );
		}
		// Get the symbol and numShares to sell
			//symbol
		while(!flag){
			System.out.println("\nEnter the symbol to sell:");
			try {
				symbol = br.readLine().trim().toUpperCase();
				flag = true;
			}
			catch ( IOException e ) {
				System.out.println( "IOException: " + e.toString() );
			}
		}
			//numShares
		flag = false;
		while(!flag){
			System.out.println("\nEnter the number of shares to sell:");
			try {
				numShares = br.readLine().trim();
				numSharesValue = Integer.parseInt(numShares);
				flag = true;
			}
			catch ( IOException e ) {
				System.out.println( "IOException: " + e.toString() );
			}
			catch ( NumberFormatException e ) {
				System.out.println( "Parsing error: " + e.toString() );
			}
		}
		// Validate transaction (owns that many shares to sell AND a closing price is available before mutual date)
			//get current date
		try {
			query = "SELECT * FROM mutualdate";
			resultSet = statement.executeQuery(query);
			resultSet.next();
			mutualDate = resultSet.getString(1);
		}
		catch ( SQLException e ) {
			System.out.println( "Error while getting mutualdate. Machine error: " + e.toString() );
		}
			//user owns that amount
		try {
			query = "SELECT symbol FROM owns WHERE login ='"+BetterFutures.getCurrentUser()+"' AND symbol = '"+symbol+"' AND shares >= "+numSharesValue;
			resultSet = statement.executeQuery( query );
			if(!resultSet.next()){ //INVALID!
				System.out.println("Sorry! You do not seem to own that amount of shares to sell! We cannot process this SELL.");
				return;
			}
		}
		catch ( SQLException e ) {
			System.out.println( "Error while validating 'user owns'. Machine error: " + e.toString() );
		}
			//closing price exists && save closing price for insert stmt. below
		try {
			query = "SELECT price, p_date FROM closingprice WHERE symbol = '"+symbol+"' AND p_date < TIMESTAMP '"+mutualDate+"' ORDER BY p_date DESC" ;
			resultSet = statement.executeQuery( query );

			if(!resultSet.next()){ //INVALID!
				System.out.println("Sorry! This fund does not seem to have a closing price! We cannot process this SELL.");
				return;
			} else {
				priceValue = resultSet.getInt(1);
			}
		} 
		catch ( SQLException e ) {
			System.out.println( "Error while validating / getting closing price. Machine error: " + e.toString() );
		}
			
		
		//update tables
		try {
			//disables
			connection.setAutoCommit(false);
			query = "ALTER TRIGGER deposit_trigger DISABLE";
			statement.executeQuery( query );

			// Decrement shares in OWNS
			query = "UPDATE owns SET shares = (shares - "+numSharesValue+") WHERE login ='"+BetterFutures.getCurrentUser()+"' AND symbol = '"+symbol+"'";
			statement.executeQuery( query );

			// Insert into TRXLOG -> (sell trigger should fire)
				//get transaction ID++
			query = "SELECT max(trans_id)+1 FROM TRXLOG";
			resultSet = statement.executeQuery(query);
			resultSet.next();
			nextTRXid = Integer.parseInt(resultSet.getString(1));
				//calculate amount
			amountValue = numSharesValue * priceValue;
			int truncate = (int)(amountValue * 100);
			amountValue = truncate/100;
				//insert 
			query = "INSERT INTO TRXLOG(trans_id, login, symbol, t_date, action, num_shares, price, amount) values("+nextTRXid+", '"+BetterFutures.getCurrentUser()+"', '"+symbol+"', TIMESTAMP '"+mutualDate+"', 'sell', "+numSharesValue+", "+priceValue+", "+amountValue+")";
			statement.executeQuery( query );

			//re-enables
			query = "ALTER TRIGGER deposit_trigger ENABLE";
			statement.executeQuery( query );
			connection.setAutoCommit(false);
			connection.commit();
		}
		catch ( SQLException e ) {
			System.out.println( "Error while updating OWNS. Machine error: " + e.toString() );
		}
		catch ( NumberFormatException e ) {
			System.out.println( "Parsing error: " + e.toString() );
		}
		finally {
			try {
				connection.setAutoCommit(true);
				if (statement != null) statement.close();
			} catch (SQLException e) {
				System.out.println( "Cannot close Statement / set auto commit. Machine error: " + e.toString() );
			}
		}
	
	}

}