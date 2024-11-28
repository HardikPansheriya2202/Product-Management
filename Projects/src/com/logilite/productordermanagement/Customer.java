package com.logilite.productordermanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Customer
{
	private String customerId;
	private String name;
	private int age;
	private String username;
	private String pin;
	private static List<Customer> customers = new ArrayList<Customer>();
	
	public Customer(String customerId, String name, int age, String username, String pin)
	{
		this.customerId = customerId;
		this.name = name;
		this.age = age;
		this.username = username;
		this.pin = pin;
	}
	
	public Customer(String customerId, String name, int age)
	{
		this.customerId = customerId;
		this.name = name;
		this.age = age;
	}
	

	public String getCustomerId()
	{
		return customerId;
	}

	public String getName()
	{
		return name;
	}

	public int getAge()
	{
		return age;
	}
	
	public String getUsername()
	{
		return username;
	}

	public String getPin()
	{
		return pin;
	}

	public static List<Customer> getCustomers()
	{
		return customers;
	}

	@Override
	public String toString()
	{
		return "Customer [customerId=" + customerId + ", name=" + name + ", age=" + age + "]";
	}
	
	public static void createCustomer(Customer customer) {
		customers.add(customer);
	}
	
	public static boolean SignupCustomer(Scanner sc) {
		String c_id = "";
		if (customers == null) {
			c_id = String.valueOf(1);
		}else {
			c_id = String.valueOf(customers.size() + 1);
		}
		
		System.out.println("Enter Customer Name : ");
		String name = sc.next();
		
		System.out.println("Enter Customer Age : ");
		int age = sc.nextInt();
		
		System.out.println("Enter Username : ");
		String username = sc.next();
		sc.nextLine();
		
		System.out.println("Enter Pin : ");
		String pin = sc.next();
		sc.nextLine();
		
		System.out.println("Enter Confirm Pin : ");
		String cpin = sc.next();
		
		if ((!username.isEmpty()) && pin.equals(cpin)) {
			customers.add(new Customer(c_id, name, age, username, pin));
			return true;
		}
		return false;
	}
}
