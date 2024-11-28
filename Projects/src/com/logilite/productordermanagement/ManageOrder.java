package com.logilite.productordermanagement;

import java.util.Scanner;

public class ManageOrder {

    public static void processOrder(Scanner sc, String loginuser, String loginpin) {
        String orderId = "ORD" + System.currentTimeMillis();
        Order order = new Order(orderId);

        Customer customer = getCustomerByCredentials(loginuser, loginpin);
        if (customer != null) {
            order.addProductsFromCart(customer);
            order.calculateTotalPrice();
            
            order.applyDiscount(15.0);
            order.calculateTax(10.0);
            
            System.out.println(order.getOrderSummary());
            
            removeProductsFromCart(loginuser, loginpin);
        } else {
            System.out.println("Invalid credentials or no cart found.");
        }
    }

    public static Customer getCustomerByCredentials(String loginuser, String loginpin) {
        for (Customer customer : ManageProduct.getCustomers()) {
            if (customer.getUsername().equals(loginuser) && customer.getPin().equals(loginpin)) {
                return customer;
            }
        }
        return null;
    }

    public static void removeProductsFromCart(String loginuser, String loginpin) {
        Customer customer = getCustomerByCredentials(loginuser, loginpin);
        if (customer != null && ManageProduct.cart.containsKey(customer)) {
            ManageProduct.cart.get(customer).clear();
            System.out.println("Cart has been cleared for customer: " + customer.getName());
        } else {
            System.out.println("No cart found for the specified customer.");
        }
    }
}
