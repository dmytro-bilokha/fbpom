package com.dmytrobilokha.fbpom;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class DefaultVersionsParser {

    private static final String DEFAULT_VERSIONS_TOKEN = "DEFAULT_VERSIONS+=";
    private static final Pattern VERSION_PATTERN = Pattern.compile("[.\\p{Alnum}_-]+=[.\\p{Alnum}_-]+");

    private final SortedSet<String> versions = new TreeSet<>();

    public boolean isDefaultVersionsLine(String line) {
        return line.startsWith(DEFAULT_VERSIONS_TOKEN);
    }

    public void parseMakeFile(String firstLine, Iterator<String> lineIterator) {
        if (!isDefaultVersionsLine(firstLine)) {
            throw new IllegalArgumentException(firstLine + " - is not a default versions line");
        }
        String line = firstLine.substring(DEFAULT_VERSIONS_TOKEN.length());
        String[] versionTokens = MakefileUtil.splitTokens(line);
        addVersions(versionTokens);
        while (MakefileUtil.isMoreTokens(versionTokens, lineIterator)) {
            line = lineIterator.next().trim();
            versionTokens = MakefileUtil.splitTokens(line);
            addVersions(versionTokens);
        }
    }

    private void addVersions(String[] versionTokens) {
        for (String versionToken : versionTokens) {
            if (VERSION_PATTERN.matcher(versionToken).matches()) {
                versions.add(versionToken);
            }
        }
    }

    public void writeVersions(Writer writer) throws IOException {
        if (versions.isEmpty()) {
            return;
        }
        writer.append(MakefileUtil.COMMENT_SYMBOL)
                .append(" Default versions")
                .append(MakefileUtil.NEW_LINE)
                .append(DEFAULT_VERSIONS_TOKEN)
                .append(MakefileUtil.formatTokens(versions))
                .append(MakefileUtil.NEW_LINE);
    }

}
