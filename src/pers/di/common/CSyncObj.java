package pers.di.common;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CSyncObj
{
	private Lock lock;
	public CSyncObj()
	{
		lock = new ReentrantLock();
	}
	public boolean Lock()
	{
		lock.lock(); 
		return true;
	}
	public boolean UnLock()
	{
		lock.unlock(); 
		return true;
	}
}
	