package pers.di.dataengine;
import java.util.ArrayList;
import java.util.List;

import pers.di.common.CFileSystem;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.common.CUtilsMath;
import pers.di.dataengine.internal.DStockDataEngineImpl;
import pers.di.dataprovider.DataProvider;
import pers.di.dataprovider.DataTestHelper;
import pers.di.dataprovider.TestDataProvider;
import pers.di.model.KLine;
import pers.di.model.TimePrice;

public class TestDAContext {
    public static List<String> stockIDs = new ArrayList<String>()
			{{add("999999");add("600000");;add("600056");add("300163");add("002468");}};

    @CTest.setup
    public void setup() {
        CLog.info("TestDAContext.setup");
        String newestDate = "2025-01-02";
		DataTestHelper.InitLocalData(newestDate, stockIDs);
    }
    @CTest.teardown
    public void teardown() {
        CLog.info("TestDAContext.teardown");
    }

    @CTest.test
	public void test_TestDAContext_setdate() {
        DAContext context = new DAContext();
        context.setDate("2025-01-02");
        CTest.EXPECT_STR_EQ(context.date(), "2025-01-02");
    }

    @CTest.test
	public void test_TestDAContext_getDayKLines() {
        DAContext context = new DAContext();
        // check one date
        {
            context.setDate("2025-01-02");
            CListObserver<KLine> list = context.getDayKLines("600000");
            CTest.EXPECT_TRUE(list.size() > 100);
            CTest.EXPECT_STR_EQ(list.end().date, "2025-01-02");
        }
        // check another date
        {
            context.setDate("2025-02-01");
            CListObserver<KLine> list = context.getDayKLines("600000");
            CTest.EXPECT_TRUE(list.size() > 100);
            CTest.EXPECT_STR_EQ(list.end().date, "2025-01-27");
        }
        {
            context.setDate("2025-02-05");
            CListObserver<KLine> list = context.getDayKLines("600000");
            CTest.EXPECT_TRUE(list.size() > 100);
            CTest.EXPECT_STR_EQ(list.end().date, "2025-02-05");
        }
    }

    @CTest.test
	public void test_TestDAContext_getAllStockID() {
        DAContext context = new DAContext();
        CListObserver<String> allStockID = context.getAllStockID();
        CTest.EXPECT_TRUE(stockIDs.size() >= 3);
        CTest.EXPECT_TRUE(allStockID.size() >= stockIDs.size());
    }

    public static void main(String[] args) {
		CSystem.start();
		CTest.ADD_TEST(TestDAContext.class);
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
