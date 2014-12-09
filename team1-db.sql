-- Drop Tables
DROP TABLE MUTUALFUND CASCADE CONSTRAINTS;
DROP TABLE CLOSINGPRICE CASCADE CONSTRAINTS;
DROP TABLE CUSTOMER CASCADE CONSTRAINTS;
DROP TABLE ADMINISTRATOR CASCADE CONSTRAINTS;
DROP TABLE ALLOCATION CASCADE CONSTRAINTS;
DROP TABLE PREFERS CASCADE CONSTRAINTS;
DROP TABLE TRXLOG CASCADE CONSTRAINTS;
DROP TABLE OWNS CASCADE CONSTRAINTS;
DROP TABLE MUTUALDATE CASCADE CONSTRAINTS;
purge recyclebin;

-- Create Tables

CREATE TABLE MUTUALFUND(
    symbol          varchar(20),
    name            varchar(30),
    description     varchar(100),
    category        varchar(10),
    c_date          date,
    CONSTRAINT MUTUALFUND_PK PRIMARY KEY (symbol)
);

CREATE TABLE CLOSINGPRICE(
    symbol  varchar(20),
    price   float,
    p_date  date,
    CONSTRAINT CLOSINGPRICE_PK PRIMARY KEY (symbol, p_date),
    CONSTRAINT CLOSINGPRICE_FK FOREIGN KEY (symbol) REFERENCES MUTUALFUND(symbol)
);

CREATE TABLE CUSTOMER(
    login       varchar(10),
    name        varchar(20),
    email       varchar(25),
    address     varchar(30),
    password    varchar(10),
    balance     float,
    CONSTRAINT CUSTOMER_PK PRIMARY KEY (login)
);

CREATE TABLE ADMINISTRATOR(
    login       varchar(10),
    name        varchar(20),
    email       varchar(25),
    address     varchar(30),
    password    varchar(10),
    CONSTRAINT ADMINISTRATOR_PK PRIMARY KEY (login)
);

CREATE TABLE ALLOCATION(
    allocation_no   int,
    login           varchar(10),
    p_date          date,
    CONSTRAINT ALLOCATION_PK PRIMARY KEY (allocation_no),
    CONSTRAINT ALLOCATION_FK FOREIGN KEY (login) REFERENCES CUSTOMER(login)
);

CREATE TABLE PREFERS(
    allocation_no   int,
    symbol          varchar(20),
    percentage      float,
    CONSTRAINT PREFERS_PK PRIMARY KEY (allocation_no, symbol),
    CONSTRAINT PREFERS_FK1 FOREIGN KEY (allocation_no) REFERENCES ALLOCATION(allocation_no),
    CONSTRAINT PREFERS_FK2 FOREIGN KEY (symbol) REFERENCES MUTUALFUND(symbol)
);

CREATE TABLE TRXLOG(
    trans_id    int,
    login       varchar(10),
    symbol      varchar(20),
    t_date      date,
    action      varchar(10),
    num_shares  int,
    price       float,
    amount      float,
    CONSTRAINT TRXLOG_PK PRIMARY KEY (trans_id),
    CONSTRAINT TRXLOG_FK1 FOREIGN KEY (login) REFERENCES CUSTOMER(login),
    CONSTRAINT TRXLOG_FK2 FOREIGN KEY (symbol) REFERENCES MUTUALFUND(symbol)
);

CREATE TABLE OWNS(
    login   varchar(10),
    symbol  varchar(20),
    shares  int,
    CONSTRAINT OWNS_PK PRIMARY KEY (login, symbol),
    CONSTRAINT OWNS_FK1 FOREIGN KEY (login) REFERENCES CUSTOMER(login),
    CONSTRAINT OWNS_FK2 FOREIGN KEY (symbol) REFERENCES MUTUALFUND(symbol)
);

CREATE TABLE MUTUALDATE(
    c_date  date,
    CONSTRAINT MUTUALDATE_PK PRIMARY KEY (c_date)
);

DROP TABLE trxlog_edits;
DROP FUNCTION GrabPrice;
DROP PROCEDURE MakePurchases;

-- A trigger to update the balance. 
---- Assume that the insert statement that sets off the trigger
---- Contains the amount of the sale (this seems to be the case based on sample data)

CREATE OR REPLACE TRIGGER balance_update
	
	AFTER INSERT
	ON trxlog
	FOR EACH ROW
	WHEN ( new.action = 'sell' ) 

	DECLARE

		md_count INT := 0;
		tr_date DATE;
	
	BEGIN

		SELECT COUNT(c_date) INTO md_count FROM mutualdate;

		IF md_count > 0 THEN

			SELECT MAX(c_date) INTO tr_date FROM mutualdate;

			IF tr_date = :new.t_date THEN

				UPDATE customer
				SET balance = ( balance + :new.amount )
				WHERE login = :new.login;


			END IF;

		END IF;

	END;
/

-- A function to grab the price of a symbol based on the date before the one stored in MUTUALDATES
---- Assume that all triggers using this function are on NEW data, i.e. data that is being done on the date of MUTUALDATE

CREATE OR REPLACE FUNCTION GrabPrice( symb VARCHAR2 )

	RETURN FLOAT IS
	ret_price FLOAT := 0;
	tr_date DATE;
	t_date DATE;

	BEGIN

		SELECT c_date INTO tr_date FROM mutualdate WHERE rownum = 1;

		SELECT MAX(p_date) INTO t_date FROM closingprice WHERE p_date < tr_date;
	
		SELECT price INTO ret_price FROM CLOSINGPRICE
		WHERE ( symbol = symb AND p_date = t_date );

		RETURN ret_price;

	END;
/

-- A global temporary table whose existence is to get around the mutating table error

CREATE GLOBAL TEMPORARY TABLE trxlog_edits ( 	trans_id INT,
						login VARCHAR2(10),
						symbol VARCHAR2(20),
						t_date DATE,
						action VARCHAR2(10),
						num_shares INT,
						price FLOAT,
						amount FLOAT )
			ON COMMIT DELETE ROWS;

-- A procedure used in the deposit trigger to buy funds after a deposit
---- Makes inserts into OWNS based on what is purchased
---- Updates balance if there is any leftover deposit money (anything the purchase of funds don't evenly go into)
---- Inserts temporary trxlog entries for the purchases into trxlog_edits			
----
---- Assume that we allocate PERCENT * DEPOSIT dollars for each fund type, excess is stored in CUSTOMER.balance

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

		SELECT MAX(c_date) INTO t_date FROM mutualdate;

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
						
-- The trigger to call the MakePurchases procedure
						
CREATE OR REPLACE TRIGGER deposit_trigger

	AFTER INSERT
	ON trxlog
	FOR EACH ROW
	WHEN ( new.action = 'deposit' )

	DECLARE

		md_count INT := 0;
		tr_date DATE;

	BEGIN

		SELECT COUNT(c_date) INTO md_count FROM mutualdate;

		IF md_count > 0 THEN

			SELECT MAX(c_date) INTO tr_date FROM mutualdate;

			IF tr_date = :new.t_date THEN

				MakePurchases( :new.login, :new.amount, :new.trans_id );

			END IF;

		END IF;

	END;
/

-- A trigger to insert the data in the temporary table trxlog_edits into trxlog, after the insert on trxlog is complete
---- To avoid recursion, we delete the content of the trxlog_edits table after opening the cursor that holds it's content

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