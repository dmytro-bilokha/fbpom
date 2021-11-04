package com.dmytrobilokha.fbpom;

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

//TODO: Change this to have separate makefile parser, option file parser and results representing structure,
//then it should be way easier to write tests.
//TODO: implement integration test which checks that if tool reads its out output as input, there are no changes
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

    //TODO: refactor, it is a bit nasty
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
        if (!(Files.isReadable(optionsFilePath) && Files.isRegularFile(optionsFilePath))) {
            return;
        }
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
        pw.append(defaultVersionsParser.getVersionsString());
        pw.append(globalOptionsParser.getOptionsString());
        List<PortOptionsParser> portList = new ArrayList<>(optionsParsersMap.values());
        Collections.sort(portList);
        for (PortOptionsParser port : portList) {
            pw.append(port.getOptionsString());
        }
    }

}
