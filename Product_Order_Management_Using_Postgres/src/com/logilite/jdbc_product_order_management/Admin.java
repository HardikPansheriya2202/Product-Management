package com.logilite.jdbc_product_order_management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Logger;

public class Admin {

    private static final Logger adminLogger = LoggerConfig.getLogger("AdminLogger", "admin.log");

    public static boolean checkAdmin(Scanner sc, Connection conn) {
        String query = "SELECT * FROM admin_data";

        String user = "";
        String pass = "";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            System.out.println("Enter User Name : ");
            user = sc.next();

            sc.nextLine();

            System.out.println("Enter Password : ");
            pass = sc.nextLine();

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                if (rs.getString(1).equals(user)) {
                    if (rs.getString(2).equals(Product_Order_Management.encryptString(pass))) {
                        adminLogger.info("Admin logged in successfully: " + user);
                        return true;
                    } else {
                        System.out.println("==> Pin is incorrect <==");
                        adminLogger.warning("Failed login attempt for admin user: " + user + " - Incorrect pin");
                    }
                } else {
                    System.out.println("==> " + user + " is not an admin <==");
                    adminLogger.warning("Failed login attempt for non-admin user: " + user);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            adminLogger.severe("Error during admin login check: " + e.getMessage());
        }

        adminLogger.warning("Failed admin login attempt for user: " + user);
        return false;
    }

    public static void changeAdminCredentials(Scanner sc, Connection conn) {
        System.out.println("\n--> Change Admin Username and Pin <--");

        String query = "UPDATE admin_data SET admin_user = ?, pin = ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            int result = 0;
            System.out.println("Enter new Username: ");
            String newUsername = sc.next();

            sc.nextLine();
            System.out.println("Enter new Pin (4 digits): ");
            String newPin = sc.next();

            if (newUsername != null && newUsername.matches("[a-zA-Z]+") && !newUsername.contains(" ") && newPin != null && newPin.length() == 4 && newPin.matches("\\d{4}")) {
                adminLogger.info("Admin changed username to: " + newUsername);
                adminLogger.info("Admin changed pin to: " + Product_Order_Management.encryptString(newPin));

                pst.setString(1, newUsername);
                pst.setString(2, Product_Order_Management.encryptString(newPin));

                result = pst.executeUpdate();
            } else {
                System.out.println("==> Invalid username format OR Invalid pin format (must contain only 4 digits). <==");
                adminLogger.warning("Admin attempted to set an invalid username: " + newUsername + " OR invalid pin: " + newPin);
            }

            if (result > 0) {
                System.out.println("==> Admin Username and Pin updated <==");
                adminLogger.info("Admin successfully updated credentials.");
            } else {
                adminLogger.warning("Admin failed to update credentials. No changes were made.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            adminLogger.severe("Error updating admin credentials: " + e.getMessage());
        }
    }
}
