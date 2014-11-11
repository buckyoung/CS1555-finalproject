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
