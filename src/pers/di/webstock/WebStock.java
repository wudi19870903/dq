package pers.di.webstock;

import pers.di.webstock.impl.WebStockImpl;

public class WebStock {
	
	public static IWebStock instance() {  
		return s_instance;  
	} 
	
	private static IWebStock s_instance = new WebStockImpl(); 
	private WebStock () {}
}
