package com.logilite.jdbc_product_order_management;
 
import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.logging.Logger;

public class PaymentProcessor {
    private static final Logger customerLogger = LoggerConfig.getLogger("CustomerLogger", "customer.log");

    static String loginuser = Product_Order_Management.loginuser;
    static String loginpin = Product_Order_Management.loginpin;
    static boolean paymentCompleted = false;
    static String paymentType = "";

    private static String inputAccountNumber(Scanner sc) {
        String accNumber;
        while (true) {
            System.out.println("Enter Your account number (16 digit): ");
            accNumber = sc.next();

            if (accNumber.matches("\\d{16}")) {
                return accNumber;
            } else {
                System.out.println("==> Please enter 16 digit account number <==");
            }
        }
    }

    private static String inputcvv(Scanner sc) {
        String cvv;
        while (true) {
            System.out.println("Enter Your CVV number (3 digit): ");
            cvv = sc.next();

            if (cvv.matches("\\d{3}")) {
                return cvv;
            } else {
                System.out.println("==> Please enter 3 digit CVV number <==");
            }
        }
    }

    private static String inputExpdate(Scanner sc) {
        String expDate;
        while (true) {
            System.out.println("Enter expiry date (MM/YY) format: ");
            expDate = sc.next();

            if (!expDate.matches("\\d{2}/\\d{2}")) {
                System.out.println("==> Please enter in (MM/YY) format <==");
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
                Date date = sdf.parse(expDate);

                if (date.after(new Date(0))) {
                    return expDate;
                } else {
                    System.out.println("==> Date must be after today's date <==");
                }
            } catch (Exception e) {
                System.out.println("Invalid date format.");
            }
        }
    }

    private static String inputName(Scanner sc) {
        String name;
        while (true) {
            sc.nextLine();
            System.out.println("Enter Card Holder Name: ");
            name = sc.nextLine();

            if (name != null && name.matches("[a-zA-Z\\s]+")) {
                return name;
            } else {
                System.out.println("==> Name should not be null and should only contain alphabets <==");
            }
        }
    }

    private static String inputwalletId(Scanner sc) {
        String walletId;
        while (true) {
            sc.nextLine();
            System.out.println("Enter Your Wallet ID : ");
            walletId = sc.next();

            if (walletId.matches("^[a-zA-Z0-9]{8,16}$")) {
                return walletId;
            } else {
                System.out.println("==> Please enter valid wallet ID or only 8 to 16 digit contains <==");
            }
        }
    }

    private static String inputPin(Scanner sc) {
        String pin;
        while (true) {
            System.out.println("Enter Your Pin : ");
            pin = sc.next();
            sc.nextLine();

            if (pin.length() == 4) {
                return pin;
            } else {
                System.out.println("==> Enter Only 4 digit Pin <==");
            }
        }
    }
    
    private static double inputAmount(Scanner sc) {
    	double balance;
        while (true) {
            System.out.println("Enter balance (positive number): ");
            if (sc.hasNextDouble()) {
                balance = sc.nextDouble();
                if (balance > 0) {
                	return balance;
                } else {
                    System.out.println("==> Price must be greater than 0 <==");
                }
            } else {
                sc.next();
                System.out.println("==> Enter a valid balance <==");
            }
        }
    }
    
    private static double inputCreditLimit(Scanner sc) {
    	double creditLimit;
        while (true) {
            System.out.println("Enter credit limit (positive number): ");
            if (sc.hasNextDouble()) {
                creditLimit = sc.nextDouble();
                if (creditLimit > 0) {
                	return creditLimit;
                } else {
                    System.out.println("==> Price must be greater than 0 <==");
                }
            } else {
                sc.next();
                System.out.println("==> Enter a valid credit limit <==");
            }
        }
    }
    
    private static double inputCurrentCredit(Scanner sc) {
    	double currentCredit;
        while (true) {
            System.out.println("Enter current credit (positive number): ");
            if (sc.hasNextDouble()) {
                currentCredit = sc.nextDouble();
                if (currentCredit > 0) {
                	return currentCredit;
                } else {
                    System.out.println("==> Price must be greater than 0 <==");
                }
            } else {
                sc.next();
                System.out.println("==> Enter a valid current credit <==");
            }
        }
    }

    public static void displayTransactions(Connection conn) {
        String query = "SELECT * FROM transactions";

        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(query);

            if (!rs.next()) {
                System.out.println("==> Transaction data is empty! <==");
                return;
            }

            System.out.printf("%-10s %-20s %-15s %-15s %-20s %-15s %s\n", "T_Id", "T_Type", "Amount", "Date", "Acc_number", "Wallet_Id", "T_Status");
            do{
                System.out.printf("%-10s %-20s %-15s %-15s %-20s %-15s %s\n", rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getDate(4), rs.getString(5), rs.getString(6), rs.getString(7));
            }while (rs.next()) ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void addDebitcardData(Connection conn, Scanner sc, String loginuser, String loginpin) {
    	String accNumber = inputAccountNumber(sc);
        String cvv = inputcvv(sc);
        String expDate = inputExpdate(sc);
        String name = inputName(sc);
        double balance = inputAmount(sc);
        
        String debitUserSQL = "SELECT * FROM debit_cards WHERE account_number = ? AND expiry_date = ? AND cvv = ? AND name = ?";
        String selectCustomerSQL = "SELECT * FROM customers WHERE username = ? AND pin = ?";
        String insertDebitSQL = "INSERT INTO debit_cards (customer_id, account_number, expiry_date, cvv, name, balance) VALUES (?, ?, ?, ?, ?, ?)";
        
        try(PreparedStatement debitPst = conn.prepareStatement(debitUserSQL);
        	PreparedStatement customerPst = conn.prepareStatement(selectCustomerSQL);
        	PreparedStatement insertDebitPst = conn.prepareStatement(insertDebitSQL))
		{
			debitPst.setString(1, accNumber);
			debitPst.setString(2, expDate);
			debitPst.setString(3, cvv);
			debitPst.setString(4, name);
			
			ResultSet dSet = debitPst.executeQuery();
			if (dSet.next()) {
				System.out.println("==> Debit card data already exists. <==");
				return;
			}
			
			customerPst.setString(1, loginuser);
			customerPst.setString(2, loginpin);
			
			ResultSet cSet = customerPst.executeQuery();
			
			int affected_row = 0;
			while(cSet.next()) {
				int customer_id = cSet.getInt(1);
				
				insertDebitPst.setInt(1, customer_id);
				insertDebitPst.setString(2, accNumber);
				insertDebitPst.setString(3, expDate);
				insertDebitPst.setString(4,cvv);
				insertDebitPst.setString(5, name);
				insertDebitPst.setDouble(6, balance);
				
				affected_row = insertDebitPst.executeUpdate();				
			}
			
			if (affected_row > 0) {
				System.out.println("Data inserted successfully.");
			}else {
				System.out.println("Data not inserted");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    
    public static void addCreditcardData(Connection conn, Scanner sc, String loginuser, String loginpin) {
    	String accNumber = inputAccountNumber(sc);
        String cvv = inputcvv(sc);
        String expDate = inputExpdate(sc);
        String name = inputName(sc);
        double creditLimit = inputCreditLimit(sc);
        double currentCredit = inputCurrentCredit(sc);
        
        String creditUserSQL = "SELECT * FROM credit_cards WHERE account_number = ? AND expiry_date = ? AND cvv = ? AND name = ?";
        String selectCustomerSQL = "SELECT * FROM customers WHERE username = ? AND pin = ?";
        String insertCreditSQL = "INSERT INTO credit_cards (customer_id, account_number, expiry_date, cvv, name, credit_limit, current_credit) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try(PreparedStatement creditPst = conn.prepareStatement(creditUserSQL);
        	PreparedStatement customerPst = conn.prepareStatement(selectCustomerSQL);
        	PreparedStatement insertCreditPst = conn.prepareStatement(insertCreditSQL))
		{
			creditPst.setString(1, accNumber);
			creditPst.setString(2, expDate);
			creditPst.setString(3, cvv);
			creditPst.setString(4, name);
			
			ResultSet dSet = creditPst.executeQuery();
			if (dSet.next()) {
				System.out.println("==> Credit card data already exists. <==");
				return;
			}
			
			customerPst.setString(1, loginuser);
			customerPst.setString(2, loginpin);
			
			ResultSet cSet = customerPst.executeQuery();
			
			int affected_row = 0;
			while(cSet.next()) {
				int customer_id = cSet.getInt(1);
				
				insertCreditPst.setInt(1, customer_id);
				insertCreditPst.setString(2, accNumber);
				insertCreditPst.setString(3, expDate);
				insertCreditPst.setString(4,cvv);
				insertCreditPst.setString(5, name);
				insertCreditPst.setDouble(6, creditLimit);
				insertCreditPst.setDouble(7, currentCredit);
				
				affected_row = insertCreditPst.executeUpdate();				
			}
			
			if (affected_row > 0) {
				System.out.println("Data inserted successfully.");
			}else {
				System.out.println("Data not inserted");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    
    public static void addWalletData(Connection conn, Scanner sc, String loginuser, String loginpin) {
    	String wallet_id = inputwalletId(sc);
        String pin = inputPin(sc);
        double balance = inputAmount(sc);
        
        String walletUserSQL = "SELECT * FROM digital_wallets WHERE wallet_id = ? AND pin = ?";
        String selectCustomerSQL = "SELECT * FROM customers WHERE username = ? AND pin = ?";
        String insertWalletSQL = "INSERT INTO digital_wallets (customer_id, wallet_id, pin, balance) VALUES (?, ?, ?, ?)";
        
        try(PreparedStatement walletPst = conn.prepareStatement(walletUserSQL);
        	PreparedStatement customerPst = conn.prepareStatement(selectCustomerSQL);
        	PreparedStatement insertWalletPst = conn.prepareStatement(insertWalletSQL))
		{
			walletPst.setString(1, wallet_id);
			walletPst.setString(2, Product_Order_Management.encryptString(pin));
			
			ResultSet wSet = walletPst.executeQuery();
			if (wSet.next()) {
				System.out.println("==> Wallet data already exists. <==");
				return;
			}
			
			customerPst.setString(1, loginuser);
			customerPst.setString(2, loginpin);
			
			ResultSet cSet = customerPst.executeQuery();
			
			int affected_row = 0;
			while(cSet.next()) {
				int customer_id = cSet.getInt(1);
				
				insertWalletPst.setInt(1, customer_id);
				insertWalletPst.setString(2, wallet_id);
				insertWalletPst.setString(3, Product_Order_Management.encryptString(pin));
				insertWalletPst.setDouble(4, balance);
				
				affected_row = insertWalletPst.executeUpdate();				
			}
			
			if (affected_row > 0) {
				System.out.println("Data inserted successfully.");
			}else {
				System.out.println("Data not inserted");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    
    public static void addPaymentData(Connection conn, Scanner sc, String loginuser, String loginpin) throws InterruptedException {
    	boolean flag = false;
        int num = 0;
        do {
            System.out.println("\n1. Enter 1 --> for Add Debit Data");
            System.out.println("2. Enter 2 --> for Add Credit Data");
            System.out.println("3. Enter 3 --> for Add Wallet Data");
            System.out.println("4. Enter 4 --> for Exit");
           
            System.out.println("\nEnter Task Which You Want to Perform : ");

            while (true) {
                try {
                    num = sc.nextInt();
                    break;
                } catch (Exception e) {
                    sc.next();
                    System.out.println("Enter Numeric value only : ");
                }
            }

            switch (num) {
                case 1: {
                	addDebitcardData(conn, sc, loginuser, loginpin);
                    break;
                }
                case 2: {
                	addCreditcardData(conn, sc, loginuser, loginpin);
                    break;
                }
                case 3: {
                	addWalletData(conn, sc, loginuser, loginpin);
                    break;
                }
                case 4: {
                	System.out.println("Exit...");
                	Product_Order_Management.mainMenu(sc, conn);
                    break;
                }
                default:
                    System.out.println("-- Enter Only 1 to 8 Number to Perform Task --\n");
            }
        } while (!flag);
    }
    
    public static void processDebitcardPayment(Connection conn, Scanner sc, String loginuser, String loginpin) throws InterruptedException {
        String debitDataSQL  = "SELECT customer_id FROM debit_cards";
        String debitUserSQL = "SELECT * FROM debit_cards WHERE customer_id = ? AND account_number = ? AND expiry_date = ? AND cvv = ? AND name = ?";
        String selectCustomerSQL = "SELECT * FROM customers WHERE username = ? AND pin = ?";
        String selectOrderSQL = "SELECT * FROM orders WHERE customer_id = ? AND payment_status = 'Pending'";
        String insertTransactionSQL = "INSERT INTO transactions (transaction_type, amount, account_number, transaction_status) VALUES (?, ?, ?, ?)";
        String updateBalanceSQL = "UPDATE debit_cards SET balance = balance - ? WHERE account_number = ?";
        String refundBalanceSQL = "UPDATE debit_cards SET balance = balance + ? WHERE account_number = ?";
        String updateOrderStatusSQL = "UPDATE orders SET payment_status = 'Refunded' WHERE order_id = ?";
        String verifyTransactionSQL = "SELECT * FROM transactions WHERE transaction_id = ? AND account_number = ? AND transaction_status = 'Success'";

        try (PreparedStatement debitPst = conn.prepareStatement(debitUserSQL);
             PreparedStatement customerPst = conn.prepareStatement(selectCustomerSQL);
             PreparedStatement orderPst = conn.prepareStatement(selectOrderSQL);
             PreparedStatement insertTransactionPst = conn.prepareStatement(insertTransactionSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement updateBalancePst = conn.prepareStatement(updateBalanceSQL);
             PreparedStatement refundBalancePst = conn.prepareStatement(refundBalanceSQL);
             PreparedStatement updateOrderStatusPst = conn.prepareStatement(updateOrderStatusSQL);
             PreparedStatement verifyTransactionPst = conn.prepareStatement(verifyTransactionSQL);
        	 Statement debitDataSt = conn.createStatement()) {
        	
        	customerPst.setString(1, loginuser);
        	customerPst.setString(2, loginpin);
        	
        	try (ResultSet rsCustomer = customerPst.executeQuery()) {
        		if (!rsCustomer.next()) {
        			System.out.println("==> Invalid login credentials. <==");
        			customerLogger.warning("Invalid login attempt for username: " + loginuser);
        			return;
        		}
        		
        		boolean debitUserFound = false;
        		try(ResultSet debitCustomer = debitDataSt.executeQuery(debitDataSQL))
    			{
    				while(debitCustomer.next()) {
    					if (debitCustomer.getInt("customer_id") == rsCustomer.getInt("customer_id")) {
    						debitUserFound = true;
    						break;
    					}
    				}
    				
    				if (!debitUserFound) {
    					System.out.println("==> Your payment data not found in debit cards <==");
    					return;
    				}
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    			}
        	
        		String accNumber = inputAccountNumber(sc);
        		String cvv = inputcvv(sc);
        		String expDate = inputExpdate(sc);
        		String name = inputName(sc);
        	
        	debitPst.setInt(1, rsCustomer.getInt("customer_id"));
            debitPst.setString(2, accNumber);
            debitPst.setString(3, expDate);
            debitPst.setString(4, cvv);
            debitPst.setString(5, name);

            try (ResultSet rsDebit = debitPst.executeQuery()) {
                if (!rsDebit.next()) {
                    System.out.println("==> Debit card details are invalid. Please add payment data first. <==");
                    customerLogger.warning("Invalid debit card details entered for account number: " + accNumber);
                    return;
                }

                double cardBalance = rsDebit.getDouble("balance");

                    int customerId = rsCustomer.getInt("customer_id");

                    orderPst.setInt(1, customerId);
                    try (ResultSet rsOrder = orderPst.executeQuery()) {
                        if (!rsOrder.next()) {
                            System.out.println("==> No pending orders found for this customer. <==");
                            return;
                        }

                        double orderAmount = rsOrder.getDouble("final_price");

                        if (cardBalance < orderAmount) {
                            System.out.println("==> Insufficient balance on debit card. <==");
                            return;
                        }

                        System.out.print("Are you sure you want to purchase this product? (y/n): ");
                        String choice = sc.nextLine();

                        if (choice.equalsIgnoreCase("y")) {
                            conn.setAutoCommit(false);

                            try {
                                updateBalancePst.setDouble(1, orderAmount);
                                updateBalancePst.setString(2, accNumber);
                                updateBalancePst.executeUpdate();

                                insertTransactionPst.setString(1, "DebitCardPayment");
                                insertTransactionPst.setDouble(2, orderAmount);
                                insertTransactionPst.setString(3, accNumber);
                                insertTransactionPst.setString(4, "Success");
                                insertTransactionPst.executeUpdate();

                                ResultSet rsGeneratedKeys = insertTransactionPst.getGeneratedKeys();
                                if (rsGeneratedKeys.next()) {
                                    int transactionId = rsGeneratedKeys.getInt(1);
                                    System.out.println("Transaction ID: " + transactionId);
                                    customerLogger.info("Payment processed successfully for customer ID: " + customerId + " with Transaction ID: " + transactionId);
                                }

                                conn.commit();
                                System.out.println("==> Payment processed successfully! <==");
                            } catch (SQLException e) {
                                conn.rollback();
                                System.out.println("==> Payment failed! Transaction rolled back. <==");
                                e.printStackTrace();
                            } finally {
                                conn.setAutoCommit(true);
                            }

                            System.out.print("Do you want to confirm the payment or request a refund? (confirm/refund) : ");
                            String action = sc.nextLine();

                            if (action.equalsIgnoreCase("confirm")) {
                                paymentCompleted = true;
                                paymentType = "paid";
                                customerLogger.info("Payment confirmed for customer ID: " + customerId);
                                System.out.println("Payment confirmed. Product will be delivered soon.");
                            } else if (action.equalsIgnoreCase("refund")) {
                                System.out.print("Enter your transaction ID for refund: ");
                                int transactionId = sc.nextInt();

                                verifyTransactionPst.setInt(1, transactionId);
                                verifyTransactionPst.setString(2, accNumber);

                                try (ResultSet rsTransaction = verifyTransactionPst.executeQuery()) {
                                    if (rsTransaction.next()) {
                                        System.out.println("Processing your refund request. Please wait...");
                                        Thread.sleep(5000);

                                        conn.setAutoCommit(false);

                                        try {
                                            refundBalancePst.setDouble(1, orderAmount);
                                            refundBalancePst.setString(2, accNumber);
                                            refundBalancePst.executeUpdate();

                                            int orderId = rsOrder.getInt("order_id");
                                            updateOrderStatusPst.setInt(1, orderId);
                                            updateOrderStatusPst.executeUpdate();

                                            insertTransactionPst.setString(1, "Refund");
                                            insertTransactionPst.setDouble(2, orderAmount);
                                            insertTransactionPst.setString(3, accNumber);
                                            insertTransactionPst.setString(4, "Success");
                                            insertTransactionPst.executeUpdate();

                                            conn.commit();
                                            paymentCompleted = true;
                                            paymentType = "refund";
                                            customerLogger.info("Refund processed for customer ID: " + customerId + " with Transaction ID: " + transactionId);
                                            System.out.println("==> Refund processed successfully! <==");
                                        } catch (SQLException e) {
                                            conn.rollback();
                                            System.out.println("==> Refund failed! Transaction rolled back. <==");
                                            e.printStackTrace();
                                        } finally {
                                            conn.setAutoCommit(true);
                                        }
                                    } else {
                                        System.out.println("==> Invalid transaction ID or transaction not eligible for refund. <==");
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("==> Invalid option! Please choose 'confirm' or 'refund'. <==");
                            }
                        } else {
                            System.out.println("==> Payment process cancelled. <==");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	 
    public static void processCreditcardPayment(Connection conn, Scanner sc, String loginuser, String loginpin) throws InterruptedException {    
        String creditDataSQL  = "SELECT customer_id FROM credit_cards";
        String creditUserSQL = "SELECT * FROM credit_cards WHERE customer_id = ? AND account_number = ? AND expiry_date = ? AND cvv = ? AND name = ?";
        String selectCustomerSQL = "SELECT * FROM customers WHERE username = ? AND pin = ?";
        String selectOrderSQL = "SELECT * FROM orders WHERE customer_id = ? AND payment_status = 'Pending'";
        String insertTransactionSQL = "INSERT INTO transactions (transaction_type, amount, account_number, transaction_status) VALUES (?, ?, ?, ?)";
        String updateBalanceSQL = "UPDATE credit_cards SET current_credit = current_credit + ? WHERE account_number = ?";
        String refundBalanceSQL = "UPDATE credit_cards SET current_credit = current_credit - ? WHERE account_number = ?";
        String updateOrderStatusSQL = "UPDATE orders SET payment_status = 'Refunded' WHERE order_id = ?";
        String verifyTransactionSQL = "SELECT * FROM transactions WHERE transaction_id = ? AND account_number = ? AND transaction_status = 'Success'";

        try (PreparedStatement creditPst = conn.prepareStatement(creditUserSQL);
             PreparedStatement customerPst = conn.prepareStatement(selectCustomerSQL);
             PreparedStatement orderPst = conn.prepareStatement(selectOrderSQL);
             PreparedStatement insertTransactionPst = conn.prepareStatement(insertTransactionSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement updateBalancePst = conn.prepareStatement(updateBalanceSQL);
             PreparedStatement refundBalancePst = conn.prepareStatement(refundBalanceSQL);
             PreparedStatement updateOrderStatusPst = conn.prepareStatement(updateOrderStatusSQL);
             PreparedStatement verifyTransactionPst = conn.prepareStatement(verifyTransactionSQL);
        	 Statement creditDataSt = conn.createStatement()) {

        	customerPst.setString(1, loginuser);
        	customerPst.setString(2, loginpin);
        	
        	try (ResultSet rsCustomer = customerPst.executeQuery()) {
        		if (!rsCustomer.next()) {
        			customerLogger.warning("Invalid login credentials for user: " + loginuser);
        			System.out.println("==> Invalid login credentials. <==");
        			return;
        		}
        		
        		boolean creditUserFound = false;
        		try(ResultSet creditCustomer = creditDataSt.executeQuery(creditDataSQL))
    			{
    				while(creditCustomer.next()) {
    					if (creditCustomer.getInt("customer_id") == rsCustomer.getInt("customer_id")) {
    						creditUserFound = true;
    						break;
    					}
    				}
    				
    				if (!creditUserFound) {
    					System.out.println("==> Your payment data not found in credit cards <==");
    					return;
    				}
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    			}
        		
        		String accNumber = inputAccountNumber(sc);
        		String cvv = inputcvv(sc);
        		String expDate = inputExpdate(sc);
        		String name = inputName(sc);
        		
        	creditPst.setInt(1, rsCustomer.getInt("customer_id"));
            creditPst.setString(2, accNumber);
            creditPst.setString(3, expDate);
            creditPst.setString(4, cvv);
            creditPst.setString(5, name);

            try (ResultSet rsCredit = creditPst.executeQuery()) {
                if (!rsCredit.next()) {
                    customerLogger.warning("Invalid credit card details entered for account number: " + accNumber);
                    System.out.println("==> Credit card details are invalid. <==");
                    return;
                }

                double currentCredit = rsCredit.getDouble("current_credit");
                double creditLimit = rsCredit.getDouble("credit_limit");


                    int customerId = rsCustomer.getInt("customer_id");

                    orderPst.setInt(1, customerId);
                    try (ResultSet rsOrder = orderPst.executeQuery()) {
                        if (!rsOrder.next()) {
                            System.out.println("==> No pending orders found for this customer. <==");
                            return;
                        }

                        double orderAmount = rsOrder.getDouble("final_price");
                        double cardBalance = creditLimit - currentCredit;

                        if (cardBalance < orderAmount) {
                            System.out.println("==> Insufficient balance on credit card. <==");
                            return;
                        }

                        System.out.print("Are you sure you want to purchase this product? (y/n): ");
                        String choice = sc.nextLine();

                        if (choice.equalsIgnoreCase("y")) {
                            conn.setAutoCommit(false);

                            try {
                                updateBalancePst.setDouble(1, orderAmount);
                                updateBalancePst.setString(2, accNumber);
                                updateBalancePst.executeUpdate();

                                insertTransactionPst.setString(1, "CreditCardPayment");
                                insertTransactionPst.setDouble(2, orderAmount);
                                insertTransactionPst.setString(3, accNumber);
                                insertTransactionPst.setString(4, "Success");
                                insertTransactionPst.executeUpdate();

                                ResultSet rsGeneratedKeys = insertTransactionPst.getGeneratedKeys();
                                if (rsGeneratedKeys.next()) {
                                    int transactionId = rsGeneratedKeys.getInt(1);
                                    System.out.println("Transaction ID: " + transactionId);
                                    customerLogger.info("Payment processed successfully for customer ID: " + customerId + " with Transaction ID: " + transactionId);
                                }

                                conn.commit();
                                System.out.println("==> Payment processed successfully! <==");
                            } catch (SQLException e) {
                                conn.rollback();
                                customerLogger.severe("Payment failed for customer ID: " + customerId + ". Error: " + e.getMessage());
                                System.out.println("==> Payment failed! Transaction rolled back. <==");
                                e.printStackTrace();
                            } finally {
                                conn.setAutoCommit(true);
                            }

                            System.out.print("Do you want to confirm the payment or request a refund? (confirm/refund) : ");
                            String action = sc.nextLine();

                            if (action.equalsIgnoreCase("confirm")) {
                            	paymentCompleted = true;
                            	paymentType = "paid";
                                customerLogger.info("Payment confirmed for customer ID: " + customerId);
                                System.out.println("Payment confirmed. Product will be delivered soon.");
                            } else if (action.equalsIgnoreCase("refund")) {
                                System.out.print("Enter your transaction ID for refund: ");
                                int transactionId = sc.nextInt();

                                verifyTransactionPst.setInt(1, transactionId);
                                verifyTransactionPst.setString(2, accNumber);

                                try (ResultSet rsTransaction = verifyTransactionPst.executeQuery()) {
                                    if (rsTransaction.next()) {
                                        System.out.println("Processing your refund request. Please wait...");
                                        Thread.sleep(5000);

                                        conn.setAutoCommit(false);

                                        try {
                                            refundBalancePst.setDouble(1, orderAmount);
                                            refundBalancePst.setString(2, accNumber);
                                            refundBalancePst.executeUpdate();

                                            int orderId = rsOrder.getInt("order_id");
                                            updateOrderStatusPst.setInt(1, orderId);
                                            updateOrderStatusPst.executeUpdate();

                                            insertTransactionPst.setString(1, "Refund");
                                            insertTransactionPst.setDouble(2, orderAmount);
                                            insertTransactionPst.setString(3, accNumber);
                                            insertTransactionPst.setString(4, "Success");
                                            insertTransactionPst.executeUpdate();

                                            conn.commit();
                                            paymentCompleted = true;
                                            paymentType = "refund";
                                            customerLogger.info("Refund processed successfully for customer ID: " + customerId + " with Transaction ID: " + transactionId);
                                            System.out.println("==> Refund processed successfully! <==");
                                        } catch (SQLException e) {
                                            conn.rollback();
                                            System.out.println("==> Refund failed! Transaction rolled back. <==");
                                            e.printStackTrace();
                                        } finally {
                                            conn.setAutoCommit(true);
                                        }
                                    } else {
                                        System.out.println("==> Invalid transaction ID or transaction not eligible for refund. <==");
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("==> Invalid option! Please choose 'confirm' or 'refund'. <==");
                            }
                        } else {
                            System.out.println("==> Payment process cancelled. <==");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            customerLogger.severe("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void processDigitalWalletPayment(Connection conn, Scanner sc, String loginuser, String loginpin) throws InterruptedException {
        String walletDataSQL  = "SELECT customer_id FROM digital_wallets";
        String walletUserSQL = "SELECT * FROM digital_wallets WHERE customer_id = ? AND wallet_id = ? AND pin = ?";
        String selectCustomerSQL = "SELECT * FROM customers WHERE username = ? AND pin = ?";
        String selectOrderSQL = "SELECT * FROM orders WHERE customer_id = ? AND payment_status = 'Pending'";
        String insertTransactionSQL = "INSERT INTO transactions (transaction_type, amount, wallet_id, transaction_status) VALUES (?, ?, ?, ?)";
        String updateBalanceSQL = "UPDATE digital_wallets SET balance = balance - ? WHERE wallet_id = ?";
        String refundBalanceSQL = "UPDATE digital_wallets SET balance = balance + ? WHERE wallet_id = ?";
        String updateOrderStatusSQL = "UPDATE orders SET payment_status = 'Refunded' WHERE order_id = ?";
        String verifyTransactionSQL = "SELECT * FROM transactions WHERE transaction_id = ? AND wallet_id = ? AND transaction_status = 'Success'";

        try (PreparedStatement walletPst = conn.prepareStatement(walletUserSQL);
             PreparedStatement customerPst = conn.prepareStatement(selectCustomerSQL);
             PreparedStatement orderPst = conn.prepareStatement(selectOrderSQL);
             PreparedStatement insertTransactionPst = conn.prepareStatement(insertTransactionSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement updateBalancePst = conn.prepareStatement(updateBalanceSQL);
             PreparedStatement refundBalancePst = conn.prepareStatement(refundBalanceSQL);
             PreparedStatement updateOrderStatusPst = conn.prepareStatement(updateOrderStatusSQL);
             PreparedStatement verifyTransactionPst = conn.prepareStatement(verifyTransactionSQL);
        	 Statement walletDataSt = conn.createStatement()) {

        	customerPst.setString(1, loginuser);
        	customerPst.setString(2, loginpin);
        	
        	try (ResultSet rsCustomer = customerPst.executeQuery()) {
        		if (!rsCustomer.next()) {
        			customerLogger.warning("Invalid login credentials for user: " + loginuser);
        			System.out.println("==> Invalid login credentials. <==");
        			return;
        		}
        		
        		boolean walletUserFound = false;
        		try(ResultSet walletCustomer = walletDataSt.executeQuery(walletDataSQL))
    			{
    				while(walletCustomer.next()) {
    					if (walletCustomer.getInt("customer_id") == rsCustomer.getInt("customer_id")) {
    						walletUserFound = true;
    						break;
    					}
    				}
    				
    				if (!walletUserFound) {
    					System.out.println("==> Your payment data not found in digital wallets <==");
    					return;
    				}
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    			}
        	
        		String wallet_id = inputwalletId(sc);
        		String pin = inputPin(sc);
        		
        	walletPst.setInt(1, rsCustomer.getInt("customer_id"));
            walletPst.setString(2, wallet_id);
            walletPst.setString(3, Product_Order_Management.encryptString(pin));

            try (ResultSet rsWallet = walletPst.executeQuery()) {
                if (!rsWallet.next()) {
                    customerLogger.warning("Invalid wallet details entered for wallet ID: " + wallet_id + ". Please add payment data first.");
                    System.out.println("==> Wallet details are invalid. <==");
                    return;
                }

                double cardBalance = rsWallet.getDouble("balance");


                    int customerId = rsCustomer.getInt("customer_id");

                    orderPst.setInt(1, customerId);
                    try (ResultSet rsOrder = orderPst.executeQuery()) {
                        if (!rsOrder.next()) {
                            System.out.println("==> No pending orders found for this customer. <==");
                            return;
                        }

                        double orderAmount = rsOrder.getDouble("final_price");

                        if (cardBalance < orderAmount) {
                            System.out.println("==> Insufficient balance on wallet. <==");
                            return;
                        }

                        System.out.print("Are you sure you want to purchase this product? (y/n): ");
                        String choice = sc.nextLine();

                        if (choice.equalsIgnoreCase("y")) {
                            conn.setAutoCommit(false);

                            try {
                                updateBalancePst.setDouble(1, orderAmount);
                                updateBalancePst.setString(2, wallet_id);
                                updateBalancePst.executeUpdate();

                                insertTransactionPst.setString(1, "WalletPayment");
                                insertTransactionPst.setDouble(2, orderAmount);
                                insertTransactionPst.setString(3, wallet_id);
                                insertTransactionPst.setString(4, "Success");
                                insertTransactionPst.executeUpdate();

                                ResultSet rsGeneratedKeys = insertTransactionPst.getGeneratedKeys();
                                if (rsGeneratedKeys.next()) {
                                    int transactionId = rsGeneratedKeys.getInt(1);
                                    System.out.println("Transaction ID: " + transactionId);
                                    customerLogger.info("Payment processed successfully for customer ID: " + customerId + " with Transaction ID: " + transactionId);
                                }

                                conn.commit();
                                System.out.println("==> Payment processed successfully! <==");
                            } catch (SQLException e) {
                                conn.rollback();
                                customerLogger.severe("Payment failed for customer ID: " + customerId + ". Error: " + e.getMessage());
                                System.out.println("==> Payment failed! Transaction rolled back. <==");
                                e.printStackTrace();
                            } finally {
                                conn.setAutoCommit(true);
                            }

                            System.out.print("Do you want to confirm the payment or request a refund? (confirm/refund) : ");
                            String action = sc.nextLine();

                            if (action.equalsIgnoreCase("confirm")) {
                            	paymentCompleted = true;
                            	paymentType = "paid";
                                customerLogger.info("Payment confirmed for customer ID: " + customerId);
                                System.out.println("Payment confirmed. Product will be delivered soon.");
                            } else if (action.equalsIgnoreCase("refund")) {
                                System.out.print("Enter your transaction ID for refund: ");
                                int transactionId = sc.nextInt();

                                verifyTransactionPst.setInt(1, transactionId);
                                verifyTransactionPst.setString(2, wallet_id);

                                try (ResultSet rsTransaction = verifyTransactionPst.executeQuery()) {
                                    if (rsTransaction.next()) {
                                        System.out.println("Processing your refund request. Please wait...");
                                        Thread.sleep(5000);

                                        conn.setAutoCommit(false);

                                        try {
                                            refundBalancePst.setDouble(1, orderAmount);
                                            refundBalancePst.setString(2, wallet_id);
                                            refundBalancePst.executeUpdate();

                                            int orderId = rsOrder.getInt("order_id");
                                            updateOrderStatusPst.setInt(1, orderId);
                                            updateOrderStatusPst.executeUpdate();

                                            insertTransactionPst.setString(1, "Refund");
                                            insertTransactionPst.setDouble(2, orderAmount);
                                            insertTransactionPst.setString(3, wallet_id);
                                            insertTransactionPst.setString(4, "Success");
                                            insertTransactionPst.executeUpdate();

                                            conn.commit();
                                            paymentCompleted = true;
                                            paymentType = "refund";
                                            customerLogger.info("Refund processed successfully for customer ID: " + customerId + " with Transaction ID: " + transactionId);
                                            System.out.println("==> Refund processed successfully! <==");
                                        } catch (SQLException e) {
                                            conn.rollback();
                                            System.out.println("==> Refund failed! Transaction rolled back. <==");
                                            e.printStackTrace();
                                        } finally {
                                            conn.setAutoCommit(true);
                                        }
                                    } else {
                                        System.out.println("==> Invalid transaction ID or transaction not eligible for refund. <==");
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("==> Invalid option! Please choose 'confirm' or 'refund'. <==");
                            }
                        } else {
                            System.out.println("==> Payment process cancelled. <==");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            customerLogger.severe("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("finally")
	public static void PaymentMainMenu(Connection conn) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("\t\t\t\t--------> Welcome To Payment System <--------");
        int num = 0;
        while (!paymentCompleted) {
            if (paymentCompleted) {
                try {
                    Product_Order_Management.customerPanel(sc, conn);
                } finally {
                    break;
                }
            }
            
            System.out.println("\n1. Enter 1 --> for Debit Card");
            System.out.println("2. Enter 2 --> for Credit Card");
            System.out.println("3. Enter 3 --> for Digital Wallet");
            System.out.println("4. Enter 4 --> for Exit\n");
            
            System.out.println("Enter Task Which You Want to Perform : ");
            
            while (true) {
                try {
                    num = sc.nextInt();
                    break;
                } catch (Exception e) {
                    sc.next();
                    System.out.println("Enter Numeric value only : ");
                }
            }
            
            switch (num) {
                case 1:
                	System.out.println(loginuser);
                    processDebitcardPayment(conn, sc, loginuser, loginpin);
                    break;
                case 2:
                    processCreditcardPayment(conn, sc, loginuser, loginpin);
                    break;
                case 3:
                    processDigitalWalletPayment(conn, sc, loginuser, loginpin);
                    break;
                case 4:
                    paymentCompleted = false;
                    System.out.println("Exit from payment system...");
                    Product_Order_Management.customerPanel(sc, conn);
                    break;
                default:
                    System.out.println("-- Enter Only 1 to 4 Number to Perform Task --\n");
            }
        }
    }
}

 