package pers.di.model;

public class TimePrice {
	
	public TimePrice()
	{
	}
	
	public void CopyFrom(TimePrice fromObj)
	{
		this.time = fromObj.time;
		this.price = fromObj.price;
	}
	
	public String time;
	
	public double price;
}