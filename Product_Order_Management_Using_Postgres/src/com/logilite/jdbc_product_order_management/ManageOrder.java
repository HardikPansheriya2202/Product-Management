package com.logilite.jdbc_product_order_management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Logger;

import com.logilite.email_implementation.Email;

public class ManageOrder {

    private static final Logger adminLogger = LoggerConfig.getLogger("AdminLogger", "admin.log");
    private static final Logger customerLogger = LoggerConfig.getLogger("CustomerLogger", "customer.log");

    public static void processOrder(Connection conn, Scanner sc, String loginuser, String loginpin) throws InterruptedException {
        String selectCustomer = "SELECT * FROM customers WHERE username = ? AND pin = ?";
        String selectCart = "SELECT * FROM cart WHERE customer_id = ?";
        String selectOrder = "SELECT * FROM orders WHERE customer_id = ? AND payment_status = 'Pending'";
        String deleteOrder = "DELETE FROM orders WHERE customer_id = ? AND payment_status = 'Pending'";
        String deleteOrderProduct = "DELETE FROM order_products WHERE order_id = ?";

        try (PreparedStatement cst = conn.prepareStatement(selectCustomer);
             PreparedStatement cartCst = conn.prepareStatement(selectCart);
             PreparedStatement orderPst = conn.prepareStatement(selectOrder);
        	 PreparedStatement deletePst = conn.prepareStatement(deleteOrder);
        	 PreparedStatement deleteOPPst = conn.prepareStatement(deleteOrderProduct)) {

            cst.setString(1, loginuser);
            cst.setString(2, loginpin);

            ResultSet rst = cst.executeQuery();
            
            if (rst.next()) {
                orderPst.setInt(1, rst.getInt("customer_id"));
                ResultSet orderRst = orderPst.executeQuery();

                if (orderRst.next()) {
                    Order pendingOrder = new Order(rst.getInt("customer_id"));
                    pendingOrder.calculateTotalPrice(conn);
                    pendingOrder.applyDiscount(15.0);
                    pendingOrder.calculateTax(5.0);
                    System.out.println("\n==> You have a pending order. Please complete the payment for the pending order first. <==\n");
                    System.out.println(pendingOrder.getOrder(conn, loginuser, loginpin));
                    
                    sc.nextLine();
                    System.out.println("You want to cancel order (y/n) : ");
                	String ans = sc.nextLine();
                	
                	if (ans.equalsIgnoreCase("y")) {
                		
                		deleteOPPst.setInt(1, orderRst.getInt("order_id"));
                		deleteOPPst.execute();
                		
                		deletePst.setInt(1, rst.getInt("customer_id"));
                		
                		int affected_row = deletePst.executeUpdate();
                		
                		if (affected_row > 0) {
                			System.out.println("==> Your order is cancelled. <==");
                			return;
                		}
                	}
                	

                	PaymentProcessor.paymentCompleted = false;
                    PaymentProcessor.paymentType = "";
                    PaymentProcessor.PaymentMainMenu(conn);

                    if (PaymentProcessor.paymentCompleted && PaymentProcessor.paymentType.equalsIgnoreCase("paid")) {
                        pendingOrder.markAsPaid();
                        pendingOrder.saveOrderToDatabase(conn, rst.getInt("customer_id"));

                        Email.send(rst.getString("email"), "Order History", pendingOrder.getOrderSummary(conn, loginuser, loginpin));

                        removeProductsFromCart(conn, rst.getInt("customer_id"), pendingOrder);

                        customerLogger.info("Customer " + loginuser + " successfully completed the pending order and payment.");
                    }else if (PaymentProcessor.paymentCompleted && PaymentProcessor.paymentType.equalsIgnoreCase("refund")) {
                    	Email.send(rst.getString("email"), "Order History", pendingOrder.getOrderSummary(conn, loginuser, loginpin));
                    	customerLogger.info("Customer " + loginuser + " Refund successfully completed.");
    				}  else {
                        System.out.println("Payment failed. Please try again.");
                        customerLogger.warning("Payment failed for customer " + loginuser);
                    }

                    return;
                }

                cartCst.setInt(1, rst.getInt("customer_id"));
                ResultSet cartRst = cartCst.executeQuery();

                if (!cartRst.next()) {
                    System.out.println("==> Your Cart Is Empty. Payment Process Failed! <==");
                    adminLogger.warning("Customer " + loginuser + " attempted to place an order but the cart is empty.");
                    return;
                }

                Order order = new Order(rst.getInt("customer_id"));
                order.calculateTotalPrice(conn);
                order.applyDiscount(15.0);
                order.calculateTax(5.0);
                
                order.saveOrderToDatabase(conn, rst.getInt("customer_id"));

                PaymentProcessor.paymentCompleted = false;
                PaymentProcessor.paymentType = "";
                PaymentProcessor.PaymentMainMenu(conn);

                if (PaymentProcessor.paymentCompleted && PaymentProcessor.paymentType.equalsIgnoreCase("paid")) {
                    order.markAsPaid();
                    order.saveOrderToDatabase(conn, rst.getInt("customer_id"));

                    Email.send(rst.getString("email"), "Order History", order.getOrderSummary(conn, loginuser, loginpin));

                    removeProductsFromCart(conn, rst.getInt("customer_id"), order);
                    customerLogger.info("Customer " + loginuser + " successfully completed the order and payment.");
                }else if (PaymentProcessor.paymentCompleted && PaymentProcessor.paymentType.equalsIgnoreCase("refund")) {
                	Email.send(rst.getString("email"), "Order History", order.getOrderSummary(conn, loginuser, loginpin));
                	customerLogger.info("Customer " + loginuser + " Refund successfully completed.");
				} 
                else {
                    System.out.println("Payment failed. Please try again.");
                    customerLogger.warning("Payment failed for customer " + loginuser);
                }

            } else {
                System.out.println("==> Customer not found! <==");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            adminLogger.severe("Error processing order for customer " + loginuser + ": " + e.getMessage());
        }
    }

    public static void removeProductsFromCart(Connection conn, int customerId, Order order) {
        String deleteOrderProductsSQL = "DELETE FROM cart WHERE customer_id = ? AND product_id IN (SELECT product_id FROM order_products WHERE order_id = ?)";
        String selectOrderSQL = "SELECT order_id FROM orders WHERE customer_id = ? AND payment_status = 'Success' ORDER BY order_id DESC LIMIT 1";

        try (PreparedStatement selectOrderStmt = conn.prepareStatement(selectOrderSQL);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteOrderProductsSQL)) {

            selectOrderStmt.setInt(1, customerId);
            ResultSet orderRs = selectOrderStmt.executeQuery();

            if (orderRs.next()) {
                int orderId = orderRs.getInt("order_id");

                deleteStmt.setInt(1, customerId);
                deleteStmt.setInt(2, orderId);
                int rowsAffected = deleteStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Specific products from your cart have been removed.");
                } else {
                    System.out.println("No products to remove from the cart.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            adminLogger.severe("Error removing products from cart for customer " + customerId + ": " + e.getMessage());
        }
    }
}
