package pers.di.common;

import pers.di.common.CFileSystem;
import pers.di.common.CSystem;
import pers.di.common.CXmlTable;
import pers.di.common.CXmlTable.CALLBACKTYPE;
import pers.di.common.CXmlTable.RowCursor;
import pers.di.common.CTest;

public class TestCXmlTable {
	
	public static int s_iOnCommited = 0;
	
	public static class NotifyCB implements CXmlTable.ICallback
	{

		@Override
		public void onNotify(CALLBACKTYPE type) {
			// TODO Auto-generated method stub
			if(CALLBACKTYPE.COMMITED == type)
			{
				s_iOnCommited++;
			}
		}
		
	}
	
	@CTest.test
	public static void test_ctable()
	{
		String datafile = CSystem.getRunSessionRoot() + "/test.xml";
		CFileSystem.removeFile(datafile);
		CTest.EXPECT_TRUE(!CFileSystem.isFileExist(datafile));
		
		CXmlTable cTable = new CXmlTable(datafile);
		
		boolean bOpen = cTable.open();
		CTest.EXPECT_TRUE(bOpen);
		cTable.registerCallback(new NotifyCB());
		
		// init row ----------------
		// row1
		{
			RowCursor cursor = cTable.addRow();
			cursor.setColume("name", "xiaoming");
			cursor.setColume("age", "23");
			cursor.setColume("ID1", "8754");
			cursor.setColume("ID2", "8754");
			cursor.setColume("ID3", "8754");
			cursor.setColume("I4D", "8754");
			cursor.setColume("ID5", "8754");
			cursor.setColume("I6D", "8754");
			cursor.setColume("I7D", "8754");
			cursor.setColume("I8D", "8754");
			cursor.setColume("I9D", "8754");
			cursor.setColume("I0D", "8754");
			cursor.setColume("IeD", "8754");
			cursor.setColume("IxD", "8754");
			cursor.setColume("IdfaD", "8754");
			cursor.setColume("I122D", "8754");
			cursor.setColume("I234D", "8754");
			CTest.EXPECT_TRUE(null != cursor);
			
			CTest.EXPECT_LONG_EQ(cursor.columesMap().size(), 17);
			
			CTest.EXPECT_NULL(cursor.getColume("XXXXXXXXXX"));
			CTest.EXPECT_NOT_NULL(cursor.getColume("I234D"));
			
		}
		
		// row2
		{
			RowCursor cursor = cTable.addRow();
			cursor.setColume("name", "wudi");
			cursor.setColume("age", "14");
			cursor.setColume("ID", "145");
			CTest.EXPECT_TRUE(null != cursor);
		}
				
		// row3
		{
			RowCursor cursor = cTable.addRow();
			cursor.setColume("name", "limy");
			cursor.setColume("age", "24");
			cursor.setColume("ID", "35");
			CTest.EXPECT_TRUE(null != cursor);
		}
		
		// traversal ----------------
		{
			RowCursor cursor = cTable.moveFirst();
			CTest.EXPECT_TRUE(null != cursor);
			CTest.EXPECT_STR_EQ(cursor.getColume("name"), "xiaoming");
			CTest.EXPECT_STR_EQ(cursor.getColume("age"), "23");
			
			cursor = cTable.moveNext();
			CTest.EXPECT_STR_EQ(cursor.getColume("name"), "wudi");
			CTest.EXPECT_STR_EQ(cursor.getColume("age"), "14");
			
			cursor = cTable.moveNext();
			CTest.EXPECT_STR_EQ(cursor.getColume("name"), "limy");
			CTest.EXPECT_STR_EQ(cursor.getColume("age"), "24");
			
			cursor = cTable.moveNext();
			CTest.EXPECT_TRUE(null == cursor);
			
			cursor = cTable.moveFirst();
			int iWhile = 0;
			while(null != cursor)
			{
				iWhile++;
				cursor = cTable.moveNext();
			}
			CTest.EXPECT_LONG_EQ(iWhile, 3);
			
			CTest.EXPECT_LONG_EQ(cTable.size(), 3);
		}
		
		// deleteRow valid
		{
			RowCursor cursor = cTable.moveFirst();
			cursor = cTable.moveNext();
			cTable.deleteRow(cursor);
			CTest.EXPECT_FALSE(cursor.valid());
			
			CTest.EXPECT_LONG_EQ(cTable.size(), 2);
		}

		CTest.EXPECT_TRUE(cTable.commit());
		
		CTest.EXPECT_LONG_EQ(s_iOnCommited, 1);
	}
	


	public static void main(String[] args) {
		CSystem.start();
		CTest.ADD_TEST(TestCXmlTable.class);
		CTest.RUN_ALL_TESTS("TestCXmlTable.");
		CSystem.stop();
	}
}
