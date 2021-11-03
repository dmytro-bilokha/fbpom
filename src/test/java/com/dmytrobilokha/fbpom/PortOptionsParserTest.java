package com.dmytrobilokha.fbpom;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.regex.Pattern;

@Test(groups = "unit")
public class PortOptionsParserTest {

    public void parsesSimpleOption() {
        var parser = PortOptionsParser.forRegularPort("category_port");
        parser.parseOptionsFile(List.of(
                "OPTIONS_FILE_UNSET+=DOCS",
                "OPTIONS_FILE_SET+=EXAMPLES"
        ).iterator());
        var makefileLines = parser.getOptionsString();
        Assert.assertTrue(Pattern.compile("category_port_UNSET\\+=\\s+DOCS").matcher(makefileLines).find());
        Assert.assertTrue(Pattern.compile("category_port_SET\\+=\\s+EXAMPLES").matcher(makefileLines).find());
    }

    public void parsesOptionWithMinus() {
        var parser = PortOptionsParser.forRegularPort("category_port");
        parser.parseOptionsFile(List.of("OPTIONS_FILE_UNSET+=DEP-RSA1024").iterator());
        var makefileLines = parser.getOptionsString();
        Assert.assertTrue(Pattern.compile("category_port_UNSET\\+=\\s+DEP-RSA1024").matcher(makefileLines).find());
    }

}
