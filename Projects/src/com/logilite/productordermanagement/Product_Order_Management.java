package com.logilite.productordermanagement;

import java.util.List;
import java.util.Scanner;

public class Product_Order_Management
{
	private final String admin = "admin";
	private final String pin = "1111";
	private static String loginuser = "";
	private static String loginpin = "";
	
	private static String inputUser(Scanner sc) {
		String user;
		while (true) {
			sc.nextLine();
			System.out.println("Enter User Name : ");
			user = sc.next();
			
			if (user != null && user.matches("[a-zA-Z]+") && (!user.contains(" "))) {
				return user;
			}else {
				System.out.println("==> user is not null or only contains alphabet or not contains spaces <==");
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
			}else {
				System.out.println("==> pin is not null <==");
				sc.nextLine();
			}
		}
	}
	
	private boolean checkAdmin(Scanner sc) {
		System.out.println("Enter User Name : ");
		String user = sc.next();
		
		sc.nextLine();
		
		System.out.println("Enter Password : ");
		String pass = sc.nextLine();
		
		if (user.equalsIgnoreCase(admin) && pass.equalsIgnoreCase(pin)) {
			return true;
		}
		return false;
	}
	
	private boolean checkCustomer(String user, String pin) {
		List<Customer> customers = Customer.getCustomers();
		
		if (customers == null) {
			return false;
		}
		
		for (Customer customer : customers) {
			if (user.equals(customer.getUsername()) && pin.equals(customer.getPin())) {
				return true;
			}
		}
		return false;
	}
	
	private static void showCustomers() {
		List<Customer> customers = Customer.getCustomers();
		System.out.println("ID\t\tCustomer Name\t\tCustomer Age\t\tUser Name");
		for (Customer customer : customers) {
			System.out.println(customer.getCustomerId() + "\t\t" + customer.getName() + "\t\t\t" + customer.getAge() + "\t\t\t" + customer.getUsername());
		}
	}
	
	private static void adminPanel(Scanner sc) {
		System.out.println("\t\t\t\t--------> Switched to Admin panel <--------");
		boolean flag = false;
		int num = 0;
		do
		{
			System.out.println("\n1. Enter 1 for --> Show Customers");
			System.out.println("2. Enter 2 for --> Show Products");
			System.out.println("3. Enter 3 for --> Add Products");
			System.out.println("4. Enter 4 for --> Display Cart");
			System.out.println("5. Enter 5 for --> for Exit\n");
			
			System.out.println("Enter Task Which You Want to Perform : ");
			
			while(true) {
				try
				{
					num = sc.nextInt();
					break;
				}
				catch (Exception e)
				{
					sc.next();
					System.out.println("Enter Numeric value only : ");
				}
			}
			
			switch (num)
			{
				case 1:
				{
					Product_Order_Management.showCustomers();
					break;
				}
				case 2:
				{
					ManageProduct.displayProduct();
					break;
				}
				case 3:
				{
					ManageProduct.addProductUsingAdmin(sc);
					break;
				}
				case 4:
				{	
					ManageProduct.displayCart();
					break;
				}
				case 5:
				{	
					System.out.println("Exit from admin panel...");
					Product_Order_Management.mainMenu(sc);
					break;
				}
				default:
					System.out.println("-- Enter Only 1 to 5 Number to Perform Task --\n");
			}
		}
		while (!flag);
	}
	
	private static void customerPanel(Scanner sc) {
		System.out.println("\t\t\t\t--------> Switched to Customer panel <--------");
		boolean flag = false;
		int num = 0;
		do
		{
			System.out.println("\n1. Enter 1 --> for Show Products");
			System.out.println("2. Enter 2 --> for Buy Products");
			System.out.println("3. Enter 3 --> for Show Your Cart");
			System.out.println("4. Enter 4 --> for Remove Product From Cart");
			System.out.println("5. Enter 5 --> for Process Order");
			System.out.println("6. Enter 6 --> for Exit\n");
			
			System.out.println("Enter Task Which You Want to Perform : ");
			
			while(true) {
				try
				{
					num = sc.nextInt();
					break;
				}
				catch (Exception e)
				{
					sc.next();
					System.out.println("Enter Numeric value only : ");
				}
			}
			
			switch (num)
			{
				case 1:
				{
					ManageProduct.displayProduct();
					break;
				}
				case 2:
				{
					ManageProduct.buyProductUsingUser(sc, loginuser, loginpin);
					break;
				}
				case 3:
				{
					ManageProduct.displaySpecificCart(loginuser, loginpin);
					break;
				}
				case 4:
				{	
					ManageProduct.removeProductFromCart(sc, loginuser, loginpin);
					break;
				}
				case 5:
				{	
					ManageOrder.processOrder(sc, loginuser, loginpin);
					break;
				}
				case 6:
				{	
					System.out.println("Exit from customer panel...");
					Product_Order_Management.mainMenu(sc);
					break;
				}
				default:
					System.out.println("-- Enter Only 1 to 6 Number to Perform Task --\n");
			}
		}
		while (!flag);
	}
	
	private static void mainMenu(Scanner sc) {
		Product_Order_Management pom = new Product_Order_Management();
		
		System.out.println("\t\t\t\t--------> Welcome To Product Order Management System <--------");
		int num = 0;
		do
		{
			System.out.println("\n1. Enter 1 --> for Login Admin Panel");
			System.out.println("2. Enter 2 --> for Login Customer Panel");
			System.out.println("3. Enter 3 --> for Signup Customer Panel");
			System.out.println("4. Enter 4 --> for Exit\n");
			
			System.out.println("Enter Task Which You Want to Perform : ");
			
			while(true) {
				try
				{
					num = sc.nextInt();
					break;
				}
				catch (Exception e)
				{
					sc.next();
					System.out.println("Enter Numeric value only : ");
				}
			}
			
			switch (num)
			{
				case 1:
				{
					if (pom.checkAdmin(sc)) {
						pom.adminPanel(sc);
					}
					break;
				}
				case 2:
				{
					String user = inputUser(sc);
					String pin = inputPin(sc);
					
					if (pom.checkCustomer(user, pin)) {
						loginuser = user;
						loginpin = pin;
						pom.customerPanel(sc);
					}else {
						System.out.println("==> Customer Not Found. Please Signup First <==");
					}
					break;
				}
				case 3:
				{	
					if (Customer.SignupCustomer(sc)) {
						pom.customerPanel(sc);
					}
					break;
				}
				case 4:
				{	
					System.out.println("Exit...");
					System.exit(0);
				}
				default:
					System.out.println("-- Enter Only 1 to 4 Number to Perform Task --\n");
			}
		}
		while (num != 4);
	}
	
	public static void main(String[] args)
	{
		
		Scanner sc = new Scanner(System.in);
		Customer.createCustomer(new Customer("1", "Hardik", 20, "hardik", "1111"));
		Customer.createCustomer(new Customer("2", "Om", 21, "om", "2222"));
		
		ManageProduct mp = new ManageProduct();
		mp.addProduct("1", "Electronics", "Laptop", 45, 50000);
		mp.addProduct("2", "Electronics", "Phone", 50, 20000);
		mp.addProduct("1", "Furniture", "Chair", 200, 1000);
		mp.addProduct("2", "Furniture", "Table", 142, 650);
		mp.addProduct("1", "Books", "Java Book", 42, 460);
		
		Product_Order_Management.mainMenu(sc);
		sc.close();
	}
}
