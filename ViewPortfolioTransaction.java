import java.sql.*;

public class ViewPortfolioTransaction extends Transaction {

	public ViewPortfolioTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		// get relevant data here from user
		
		Statement statement = null;
		ResultSet resultSet = null;
		String query = "";
		String result = "";
		float totalCost = 0.0f;
		float totalIncome = 0.0f;
		float adjustedCost = 0.0f;
		float totalValue = 0.0f;
		float yield = 0.0f;
		String mutualDate = "";

		//Get mutualDate
		try {
			statement = connection.createStatement();
			query = "SELECT * FROM mutualdate";
			resultSet = statement.executeQuery(query);
			resultSet.next();
			mutualDate = resultSet.getString(1);
		}
		catch ( SQLException e ) {
			System.out.println( "Error while getting mutualdate. Machine error: " + e.toString() );
		}


		// Print what the user owns and get the totalCost, totalIncome, adjustedCost, totalValue, and yield
		try {

			query = "select symbol, new_price, owns.shares, (new_price * owns.shares) as currentValue from owns, ( select price as new_price, symbol as new_sym from closingprice, ( select symbol as sym, max(p_date) as new_date from closingprice group by symbol ) where symbol = sym and p_date = new_date ) where owns.login = '"+BetterFutures.getCurrentUser()+"' and owns.symbol = new_sym";
			resultSet = statement.executeQuery(query);

			//Print OWNS REPORT
			System.out.println("\n= = ==  CURRENT VALUE OF OWNED SHARES == = =");
			while ( resultSet.next() ) {
				System.out.println( resultSet.getString(1) + " \tOwn: " + resultSet.getString(2) + " shares \tAt $"+resultSet.getString(3)"/share \t = $"+resultSet.getString(4) );
			}
			System.out.println("= = == ============================== == = =");

			//get the totalCost
			query = "SELECT SUM(amount) FROM trxlog WHERE ACTION = 'buy' and t_date <= TIMESTAMP '"+mutualDate+"' AND login='"+BetterFutures.getCurrentUser()+"'";
			resultSet = statement.executeQuery(query);
			if(resultSet.next()){ //if not null...
				totalCost = resultSet.getFloat(1);
			}

			//get the totalIncome
			query = "SELECT SUM(amount) FROM trxlog WHERE ACTION = 'sell' and t_date <= TIMESTAMP '"+mutualDate+"' AND login='"+BetterFutures.getCurrentUser()+"'";
			resultSet = statement.executeQuery(query);
			if(resultSet.next()){ //if not null...
				totalIncome = resultSet.getFloat(1);
			}

			//calculate adjustedCost
			adjustedCost = totalCost - totalIncome; 
			int truncate = (int)(adjustedCost * 100);
			adjustedCost = truncate/100;

			//get the totalValue
			query = "select sum(currentValue) as sumValue from ( select ( new_price * owns.shares ) as currentValue, symbol from owns, ( select price as new_price, symbol as new_sym from closingprice, ( select symbol as sym, max(p_date) as new_date from closingprice group by symbol ) where symbol = sym and p_date = new_date ) where owns.login = '"+BetterFutures.getCurrentUser()+"' and owns.symbol = new_sym )";
			resultSet = statement.executeQuery(query);
			if(resultSet.next()){ //if not null...
				totalValue = resultSet.getFloat(1);
			}

			//calculate yield
			yield = totalValue - adjustedCost;
			truncate = (int)(yield * 100);
			yield = truncate/100;



			//PRINT STATISTICS
			System.out.println("\n--------------------------");
			System.out.println("Total Cost: $ " + totalCost);
			System.out.println("Yield: $ " + yield);
			System.out.println("Total Value: $ " + totalValue);
		}
		catch ( SQLException e ) {
			System.out.println( "Error producing report. Machine error: " + e.toString() );
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