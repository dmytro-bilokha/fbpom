package com.dmytrobilokha.fbpom;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

@Test(groups = "unit")
public class DefaultVersionsParserTest {

    private static final String SIMPLE_VERSIONS = "DEFAULT_VERSIONS+=	java=11";
    private static final String DOUBLE_VERSIONS = "DEFAULT_VERSIONS+=	java=11 python=3.8";
    private static final String DOUBLE_MULTILINE_VERSIONS = "DEFAULT_VERSIONS+=	java=11 \\" + System.lineSeparator()
            + System.lineSeparator()
            + "python=3.8";

    @Test(groups = "default-parser-line-recognition")
    public void recognizesValidSimpleVersionLine() {
        var parser = new DefaultVersionsParser();
        Assert.assertTrue(parser.isDefaultVersionsLine(SIMPLE_VERSIONS));
    }

    @Test(dependsOnGroups = "default-parser-line-recognition")
    public void parsesSimpleVersionLine() {
        var parser = new DefaultVersionsParser();
        parser.parseMakeFile(SIMPLE_VERSIONS, Collections.emptyIterator());
        Assert.assertTrue(parser.getVersionsString().contains(SIMPLE_VERSIONS));
    }

    @Test(groups = "default-parser-line-recognition")
    public void recognizesValidMultiVersionLine() {
        var parser = new DefaultVersionsParser();
        Assert.assertTrue(parser.isDefaultVersionsLine(DOUBLE_VERSIONS));
    }

    @Test(dependsOnGroups = "default-parser-line-recognition")
    public void parsesDoubleVersionLine() {
        var parser = new DefaultVersionsParser();
        parser.parseMakeFile(DOUBLE_VERSIONS, Collections.emptyIterator());
        Assert.assertTrue(parser.getVersionsString().contains(DOUBLE_VERSIONS));
    }

    @Test(dependsOnGroups = "default-parser-line-recognition")
    public void parsesDoubleMultilineVersionLine() {
        var parser = new DefaultVersionsParser();
        parser.parseMakeFile(DOUBLE_MULTILINE_VERSIONS, Collections.emptyIterator());
        Assert.assertTrue(parser.getVersionsString().contains(DOUBLE_VERSIONS));
    }

    @Test(groups = "default-parser-line-recognition")
    public void recognizesNonValidVersionLine() {
        var parser = new DefaultVersionsParser();
        Assert.assertFalse(parser.isDefaultVersionsLine("OPTIONS_SET+=	java=11"));
    }

}
