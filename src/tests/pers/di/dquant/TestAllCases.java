package pers.di.dquant;

import pers.di.common.CSystem;
import pers.di.common.CTest;

public class TestAllCases {
    public static void addAllTestCases() {
        CTest.ADD_TEST(TestDQuantPick.class);
        CTest.ADD_TEST(TestDQuantTransaction.class);
	}

    public static void main(String[] args) {
		CSystem.start();
		addAllTestCases();
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
