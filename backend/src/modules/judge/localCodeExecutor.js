import fs from 'fs/promises';
import path from 'path';
import os from 'os';
import { spawn } from 'child_process';
import { Language } from '@prisma/client';

export class LocalCodeExecutor {
  static async execute(language, sourceCode, inputData, timeLimitMs, _memoryLimitMb) {
    let tempDir = null;
    try {
      tempDir = await fs.mkdtemp(path.join(os.tmpdir(), 'codeforge_exec_'));
      const isWindows = process.platform === 'win32';

      const fileName = language === Language.JAVA ? 'Solution.java' : 'solution.cpp';
      const sourceFilePath = path.join(tempDir, fileName);
      await fs.writeFile(sourceFilePath, sourceCode, 'utf-8');

      // 1. Compilation Phase
      if (language === Language.JAVA) {
        const javacCmd = process.env.JAVA_HOME
          ? path.join(process.env.JAVA_HOME, 'bin', isWindows ? 'javac.exe' : 'javac')
          : 'javac';

        const compileRes = await this.runProcess(javacCmd, ['Solution.java'], tempDir, '', 10000);
        if (compileRes.exitCode !== 0) {
          compileRes.isCompileError = true;
          return compileRes;
        }
      } else if (language === Language.CPP) {
        const outputBinary = isWindows ? 'solution.exe' : 'solution';
        const compileRes = await this.runProcess(
          'g++',
          ['-O3', 'solution.cpp', '-o', outputBinary],
          tempDir,
          '',
          10000
        );

        if (compileRes.exitCode !== 0) {
          compileRes.isCompileError = true;
          return compileRes;
        }
      }

      // 2. Execution Phase
      let runCmd;
      let runArgs;

      if (language === Language.JAVA) {
        runCmd = process.env.JAVA_HOME
          ? path.join(process.env.JAVA_HOME, 'bin', isWindows ? 'java.exe' : 'java')
          : 'java';
        runArgs = ['Solution'];
      } else {
        const binaryName = isWindows ? 'solution.exe' : './solution';
        runCmd = path.join(tempDir, binaryName);
        runArgs = [];
      }

      return await this.runProcess(runCmd, runArgs, tempDir, inputData, timeLimitMs);
    } catch (err) {
      return {
        exitCode: -1,
        stdout: '',
        stderr: `Internal Execution Error: ${err.message || err}`,
        executionTimeMs: 0,
        memoryUsedKb: 0,
        isTimeout: false,
        isCompileError: true,
      };
    } finally {
      if (tempDir) {
        try {
          await fs.rm(tempDir, { recursive: true, force: true });
        } catch {
          // Ignore cleanup errors
        }
      }
    }
  }

  static runProcess(command, args, cwd, inputData, timeoutMs) {
    return new Promise((resolve) => {
      const startTime = Date.now();
      let stdout = '';
      let stderr = '';
      let isTimeout = false;

      const child = spawn(command, args, { cwd });

      const timer = setTimeout(() => {
        isTimeout = true;
        child.kill('SIGKILL');
      }, timeoutMs);

      if (inputData && inputData.length > 0) {
        child.stdin.write(inputData);
        child.stdin.end();
      } else {
        child.stdin.end();
      }

      child.stdout.on('data', (data) => {
        stdout += data.toString();
      });

      child.stderr.on('data', (data) => {
        stderr += data.toString();
      });

      child.on('error', (err) => {
        clearTimeout(timer);
        resolve({
          exitCode: -1,
          stdout,
          stderr: stderr || err.message,
          executionTimeMs: Date.now() - startTime,
          memoryUsedKb: Math.round(process.memoryUsage().heapUsed / 1024),
          isTimeout: false,
          isCompileError: true,
        });
      });

      child.on('close', (code) => {
        clearTimeout(timer);
        const executionTimeMs = Date.now() - startTime;
        const memoryUsedKb = Math.round(process.memoryUsage().heapUsed / 1024);

        if (isTimeout) {
          return resolve({
            exitCode: -1,
            stdout,
            stderr: `Time Limit Exceeded (${timeoutMs}ms)`,
            executionTimeMs,
            memoryUsedKb,
            isTimeout: true,
            isCompileError: false,
          });
        }

        resolve({
          exitCode: code ?? 0,
          stdout: stdout.trim(),
          stderr: stderr.trim(),
          executionTimeMs,
          memoryUsedKb,
          isTimeout: false,
          isCompileError: false,
        });
      });
    });
  }
}
