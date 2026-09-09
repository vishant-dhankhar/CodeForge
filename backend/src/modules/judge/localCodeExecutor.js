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

      const finalCode = this.wrapCodeWithHarness(language, sourceCode);
      const isJavaRunner = language === Language.JAVA && finalCode.includes('class SolutionRunner');

      const fileName = language === Language.JAVA
        ? (isJavaRunner ? 'SolutionRunner.java' : 'Solution.java')
        : 'solution.cpp';
      const sourceFilePath = path.join(tempDir, fileName);
      await fs.writeFile(sourceFilePath, finalCode, 'utf-8');

      // 1. Compilation Phase
      if (language === Language.JAVA) {
        const javacCmd = process.env.JAVA_HOME
          ? path.join(process.env.JAVA_HOME, 'bin', isWindows ? 'javac.exe' : 'javac')
          : 'javac';

        const compileRes = await this.runProcess(javacCmd, [fileName], tempDir, '', 10000);
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
        runArgs = [isJavaRunner ? 'SolutionRunner' : 'Solution'];
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

  static wrapCodeWithHarness(language, sourceCode) {
    const hasMain = sourceCode.includes('main(') || sourceCode.includes('main (');
    if (hasMain) {
      return sourceCode;
    }

    if (language === Language.CPP) {
      if (sourceCode.includes('twoSum')) {
        return `#include <iostream>
#include <vector>
#include <string>
#include <algorithm>
using namespace std;

${sourceCode}

int main() {
    int n;
    if (!(cin >> n)) return 0;
    vector<int> nums(n);
    for (int i = 0; i < n; i++) cin >> nums[i];
    int target;
    cin >> target;

    Solution solver;
    vector<int> res = solver.twoSum(nums, target);
    for (size_t i = 0; i < res.size(); i++) {
        cout << res[i] << (i + 1 == res.size() ? "" : " ");
    }
    cout << endl;
    return 0;
}`;
      } else if (sourceCode.includes('reverseString')) {
        return `#include <iostream>
#include <string>
#include <algorithm>
using namespace std;

${sourceCode}

int main() {
    string s;
    if (!(cin >> s)) return 0;

    Solution solver;
    string res = solver.reverseString(s);
    cout << res << endl;
    return 0;
}`;
      }
    } else if (language === Language.JAVA) {
      if (sourceCode.includes('twoSum')) {
        return `import java.util.*;

${sourceCode}

public class SolutionRunner {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
        int target = sc.nextInt();

        Solution solver = new Solution();
        int[] res = solver.twoSum(nums, target);
        for (int i = 0; i < res.length; i++) {
            System.out.print(res[i] + (i + 1 == res.length ? "" : " "));
        }
        System.out.println();
    }
}`;
      } else if (sourceCode.includes('reverseString')) {
        return `import java.util.*;

${sourceCode}

public class SolutionRunner {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNext()) return;
        String s = sc.next();

        Solution solver = new Solution();
        String res = solver.reverseString(s);
        System.out.println(res);
    }
}`;
      }
    }

    return sourceCode;
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
