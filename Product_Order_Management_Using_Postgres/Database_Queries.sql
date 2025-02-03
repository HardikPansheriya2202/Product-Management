CREATE TABLE admin_data (admin_user varchar (50), pin varchar (50));

INSERT INTO admin_data VALUES ('admin', 'b59c67bf196a4758191e42f76670ceba'); -- username -> admin, password -> 1234

CREATE TABLE customers (customer_id SERIAL PRIMARY KEY, name varchar (50), age int, username varchar (50), pin varchar (50));

CREATE TABLE products (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    price DECIMAL(10, 2),
    quantity INT,
    category VARCHAR(50)
);

CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    customer_id INT,
    total_price DECIMAL(10, 2),
    discount DECIMAL(10, 2),
    tax DECIMAL(10, 2),
    final_price DECIMAL(10, 2),
    payment_status BOOLEAN,
FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE order_products (
    order_product_id SERIAL PRIMARY KEY,
    order_id INT,
    product_id INT,
    quantity INT,
FOREIGN KEY (order_id) REFERENCES orders(order_id),
FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE cart (
    customer_id INT,
    product_id INT,
    quantity INT NOT NULL,
PRIMARY KEY (customer_id, product_id),
FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE debit_cards (
    id SERIAL PRIMARY KEY,
    customer_id INT,
    account_number VARCHAR(16) UNIQUE,
    expiry_date VARCHAR(5),
    cvv VARCHAR(3),
    name VARCHAR(100),
    balance numeric(10,2),
FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

INSERT INTO debit_cards (account_number, expiry_date, cvv, name, balance) VALUES 
								('1234567890123456', '08/28', '123', 'Hardik', 15000),
								('1111111111111111', '02/25', '111', 'Om', 50000),
								('2222222222222222', '04/30', '222', 'Yogesh', 14200),
								('3333333333333333', '11/27', '333', 'Chirag', 23006),
								('4444444444444444', '10/28', '444', 'Bhautik', 16230);
					
CREATE TABLE credit_cards (
    id SERIAL PRIMARY KEY,
    customer_id INT,
    account_number VARCHAR(16) UNIQUE,
    expiry_date VARCHAR(5),
    cvv VARCHAR(3),
    name VARCHAR(100),
    credit_limit numeric(10,2),
    current_credit numeric(10,2),
FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

INSERT INTO credit_cards (account_number, expiry_date, cvv, name, credit_limit, current_credit) VALUES 
								('5555555555555555', '12/25', '555', 'Kishan', 50000, 25000),
								('6666666666666666', '10/29', '666', 'Hardik', 44000, 20000),
								('7777777777777777', '04/26', '777', 'Yogesh', 54000, 18000),
								('8888888888888888', '01/27', '888', 'Chirag', 26000, 5000),
								('9999999999999999', '03/30', '999', 'Bhautik', 58000, 15000);

CREATE TABLE digital_wallets (
    id SERIAL PRIMARY KEY,
    customer_id INT,
    wallet_id VARCHAR(16) UNIQUE,
    pin VARCHAR(4),
    balance numeric(10,2),
FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

INSERT INTO digital_wallets (wallet_id, pin, balance) VALUES 
							('Hardik111', '1111', 24500),
							('Ompatel222', '2222', 15200),
							('Yogesh333', '3333', 29540),
							('Chirag444', '4444', 42150),
							('Bhautik555', '5555', 36500);

CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    transaction_type VARCHAR(20),
    amount numeric(10,2),
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    account_number VARCHAR(16),
    wallet_id VARCHAR(16),
    transaction_status VARCHAR(20)
);
