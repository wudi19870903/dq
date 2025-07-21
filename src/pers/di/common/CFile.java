package pers.di.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class CFile {
	
	public static void fileWrite(String fileName, String content, boolean append)
	{
		File cfile =new File(fileName);
		try
		{
			FileOutputStream cOutputStream = new FileOutputStream(cfile, append);
			cOutputStream.write(content.getBytes());
			cOutputStream.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception:" + e.getMessage()); 
		}
	}
	
	public static String fileRead(String fileName)
	{
		File cfile=new File(fileName);
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cfile));
	        int fileLen = (int)cfile.length();
	        char[] chars = new char[fileLen];
	        reader.read(chars);
	        reader.close();
	        return String.valueOf(chars);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception:" + e.getMessage()); 
		}
		return null;
	}
	
	public void CFile()
	{
		
	}
}
