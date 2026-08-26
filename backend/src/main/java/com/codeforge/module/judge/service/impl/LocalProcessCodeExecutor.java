package com.codeforge.module.judge.service.impl;

import com.codeforge.common.enums.ProgrammingLanguage;
import com.codeforge.module.judge.dto.ExecutionResult;
import com.codeforge.module.judge.service.CodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LocalProcessCodeExecutor implements CodeExecutor {

    @Override
    public ExecutionResult execute(
            ProgrammingLanguage language,
            String sourceCode,
            String inputData,
            int timeLimitMs,
            int memoryLimitMb
    ) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("codeforge_exec_");
            String fileName = language == ProgrammingLanguage.JAVA ? "Solution.java" : "solution.cpp";
            Path sourceFilePath = tempDir.resolve(fileName);
            Files.writeString(sourceFilePath, sourceCode, StandardCharsets.UTF_8);

            // 1. Compilation Phase
            if (language == ProgrammingLanguage.JAVA) {
                String javacCmd = getJavacCommand();
                ExecutionResult compileResult = runProcess(
                        new ProcessBuilder(javacCmd, sourceFilePath.getFileName().toString()),
                        tempDir,
                        null,
                        10000
                );
                if (compileResult.getExitCode() != 0) {
                    compileResult.setCompileError(true);
                    return compileResult;
                }
            } else if (language == ProgrammingLanguage.CPP) {
                boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
                String outputBinary = isWindows ? "solution.exe" : "./solution";
                try {
                    ExecutionResult compileResult = runProcess(
                            new ProcessBuilder("g++", "-O3", sourceFilePath.getFileName().toString(), "-o", outputBinary),
                            tempDir,
                            null,
                            10000
                    );
                    if (compileResult.getExitCode() != 0) {
                        compileResult.setCompileError(true);
                        return compileResult;
                    }
                } catch (IOException ioEx) {
                    return ExecutionResult.builder()
                            .exitCode(1)
                            .isCompileError(true)
                            .stderr("C++ compiler (g++) is not found in system PATH. Please ensure MinGW/g++ is installed or submit your solution in Java.")
                            .build();
                }
            }

            // 2. Execution Phase
            ProcessBuilder runPb;
            if (language == ProgrammingLanguage.JAVA) {
                String javaCmd = getJavaCommand();
                runPb = new ProcessBuilder(javaCmd, "-Xmx" + memoryLimitMb + "m", "Solution");
            } else {
                boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
                String binaryName = isWindows ? "solution.exe" : "./solution";
                runPb = new ProcessBuilder(tempDir.resolve(binaryName).toString());
            }

            return runProcess(runPb, tempDir, inputData, timeLimitMs);

        } catch (Exception ex) {
            log.error("Execution failure for language {}", language, ex);
            return ExecutionResult.builder()
                    .exitCode(-1)
                    .stderr("Internal Execution Error: " + ex.getMessage())
                    .build();
        } finally {
            cleanupDirectory(tempDir);
        }
    }

    private String getJavacCommand() {
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            String exe = isWindows ? "javac.exe" : "javac";
            Path javacPath = Path.of(javaHome, "bin", exe);
            if (Files.exists(javacPath)) {
                return javacPath.toAbsolutePath().toString();
            }
        }
        return "javac";
    }

    private String getJavaCommand() {
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            String exe = isWindows ? "java.exe" : "java";
            Path javaPath = Path.of(javaHome, "bin", exe);
            if (Files.exists(javaPath)) {
                return javaPath.toAbsolutePath().toString();
            }
        }
        return "java";
    }

    private ExecutionResult runProcess(
            ProcessBuilder pb,
            Path workingDir,
            String inputData,
            int timeoutMs
    ) throws IOException, InterruptedException {
        pb.directory(workingDir.toFile());
        long startTime = System.currentTimeMillis();

        Process process = pb.start();

        // Feed stdin if provided
        if (inputData != null && !inputData.isEmpty()) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(inputData.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (IOException ignored) {
            }
        } else {
            process.getOutputStream().close();
        }

        boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        long executionTimeMs = System.currentTimeMillis() - startTime;

        if (!finished) {
            process.destroyForcibly();
            return ExecutionResult.builder()
                    .isTimeout(true)
                    .executionTimeMs((int) executionTimeMs)
                    .stderr("Time Limit Exceeded (" + timeoutMs + "ms)")
                    .build();
        }

        String stdout = readStream(process.getInputStream());
        String stderr = readStream(process.getErrorStream());

        return ExecutionResult.builder()
                .exitCode(process.exitValue())
                .stdout(stdout)
                .stderr(stderr)
                .executionTimeMs((int) executionTimeMs)
                .memoryUsedKb((int) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024)
                .build();
    }

    private String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }

    private void cleanupDirectory(Path dir) {
        if (dir != null && Files.exists(dir)) {
            try {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (Exception ex) {
                log.warn("Failed to clean up temporary execution directory: {}", dir, ex);
            }
        }
    }
}
