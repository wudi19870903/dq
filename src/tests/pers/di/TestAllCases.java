package pers.di;

import pers.di.common.CSystem;
import pers.di.common.CTest;

public class TestAllCases {
    public static void addAllTestCases() {
        pers.di.common.TestAllCases.addAllTestCases();
        pers.di.webstock.TestAllCases.addAllTestCases();
        pers.di.dataprovider.TestAllCases.addAllTestCases();
	}
    public static void main(String[] args) {
       	CSystem.start();
        addAllTestCases();
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
    }
}
