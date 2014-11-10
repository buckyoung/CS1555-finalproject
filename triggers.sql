CREATE OR REPLACE TRIGGER balance_update
	
	AFTER INSERT
	ON trxlog
	FOR EACH ROW
	WHEN ( new.action = 'sell' ) 
	
	BEGIN

		UPDATE customer 
		SET balance = UpdateBalance( balance, :new.symbol, :new.num_shares )
		WHERE login = :new.login;

	END;
/

CREATE OR REPLACE FUNCTION UpdateBalance( balance FLOAT, symb VARCHAR2, num_shares INT )

	RETURN FLOAT IS
	new_bal FLOAT := 0;

	fund_price FLOAT := 0;
	t_date DATE := SELECT TO_DATE( SELECT * FROM MUTUALDATE, 'DD-MON-YY' ) - 1 FROM DUAL;

	BEGIN
	
		SELECT price INTO fund_price FROM CLOSINGPRICE
		WHERE ( symbol = symb AND p_date = t_date );

		new_bal := balance + ( fund_price * num_shares );

		RETURN new_bal;

	END;
/

CREATE OR REPLACE FUNCTION GrabPrice( symb VARCHAR2 )

	RETURN FLOAT IS
	ret_price FLOAT := 0;
	t_date DATE := SELECT TO_DATE( SELECT * FROM MUTUALDATE, 'DD-MON-YY' ) - 1 FROM DUAL;

	BEGIN
	
		SELECT price INTO ret_price FROM CLOSINGPRICE
		WHERE ( symbol = symb AND p_date = t_date );

		RETURN ret_price;

	END;
/

CREATE OR REPLACE TRIGGER deposit_trigger

	AFTER INSERT
	ON trxlog
	FOR EACH ROW
	WHEN ( new.action = 'deposit' )

	BEGIN

		MakePurchases( :new.login, :new.amount );

	END;
/

CREATE OR REPLACE PROCEDURE MakePurchases( login_name VARCHAR2, dep_amnt FLOAT )
	IS 

	CURSOR pref_cursor IS SELECT * FROM prefers
		WHERE ( allocation_no = ( SELECT allocation_no FROM 
			( SELECT * FROM ALLOCATION ORDER BY allocation_no DESC )
			WHERE rownum = 1 ) );
	pref_row prefers%ROWTYPE;
	
	partial_dep FLOAT := 0.0;
	num_shares INT := 0;
	new_id INT := 0;
	t_date DATE := SELECT TO_DATE( SELECT * FROM MUTUALDATE, 'DD-MON-YY' ) - 1 FROM DUAL;
	leftover FLOAT := 0.0;
	
	BEGIN

		IF NOT pref_cursor%ISOPEN
			THEN OPEN pref_cursor;
		END IF;

		LOOP
			FETCH pref_cursor INTO pref_row;
			EXIT WHEN pref_cursor%NOTFOUND;
			partial_dep := pref_row.percentage * dep_amnt;
			num_shares := partial_dep \ GrabPrice( pref_row.symbol );
			SELECT MAX(trans_id) INTO new_id FROM TRXLOG;
			new_id  := new_id + 1;

			INSERT INTO trxlog (trans_id, login, symbol, t_date, action, num_shares, price, amount)
				VALUES ( new_id, login_name, pref_row.symbol, t_date, 'buy', num_shares, 
				GrabPrice( pref_row.symbol ), GrabPrice( pref_row.symbol ) * num_shares );

			leftover := leftover + ( dep_amnt - ( num_shares * GrabPrice( pref_row.symbol ) );

		END LOOP;

		CLOSE pref_cursor;

		UPDATE customer
		SET balance = leftover
		WHERE login = login_name;

	END;
/