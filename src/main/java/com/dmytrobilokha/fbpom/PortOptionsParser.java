package com.dmytrobilokha.fbpom;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PortOptionsParser implements Comparable<PortOptionsParser> {

    private static final String SET = "_SET+=";
    private static final String UNSET = "_UNSET+=";
    private static final Pattern OPTION_PATTERN = Pattern.compile("[\\p{Alnum}_]+");
    private static final Pattern CATEGORY_NAME_SEPARATOR_PATTERN = Pattern.compile("_");

    private final Map<String, OptionStatus> optionsMap = new HashMap<>();
    private final String name;
    private final String slashedName;
    private final String inOptionsSet;
    private final String inOptionsUnset;
    private final String outOptionsSet;
    private final String outOptionsUnset;

    private PortOptionsParser(String name,
                              String slashedName,
                              String inOptionsSet,
                              String inOptionsUnset,
                              String outOptionsSet,
                              String outOptionsUnset) {
        this.name = name;
        this.slashedName = slashedName;
        this.inOptionsSet = inOptionsSet;
        this.inOptionsUnset = inOptionsUnset;
        this.outOptionsSet = outOptionsSet;
        this.outOptionsUnset = outOptionsUnset;
    }

    public static PortOptionsParser forRegularPort(String portName) {
        return new PortOptionsParser(
                portName,
                CATEGORY_NAME_SEPARATOR_PATTERN.matcher(portName).replaceFirst("/"),
                "OPTIONS_FILE_SET+=",
                "OPTIONS_FILE_UNSET+=",
                portName + SET,
                portName + UNSET
        );
    }

    public static PortOptionsParser forGlobalOptions() {
        String prefix = "OPTIONS";
        return new PortOptionsParser(
                prefix,
                "Global ports",
                prefix + SET,
                prefix + UNSET,
                prefix + SET,
                prefix + UNSET
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PortOptionsParser that = (PortOptionsParser) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public void parseOptionsFile(Iterator<String> lineIterator) {
        OptionStatus lineStatus = OptionStatus.NOT_DEFINED;
        while (lineIterator.hasNext()) {
            String line = lineIterator.next().trim();
            if (line.startsWith(inOptionsSet)) {
                lineStatus = OptionStatus.SET;
                line = line.substring(inOptionsSet.length());
            } else if (line.startsWith(inOptionsUnset)) {
                lineStatus = OptionStatus.UNSET;
                line = line.substring(inOptionsUnset.length());
            }
            if (lineStatus != OptionStatus.NOT_DEFINED) {
                String[] options = MakefileUtil.splitTokens(line);
                addOptions(lineStatus, options);
                if (options.length > 0 && !MakefileUtil.NEXT_LINE.equals(options[options.length - 1]))
                    lineStatus = OptionStatus.NOT_DEFINED;
            }
        }
    }

    public void parseMakeFile(String firstLine, Iterator<String> lineIterator) {
        OptionStatus lineStatus;
        String line;
        if (firstLine.startsWith(outOptionsSet)) {
            lineStatus = OptionStatus.SET;
            line = firstLine.substring(outOptionsSet.length());
        } else if (firstLine.startsWith(outOptionsUnset)) {
            lineStatus = OptionStatus.UNSET;
            line = firstLine.substring(outOptionsUnset.length());
        } else {
            return;
        }
        String[] options = MakefileUtil.splitTokens(line);
        addOptions(lineStatus, options);
        while (MakefileUtil.isMoreTokens(options, lineIterator)) {
            line = lineIterator.next().trim();
            options = MakefileUtil.splitTokens(line);
            addOptions(lineStatus, options);
        }
    }

    private void addOptions(OptionStatus status, String[] options) {
        if (status != OptionStatus.SET && status != OptionStatus.UNSET)
            return;
        for (String option : options) {
            if (!OPTION_PATTERN.matcher(option).matches())
                continue;
            optionsMap.put(option, status);
        }
    }

    public void removeDuplicatingOptions(PortOptionsParser parentPort) {
        for (Map.Entry<String, OptionStatus> parentOptionEntry : parentPort.optionsMap.entrySet()) {
            optionsMap.remove(parentOptionEntry.getKey(), parentOptionEntry.getValue());
        }
    }

    public void writeOptions(Writer writer) throws IOException {
        writer.append(getOptionsComment())
                .append(getSetOptionsString())
                .append(getUnsetOptionsString())
                .append(MakefileUtil.NEW_LINE);
    }

    private String getOptionsComment() {
        return MakefileUtil.COMMENT_SYMBOL + " " + slashedName + " options" + MakefileUtil.NEW_LINE;
    }

    private String getSetOptionsString() {
        String optionsString = MakefileUtil.formatTokens(getSortedOptionsWithStatus(OptionStatus.SET));
        if (optionsString.isEmpty())
            return optionsString;
        return outOptionsSet + optionsString;

    }

    private List<String> getSortedOptionsWithStatus(OptionStatus status) {
        return optionsMap.entrySet()
                .stream()
                .filter(es -> es.getValue() == status)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    private String getUnsetOptionsString() {
        String optionsString = MakefileUtil.formatTokens(getSortedOptionsWithStatus(OptionStatus.UNSET));
        if (optionsString.isEmpty())
            return optionsString;
        return outOptionsUnset + optionsString;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(PortOptionsParser o) {
        return this.name.compareTo(o.name);
    }

    public enum OptionStatus {
        SET, UNSET, NOT_DEFINED;
    }

}
