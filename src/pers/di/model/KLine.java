package pers.di.model;

/*
 * K数据
 * 时间 date time 代表时间段结束时间点
 * 日期时间-开盘价-收盘价-最低价-最高价-成交量
 */
public class KLine implements Comparable
{
	// 2015-09-18 or null
	public String date;
	// 13:25:20 or null
	public String time;
	
	public double open;
	public double close;
	public double low;
	public double high;
	public double volume;
	
	public double entityMidle() // 实体中点
	{
		return (open+close)/2;
	}
	public double entityHigh() // 实体高点
	{
		return open>close?open:close;
	}
	public double entityLow() // 实体低点
	{
		return open>close?close:open;
	}
	public double midle() // 高低中点
	{
		return (low+high)/2;
	}
	public double maxWave() // 最大波动
	{
		return (high-low)/low;
	}
	public boolean isRed() {
		return close>open;
	}
	
	public void CopyFrom(KLine c)
	{
		date = c.date;
		time = c.time;
		open = c.open;
		close = c.close;
		high = c.high;
		low = c.low;
		volume = c.volume;
	}
	
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		KLine sdto = (KLine)arg0;
		int iRet = 0;
		// date compare
		if(null != this.date && null != sdto.date)
		{
			iRet = this.date.compareTo(sdto.date);
			if(0 == iRet)
			{
				// time compare
				if(null != this.time && null != sdto.time)
				{
					return this.time.compareTo(sdto.time);
				}
				else if(null != this.time && null == sdto.time)
				{
					return 1;
				}
				else 
				{
					return 0;
				}
			}
			else
			{
				return iRet;
			}
		}
		else if(null != this.date && null == sdto.date)
		{
			return 1;
		}
		else 
		{
			return 0;
		}
	}
}