package pers.di.dataprovider;

import java.util.ArrayList;
import java.util.List;

import pers.di.common.CFileSystem;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;

public class TestDataProvider {
    @CTest.test
	public void test_dataRoot() {
        String dataRoot = DataProvider.getInstance().dataRoot();
		CTest.EXPECT_TRUE(dataRoot != null);
        CTest.EXPECT_TRUE(dataRoot.contains("rw/data"));
    }

    @CTest.test
	public void test_getLocalAllStockIDList() {
        List<String> stockList = new ArrayList<>(); 
        int ret = DataProvider.getInstance().getLocalAllStockIDList(stockList);
		CTest.EXPECT_TRUE(0 == ret);
        CTest.EXPECT_TRUE(stockList.size() > 0);
        System.out.println("stockList size:" + stockList.size());
        for (int i = 0; i < 4; i++) {
            System.out.println(stockList.get(i));
        }
    }
    @CTest.test
	public void test_updateOneLocalStocks() {
        {
            String stockID = "601398"; //工商银行

            // clear
            String dateDir = DataProvider.getInstance().dataRoot() + "\\" + stockID;
            CFileSystem.removeDir(dateDir);
            CTest.EXPECT_FALSE(CFileSystem.isDirExist(dateDir));

            int ret = DataProvider.getInstance().updateOneLocalStocks(stockID);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(CFileSystem.isDirExist(dateDir));
            System.out.println("update stockID:" + stockID);
        }
        {
            String stockID = "920819"; //颖泰生物

            // clear
            String dateDir = DataProvider.getInstance().dataRoot() + "\\" + stockID;
            CFileSystem.removeDir(dateDir);
            CTest.EXPECT_FALSE(CFileSystem.isDirExist(dateDir));

            int ret = DataProvider.getInstance().updateOneLocalStocks(stockID);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(CFileSystem.isDirExist(dateDir));
            System.out.println("update stockID:" + stockID);
        }

    }
    

    public static void main(String[] args) {
		CSystem.start();
		CTest.ADD_TEST(TestDataProvider.class);
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
