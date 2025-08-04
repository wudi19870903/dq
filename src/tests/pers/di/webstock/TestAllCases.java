package pers.di.webstock;

import java.util.ArrayList;
import java.util.List;

import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.common.CUtilsDateTime;
import pers.di.common.TestCDateTimeThruster;
import pers.di.common.TestCQThread;
import pers.di.common.TestCThread;
import pers.di.common.TestCUtilsDateTime;
import pers.di.common.TestCWatiObj;
import pers.di.webstock.*;
import pers.di.webstock.IWebStock.*;

public class TestAllCases {
	public static void addAllTestCases() {
        CTest.ADD_TEST(TestWebStock.class);
	}

    public static void main(String[] args) {
       	CSystem.start();
		addAllTestCases();
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
    }
}
