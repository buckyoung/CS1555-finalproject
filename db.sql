-- Drop Tables
DROP TABLE IF EXISTS MUTUALFUND CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS CLOSINGPRICE CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS CUSTOMER CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS ADMINISTRATOR CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS ALLOCATION CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS PREFERS CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS TRXLOG CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS OWNS CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS MUTUALDATE CASCADE CONSTRAINTS;
purge recyclebin;

-- Create Tables
`
– MUTUALFUND( symbol, name, description, category, c date ) 
PK (symbol)
∗ symbol: varchar(20)
∗ name: varchar(30)
∗ description: varchar(100) 
∗ category: varchar(10)
∗ c date: date
`
CREATE TABLE MUTUALFUND(
    symbol          varchar(20),
    name            varchar(30),
    description     varchar(100),
    category        varchar(10),
    c_date          date,
    CONSTRAINT MUTUALFUND_PK PRIMARY KEY (symbol)
);

`
– CLOSINGPRICE( symbol, price, p date ) 
PK (symbol, p date)
FK (symbol) → MUTUALFUND(symbol) 
∗ symbol: varchar(20) 
∗ price: float
∗ p date: date
`
CREATE TABLE CLOSINGPRICE(
    symbol  varchar(20),
    price   float,
    p_date  date,
    CONSTRAINT CLOSINGPRICE_PK PRIMARY KEY (symbol, p_date),
    CONSTRAINT CLOSINGPRICE_FK FOREIGN KEY (symbol) REFERENCES MUTUALFUND(symbol)
);

`
– CUSTOMER( login, name, email, address, password, balance ) 
PK (login)
∗ login varchar(10)
∗ name varchar(20)
∗ email varchar(20)
∗ address varchar(30) 
∗ password varchar(10) 
∗ balance float
`
CREATE TABLE CUSTOMER(
    login       varchar(10),
    name        varchar(20),
    email       varchar(20),
    address     varchar(30),
    password    varchar(10),
    balance     float,
    CONSTRAINT CUSTOMER_PK PRIMARY KEY (login)
);

`
– ADMINISTRATOR( login, name, email, address, password ) 
PK (login)
∗ login varchar(10)
∗ name varchar(20)
∗ email varchar(20)
∗ address varchar(30) 
∗ password varchar(10)
`
CREATE TABLE ADMINISTRATOR(
    login       varchar(10),
    name        varchar(20),
    email       varchar(20),
    address     varchar(30),
    password    varchar(10),
    CONSTRAINT ADMINISTRATOR_PK PRIMARY KEY (login)
);

`
– ALLOCATION( allocation no, login, p date ) 
PK (allocation no)
FK (login) → CUSTOMER(login)
∗ allocation no: int 
∗ login: varchar(10) 
∗ p date: date
`
CREATE TABLE ALLOCATION(
    allocation_no   int,
    login           varchar(10),
    p_date          date,
    CONSTRAINT ALLOCATION_PK PRIMARY KEY (allocation_no),
    CONSTRAINT ALLOCATION_FK FOREIGN KEY (login) REFERENCES CUSTOMER(login)
);

`
– PREFERS( allocation no, symbol, percentage )
PK (allocation no, symbol)
FK (allocation no) → ALLOCATION(allocation no) 
FK (symbol) → MUTUALFUND(symbol)
∗ allocation no: int
∗ symbol: varchar(20) 
∗ percentage: float
`
CREATE TABLE PREFERS(
    allocation_no   int,
    symbol          varchar(20),
    percentage      float,
    CONSTRAINT PREFERS_PK PRIMARY KEY (allocation_no, symbol),
    CONSTRAINT PREFERS_FK1 FOREIGN KEY (allocation_no) REFERENCES ALLOCATION(allocation_no),
    CONSTRAINT PREFERS_FK2 FOREIGN KEY (symbol) REFERENCES MUTUALFUND(symbol)
);

`
– TRXLOG(trans id, login, symbol, t date, action, num shares, price, amount) 
PK (trans id)
FK (login) → CUSTOMER(login)
FK (symbol) → MUTUALFUND(symbol)
∗ trans id: int
∗ login varchar(10)
∗ symbol: varchar(20)
∗ t date: date
∗ action: varchar(10) //see comment + below 
∗ num shares: int
∗ price: float
∗ amount: float
+ action is either ’deposit’ or ’sell’ or ’buy’
A deposit transaction should trigger a set of buy transactions showing that the money deposited into a customer’s account will automatically be invested to mu- tual funds based on their preferences.
`
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

`
– OWNS( login, symbol, shares )
PK (login, symbol)
FK (login) → CUSTOMER(login)
FK (symbol) → MUTUALFUND(symbol) 
∗ login varchar(10)
∗ symbol: varchar(20) 
∗ shares: int
`
CREATE TABLE OWNS(
    login   varchar(10),
    symbol  varchar(20),
    shares  int,
    CONSTRAINT OWNS_PK PRIMARY KEY (login, symbol),
    CONSTRAINT OWNS_FK1 FOREIGN KEY (login) REFERENCES CUSTOMER(login),
    CONSTRAINT OWNS_FK2 FOREIGN KEY (symbol) REFERENCES MUTUALFUND(symbol)
);

`
– MUTUALDATE ( c date ) 
PK (c date)
∗ c date: date
`
CREATE TABLE MUTUALDATE(
    c_date  date,
    CONSTRAINT MUTUALDATE_PK PRIMARY KEY (c_date)
);
