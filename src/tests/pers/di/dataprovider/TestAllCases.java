package pers.di.dataprovider;

import pers.di.common.CSystem;
import pers.di.common.CTest;

public class TestAllCases {
    public static void addAllTestCases() {
        CTest.ADD_TEST(TestDataProvider.class);
	}

    public static void main(String[] args) {
		CSystem.start();
		addAllTestCases();
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
