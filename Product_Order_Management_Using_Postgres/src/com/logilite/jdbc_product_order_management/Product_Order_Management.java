package com.logilite.jdbc_product_order_management;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Product_Order_Management {

    private static final Logger adminLogger = LoggerConfig.getLogger("AdminLogger", "admin.log");
    private static final Logger customerLogger = LoggerConfig.getLogger("CustomerLogger", "customer.log");

    protected static String loginuser = "";
    protected static String loginpin = "";
    private static MessageDigest md;

    public static String encryptString(String s) {
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] sBytes = s.getBytes();
            md.reset();
            byte[] digested = md.digest(sBytes);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digested.length; i++) {
                sb.append(Integer.toHexString(0xff & digested[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Product_Order_Management.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static String inputUser(Scanner sc) {
        String user;
        while (true) {
            sc.nextLine();
            System.out.println("Enter User Name : ");
            user = sc.next();

            if (user != null && user.matches("[a-zA-Z]+") && (!user.contains(" "))) {
                return user;
            } else {
                System.out.println("==> User is not null, only contains alphabet, and does not contain spaces <==");
            }
        }
    }

    private static String inputPin(Scanner sc) {
        String pin;
        while (true) {
            System.out.println("Enter Pin : ");
            pin = sc.next();

            if (pin != null && pin.length() == 4) {
                return pin;
            } else {
                System.out.println("==> Pin is not null and should have 4 digits <==");
                sc.nextLine();
            }
        }
    }

    private static void adminPanel(Scanner sc, Connection conn) throws InterruptedException {
        System.out.println("\t\t\t\t--------> Switched to Admin panel <--------");
        adminLogger.info("Admin panel access granted");

        boolean flag = false;
        int num = 0;
        do {
            System.out.println("\n1. Enter 1 for --> Show Customers");
            System.out.println("2. Enter 2 for --> Show Products");
            System.out.println("3. Enter 3 for --> Add Products");
            System.out.println("4. Enter 4 for --> Update Product");
            System.out.println("5. Enter 5 for --> Remove Product");
            System.out.println("6. Enter 6 for --> Display Cart");
            System.out.println("7. Enter 7 for --> Show All Transactions");
            System.out.println("8. Enter 8 for --> Show All Orders");
            System.out.println("9. Enter 9 for --> Change Admin Username/Pin");
            System.out.println("10. Enter 10 for --> Exit\n");

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
                case 1: {
                    Customer.displayCustomers(conn);
                    adminLogger.info("Displayed customers list");
                    break;
                }
                case 2: {
                    ManageProduct.displayProducts(conn);
                    adminLogger.info("Displayed product list");
                    break;
                }
                case 3: {
                    ManageProduct.addProduct(conn, sc);
                    adminLogger.info("Product added");
                    break;
                }
                case 4: {
                    ManageProduct.updateProduct(conn, sc);
                    adminLogger.info("Product updated");
                    break;
                }
                case 5: {
                    ManageProduct.removeProduct(conn, sc);
                    adminLogger.info("Product removed");
                    break;
                }
                case 6: {
                    ManageProduct.displayCart(conn);
                    adminLogger.info("Displayed cart");
                    break;
                }
                case 7: {
                    PaymentProcessor.displayTransactions(conn);
                    adminLogger.info("Displayed all transactions");
                    break;
                }
                case 8: {
                    Order.readAllOrdersFromDatabase(conn);
                    adminLogger.info("Displayed all orders");
                    break;
                }
                case 9: {
                    Admin.changeAdminCredentials(sc, conn);
                    adminLogger.info("Admin credentials changed");
                    break;
                }
                case 10: {
                    System.out.println("Exit from admin panel...");
                    adminLogger.info("Exiting admin panel");
                    Product_Order_Management.mainMenu(sc, conn);
                    break;
                }
                default:
                    System.out.println("-- Enter Only 1 to 10 Number to Perform Task --\n");
            }
        } while (!flag);
    }

    public static void customerPanel(Scanner sc, Connection conn) throws InterruptedException {
        System.out.println("\t\t\t\t--------> Switched to Customer panel <--------");
        customerLogger.info("Customer panel access granted");

        boolean flag = false;
        int num = 0;
        do {
            System.out.println("\n1. Enter 1 --> for Show Products");
            System.out.println("2. Enter 2 --> for Buy Products");
            System.out.println("3. Enter 3 --> for Show Your Cart");
            System.out.println("4. Enter 4 --> for Remove Product From Cart");
            System.out.println("5. Enter 5 --> for Clear Full Cart");
            System.out.println("6. Enter 6 --> for Process Order");
            System.out.println("7. Enter 7 --> for Add Payment Data");
            System.out.println("8. Enter 8 --> for Change Your Username/Pin");
            System.out.println("9. Enter 9 --> for Exit\n");

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
                case 1: {
                    ManageProduct.displayProducts(conn);
                    customerLogger.info("Displayed product list");
                    break;
                }
                case 2: {
                    ManageProduct.buyProduct(conn, sc, loginuser, loginpin);
                    customerLogger.info("Customer bought products");
                    break;
                }
                case 3: {
                    ManageProduct.displaySpecificCart(conn, loginuser, loginpin);
                    customerLogger.info("Displayed customer cart");
                    break;
                }
                case 4: {
                    ManageProduct.removeProductFromCart(conn, sc, loginuser, loginpin);
                    customerLogger.info("Customer removed product from cart");
                    break;
                }
                case 5: {
                    ManageProduct.removeAllCartItem(conn, loginuser, loginpin);
                    customerLogger.info("Customer cleared full cart");
                    break;
                }
                case 6: {
                    ManageOrder.processOrder(conn, sc, loginuser, loginpin);
                    customerLogger.info("Customer processed order");
                    break;
                }
                case 7: {
                    PaymentProcessor.addPaymentData(conn, sc, loginuser, loginpin);
                    customerLogger.info("Customer Added Payment Data");
                    break;
                }
                case 8: {
                    Customer.changeCustomerCredentials(sc, conn, loginuser, loginpin);
                    customerLogger.info("Customer credentials changed");
                    break;
                }
                case 9: {
                    System.out.println("Exit from customer panel...");
                    customerLogger.info("Exiting customer panel");
                    Product_Order_Management.mainMenu(sc, conn);
                    break;
                }
                default:
                    System.out.println("-- Enter Only 1 to 9 Number to Perform Task --\n");
            }
        } while (!flag);
    }

    public static void mainMenu(Scanner sc, Connection conn) throws InterruptedException {
        System.out.println("\t\t\t\t--------> Welcome To Product Order Management System <--------");
        int num = 0;
        do {
            System.out.println("\n1. Enter 1 --> for Login Admin Panel");
            System.out.println("2. Enter 2 --> for Login Customer Panel");
            System.out.println("3. Enter 3 --> for Signup Customer Panel");
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
                case 1: {
                    if (Admin.checkAdmin(sc, conn)) {
                        adminLogger.info("Admin logged in");
                        Product_Order_Management.adminPanel(sc, conn);
                    }
                    break;
                }
                case 2: {
                    String user = inputUser(sc);
                    String pin = inputPin(sc);

                    if (Customer.checkCustomer(conn, user, pin)) {
                        loginuser = user;
                        loginpin = encryptString(pin);
                        customerLogger.info("Customer logged in");
                        Product_Order_Management.customerPanel(sc, conn);
                    } else {
                        System.out.println("==> Customer Not Found. Please Signup First <==");
                    }
                    break;
                }
                case 3: {
                    if (Customer.SignupCustomer(sc, conn)) {
                        customerLogger.info("Customer signed up");
                        PaymentProcessor.addPaymentData(conn, sc, loginuser, loginpin);
                        Product_Order_Management.customerPanel(sc, conn);
                    } else {
                        System.out.println("==> Invalid data, customer not registered! <==");
                    }
                    break;
                }
                case 4: {
                    System.out.println("Exit...");
                    System.exit(0);
                }
                default:
                    System.out.println("-- Enter Only 1 to 4 Number to Perform Task --\n");
            }
        } while (num != 4);
    }

    public static void main(String[] args) {
        try (Connection conn = DBConnect.getConnection();
             Scanner sc = new Scanner(System.in)) {
            Product_Order_Management.mainMenu(sc, conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
