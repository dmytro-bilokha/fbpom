package com.dmytrobilokha.fbpom;

import org.testng.Assert;

import java.util.regex.Pattern;

public final class TestUtil {

    private TestUtil() {
        //No instance
    }

    public static void assertHasRegex(String input, String expectedPattern) {
        Assert.assertTrue(
                Pattern.compile(expectedPattern).matcher(input).find(),
                "Input string expected to contain pattern:" + MakefileUtil.NEW_LINE
                + expectedPattern + MakefileUtil.NEW_LINE
                + "But it doesn't. Actual input string is:" + MakefileUtil.NEW_LINE
                + input
        );
    }

}
