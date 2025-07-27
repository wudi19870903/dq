package pers.di.dataprovider.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import pers.di.common.CFileSystem;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CUtilsDateTime;
import pers.di.dataprovider.DataProvider;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import pers.di.model.*;
import pers.di.webstock.WebStock;

public class DataProviderImpl extends DataProvider
{ 
    private Formatter mFmt = new Formatter(System.out);
    private DataStorage mDataStorage = new DataStorage();

    @Override
    public String dataRoot() { 
        return mDataStorage.dataRoot();
    }

    @Override
    public int updateAllLocalStocks()
    { 
        long lTCBegin = CUtilsDateTime.GetCurrentTimeMillis();
        // 更新指数k
		String ShangZhiId = "999999";
		String ShangZhiName = "上阵指数";
		int errDownloadSZ = this.updateOneLocalStocks(ShangZhiId);
		String newestDate = "";
		if(0 == errDownloadSZ) {
			List<KLine> ctnKLine = new ArrayList<KLine>();
			int error = mDataStorage.getLocalStockIDKLineList(ShangZhiId, ctnKLine);
			if(0 == error && ctnKLine.size() > 0) {
				newestDate = ctnKLine.get(ctnKLine.size()-1).date;
			}
			mFmt.format("[%s] update success: %s (%s) item:%d date:%s\n", 
					CUtilsDateTime.GetCurDateTimeStr(), ShangZhiId, ShangZhiName, ctnKLine.size(), newestDate);
		} else {
			mFmt.format("[%s] downloadAllStockFullData ERROR: %s error(%d)\n", 
					CUtilsDateTime.GetCurDateTimeStr(), ShangZhiId, errDownloadSZ);
			return -10;
		}
        // 检查【当前最新交易日日期】
        if(newestDate.length() != "0000-00-00".length()) {
            mFmt.format("[%s] downloadAllStockFullData ERROR, saveAllStockFullDataTimestamps failed!\n", 
				CUtilsDateTime.GetCurDateTimeStr());
            return -2;
        }

        // 【本地整体数据更新日期】和【当前最新交易日日期】一致的话，则不需要更新
        CObjectContainer<String> ctnAllStockFullDataTimestamps = new CObjectContainer<String>();
		int errAllStockFullDataTimestamps = mDataStorage.getAllStockFullDataTimestamps(ctnAllStockFullDataTimestamps);
		if(0 == errAllStockFullDataTimestamps) {
			if(ctnAllStockFullDataTimestamps.get().compareTo(newestDate) >= 0) {
				mFmt.format("[%s] update all success! (current is newest, local: %s)\n", 
						CUtilsDateTime.GetCurDateTimeStr(), ctnAllStockFullDataTimestamps.get());
				return 0;
			}
		}

        // 尝试更新所有股票日线数据
		List<String> stockAllList = new ArrayList<String>();
		int errAllStockList = this.getLocalAllStockIDList(stockAllList);
		if (0 != errAllStockList) {
			CLog.error("DATAAPI", "DataProvider.getLocalAllStockIDList failed\n");
		}
		if(0 == errAllStockList)
		{
			int iAllStockListSize = stockAllList.size();
			for(int i = 0; i < iAllStockListSize; i++)  
	        {  
				String stockID = stockAllList.get(i);

                // 获取【本地单个日K数据最新日期】，如果已经和【当前最新交易日日期】一致的话，则不需要更新
                String latestDate = DataUtils.getLocalStockIDLatestDate(mDataStorage, stockID);
                if (latestDate.compareTo(newestDate) >= 0) {
                    double fCostTime = (CUtilsDateTime.GetCurrentTimeMillis() - lTCBegin)/1000.0f;
                    mFmt.format("[%s] current is newest %d/%d %.3fs: %s item:%d date:%s\n", 
		    					CUtilsDateTime.GetCurDateTimeStr(), i, iAllStockListSize, fCostTime, 
		    					stockID, null, latestDate);
                    continue;
                }

				CObjectContainer<Integer> ctnCount = new CObjectContainer<Integer>();
				int errDownloaddStockFullData = this.updateOneLocalStocks(stockID);
	           
				double fCostTime = (CUtilsDateTime.GetCurrentTimeMillis() - lTCBegin)/1000.0f;
				
				if(0 == errDownloaddStockFullData)
				{
					List<KLine> ctnKLine = new ArrayList<KLine>();
					int errKLine = mDataStorage.getLocalStockIDKLineList(stockID, ctnKLine);
		    		if(0 == errKLine && ctnKLine.size() > 0)
		    		{
		    			String stockNewestDate = ctnKLine.get(ctnKLine.size()-1).date;
		    			mFmt.format("[%s] update success %d/%d %.3fs: %s item:%d date:%s\n", 
		    					CUtilsDateTime.GetCurDateTimeStr(), i, iAllStockListSize, fCostTime, 
		    					stockID, ctnCount.get(), stockNewestDate);
		    		}
		            else
		            {
		            	mFmt.format("[%s] update ERROR %d/%d %.3fs: %s error(%d)\n", 
		            			CUtilsDateTime.GetCurDateTimeStr(), i, iAllStockListSize, fCostTime, 
		            			stockID, errDownloaddStockFullData);
		            }
				}
				else
				{
					mFmt.format("[%s] update ERROR %d/%d %.3fs: %s error(%d)\n", 
							CUtilsDateTime.GetCurDateTimeStr(), i, iAllStockListSize, fCostTime, 
							stockID, errDownloaddStockFullData);
				}   
				
	        } 

            // update AllStockFullDataTimestamps
			mDataStorage.saveAllStockFullDataTimestamps(newestDate);

			double fCostTimeAll = (CUtilsDateTime.GetCurrentTimeMillis() - lTCBegin)/1000.0f;
			mFmt.format("[%s] update success all %.3fs, count: %d\n", 
					CUtilsDateTime.GetCurDateTimeStr(), fCostTimeAll, stockAllList.size()); 

		} else {
			mFmt.format("[%s] downloadAllStockFullData ERROR, WebStockLayer.getAllStockList failed!\n", 
					CUtilsDateTime.GetCurDateTimeStr());
			return -1;
		}

        return 0;
    }

    @Override
    public int updateOneLocalStocks(String stockID) {
        int error = 0;
        String curDate = CUtilsDateTime.GetCurDateStr();
        String paramToDate = curDate.replace("-", "");

        List<KLine> ctnKLineNew = new ArrayList<KLine>();
        int errGetWebKLineNew = WebStock.instance().getKLine(stockID, ctnKLineNew);// 获取网络日K数据
        
        List<DividendPayout> ctnDividendPayoutNew = new ArrayList<DividendPayout>();
        int errDividendPayoutNew = WebStock.instance().getDividendPayout(stockID, ctnDividendPayoutNew);//获取网络分红派息数据
        
        if(0 == errGetWebKLineNew 
                && 0 == errDividendPayoutNew )
        {
            // 网络获取日K，分红派息 成功
            try {
                // 保存到本地
                mDataStorage.saveLocalStockIDKLineList(stockID, ctnKLineNew);
                mDataStorage.saveDividendPayout(stockID, ctnDividendPayoutNew);
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage()); 
                error = -21;
                return error;
            }
            
            List<KLine> ctnKLineLocalNew = new ArrayList<KLine>();
            int errKLineLocalNew = mDataStorage.getLocalStockIDKLineList(stockID, ctnKLineLocalNew);
            if(errKLineLocalNew == 0) {
                //最新数据下载成功
                error = 0;
                return error;
            } else {
                error = -23;
                return error;
            }
        } else {
            // 下载日K，分红派息 失败
            error = -10;
            return error;
        }
    }

    @Override
    public int getLocalAllStockIDList(List<String> list) { 
        return mDataStorage.getLocalAllStockIDList(list);
    }
}