package com.dmytrobilokha.fbpom;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

@Test(groups = "unit")
public class PortOptionsParserTest {

    public void parsesSimpleOption() {
        var parser = PortOptionsParser.forRegularPort("category_port");
        parser.parseOptionsFile(List.of(
                "OPTIONS_FILE_UNSET+=DOCS",
                "OPTIONS_FILE_SET+=EXAMPLES"
        ).iterator());
        var makefileLines = parser.getOptionsString();
        TestUtil.assertHasRegex(makefileLines, "category_port_UNSET\\+=\\s+DOCS");
        TestUtil.assertHasRegex(makefileLines, "category_port_SET\\+=\\s+EXAMPLES");
    }

    public void parsesSimpleMakefileOption() {
        var parser = PortOptionsParser.forRegularPort("category_port");
        parser.parseMakeFile("category_port_UNSET+=DOCS", Collections.<String>emptyList().iterator());
        var makefileLines = parser.getOptionsString();
        TestUtil.assertHasRegex(makefileLines, "category_port_UNSET\\+=\\s+DOCS");
    }

    public void parsesOptionWithMinus() {
        var parser = PortOptionsParser.forRegularPort("category_port");
        parser.parseOptionsFile(List.of("OPTIONS_FILE_UNSET+=DEP-RSA1024").iterator());
        var makefileLines = parser.getOptionsString();
        TestUtil.assertHasRegex(makefileLines, "category_port_UNSET\\+=\\s+DEP-RSA1024");
    }

    public void parsesMultilineMakefileOption() {
        var parser = PortOptionsParser.forRegularPort("category_port");
        parser.parseMakeFile("category_port_UNSET+=   AOPTION BOPTION \\", List.of("\t\tCOPTION").iterator());
        var makefileLines = parser.getOptionsString();
        TestUtil.assertHasRegex(makefileLines, "category_port_UNSET\\+=\\s+AOPTION\\s+BOPTION\\s+COPTION");
    }

}
