package com.logilite.jdbc_product_order_management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Logger;

public class ManageProduct
{
    private static final Logger adminLogger = LoggerConfig.getLogger("AdminLogger", "admin.log");
    private static final Logger customerLogger = LoggerConfig.getLogger("CustomerLogger", "customer.log");

    public static void addProduct(Connection conn, Scanner sc) {
        System.out.println("Enter category: ");
        String category = sc.next();
        
        sc.nextLine();
        System.out.println("Enter product name: ");
        String productName = sc.nextLine();

        int quan = 0;
        while (true) {
            try {
                System.out.println("Enter product quantity (positive integer): ");
                quan = sc.nextInt();

                if (quan > 0) {
                    break;
                } else {
                    System.out.println("==> Quantity must be greater than 0 <==");
                }
            } catch (Exception e) {
                sc.next();
                System.out.println("==> Enter a valid integer value <==");
            }
        }

        double price = 0.0;
        while (true) {
            try {
                System.out.println("Enter product price (positive number): ");
                price = sc.nextDouble();
                if (price > 0) {
                    break;
                } else {
                    System.out.println("==> Price must be greater than 0 <==");
                }
            } catch (Exception e) {
                sc.next();
                System.out.println("==> Enter a valid price <==");
            }
        }

        String insertProductSQL = "INSERT INTO products (name, price, quantity, category) VALUES (?, ?, ?, ?)";
        String selectProductSQL = "SELECT * FROM products WHERE category = ? AND name = ?";
        String updateProductSQL = "UPDATE products SET quantity = quantity + ? WHERE category = ? AND name = ?";

        try (PreparedStatement ps = conn.prepareStatement(insertProductSQL);
             PreparedStatement selectPst = conn.prepareStatement(selectProductSQL);
             PreparedStatement updatePst = conn.prepareStatement(updateProductSQL)) {

            int affectedRows = 0;
            
            selectPst.setString(1, category);
            selectPst.setString(2, productName);
            ResultSet rSet = selectPst.executeQuery();

            if (rSet.next()) {
                updatePst.setInt(1, quan);
                updatePst.setString(2, category);
                updatePst.setString(3, productName);
                affectedRows = updatePst.executeUpdate();
            } else {
                ps.setString(1, productName);
                ps.setDouble(2, price);
                ps.setInt(3, quan);
                ps.setString(4, category);
                affectedRows = ps.executeUpdate();
            }

            if (affectedRows > 0) {
                System.out.println("Product added successfully.");
                adminLogger.info("Product added: " + productName + " in category: " + category + ", Quantity: " + quan + ", Price: ₹" + price);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            adminLogger.warning("Error adding product: " + e.getMessage());
        }
    }
    
    

    public static void updateProduct(Connection conn, Scanner sc) {
        int id = 0;
        while (true) {
            System.out.println("Enter product ID to update: ");
            if (sc.hasNextInt()) {
                id = sc.nextInt();
                break;
            } else {
                sc.next();
                System.out.println("==> Enter a valid integer product ID <==");
            }
        }

        sc.nextLine();
        System.out.println("Enter new name: ");
        String newName = sc.nextLine();

        int newQuantityInput = 0;
        while (true) {
            System.out.println("Enter new quantity (positive integer): ");
            if (sc.hasNextInt()) {
                newQuantityInput = sc.nextInt();
                if (newQuantityInput > 0) {
                    break;
                } else {
                    System.out.println("==> Quantity must be greater than 0 <==");
                }
            } else {
                sc.next();
                System.out.println("==> Enter a valid integer quantity <==");
            }
        }

        double newPriceInput = 0.0;
        while (true) {
            System.out.println("Enter new price (positive number): ");
            if (sc.hasNextDouble()) {
                newPriceInput = sc.nextDouble();
                if (newPriceInput > 0) {
                    break;
                } else {
                    System.out.println("==> Price must be greater than 0 <==");
                }
            } else {
                sc.next();
                System.out.println("==> Enter a valid price <==");
            }
        }

        String updateProductSQL = "UPDATE products SET name = ?, quantity = ?, price = ? WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(updateProductSQL)) {
            ps.setString(1, newName);
            ps.setInt(2, newQuantityInput);
            ps.setDouble(3, newPriceInput);
            ps.setInt(4, id);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Product with ID " + id + " updated.");
                adminLogger.info("Product with ID " + id + " updated: New name: " + newName + ", Quantity: " + newQuantityInput + ", Price: ₹" + newPriceInput);
            } else {
                System.out.println("Product with ID " + id + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            adminLogger.warning("Error updating product: " + e.getMessage());
        }
    }

    public static void removeProduct(Connection conn, Scanner sc) {
        int id = 0;
        while (true) {
            System.out.println("Enter product ID to remove: ");
            if (sc.hasNextInt()) {
                id = sc.nextInt();
                break;
            } else {
                sc.next();
                System.out.println("==> Enter a valid integer product ID <==");
            }
        }

        String deleteProductSQL = "DELETE FROM products WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(deleteProductSQL)) {

            ps.setInt(1, id);

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Product with ID " + id + " removed.");
                adminLogger.info("Product with ID " + id + " removed.");
            } else {
                System.out.println("Product with ID " + id + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            adminLogger.warning("Error removing product: " + e.getMessage());
        }
    }
    
    public static void displayProducts(Connection conn) {
        String selectProductSQL = "SELECT * FROM products ORDER BY category ASC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectProductSQL)) {

            System.out.printf("%-15s %-15s %-15s %-15s %s\n", "Product_ID", "Category", "Product", "Quantity", "Price(₹)");

            if (!rs.next()) {
            	System.out.println("==> Not any product added yet! <==");
            	return;
            }
            do{
                System.out.printf("%-15s %-15s %-15s %-15s %.2f\n",
                        rs.getInt("product_id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"));
            }while (rs.next());
        } catch (SQLException e) {
        	e.printStackTrace();
            adminLogger.warning("Error displaying products: " + e.getMessage());
        }
 }

    public static boolean buyProduct(Connection conn, Scanner sc, String username, String pin) {
        int productId = 0;
        while (true) {
            System.out.println("Enter product ID to buy: ");
            if (sc.hasNextInt()) {
                productId = sc.nextInt();
                break;
            } else {
                sc.next();
                System.out.println("==> Enter a valid integer product ID <==");
            }
        }

        int quantity = 0;
        while (true) {
            System.out.println("Enter product's quantity: ");
            if (sc.hasNextInt()) {
                quantity = sc.nextInt();
                if (quantity > 0) {
                    break;
                } else {
                    System.out.println("==> Quantity must be greater than 0 <==");
                }
            } else {
                sc.next();
                System.out.println("==> Enter a valid quantity <==");
            }
        }

        String selectProductSQL = "SELECT * FROM products WHERE product_id = ?";
        String selectCustomerSQL = "SELECT * FROM customers WHERE username = ? AND pin = ?";
        String updateProductStockSQL = "UPDATE products SET quantity = quantity - ? WHERE product_id = ?";
        String selectCartSQL = "SELECT * FROM cart WHERE customer_id = ? AND product_id = ?";
        String insertCartSQL = "INSERT INTO cart (customer_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateCartSQL = "UPDATE cart SET quantity = quantity + ? WHERE customer_id = ? AND product_id = ?";

        try (PreparedStatement psProduct = conn.prepareStatement(selectProductSQL);
             PreparedStatement psCustomer = conn.prepareStatement(selectCustomerSQL);
             PreparedStatement psUpdateProduct = conn.prepareStatement(updateProductStockSQL);
             PreparedStatement psSelectCart = conn.prepareStatement(selectCartSQL);
             PreparedStatement psInsertCart = conn.prepareStatement(insertCartSQL);
             PreparedStatement psUpdateCart = conn.prepareStatement(updateCartSQL)) {

            psProduct.setInt(1, productId);
            ResultSet rsProduct = psProduct.executeQuery();

            if (rsProduct.next()) {
                int availableQuantity = rsProduct.getInt("quantity");

                if (availableQuantity < quantity) {
                    System.out.println("Not enough stock.");
                    return false;
                }

                psCustomer.setString(1, username);
                psCustomer.setString(2, pin);
                ResultSet rsCustomer = psCustomer.executeQuery();

                if (!rsCustomer.next()) {
                    System.out.println("Customer not found.");
                    return false;
                }

                psUpdateProduct.setInt(1, quantity);
                psUpdateProduct.setInt(2, productId);
                psUpdateProduct.executeUpdate();

                int customerId = rsCustomer.getInt("customer_id");

                psSelectCart.setInt(1, customerId);
                psSelectCart.setInt(2, productId);
                ResultSet rsCart = psSelectCart.executeQuery();

                if (rsCart.next()) {
                    psUpdateCart.setInt(1, quantity);
                    psUpdateCart.setInt(2, customerId);
                    psUpdateCart.setInt(3, productId);
                    psUpdateCart.executeUpdate();
                    System.out.println("Product updated in cart. Added quantity: " + quantity);
                } else {
                    psInsertCart.setInt(1, customerId);
                    psInsertCart.setInt(2, productId);
                    psInsertCart.setInt(3, quantity);
                    psInsertCart.executeUpdate();
                    System.out.println("Product added to cart.");
                }

                customerLogger.info("Customer: " + username + " bought product ID: " + productId + ", Quantity: " + quantity);
                return true;
            } else {
                System.out.println("Product not found.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            customerLogger.warning("Error buying product : " + e.getMessage());
            return false;
        }
    }

    public static void displaySpecificCart(Connection conn, String loginuser, String loginpin) {
        String selectCustomerSQL = "SELECT * FROM customers WHERE username = ? AND pin = ?";
        String selectCartSQL = "SELECT p.name, p.price, c.quantity, p.category, p.product_id FROM cart c " +
                                "JOIN products p ON c.product_id = p.product_id " +
                                "WHERE c.customer_id = ?";

        boolean isCartEmpty = true;

        try (PreparedStatement psCustomer = conn.prepareStatement(selectCustomerSQL);
             PreparedStatement psCart = conn.prepareStatement(selectCartSQL)) {

            psCustomer.setString(1, loginuser);
            psCustomer.setString(2, loginpin);
            ResultSet rsCustomer = psCustomer.executeQuery();

            while(rsCustomer.next()) {
                int customerId = rsCustomer.getInt("customer_id");

                psCart.setInt(1, customerId);
                ResultSet rsCart = psCart.executeQuery();

                if (!rsCart.next()) {
                    System.out.println("==> Your Cart Is Empty! <==");
                    return;
                }

                double totalPrice = 0.0;

                System.out.println("\nC_ID : " + rsCustomer.getInt("customer_id") +
                                   " C_Name : " + rsCustomer.getString("name") +
                                   " C_Age : " + rsCustomer.getInt("age"));

                System.out.println("\nProducts -->");

                System.out.printf("%-15s %-15s %-15s %-15s %-15s %s\n", "Category", "P_ID", "Product", "Quantity", "Price(₹)", "Total Amount(₹)");

                do {
                    isCartEmpty = false;
                    String category = rsCart.getString("category");
                    String productId = rsCart.getString("product_id");
                    String productName = rsCart.getString("name");
                    int quantity = rsCart.getInt("quantity");
                    double price = rsCart.getDouble("price");
                    double productTotal = quantity * price;

                    System.out.printf("%-15s %-15s %-15s %-15d %-15.2f %.2f\n", 
                            category, 
                            productId, 
                            productName, 
                            quantity, 
                            price, 
                            productTotal);

                    totalPrice += productTotal;
                } while (rsCart.next());

                System.out.println("\n----------------------              Total Amount : " + totalPrice + "              ----------------------\n");

                customerLogger.info("Customer: " + loginuser + " viewed cart. Total: ₹" + totalPrice);
            }       
        } catch (SQLException e) {
            e.printStackTrace();
            customerLogger.warning("Error displaying cart for customer: " + loginuser + ". Error: " + e.getMessage());
        }

        if (isCartEmpty) {
            System.out.println("\n==> Your Cart Is Empty! <==");
        }
    }
	 
    public static void displayCart(Connection conn) {
        String selectCustomerSQL = "SELECT * FROM customers JOIN cart ON customers.customer_id = cart.customer_id";
        String selectCartSQL = "SELECT p.name, p.price, c.quantity, p.category, p.product_id FROM cart c " +
                               "JOIN products p ON c.product_id = p.product_id " +
                               "WHERE c.customer_id = ?";

        try (Statement customerStmt = conn.createStatement();
             PreparedStatement stmt = conn.prepareStatement(selectCartSQL)) {

            ResultSet crs = customerStmt.executeQuery(selectCustomerSQL);

            boolean cartExists = false;

            while (crs.next()) {
                System.out.println("\nC_ID : " + crs.getInt(1) + " C_Name : " + crs.getString(2) + " C_Age : " + crs.getInt(3));
                System.out.println("\nProducts -->");
                System.out.printf("%-15s %-15s %-15s %-15s %-15s %s\n", "Category", "P_ID", "Product", "Quantity", "Price(₹)", "Total Amount(₹)");
                double totalPrice = 0.0;

                stmt.setInt(1, crs.getInt("customer_id"));

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    cartExists = true;
                    String category = rs.getString("category");
                    int productId = rs.getInt("product_id");
                    String productName = rs.getString("name");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");

                    double totalAmount = quantity * price;
                    totalPrice += totalAmount;

                    System.out.printf("%-15s %-15s %-15s %-15s %-15s %s\n", category, productId, productName, quantity, price, totalAmount);
                }

                System.out.println("\n----------------------              Total Amount : " + totalPrice + "              ----------------------\n");
                adminLogger.info("Admin viewed cart for Customer ID " + crs.getInt("customer_id") + ". Total: ₹" + totalPrice);
            }

            if (!cartExists) {
                System.out.println("==> No products found in the cart for any customer. <==");
                adminLogger.info("Admin found no products in the cart.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            adminLogger.warning("Error displaying cart: " + e.getMessage());
        }
    }

    public static void removeProductFromCart(Connection conn, Scanner sc, String loginuser, String loginpin) {
        String selectCartSQL = "SELECT cp.product_id, cp.quantity, p.category, p.price " +
                               "FROM cart cp " +
                               "JOIN products p ON cp.product_id = p.product_id " +
                               "JOIN customers c ON cp.customer_id = c.customer_id " +
                               "WHERE c.username = ? AND c.pin = ?";

        String selectCustomer = "SELECT * FROM customers WHERE username = ? and pin = ?";
        String deleteProductSQL = "DELETE FROM cart WHERE customer_id = (SELECT customer_id FROM customers WHERE username = ? AND pin = ?) AND product_id = ?";
        String updateStockSQL = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";
        String updateProductSQL = "UPDATE cart SET quantity = quantity - ? WHERE customer_id = ? AND product_id = ?";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectCartSQL)) {

            selectStmt.setString(1, loginuser);
            selectStmt.setString(2, loginpin);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("==> Your Cart Is Empty, Product Not Added Yet! <==");
                    customerLogger.info("Customer: " + loginuser + " tried to remove a product, but cart is empty.");
                    return;
                }

                System.out.println("\nYour current cart items : \n");
                do {
                    System.out.println("Product ID: " + rs.getString("product_id") +
                                       ", Category: " + rs.getString("category") +
                                       ", Quantity: " + rs.getInt("quantity") +
                                       ", Price: ₹" + rs.getDouble("price"));
                } while (rs.next());

                System.out.println("\nEnter Product ID which you want to remove: ");
                int productId = 0;

                while (true) {
                    try {
                        productId = sc.nextInt();
                        break;
                    } catch (Exception e) {
                        sc.next();
                        System.out.println("Invalid input! Please enter a valid product ID.");
                    }
                }

                System.out.println("Enter how much quantity you want to remove: ");
                int quantityToRemove = 0;

                while (true) {
                    try {
                        quantityToRemove = sc.nextInt();
                        if (quantityToRemove <= 0) {
                            System.out.println("Quantity must be a positive number.");
                            continue;
                        }
                        break;
                    } catch (Exception e) {
                        sc.next();
                        System.out.println("Invalid input! Please enter a numeric value for quantity.");
                    }
                }

                try (PreparedStatement selectCartStmt = conn.prepareStatement(selectCartSQL)) {
                    selectCartStmt.setString(1, loginuser);
                    selectCartStmt.setString(2, loginpin);
                    try (ResultSet resultSet = selectCartStmt.executeQuery()) {
                        boolean productFound = false;
                        int currentCartQuantity = 0;

                        while (resultSet.next()) {
                            if (resultSet.getInt("product_id") == productId) {
                                productFound = true;
                                currentCartQuantity = resultSet.getInt("quantity");
                                break;
                            }
                        }

                        if (!productFound) {
                            System.out.println("Product ID not found in your cart.");
                            return;
                        }

                        if (quantityToRemove > currentCartQuantity) {
                            System.out.println("Error: Quantity to remove exceeds the quantity in your cart.");
                            return;
                        }

                        int rowsAffected = 0;
                        if (currentCartQuantity > quantityToRemove) {
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateProductSQL);
                                 PreparedStatement cst = conn.prepareStatement(selectCustomer)) {

                                cst.setString(1, loginuser);
                                cst.setString(2, loginpin);
                                ResultSet customerData = cst.executeQuery();

                                while (customerData.next()) {
                                    updateStmt.setInt(1, quantityToRemove);
                                    updateStmt.setInt(2, customerData.getInt("customer_id"));
                                    updateStmt.setInt(3, productId);
                                    rowsAffected = updateStmt.executeUpdate();
                                }
                            }
                        } else if (currentCartQuantity == quantityToRemove) {
                            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteProductSQL)) {
                                deleteStmt.setString(1, loginuser);
                                deleteStmt.setString(2, loginpin);
                                deleteStmt.setInt(3, productId);
                                rowsAffected = deleteStmt.executeUpdate();
                            }
                        }

                        if (rowsAffected > 0) {
                            try (PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSQL)) {
                                updateStockStmt.setInt(1, quantityToRemove);
                                updateStockStmt.setInt(2, productId);
                                updateStockStmt.executeUpdate();
                                System.out.println("Successfully removed " + quantityToRemove + " units of product ID " + productId + " from your cart.");
                                customerLogger.info("Customer: " + loginuser + " removed " + quantityToRemove + " units of product ID " + productId);
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        customerLogger.warning("Error processing cart data for customer: " + loginuser + ". Error: " + e.getMessage());
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                customerLogger.warning("Error retrieving cart items for customer: " + loginuser + ". Error: " + e.getMessage());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            customerLogger.warning("Error connecting to the database for customer: " + loginuser + ". Error: " + e.getMessage());
        }
    }

    public static void removeAllCartItem(Connection conn, String loginuser, String loginpin) {
        String selectCartSQL = "SELECT cp.product_id, cp.quantity, p.category, p.price " +
                               "FROM cart cp " +
                               "JOIN products p ON cp.product_id = p.product_id " +
                               "JOIN customers c ON cp.customer_id = c.customer_id " +
                               "WHERE c.username = ? AND c.pin = ?";

        String deleteCartSQL = "DELETE FROM cart WHERE customer_id = (SELECT customer_id FROM customers WHERE username = ? AND pin = ?)";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectCartSQL);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteCartSQL)) {

            selectStmt.setString(1, loginuser);
            selectStmt.setString(2, loginpin);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("==> Your Cart Is Empty! <==");
                    customerLogger.info("Customer: " + loginuser + " tried to remove all items, but cart is empty.");
                    return;
                }

                do {
                    int productId = rs.getInt("product_id");
                    int quantity = rs.getInt("quantity");

                    String updateStockSQL = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";
                    try (PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSQL)) {
                        updateStockStmt.setInt(1, quantity);
                        updateStockStmt.setInt(2, productId);
                        updateStockStmt.executeUpdate();
                        customerLogger.info("Customer: " + loginuser + " restored " + quantity + " units of product ID " + productId + " to stock.");
                    }

                    System.out.println("Restored " + quantity + " units of product ID " + productId + " to the stock.");
                } while (rs.next());

                deleteStmt.setString(1, loginuser);
                deleteStmt.setString(2, loginpin);
                int rowsAffected = deleteStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("All items have been removed from your cart.");
                    customerLogger.info("Customer: " + loginuser + " removed all items from the cart.");
                } else {
                    System.out.println("Failed to remove items from the cart.");
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            customerLogger.warning("Error removing all cart items for customer: " + loginuser + ". Error: " + e.getMessage());
        }
    }

}
