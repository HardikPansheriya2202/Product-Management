package com.logilite.productordermanagement;

public class Product
{
	private String productId;
	private String name;
	private int quantity;
	private double price;

	
	public Product(String id, String name, int quantity, double price)
	{
		this.productId = id;
		this.name = name;
		this.quantity = quantity;
		this.price = price;
	}

	public String getProductId()
	{
		return productId;
	}

	public String getName()
	{
		return name;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public double getPrice()
	{
		return price;
	}
	
	public boolean reduceStock(int quantity) {
		if (this.quantity >= quantity) {
			this.quantity -= quantity;
			return true;
		}
		return false;
	}
	
	public void increaseStock(int quantity) {
		this.quantity += quantity;
	}

	@Override
	public String toString()
	{
		return "Product [id=" + productId + ", name=" + name + ", quantity=" + quantity + ", price="
				+ price + "]";
	}
	
	public void setProductId(String productId)
	{
		this.productId = productId;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public void displayProductDetails() {
		System.out.printf("%-15s %-15s %-15s %-15s %s\n", " ", productId, name, quantity, price);
	}
}
