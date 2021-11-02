package com.dmytrobilokha.fbpom;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildConfigParser {

    private static final Path OPTIONS_FILE = Paths.get("options");
    private static final Pattern SET_UNSET_PATTERN = Pattern.compile("_(UN)?SET\\+=");

    private final Path makeFilePath;
    private final Path optionsDirectoryPath;
    private final FsService fsService;

    private final DefaultVersionsParser defaultVersionsParser = new DefaultVersionsParser();
    private final PortOptionsParser globalOptionsParser = PortOptionsParser.forGlobalOptions();
    private final Map<String, PortOptionsParser> optionsParsersMap = new HashMap<>();

    public BuildConfigParser(Path makeFilePath,
                             Path optionsDirectoryPath,
                             FsService fsService) {
        this.makeFilePath = makeFilePath;
        this.optionsDirectoryPath = optionsDirectoryPath;
        this.fsService = fsService;
    }

    public void parse() {
        parseMakeFile();
        parseOptionsFiles();
        deduplicateOptions();
    }

    private void parseMakeFile() {
        if (!fsService.isReadableRegularFile(makeFilePath)) {
            return;
        }
        for (Iterator<String> makeFileLinesIterator = fsService.getFileLinesIterator(makeFilePath);
             makeFileLinesIterator.hasNext();) {
            String makeFileLine = makeFileLinesIterator.next().trim();
            if (makeFileLine.startsWith(MakefileUtil.COMMENT_SYMBOL)) {
                continue;
            }
            String portName;
            Matcher matcher = SET_UNSET_PATTERN.matcher(makeFileLine);
            if (!matcher.find()) {
                if (!defaultVersionsParser.isDefaultVersionsLine(makeFileLine)) {
                    continue;
                }
                defaultVersionsParser.parseMakeFile(makeFileLine, makeFileLinesIterator);
                continue;
            }
            portName = makeFileLine.substring(0, matcher.start());
            if (globalOptionsParser.getName().equals(portName)) {
                globalOptionsParser.parseMakeFile(makeFileLine, makeFileLinesIterator);
            } else {
                PortOptionsParser portOptionsParser = optionsParsersMap
                        .computeIfAbsent(portName, PortOptionsParser::forRegularPort);
                portOptionsParser.parseMakeFile(makeFileLine, makeFileLinesIterator);
            }
        }
    }

    private void parseOptionsFiles() {
        if (!fsService.isReadableExecutableDirectory(optionsDirectoryPath)) {
            return;
        }
        fsService.getDirectoryListing(optionsDirectoryPath)
                .filter(fsService::isReadableExecutableDirectory)
                .forEach(this::parseOptionsDirectory);
    }

    private void parseOptionsDirectory(Path optionsDirectory) {
        Path optionsFilePath = optionsDirectory.resolve(OPTIONS_FILE);
        if (!(Files.isReadable(optionsFilePath) && Files.isRegularFile(optionsFilePath)))
            return;
        String portName = optionsDirectory.getFileName().toString();
        PortOptionsParser port = optionsParsersMap.computeIfAbsent(portName, PortOptionsParser::forRegularPort);
        port.parseOptionsFile(fsService.getFileLinesIterator(optionsFilePath));
    }

    private void deduplicateOptions() {
        for (PortOptionsParser port : optionsParsersMap.values()) {
            port.removeDuplicatingOptions(globalOptionsParser);
        }
    }

    public void printMergedMakeFile(PrintWriter pw) {
        try {
            defaultVersionsParser.writeVersions(pw);
            globalOptionsParser.writeOptions(pw);
            List<PortOptionsParser> portList = new ArrayList<>(optionsParsersMap.values());
            Collections.sort(portList);
            for (PortOptionsParser port : portList) {
                port.writeOptions(pw);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to print merged make file", e);
        }
    }

}
