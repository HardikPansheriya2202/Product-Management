package com.logilite.productordermanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order {

    private String orderId;
    private double totalPrice;
    private double price;
    private double discount;
    private double tax;
    private double finalPrice;
    private Map<Customer, Map<String, List<Product>>> orderProducts = new HashMap<>();
    private Map<Customer, Map<String, List<Product>>> cart = ManageProduct.getCart();

    public Order(String orderId) {
        this.orderId = orderId;
    }

    public void addProductsFromCart(Customer customer) {
        if (cart.containsKey(customer)) {
            Map<String, List<Product>> customerCart = cart.get(customer);
            Map<String, List<Product>> customerOrder = new HashMap<>();

            for (Map.Entry<String, List<Product>> entry : customerCart.entrySet()) {
                String category = entry.getKey();
                List<Product> productList = new ArrayList<>();

                for (Product product : entry.getValue()) {
                    if (product.getQuantity() > 0) {
                        productList.add(new Product(product.getProductId(), product.getName(), product.getQuantity(), product.getPrice()));
                    }
                }
                if (!productList.isEmpty()) {
                    customerOrder.put(category, productList);
                }
            }

            orderProducts.put(customer, customerOrder);
        }
    }

    public void calculateTotalPrice() {
        totalPrice = 0.0;
        for (Map<String, List<Product>> customerOrders : orderProducts.values()) {
            for (List<Product> productList : customerOrders.values()) {
                for (Product product : productList) {
                    totalPrice += product.getPrice() * product.getQuantity();
                }
            }
        }
        price = totalPrice;
    }

    public void applyDiscount(double totalDiscount) {
        discount = (totalPrice * totalDiscount) / 100;
        totalPrice -= discount;
    }

    public void calculateTax(double taxRate) {
        tax = (totalPrice * taxRate) / 100;
        finalPrice = totalPrice + tax;
    }

    public String getOrderSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Customer:\n");
        for (Map.Entry<Customer, Map<String, List<Product>>> entry : orderProducts.entrySet()) {
            Customer customer = entry.getKey();
            sb.append("C_ID: ").append(customer.getCustomerId())
              .append("\tC_Name: ").append(customer.getName())
              .append("\tC_Age: ").append(customer.getAge()).append("\n");
            sb.append("Products:\n");
            for (Map.Entry<String, List<Product>> categoryEntry : entry.getValue().entrySet()) {
                String category = categoryEntry.getKey();
                for (Product product : categoryEntry.getValue()) {
                    sb.append("Category: ").append(category)
                      .append("\tP_ID: ").append(product.getProductId())
                      .append("\tP_Name: ").append(product.getName())
                      .append("\tP_Price: ").append(product.getPrice())
                      .append("\tQuantity: ").append(product.getQuantity())
                      .append("\n");
                }
            }
        }
        sb.append("\n-----------------------------\n");
        sb.append("Total Price: ₹").append(price).append("\n");
        sb.append("Discount: ₹").append(discount).append("\n");
        sb.append("Tax: ₹").append(tax).append("\n");
        sb.append("Final Price: ₹").append(finalPrice).append("\n");
        sb.append("-----------------------------");

        return sb.toString();
    }
}
