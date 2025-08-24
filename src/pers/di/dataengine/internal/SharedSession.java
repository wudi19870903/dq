package pers.di.dataengine.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pers.di.dataengine.DAContext;
import pers.di.dataengine.IEngineListener;

public class SharedSession {
    public SharedSession()
	{
        tranDayChecker = new TranDayChecker(this);
        listeners = new ArrayList<IEngineListener>();
		listenerContext = new HashMap<IEngineListener, DAContext>();
	}
	
	// base parameter
	public boolean bHistoryTest;
	public String beginDate;
	public String endDate;
	public boolean bConfigFailed;
	
    public TranDayChecker tranDayChecker;
    public List<IEngineListener> listeners;
	public Map<IEngineListener, DAContext> listenerContext;
}
