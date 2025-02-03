package com.logilite.jdbc_product_order_management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class Order {
    private static final Logger adminLogger = LoggerConfig.getLogger("AdminLogger", "admin.log");
    private static final Logger customerLogger = LoggerConfig.getLogger("CustomerLogger", "customer.log");

    private double totalPrice;
    private double price;
    private double discount;
    private double tax;
    private static double finalPrice;
    private boolean paymentStatus = false;

    private int customerId;

    public Order(int customerId) {
        this.customerId = customerId;
    }

    public boolean isPaid() {
        return paymentStatus;
    }

    public void markAsPaid() {
        this.paymentStatus = true;
        customerLogger.info("Customer ID " + customerId + " marked the order as paid.");
    }

    public void calculateTotalPrice(Connection conn) {
        String checkPendingOrdersQuery = "SELECT o.order_id FROM orders o WHERE o.customer_id = ? AND o.payment_status = 'Pending'";
        boolean hasPendingOrders = false;

        try (PreparedStatement pst = conn.prepareStatement(checkPendingOrdersQuery)) {
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                hasPendingOrders = true;
                customerLogger.info("Customer ID " + customerId + " has pending orders.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            customerLogger.severe("Error checking for pending orders for customer ID " + customerId + ": " + e.getMessage());
        }

        if (hasPendingOrders) {
            calculateFromPendingOrders(conn);
        } else {
            calculateFromCart(conn);
        }
    }

    private void calculateFromPendingOrders(Connection conn) {
        String query = "SELECT p.product_id, p.price, op.quantity " +
                       "FROM order_products op " +
                       "JOIN products p ON op.product_id = p.product_id " +
                       "JOIN orders o ON op.order_id = o.order_id " +
                       "WHERE o.customer_id = ? AND o.payment_status = 'Pending'";

        totalPrice = 0.0;
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                double productPrice = rs.getDouble("price");
                int productQuantity = rs.getInt("quantity");
                totalPrice += productPrice * productQuantity;
            }

            this.price = totalPrice;
            customerLogger.info("Customer ID " + customerId + " calculated total price from pending orders: ₹" + totalPrice);
        } catch (SQLException e) {
            e.printStackTrace();
            customerLogger.severe("Error calculating total price from pending orders for customer ID " + customerId + ": " + e.getMessage());
        }
    }

  
    private void calculateFromCart(Connection conn) {
        String query = "SELECT p.product_id, p.price, c.quantity " +
                       "FROM cart c " +
                       "JOIN products p ON c.product_id = p.product_id " +
                       "WHERE c.customer_id = ?";

        totalPrice = 0.0;
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                double productPrice = rs.getDouble("price");
                int productQuantity = rs.getInt("quantity");
                totalPrice += productPrice * productQuantity;
            }

            this.price = totalPrice;
            customerLogger.info("Customer ID " + customerId + " calculated total price from cart: ₹" + totalPrice);
        } catch (SQLException e) {
            e.printStackTrace();
            customerLogger.severe("Error calculating total price from cart for customer ID " + customerId + ": " + e.getMessage());
        }
    }

    public void applyDiscount(double totalDiscount) {
        discount = (totalPrice * totalDiscount) / 100;
        totalPrice -= discount;
        customerLogger.info("Customer ID " + customerId + " applied discount of ₹" + discount);
    }

    public void calculateTax(double taxRate) {
        tax = (totalPrice * taxRate) / 100;
        finalPrice = totalPrice + tax;
        customerLogger.info("Customer ID " + customerId + " calculated tax of ₹" + tax);
    }
    
    public String getOrder(Connection conn, String loginuser, String loginpin) {
        StringBuilder sb = new StringBuilder();
        String selectCustomer = "SELECT * FROM customers WHERE username = ? AND pin = ?";
        String pendingOrdersQuery = "SELECT order_id FROM orders WHERE customer_id = ? AND payment_status = 'Pending'";
        String cartQuery = "SELECT p.product_id, p.name, p.price, c.quantity " +
                           "FROM cart c " +
                           "JOIN products p ON c.product_id = p.product_id " +
                           "WHERE c.customer_id = ?";
        String orderProductsQuery = "SELECT p.product_id, p.name, p.price, op.quantity " +
                                    "FROM order_products op " +
                                    "JOIN products p ON op.product_id = p.product_id " +
                                    "WHERE op.order_id = ?";

        try (PreparedStatement cst = conn.prepareStatement(selectCustomer)) {
            cst.setString(1, loginuser);
            cst.setString(2, loginpin);
            
            ResultSet rst = cst.executeQuery();

            if (rst.next()) {
                int customerId = rst.getInt("customer_id");
                sb.append("Customer Name: ").append(rst.getString("name")).append("\n");
                sb.append("Customer Age: ").append(rst.getInt("age")).append("\n");

                try (PreparedStatement pst = conn.prepareStatement(pendingOrdersQuery)) {
                    pst.setInt(1, customerId);
                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        sb.append("Pending Orders:\n");
                        do {
                            int orderId = rs.getInt("order_id");
                            sb.append("Order ID: ").append(orderId).append("\n");

                            try (PreparedStatement pst2 = conn.prepareStatement(orderProductsQuery)) {
                                pst2.setInt(1, orderId);
                                ResultSet rs2 = pst2.executeQuery();

                                sb.append("Products in this Order:\n");
                                while (rs2.next()) {
                                    sb.append("\tProduct Name: ").append(rs2.getString("name"))
                                      .append("\tPrice: ₹").append(rs2.getDouble("price"))
                                      .append("\tQuantity: ").append(rs2.getInt("quantity"))
                                      .append("\n");
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                sb.append("Error fetching products for order ID ").append(orderId).append("\n");
                            }
                        } while (rs.next());
                    } else {
                        sb.append("No pending orders. Showing all products in the cart:\n");

                        try (PreparedStatement pst3 = conn.prepareStatement(cartQuery)) {
                            pst3.setInt(1, customerId);
                            ResultSet rs3 = pst3.executeQuery();

                            sb.append("Products in Cart:\n");
                            while (rs3.next()) {
                                sb.append("\tProduct Name: ").append(rs3.getString("name"))
                                  .append("\tPrice: ₹").append(rs3.getDouble("price"))
                                  .append("\tQuantity: ").append(rs3.getInt("quantity"))
                                  .append("\n");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            sb.append("Error fetching products from cart for customer ID ").append(customerId).append("\n");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    sb.append("Error checking for pending orders for customer ID ").append(rst.getInt("customer_id")).append("\n");
                }
            } else {
                sb.append("No customer found with the provided username and PIN.\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sb.append("Error fetching customer data: ").append(e.getMessage()).append("\n");
        }
        
        sb.append("\n-----------------------------\n");
        sb.append("Total Price: ₹").append(String.format("%-8.2f", price)).append("\n");
        sb.append("Discount: ₹").append(String.format("%-8.2f", discount)).append("\n");
        sb.append("Tax: ₹").append(String.format("%-8.2f", tax)).append("\n");
        sb.append("Final Price: ₹").append(String.format("%-8.2f", finalPrice)).append("\n");
        sb.append("-----------------------------");
        sb.append("\nPayment Status: ").append(paymentStatus ? "Paid" : "Not Paid").append("\n");

        return sb.toString();
    }


    public String getOrderSummary(Connection conn, String loginuser, String loginpin) {
        StringBuilder sb = new StringBuilder();

        String query = "SELECT name, age, order_id, order_date FROM customers JOIN orders ON customers.customer_id = orders.customer_id WHERE customers.customer_id = ?";
        String selectCustomer = "SELECT * FROM customers WHERE username = ? AND pin = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             PreparedStatement cst = conn.prepareStatement(selectCustomer)) {

            cst.setString(1, loginuser);
            cst.setString(2, loginpin);

            ResultSet rst = cst.executeQuery();

            while (rst.next()) {
                stmt.setInt(1, rst.getInt("customer_id"));
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    sb.append("<html><body style='font-family: Arial, sans-serif; color: #333;'>")
                      .append("<h2 style='color: #5D8AA8;'>Order Summary for Customer: ").append(rs.getString("name")).append("</h2>")
                      .append("<h3>Order Date: ").append(rs.getString("order_date")).append("</h3>")
                      .append("<table style='width: 100%; border-collapse: collapse; margin-bottom: 20px;'>")
                      .append("<tr style='background-color: #f2f2f2;'><th style='text-align: left; padding: 8px;'>Order ID</th><td style='padding: 8px;'>").append(rs.getInt("order_id")).append("</td></tr>")
                      .append("<tr><th style='text-align: left; padding: 8px;'>Customer Name</th><td style='padding: 8px;'>").append(rs.getString("name")).append("</td></tr>")
                      .append("<tr style='background-color: #f2f2f2;'><th style='text-align: left; padding: 8px;'>Customer Age</th><td style='padding: 8px;'>").append(rs.getInt("age")).append("</td></tr>")
                      .append("</table>");

                }

                sb.append("<h3 style='color: #4CAF50;'>Products :</h3>");
                sb.append("<table style='width: 100%; border-collapse: collapse; margin-bottom: 20px;'>")
                  .append("<tr style='background-color: #e0e0e0;'><th style='text-align: left; padding: 8px;'>Product Name</th><th style='text-align: left; padding: 8px;'>Price</th><th style='text-align: left; padding: 8px;'>Quantity</th></tr>");

                query = "SELECT p.product_id, p.name, p.price, c.quantity " +
                        "FROM cart c " +
                        "JOIN products p ON c.product_id = p.product_id " +
                        "WHERE c.customer_id = ?";
                try (PreparedStatement pst2 = conn.prepareStatement(query)) {
                    pst2.setInt(1, rst.getInt("customer_id"));
                    ResultSet rs2 = pst2.executeQuery();

                    while (rs2.next()) {
                        sb.append("<tr><td style='padding: 8px;'>").append(rs2.getString("name"))
                          .append("</td><td style='padding: 8px;'>₹").append(rs2.getDouble("price"))
                          .append("</td><td style='padding: 8px;'>").append(rs2.getInt("quantity"))
                          .append("</td></tr>");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    customerLogger.severe("Error fetching products for order summary of customer ID " + rst.getInt("customer_id") + ": " + e.getMessage());
                }
                sb.append("</table>");

            }
        } catch (SQLException e) {
            e.printStackTrace();
            customerLogger.severe("Error fetching order summary for customer: " + e.getMessage());
        }

        sb.append("<h3 style='color: #FF7043;'>Pricing Summary:</h3>")
          .append("<table style='width: 100%; border-collapse: collapse;'>")
          .append("<tr style='background-color: #f2f2f2;'><th style='text-align: left; padding: 8px;'>Total Price</th><td style='padding: 8px;'>₹").append(String.format("%.2f", totalPrice)).append("</td></tr>")
          .append("<tr><th style='text-align: left; padding: 8px;'>Discount</th><td style='padding: 8px;'>₹").append(String.format("%.2f", discount)).append("</td></tr>")
          .append("<tr style='background-color: #f2f2f2;'><th style='text-align: left; padding: 8px;'>Tax</th><td style='padding: 8px;'>₹").append(String.format("%.2f", tax)).append("</td></tr>")
          .append("<tr><th style='text-align: left; padding: 8px;'>Final Price</th><td style='padding: 8px;'>₹").append(String.format("%.2f", finalPrice)).append("</td></tr>")
          .append("</table>");

        sb.append("<h3 style='color: #FF8C00;'>Payment Status:</h3>")
          .append("<b style='font-size: 18px;'>").append(paymentStatus ? "<span style='color: green;'>Paid</span>" : "<span style='color: red;'>Refunded</span>").append("</b>");

        sb.append("</body></html>");

        return sb.toString();
    }


    public void saveOrderToDatabase(Connection conn, int customer_id) {
        if (paymentStatus) {
            String updateOrderSQL = "UPDATE orders SET payment_status = ? WHERE customer_id = ? AND payment_status = 'Pending'";

            try (PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderSQL)) {
                updateOrderStmt.setString(1, "Success");
                updateOrderStmt.setInt(2, customer_id);
                updateOrderStmt.execute();
                adminLogger.info("Admin updated payment status to 'Success' for customer ID " + customer_id);
                return;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                adminLogger.severe("Error updating payment status for customer ID " + customer_id + ": " + e.getMessage());
            }
        }

        String insertOrderSQL = "INSERT INTO orders (customer_id, total_price, discount, tax, final_price, payment_status) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
        String insertOrderProductsSQL = "INSERT INTO order_products (order_id, product_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement orderProductsStmt = conn.prepareStatement(insertOrderProductsSQL)) {

            orderStmt.setInt(1, customerId);
            orderStmt.setDouble(2, price);
            orderStmt.setDouble(3, discount);
            orderStmt.setDouble(4, tax);
            orderStmt.setDouble(5, finalPrice);
            orderStmt.setString(6, "Pending");
            orderStmt.executeUpdate();

            ResultSet rs = orderStmt.getGeneratedKeys();
            if (rs.next()) {
                int generatedOrderId = rs.getInt(1);

                String productQuery = "SELECT product_id, quantity FROM cart WHERE customer_id = ?";
                try (PreparedStatement productStmt = conn.prepareStatement(productQuery)) {
                    productStmt.setInt(1, customerId);
                    ResultSet productRs = productStmt.executeQuery();

                    while (productRs.next()) {
                        String productId = productRs.getString("product_id");
                        int quantity = productRs.getInt("quantity");

                        orderProductsStmt.setInt(1, generatedOrderId);
                        orderProductsStmt.setInt(2, Integer.parseInt(productId));
                        orderProductsStmt.setInt(3, quantity);
                        orderProductsStmt.addBatch();
                    }
                    orderProductsStmt.executeBatch();
                }
                adminLogger.info("Admin created new order for customer ID " + customerId + " with Order ID " + generatedOrderId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            customerLogger.severe("Error saving order to database for customer ID " + customerId + ": " + e.getMessage());
        }
    }

    public static void readAllOrdersFromDatabase(Connection conn) {
        String selectOrdersSQL = "SELECT o.order_id, c.name, o.total_price, o.discount, o.tax, o.final_price, o.payment_status " +
                                 "FROM orders o JOIN customers c ON o.customer_id = c.customer_id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectOrdersSQL)) {

        	if (!rs.next()) {
        		System.out.println("==> Not any orders created by customer! <==");
        		return;
        	}
        	
            System.out.printf("%-15s %-25s %-15s %-15s %-15s %-15s %s\n", "Order ID", "Customer", "Total Price", "Discount", "Tax", "Final Price", "Payment Status");
            do{
                int orderId = rs.getInt("order_id");
                String customerName = rs.getString("name");
                double totalPrice = rs.getDouble("total_price");
                double discount = rs.getDouble("discount");
                double tax = rs.getDouble("tax");
                double finalPrice = rs.getDouble("final_price");
                String paymentStatus = rs.getString("payment_status");

                System.out.printf("%-15s %-25s %-15s %-15s %-15s %-15s %s\n", orderId, customerName, totalPrice, discount, tax, finalPrice, paymentStatus);
            }while (rs.next());

            adminLogger.info("Admin retrieved all orders from the database.");

        } catch (SQLException e) {
            e.printStackTrace();
            adminLogger.severe("Error reading orders from database: " + e.getMessage());
        }
    }
}
