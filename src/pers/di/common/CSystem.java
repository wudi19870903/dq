package pers.di.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CSystem {
	
	public static void start()
	{
		CUtilsDateTime.start();
		
		// config log
		String logconfigDir = getRWRoot() + "\\config";
		String logDir = getRunSessionRoot();
		CLog.config_setLogCfg(logconfigDir, "log_config.xml");
		CLog.config_setLogFile(logDir, "default.log");
		CLog.start();
		
		CLog.debug("COMMON", "RWRoot: %s", getRWRoot());
		CLog.debug("COMMON", "RunSessionRoot: %s", getRunSessionRoot());
	}
	
	public static void stop()
	{
		CLog.stop();
		
		CUtilsDateTime.stop();
	}
	
	public static String getRWRoot()
	{
		return s_RWRoot;
	}
	
	public static String getRunSessionRoot()
	{
		return s_RunSessionRoot;
	}
	
	public static void exit(int errCode)
	{
		CLog.error("ERROR", "Program is exited error(%d)!\nStackTrace:\n%s\n", errCode, getCallStatck());
		CLog.stop();
		System.exit(errCode);
	}
	
	public static String getCallStatck() {
		String traceString = "";
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
            	if(0==i){continue;} // ignore current method.
            	traceString += stackElements[i].getFileName()+":";
            	traceString += stackElements[i].getLineNumber()+" ";
            	traceString += stackElements[i].getClassName()+":";
            	traceString += stackElements[i].getMethodName()+"\n";
            }
        }
        return traceString;
    }
	
	private static String s_RWRoot;
	private static String s_RunSessionRoot;
	static {
		// init rwRoot
		String rwRoot = CSystem.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		rwRoot = CFileSystem.getParentDir(rwRoot);
		rwRoot = rwRoot + "\\rw";
		if(!CFileSystem.isDirExist(rwRoot))
		{
			CFileSystem.createDir(rwRoot);
		}
		System.out.println("CSystem-Static-Init rwRoot:" + rwRoot);
		s_RWRoot = rwRoot;
		// init runSessionRoot
		SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMddHHmmss");
		String runSessionRoot = rwRoot + "\\RunSession\\" + sdf.format(new Date());
		if(!CFileSystem.isDirExist(runSessionRoot))
		{
			CFileSystem.createDir(runSessionRoot);
		}
		System.out.println("CSystem-Static-Init runSessionRoot:" + runSessionRoot);
		s_RunSessionRoot = runSessionRoot;
	}
}
