package com.codeforge.common.enums;

import lombok.Getter;

@Getter
public enum ProgrammingLanguage {
    JAVA("Java 21", "Solution.java", "javac Solution.java", "java Solution"),
    CPP("C++ 17", "solution.cpp", "g++ -O3 solution.cpp -o solution", "./solution");

    private final String displayName;
    private final String sourceFileName;
    private final String compileCommand;
    private final String runCommand;

    ProgrammingLanguage(String displayName, String sourceFileName, String compileCommand, String runCommand) {
        this.displayName = displayName;
        this.sourceFileName = sourceFileName;
        this.compileCommand = compileCommand;
        this.runCommand = runCommand;
    }
}
