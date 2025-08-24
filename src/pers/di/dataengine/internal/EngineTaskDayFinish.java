package pers.di.dataengine.internal;

import pers.di.common.CDateTimeThruster;
import pers.di.common.CLog;
import pers.di.dataengine.DAContext;
import pers.di.dataengine.IEngineListener;

public class EngineTaskDayFinish extends CDateTimeThruster.ScheduleTask
{
	public EngineTaskDayFinish(String time, SharedSession tss) {
		super("DayFinish", time, 16);
		m_taskSharedSession = tss;
	}
	@Override
	public void doTask(String date, String time) {
		if(!m_taskSharedSession.tranDayChecker.check(date, time))
		{
			return;
		}
		CLog.debug("DENGINE", "[%s %s] EngineTaskDayFinish", date, time);
		
		// callback listener onMinuteTimePrices
		for(int i=0; i<m_taskSharedSession.listeners.size(); i++)
		{
			IEngineListener listener = m_taskSharedSession.listeners.get(i);
			DAContext context = m_taskSharedSession.listenerContext.get(listener);
			context.setDateTime(date, time);
			listener.onTradingDayFinish(context);
		}
	}
	private SharedSession m_taskSharedSession;
}