package com.dmytrobilokha.fbpom;

import java.io.PrintWriter;
import java.nio.file.Paths;

public class FbpomCli {

    private static final String BASE_PATH = "/usr/local/etc/poudriere.d/";
    private static final String MAKE_CONF_SUFFIX = "-make.conf";
    private static final String OPTIONS_DIRECTORY_SUFFIX = "-options";

    private FbpomCli() {
        //No instance
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Provide one option - file/directory prefix");
            System.exit(1);
        }
        String prefix = args[0];
        BuildConfigParser parser = new BuildConfigParser(
                Paths.get(BASE_PATH + prefix + MAKE_CONF_SUFFIX),
                Paths.get(BASE_PATH + prefix + OPTIONS_DIRECTORY_SUFFIX),
                new FsService()
        );
        parser.parse();
        try (PrintWriter pw = new PrintWriter(System.out)) {
            parser.printMergedMakeFile(pw);
        }
    }

}
