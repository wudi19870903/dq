package pers.di.account;

import pers.di.common.CSystem;
import pers.di.common.CTest;

public class TestAllCases {
    public static void addAllTestCases() {
        CTest.ADD_TEST(TestAccountController.class);
        CTest.ADD_TEST(TestCommisionDealOrlder.class);
        CTest.ADD_TEST(TestMockAccountMockMarketOpe.class);
	}

    public static void main(String[] args) {
		CSystem.start();
		addAllTestCases();
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
