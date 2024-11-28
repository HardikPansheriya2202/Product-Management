package com.logilite.productordermanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ManageProduct
{
	static Map<String, Map<String, Product>> products;
	static Map<Customer, Map<String, List<Product>>> cart = new HashMap<>();
	static List<Customer> customers = Customer.getCustomers();
	
	public static Map<String, Map<String, Product>> getProducts()
	{
		return products;
	}

	public static Map<Customer, Map<String, List<Product>>> getCart() 
	{
        return cart;
    }

	public static List<Customer> getCustomers()
	{
		return customers;
	}

	public ManageProduct() {
		products = new HashMap<String, Map<String,Product>>();
	}
	
	public void addProduct(String id,String category, String productName, int quantity, double price) {
        products.putIfAbsent(category, new HashMap<String, Product>());
        Map<String, Product> categoryProducts = products.get(category);
        
        if (!categoryProducts.containsKey(id)) {
            categoryProducts.put(id, new Product(id, productName, quantity, price));
        } else {
            Product product = categoryProducts.get(id);
            product.increaseStock(quantity);
        }
    }
	
	public static void addProductUsingAdmin(Scanner sc) {
        System.out.println("Enter category : ");
        String category = sc.next();

        sc.nextLine();
        System.out.println("Enter product name : ");
        String name = sc.nextLine();
        
        String id = String.valueOf(products.get(category).size() + 1);

        int quan = 0;
        while (true) {
            try {
                System.out.println("Enter product quantity : ");
                quan = sc.nextInt();

                if (quan > 0) {
                    break;
                }
            } catch (Exception e) {
                sc.next();
                System.out.println("==> Enter only integer value <==");
            }
        }

        double price = 0.0;
        while (true) {
            try {
                System.out.println("Enter product price : ");
                price = sc.nextDouble();
                break;
            } catch (Exception e) {
                sc.next();
                System.out.println("==> Enter only integer value <==");
            }
        }

        products.putIfAbsent(category, new HashMap<String, Product>());
        Map<String, Product> categoryProducts = products.get(category);

        if (!categoryProducts.containsKey(id)) {
            categoryProducts.put(id, new Product(id, name, quan, price));
            System.out.println("Product " + name + " added in category " + category + " with quantity " + quan + " and price ₹" + price);
        } else {
            Product product = categoryProducts.get(name);
            product.increaseStock(quan);
            System.out.println("Product " + name + " restocked in category " + category + " with " + quan + " items. New quantity : " + product.getQuantity());
        }
    }
	
	 public static boolean buyProductUsingUser(Scanner sc, String loginuser, String loginpin) {
	        System.out.println("Enter category: ");
	        String category = sc.next();

	        sc.nextLine();
	        System.out.println("Enter product ID which you want to buy: ");
	        String id = sc.nextLine();

	        int quan;
	        while (true) {
	            try {
	                System.out.println("Enter product's quantity: ");
	                quan = sc.nextInt();
	                break;
	            } catch (Exception e) {
	                sc.next();
	                System.out.println("==> Enter only integer value <==");
	            }
	        }

	        if (products.containsKey(category) && products.get(category).containsKey(id)) {
	            Product product = products.get(category).get(id);
	            boolean result = product.reduceStock(quan);

	            if (result) {
	                System.out.println("Bought " + quan + " units of product " + product.getName() + " from category " + category);

	                Customer c = null;
	                for (Customer customer : customers) {
	                    if (customer.getUsername().equals(loginuser) && customer.getPin().equals(loginpin)) {
	                        c = customer;
	                        break;
	                    }
	                }

	                if (c == null) {
	                    System.out.println("User not found!");
	                    return false;
	                }

	                cart.putIfAbsent(c, new HashMap<>());
	                Map<String, List<Product>> customerProducts = cart.get(c);
	                customerProducts.putIfAbsent(category, new ArrayList<>());

	                List<Product> categoryProducts = customerProducts.get(category);
	                boolean productExists = false;

	                for (Product existingProduct : categoryProducts) {
	                    if (existingProduct.getProductId().equals(id)) {
	                        existingProduct.setQuantity(existingProduct.getQuantity() + quan);
	                        productExists = true;
	                        break;
	                    }
	                }

	                if (!productExists) {
	                    categoryProducts.add(new Product(id, product.getName(), quan, product.getPrice()));
	                }
	            } else {
	                System.out.println("Not enough stock of product " + product.getName() + " to buy " + quan + " units.");
	            }
	            return result;
	        }

	        System.out.println("Product ID " + id + " not found in category " + category);
	        return false;
	    }


	
	public static void displayProduct() {
        System.out.printf("%-15s %-15s %-15s %-15s %s\n", "Category", "ID", "Product", "Quantity", "Price(₹)");
        for (String category : products.keySet()) {
            System.out.println(category + "->");
            for (Product product : products.get(category).values()) {
                product.displayProductDetails();
            }
            System.out.println();
        }
    }
	 
	
	public static void displayCart() {
		for (Customer customer : cart.keySet()) {
			System.out.println("\nC_ID : " + customer.getCustomerId() + " C_Name : " + customer.getName() + " C_Age : " + customer.getAge());
			System.out.println("\nProducts -->");
			System.out.printf("%-15s %-15s %-15s %-15s %-15s %s\n", "Category", "P_ID", "Product", "Quantity", "Price(₹)", "Total Amount(₹)");
			double totalPrice = 0.0;
			
			for (Map.Entry<String, List<Product>> entry : cart.get(customer).entrySet()) {
				String category = entry.getKey();
				for (Product product : entry.getValue()) {
					totalPrice += (product.getQuantity() * product.getPrice());
					System.out.printf("%-15s %-15s %-15s %-15d %-15.2f %.2f\n", category, product.getProductId(), product.getName(), product.getQuantity(), product.getPrice(), product.getQuantity() * product.getPrice());
				}
			}
			System.out.println("\n----------------------              Total Amount : " + totalPrice + "              ----------------------\n");
		}
	}
	
	public static void displaySpecificCart(String loginuser, String loginpin) {
		for (Customer customer : cart.keySet()) {
			if (customer.getUsername().equals(loginuser) && customer.getPin().equals(loginpin)) {
				System.out.println("\nC_ID : " + customer.getCustomerId() + " C_Name : " + customer.getName() + " C_Age : " + customer.getAge());
				System.out.println("\nProducts -->");
				System.out.printf("%-15s %-15s %-15s %-15s %-15s %s\n", "Category", "P_ID", "Product", "Quantity", "Price(₹)", "Total Amount(₹)");
				double totalPrice = 0.0;
				
				for (Map.Entry<String, List<Product>> entry : cart.get(customer).entrySet()) {
					String category = entry.getKey();
					for (Product product : entry.getValue()) {
						totalPrice += (product.getQuantity() * product.getPrice());
						System.out.printf("%-15s %-15s %-15s %-15d %-15.2f %.2f\n", category, product.getProductId(), product.getName(), product.getQuantity(), product.getPrice(), product.getQuantity() * product.getPrice());
					}
				}
				System.out.println("\n----------------------              Total Amount : " + totalPrice + "              ----------------------\n");
			}
		}
	}
	

	public static void removeProductFromCart(Scanner sc, String loginuser, String loginpin) {
        for (Customer customer : cart.keySet()) {
            if (customer.getUsername().equals(loginuser) && customer.getPin().equals(loginpin)) {
                System.out.println("Enter Category : ");
                String category = sc.next();

                sc.nextLine();
                System.out.println("Enter Product ID Which You Want To Remove : ");
                String id = sc.nextLine();

                System.out.println("Enter How much Quantity You Want To Remove : ");
                int quan = sc.nextInt();

                if (cart.get(customer).containsKey(category)) {
                    List<Product> categoryProducts = cart.get(customer).get(category);
                    Product targetProduct = null;

                    for (Product product : categoryProducts) {
                        if (product.getProductId().equals(id)) {
                            targetProduct = product;
                            break;
                        }
                    }

                    if (targetProduct != null) {
                        if (targetProduct.getQuantity() > quan) {
                            targetProduct.setQuantity(targetProduct.getQuantity() - quan);
                            
                            Map<String, Product> existProducts = products.get(category);
                            Product addproduct = existProducts.get(id);
                            addproduct.increaseStock(quan);
                            
                        } else if (targetProduct.getQuantity() == quan) {
                            categoryProducts.remove(targetProduct);
                            
                            Map<String, Product> existProducts = products.get(category);
                            Product addproduct = existProducts.get(id);
                            addproduct.increaseStock(quan);
                            
                        } else {
                            System.out.println("Quantity is more than actual cart quantity");
                        }
                    } else {
                        System.out.println("Product ID is not found");
                    }
                } else {
                    System.out.println("Category Product not found");
                }
                return;
            }
        }
        System.out.println("User not found");
    }
	
}
