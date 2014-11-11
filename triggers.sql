DROP TABLE trxlog_edits;
DROP FUNCTION GrabPrice;
DROP PROCEDURE MakePurchases;

CREATE OR REPLACE TRIGGER balance_update
	
	AFTER INSERT
	ON trxlog
	FOR EACH ROW
	WHEN ( new.action = 'sell' ) 
	
	BEGIN

		UPDATE customer 
		SET balance = ( balance + :new.amount )
		WHERE login = :new.login;

	END;
/

CREATE OR REPLACE FUNCTION GrabPrice( symb VARCHAR2 )

	RETURN FLOAT IS
	ret_price FLOAT := 0;
	t_date DATE;

	BEGIN

		SELECT TO_DATE( ( SELECT * FROM mutualdate where rownum = 1 ), 'DD-MON-YY' ) - 1 INTO t_date FROM dual;
	
		SELECT price INTO ret_price FROM CLOSINGPRICE
		WHERE ( symbol = symb AND p_date = t_date );

		RETURN ret_price;

	END;
/

CREATE GLOBAL TEMPORARY TABLE trxlog_edits ( 	trans_id INT,
						login VARCHAR2(10),
						symbol VARCHAR2(20),
						t_date DATE,
						action VARCHAR2(10),
						num_shares INT,
						price FLOAT,
						amount FLOAT )
			ON COMMIT DELETE ROWS;

CREATE OR REPLACE PROCEDURE MakePurchases( login_name VARCHAR2, dep_amnt FLOAT, t_id INT )
	IS 

	CURSOR pref_cursor IS SELECT * FROM prefers
		WHERE ( allocation_no = ( SELECT allocation_no FROM 
			( SELECT * FROM ALLOCATION ORDER BY allocation_no DESC )
			WHERE rownum = 1 ) );

	pref_row prefers%ROWTYPE;

	partial_dep FLOAT := 0.0;
	num_shares INT := 0;
	t_date DATE;
	leftover FLOAT := 0.0;
	already_owned INT := 0;	

	BEGIN

		SELECT TO_DATE( ( SELECT * FROM mutualdate WHERE rownum = 1 ), 'DD-MON-YY' ) - 1 INTO t_date FROM dual;

		IF NOT pref_cursor%ISOPEN
			THEN OPEN pref_cursor;
		END IF;

		LOOP
			FETCH pref_cursor INTO pref_row;
			EXIT WHEN pref_cursor%NOTFOUND;
			partial_dep := pref_row.percentage * dep_amnt;
			SELECT trunc( partial_dep / GrabPrice( pref_row.symbol ) ) INTO num_shares from dual;

			SELECT COUNT(*) into already_owned FROM owns WHERE ( login = login_name AND symbol = pref_row.symbol );

			IF already_owned = 1 THEN
				UPDATE owns
				SET shares = ( shares + num_shares )
				WHERE ( login = login_name AND symbol = pref_row.symbol );			
			ELSE
				INSERT INTO owns (login, symbol, shares)
				VALUES ( login_name, pref_row.symbol, num_shares );
			END IF;

			INSERT INTO trxlog_edits ( trans_id, login, symbol, t_date, action, num_shares, price, amount)
				VALUES ( t_id, login_name, pref_row.symbol, t_date, 'buy', num_shares, 
				GrabPrice( pref_row.symbol ), GrabPrice( pref_row.symbol ) * num_shares );

			leftover := leftover + ( partial_dep - ( num_shares * GrabPrice( pref_row.symbol ) ) );

			

		END LOOP;

		CLOSE pref_cursor;

		UPDATE customer
		SET balance = balance + leftover
		WHERE login = login_name;

	END;
/
						
CREATE OR REPLACE TRIGGER deposit_trigger

	AFTER INSERT
	ON trxlog
	FOR EACH ROW
	WHEN ( new.action = 'deposit' )

	BEGIN

		MakePurchases( :new.login, :new.amount, :new.trans_id );

	END;
/

CREATE OR REPLACE TRIGGER deposit_trigger_update

	AFTER INSERT
	ON trxlog

	DECLARE
	
		CURSOR trxlog_temp IS
			SELECT * FROM trxlog_edits;
		trxlog_row trxlog_edits%ROWTYPE;
		t_id INT := 0;
		has_rows INT := 0;
		
	BEGIN

		SELECT COUNT(*) INTO has_rows FROM trxlog_edits;
	
		IF has_rows > 0 THEN
		
			IF NOT trxlog_temp%ISOPEN THEN 
				OPEN trxlog_temp;
			END IF;

			DELETE FROM trxlog_edits;

			LOOP
				FETCH trxlog_temp INTO trxlog_row;
				EXIT WHEN trxlog_temp%NOTFOUND;
				
				SELECT MAX(trans_id) + 1 INTO t_id FROM trxlog;
				
				INSERT INTO trxlog ( trans_id, login, symbol, t_date, action, num_shares, price, amount )
					VALUES ( t_id, trxlog_row.login, trxlog_row.symbol, trxlog_row.t_date, trxlog_row.action, trxlog_row.num_shares, trxlog_row.price, trxlog_row.amount );

			END LOOP;

			CLOSE trxlog_temp;
			
		END IF;
		
	END;
/

-- Simple tests of the triggers. May want to change so that it works regardless of existing data, with checks to make sure the inserts
-- don't violate Unique/primary key constraints (i.e. the new test data doesn't have the same primary key as something already in the database)
-- When fixing the test data, make sure to use dates relative to MUTUALDATE

-- For now, we are using the data already in data.sql as a basis for these tests

-- Run order: db.sql -> data.sql -> triggers.sql

select balance from customer where login = 'mike';
INSERT INTO TRXLOG(trans_id, login, symbol, t_date, action, num_shares, price, amount) values(4, 'mike', 'RE', '04-APR-14', 'sell', 50, 15, 750);
select balance from customer where login = 'mike';
INSERT INTO TRXLOG(trans_id, login, symbol, t_date, action, num_shares, price, amount) values(5, 'mike', NULL, '04-APR-14', 'deposit', NULL, NULL, 1000);
select * from trxlog where trans_id > 5;
