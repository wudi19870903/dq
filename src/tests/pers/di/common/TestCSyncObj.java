package pers.di.common;

import pers.di.common.CLog;
import pers.di.common.CSyncObj;

public class TestCSyncObj {
	
	public static void main(String[] args) {
		
		CLog.debug("TEST", "TestCSyncObj begin\n");
		
		CSyncObj cSync = new CSyncObj();
		
		cSync.Lock();
		
		cSync.UnLock();
	
		CLog.debug("TEST", "TestCSyncObj end\n");
	}
}
