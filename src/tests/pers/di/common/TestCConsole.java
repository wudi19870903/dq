package pers.di.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import pers.di.common.*;

public class TestCConsole {
	public static class TestConsole implements CConsole.IHandler
	{
		public void command(String cmd) 
		{
			CLog.debug("TEST", "command:%s\n", cmd);
		}
	}
	
	public static void main(String[] args) {
		CLog.debug("TEST", "Test TestCConsole begin\n");
		
		CConsole console = new CConsole();
		console.Start(new TestConsole());
		
		CThread.msleep(1000*10);
		
		CLog.debug("TEST", "Stop Console\n");
		
		console.Stop();
		
		CLog.debug("TEST", "Test TestCConsole end\n");
	}
}
