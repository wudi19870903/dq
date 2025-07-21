package pers.di.common;

import pers.di.common.*;

public class TestCHttp {

	public static void main(String[] args) {
		
		CHttp cCHttp = new CHttp();
		// TODO Auto-generated method stub
		String url1 = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sz002095&scale=60&ma=no&datalen=1023";
		String url2 = "http://vip.stock.finance.sina.com.cn/corp/go.php/vISSUE_ShareBonus/stockid/300163.phtml";
		String url3 = "http://quotes.money.163.com/service/chddata.html?code=0601857&start=20071105&end=20150618&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";
		String htmlstr = cCHttp.getWebData(url3, 1000);
		System.out.println(htmlstr); 
	}

}
