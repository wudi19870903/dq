package pers.di.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pers.di.common.*;

/**
 * 
 * @author wudi
 *
 * 股票数据扩展计算类
 * 1-股票日K机能计算
 * 2-股票日内分时机能计算
 */
public class StockUtils {
	
	// 计算index的临近日平均价（count=1时，表示昨天，今天，明天的均价）
	static public double GetAveNear(List<KLine> dayklist, int count, int index)
	{
		if(dayklist.size() == 0) return 0.0f;
		double value = 0.0f;
		int iB = index-count;
		int iE = index+count;
		if(iB<0) iB=0;
		if(iE>dayklist.size()-1) iB=dayklist.size()-1;
		double sum = 0.0f;
		int sumcnt = 0;
		for(int i = iB; i <= iE; i++)  
        {  
			KLine cDayKData = dayklist.get(i);  
			sum = sum + cDayKData.entityMidle();
			sumcnt++;
        }
		value = sum/sumcnt;
		return value;
	}
	
	// 均线计算，计算date日期前count天均线价格
	static public double GetMA(List<KLine> dayklist, int count, int index)
	{
		if(dayklist.size() == 0) return 0.0f;
		double value = 0.0f;
		int iE = index;
		int iB = iE-count+1;
		if(iB<0) iB=0;
		double sum = 0.0f;
		int sumcnt = 0;
		for(int i = iB; i <= iE; i++)  
        {  
			KLine cDayKData = dayklist.get(i);  
			sum = sum + cDayKData.close;
			sumcnt++;
        }
		value = sum/sumcnt;
		return value;
	}
	
	
	// 计算某日收盘涨跌幅（参考开盘）
	static public double GetInreaseRatioRefOpen(List<KLine> dayklist, int index)
	{
		double ratio = 0.0f;
		if(index >= 0 && index < dayklist.size())
		{
			KLine cKLineCur = dayklist.get(index);
			if(cKLineCur.close != 0)
			{
				ratio = (cKLineCur.close - cKLineCur.open)/cKLineCur.close;
			}
		}
		return ratio;
	}
	static public double GetInreaseRatioRefOpen(List<KLine> dayklist, String date)
	{
		int index = StockUtils.indexDayK(dayklist, date);
		return GetInreaseRatioRefOpen(dayklist, index);
	}
	
	// 计算某日收盘涨跌幅（参考昨日收盘）
	static public double GetInreaseRatio(List<KLine> dayklist, int index)
	{
		double ratio = 0.0f;
		if(index > 0 && index < dayklist.size())
		{
			KLine cKLineCur = dayklist.get(index);
			KLine cKLineBefore = dayklist.get(index-1);
			if(cKLineBefore.close != 0)
			{
				ratio = (cKLineCur.close - cKLineBefore.close)/cKLineBefore.close;
			}
		}
		return ratio;
	}
	static public double GetInreaseRatio(List<KLine> dayklist, String date)
	{
		int index = StockUtils.indexDayK(dayklist, date);
		return GetInreaseRatio(dayklist, index);
	}

	
	// 查找日期索引，返回list中某日期index索引, -1为没有找到
	static public int indexDayK(List<KLine> dayklist, String dateStr)
	{
		int index = -1;
		for(int k = 0; k<dayklist.size(); k++ )
		{
			KLine cDayKDataTmp = dayklist.get(k);
			if(cDayKDataTmp.date.compareTo(dateStr) == 0)
			{
				index = k;
				break;
			}
		}
		return index;
	}
	
	// 查找日期索引，返回list中某日期（含/不含）之后的第一天index索引
	static public int indexDayKAfterDate(List<KLine> dayklist, String dateStr, boolean bContainSelf)
	{
		int index = 0;
		for(int k = 0; k<dayklist.size(); k++ )
		{
			KLine cDayKDataTmp = dayklist.get(k);
			boolean bFind = false;
			if(bContainSelf)
			{
				bFind = cDayKDataTmp.date.compareTo(dateStr) >= 0;
			}
			else
			{
				bFind = cDayKDataTmp.date.compareTo(dateStr) > 0;
			}
			if(bFind)
			{
				index = k;
				break;
			}
		}
		return index;
	}
	
	// 查找日期索引，返回list中某日期（含/不含）之前的第一天index索引
	static public int indexDayKBeforeDate(List<KLine> dayklist, String dateStr, boolean bContainSelf)
	{
		int index = 0;
		for(int k = dayklist.size()-1; k >= 0; k-- )
		{
			KLine cDayKDataTmp = dayklist.get(k);
			boolean bFind = false;
			if(bContainSelf)
			{
				bFind = cDayKDataTmp.date.compareTo(dateStr) <= 0;
			}
			else
			{
				bFind = cDayKDataTmp.date.compareTo(dateStr) < 0;
			}
			if(bFind)
			{
				index = k;
				break;
			}
		}
		return index;
	}
	
	// 计算i到j日的最高价格的索引
	static public int indexHigh(List<KLine> dayklist, int i, int j)
	{
		int index = i;
		double high = -100000.0f;
		for(int k = i; k<=j; k++ )
		{
			KLine cDayKDataTmp = dayklist.get(k);
			if(cDayKDataTmp.high > high) 
			{
				high = cDayKDataTmp.high;
				index = k;
			}
		}
		return index;
	}
	
	// 计算i到j日的最低价格的索引
	static public int indexLow(List<KLine> dayklist, int i, int j)
	{
		int index = i;
		double low = 100000.0f;
		for(int k = i; k<=j; k++ )
		{
			KLine cDayKDataTmp = dayklist.get(k);
			if(cDayKDataTmp.low < low) 
			{
				low = cDayKDataTmp.low;
				index = k;
			}
		}
		return index;
	}
	
	public static List<KLine> subKLineData(List<KLine> oriList, String fromDate, String endDate)
	{
		List<KLine> newKLineData = new ArrayList<KLine>();
		for(int i = 0; i <oriList.size(); i++)  
        {  
			KLine cKLine = oriList.get(i);  
			if(cKLine.date.compareTo(fromDate) >= 0 &&
					cKLine.date.compareTo(endDate) <= 0)
			{
				KLine cNewKLine = new KLine();
				cNewKLine.CopyFrom(cKLine);
				newKLineData.add(cNewKLine);
			}
        }
		return newKLineData;
	}
	
	public static List<TimePrice> subTimePriceData(List<TimePrice> oriList, String fromTime, String endTime)
	{
		List<TimePrice> newTimePriceData = new ArrayList<TimePrice>();
		for(int i = 0; i <oriList.size(); i++)  
        {  
			TimePrice cTimePrice = oriList.get(i);  
			if(cTimePrice.time.compareTo(fromTime) >= 0 &&
					cTimePrice.time.compareTo(endTime) <= 0)
			{
				TimePrice cNewTimePrice = new TimePrice();
				cNewTimePrice.CopyFrom(cTimePrice);
				newTimePriceData.add(cNewTimePrice);
			}
        }
		return newTimePriceData;
	}
	
	// 计算i到j日的最高价格的索引
	static public int indexTimePriceHigh(List<TimePrice> list, int i, int j)
	{
		int index = i;
		double high = -100000.0;
		for(int k = i; k<=j; k++ )
		{
			TimePrice cTimePrice = list.get(k);
			if(cTimePrice.price > high) 
			{
				high = cTimePrice.price;
				index = k;
			}
		}
		return index;
	}
	
	// 计算i到j日的最低价格的索引
	static public int indexTimePriceLow(List<TimePrice> list, int i, int j)
	{
		int index = i;
		double low = 100000.0;
		for(int k = i; k<=j; k++ )
		{
			TimePrice cTimePrice = list.get(k);
			if(cTimePrice.price < low) 
			{
				low = cTimePrice.price;
				index = k;
			}
		}
		return index;
	}
	
	
	/**
	 * ***************************************************************************
	 * 
	 */
	
	public static CListObserver<KLine> subKLineListObserver(CListObserver<KLine> oriObs, String fromDate, String endDate)
	{
		CListObserver<KLine> newObs = new CListObserver<KLine>();
		int iBase = -1;
		int iSize = 0;
		for(int i = 0; i <oriObs.size(); i++)  
        {  
			KLine cKLine = oriObs.get(i);  
			if(cKLine.date.compareTo(fromDate) >= 0)
			{
				iBase = i;
			}
			if(iBase >= 0 && cKLine.date.compareTo(endDate) <= 0)
			{
				iSize++;
			}
        }
		if(iBase >=0)
		{
			newObs.build(oriObs, iBase, iSize);
		}
		return newObs;
	}
	
	// 查找日期索引，返回list中某日期（含/不含）之后的第一天index索引
	static public int indexDayKAfterDate(CListObserver<KLine> oriObs, String dateStr, boolean bContainSelf)
	{
		int index = 0;
		for(int k = 0; k<oriObs.size(); k++ )
		{
			KLine cDayKDataTmp = oriObs.get(k);
			boolean bFind = false;
			if(bContainSelf)
			{
				bFind = cDayKDataTmp.date.compareTo(dateStr) >= 0;
			}
			else
			{
				bFind = cDayKDataTmp.date.compareTo(dateStr) > 0;
			}
			if(bFind)
			{
				index = k;
				break;
			}
		}
		return index;
	}
	
	static public int indexDayKBeforeDate(CListObserver<KLine> oriObs, String dateStr, boolean bContainSelf)
	{
		int index = 0;
		for(int k = oriObs.size()-1; k >= 0; k-- )
		{
			KLine cDayKDataTmp = oriObs.get(k);
			boolean bFind = false;
			if(bContainSelf)
			{
				bFind = cDayKDataTmp.date.compareTo(dateStr) <= 0;
			}
			else
			{
				bFind = cDayKDataTmp.date.compareTo(dateStr) < 0;
			}
			if(bFind)
			{
				index = k;
				break;
			}
		}
		return index;
	}
}
