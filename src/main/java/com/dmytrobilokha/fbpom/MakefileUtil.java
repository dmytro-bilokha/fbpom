package com.dmytrobilokha.fbpom;

import java.util.Iterator;
import java.util.regex.Pattern;

public class MakefileUtil {

    public static final String COMMENT_SYMBOL = "#";
    public static final String NEXT_LINE = "\\";
    public static final String NEW_LINE = System.lineSeparator();

    private static final String NEW_OPTION_LINE = NEXT_LINE + NEW_LINE + "\t\t\t";
    private static final int NEW_LINE_TRESHOLD = 50;
    private static final Pattern SEPARATION_PATTERN = Pattern.compile("\\s");

    private MakefileUtil() {
        //No instance
    }

    public static String formatTokens(Iterable<String> tokens) {
        if (!tokens.iterator().hasNext()) {
            return "";
        }
        StringBuilder outputBuilder = new StringBuilder("\t");
        int lineLength = 0;
        for (String token : tokens) {
            if (lineLength + token.length() > NEW_LINE_TRESHOLD && token.length() < NEW_LINE_TRESHOLD) {
                outputBuilder.append(NEW_OPTION_LINE);
                lineLength = 0;
            }
            outputBuilder.append(token).append(' ');
            lineLength += token.length() + 1;
        }
        outputBuilder.append(NEW_LINE);
        return outputBuilder.toString();
    }

    public static String[] splitTokens(String text) {
        return SEPARATION_PATTERN.split(text);
    }

    public static boolean isMoreTokens(String[] tokens, Iterator<String> lineIterator) {
        return (tokens.length == 0 || NEW_LINE.equals(tokens[tokens.length - 1])) && lineIterator.hasNext();
    }

}
