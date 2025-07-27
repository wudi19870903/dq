package pers.di.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CFileSystem {

	public static boolean isDirExist(String dirName)
	{
		File folder = new File(dirName);
		if(folder.exists() && folder.isDirectory())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public static boolean isFileExist(String fileName)
	{
		File folder = new File(fileName);
		if(folder.exists() && !folder.isDirectory())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public static boolean createDir(String dirName)
	{
		File folder = new File(dirName);
		folder.mkdirs();
		return true;
	}
	public static int clearDir(String dirName)
	{
		File folder =new File(dirName);
		if(!folder.exists())      
		{        
			return 0;
		}
		return help_deleteFile(folder);
	}
	public static int removeDir(String dirName)
	{
		File folder =new File(dirName);
		if(!folder.exists())      
		{        
			return 0;
		}
		if(0 == help_deleteFile(folder))
		{
			return 0;
		}
		if(folder.exists())
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
	
	public static String getParentDir(String dirStr)
	{
		File file = new File(dirStr);
		return file.getParent();
	}
	
	public static boolean removeFile(String fileName)
	{
		File file = new File(fileName);
		return file.delete();
	}
	/*
	 * ********************************************************************
	 */
	
	private static int help_deleteFile(File file) {  
	    if (file.exists()) 
	    {
			if (file.isFile()) 
			{
				//是文件  
			    if(!file.delete()) //删除文件   
			    {
			    	return -1;
			    }
			} 
			else if (file.isDirectory()) 
			{
				//是一个目录  
			    File[] files = file.listFiles();//声明目录下所有的文件 files[];  
			    for (int i = 0;i < files.length;i ++) {//遍历目录下所有的文件  
			    	help_deleteFile(files[i]);//把每个文件用这个方法进行迭代  
			    }  
			    if(!file.delete()) //删除文件夹  
			    {
			    	return -1;
			    }
			 }  
	    } 
	    return 0;
	}  
}
