package com.logilite.jdbc_product_order_management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Logger;

public class Customer {
    
    private static final Logger adminLogger = LoggerConfig.getLogger("AdminLogger", "admin.log");
    private static final Logger customerLogger = LoggerConfig.getLogger("CustomerLogger", "customer.log");

    public static boolean SignupCustomer(Scanner sc, Connection conn) {
        String query1 = "SELECT username FROM customers";
        String query2 = "INSERT INTO customers(name, age, email, username, pin) VALUES(?,?,?,?,?)";

        try (PreparedStatement pst = conn.prepareStatement(query1);
             PreparedStatement pst1 = conn.prepareStatement(query2)) {
            
            ResultSet rs = pst.executeQuery();

            String name = "";
            while (true) {
            	sc.nextLine();
                System.out.println("Enter Customer Name: ");
                name = sc.nextLine();
                if (!name.isEmpty()) {
                    break;
                }
                System.out.println("Name cannot be empty. Please enter a valid name.");
            }

            int age = 0;
            while (true) {
                System.out.println("Enter Customer Age: ");
                if (sc.hasNextInt()) {
                    age = sc.nextInt();
                    if (age > 0) {
                        break;
                    } else {
                        System.out.println("Age must be a positive number. Please try again.");
                    }
                } else {
                    sc.next();
                    System.out.println("Invalid input. Please enter a valid age.");
                }
            }

            String email = "";
            while (true) {
            	sc.nextLine();
            	System.out.println("Enter email id: ");
            	email = sc.nextLine();
            	if (!email.isEmpty() && email.matches("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" 
            	        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")) {
            			break;
            	} else {
            		System.out.println("email cannot be empty or invalid email pattern. Please enter a valid email.");
            	}
            }
            
            String username = "";
            while (true) {
                System.out.println("Enter Username: ");
                username = sc.nextLine();
                if (!username.isEmpty()) {
                    boolean usernameExists = false;
                    while (rs.next()) {
                        if (rs.getString(1).equals(username)) {
                            usernameExists = true;
                            break;
                        }
                    }
                    if (usernameExists) {
                        System.out.println("Username already exists. Please choose a different username.");
                    } else {
                        break;
                    }
                } else {
                    System.out.println("Username cannot be empty. Please enter a valid username.");
                }
            }
            

            String pin = "";
            while (true) {
                System.out.println("Enter Pin: ");
                pin = sc.next().trim();
                sc.nextLine();
                if (!pin.isEmpty() && pin.length() >= 4) {
                    break;
                } else {
                    System.out.println("Pin must be at least 4 characters long. Please try again.");
                }
            }

            String cpin = "";
            while (true) {
                System.out.println("Enter Confirm Pin: ");
                cpin = sc.next().trim();
                sc.nextLine();
                if (!cpin.isEmpty() && pin.equals(cpin)) {
                    break;
                } else if (!pin.equals(cpin)) {
                    System.out.println("Pins do not match. Please re-enter.");
                } else {
                    System.out.println("Confirm Pin cannot be empty.");
                }
            }

            pst1.setString(1, name);
            pst1.setInt(2, age);
            pst1.setString(3, email);
            pst1.setString(4, username);
            pst1.setString(5, Product_Order_Management.encryptString(pin));

            pst1.execute();
            Product_Order_Management.loginuser = username;
            Product_Order_Management.loginpin = Product_Order_Management.encryptString(pin);

            customerLogger.info("Customer registered successfully with username: " + username);
            System.out.println("Customer successfully registered!");
            return true;

        } catch (SQLException e) {
            customerLogger.severe("Error during customer signup: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static void changeCustomerCredentials(Scanner sc, Connection conn, String loginuser, String loginpin) {
        System.out.println("\n--> Change Customer Username and Pin <--");

        String query1 = "SELECT username, pin FROM customers";
        String query2 = "UPDATE customers SET username = ?, pin = ? WHERE username = ? AND pin = ?";

        try (PreparedStatement pst = conn.prepareStatement(query1);
             PreparedStatement pst2 = conn.prepareStatement(query2)) {

            ResultSet rs = pst.executeQuery();
            boolean success = false;

            String newUsername = "";
            while (true) {
                System.out.println("Enter new Username: ");
                newUsername = sc.nextLine();
                if (!newUsername.isEmpty()) {
                    boolean usernameExists = false;
                    while (rs.next()) {
                        if (rs.getString(1).equals(newUsername)) {
                            if (rs.getString(1).equals(loginuser)) {
                                usernameExists = false;
                            } else {
                                usernameExists = true;
                            }
                            break;
                        }
                    }
                    if (usernameExists) {
                        System.out.println("Username already exists. Please choose a different username.");
                    } else {
                        break;
                    }
                } else {
                    System.out.println("Username cannot be empty. Please enter a valid username.");
                }
            }

            sc.nextLine();
            System.out.println("Enter new Pin (4 digits): ");
            String newPin = sc.next();
            if (newUsername != null && newPin != null && newPin.length() == 4 && newPin.matches("\\d{4}")) {
                pst2.setString(1, newUsername);
                pst2.setString(2, Product_Order_Management.encryptString(newPin));
                pst2.setString(3, loginuser);
                pst2.setString(4, loginpin);
                pst2.execute();
                success = true;
            } else {
                System.out.println("==> Invalid username format OR Invalid pin format (must be 4 digits only). <==");
            }

            if (success) {
                Product_Order_Management.loginuser = newUsername;
                Product_Order_Management.loginpin = Product_Order_Management.encryptString(newPin);
                customerLogger.info("Customer updated username to: " + newUsername);
                customerLogger.info("Customer updated pin.");
                System.out.println("==> Your Username and Pin updated <==");
            }

        } catch (Exception e) {
            customerLogger.severe("Error during customer credentials update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean checkCustomer(Connection conn, String user, String pin) {
        String query1 = "SELECT username, pin FROM customers";
        try (PreparedStatement pst = conn.prepareStatement(query1)) {
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                if (rs.getString(1).equals(user) && rs.getString(2).equals(Product_Order_Management.encryptString(pin))) {
                    customerLogger.info("Customer login successful for username: " + user);
                    return true;
                }
            }
        } catch (Exception e) {
            customerLogger.severe("Error during customer login check: " + e.getMessage());
            e.printStackTrace();
        }
        customerLogger.warning("Failed login attempt for username: " + user);
        return false;
    }

    public static void displayCustomers(Connection conn) {
        String query = "SELECT * FROM customers";
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(query);

            System.out.printf("%-10s %-23s %-15s %-30s %s\n", "ID", "Customer Name", "Customer Age", "Email", "User Name");
            while (rs.next()) {
                System.out.printf("%-10s %-23s %-15s %-30s %s\n", rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5));
            }
            adminLogger.info("Admin viewed all customers.");
        } catch (Exception e) {
            adminLogger.severe("Error displaying customers: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
