import { GoogleGenAI } from '@google/genai';
import { prisma } from '../../config/prisma.js';

const SYSTEM_INSTRUCTION = `You are CodeForge AI, a Socratic coding mentor and debugging assistant. Your job is to help users understand problem logic, debug failing code, and optimize algorithms.

STRICT GUARDRAILS:
1. NEVER output a full runnable code solution or complete class/function implementations.
2. NEVER write complete Solution class code or main function code for the user.
3. Instead, provide guiding questions, conceptual algorithm steps, time/space complexity analysis, and short 1-2 line pseudocode snippets if helpful.
4. When debugging a failing verdict (e.g. WRONG_ANSWER, TIME_LIMIT_EXCEEDED, RUNTIME_ERROR), explain WHY the logic breaks on specific edge cases (e.g., negative numbers, empty arrays, integer overflow, pointer bounds) without writing the solution for them.
5. Format your response cleanly using GitHub Markdown. Keep your tone encouraging, concise, and focused on helping the user learn.`;

export class AiService {
  static async getGuidance(input) {
    const { problemSlug, currentCode, language, verdict, errorOutput, promptType, userQuestion } = input;

    // Fetch problem context
    const problem = await prisma.problem.findUnique({
      where: { slug: problemSlug },
      include: {
        testCases: { where: { isSample: true } },
      },
    });

    const problemTitle = problem ? problem.title : problemSlug;
    const problemDesc = problem ? problem.description : '';

    const apiKey = process.env.GEMINI_API_KEY;

    if (apiKey && apiKey.trim().length > 0) {
      try {
        const ai = new GoogleGenAI({ apiKey });

        const promptPayload = `
### Problem Context
- Title: ${problemTitle}
- Description: ${problemDesc}

### User Submission Info
- Programming Language: ${language}
- Current Code:
\`\`\`${language.toLowerCase()}
${currentCode || '// No code written yet'}
\`\`\`

### Execution Status & Diagnostics
- Latest Verdict: ${verdict || 'Not submitted yet'}
- Error / Diagnostic Output: ${errorOutput || 'None'}

### User Request
- Request Type: ${promptType}
- Specific Question from User: ${userQuestion || 'Can you give me guidance on how to solve or debug this problem?'}

Please provide Socratic guidance, algorithm hints, or debugging insight following your strict system instructions (NO FULL CODE SOLUTIONS).
`;

        const response = await ai.models.generateContent({
          model: 'gemini-2.0-flash',
          contents: promptPayload,
          config: {
            systemInstruction: SYSTEM_INSTRUCTION,
            temperature: 0.7,
            maxOutputTokens: 800,
          },
        });

        const text = response.text || response.candidates?.[0]?.content?.parts?.[0]?.text;
        if (text) {
          return {
            reply: text.trim(),
            promptType,
            isDemoFallback: false,
          };
        }
      } catch (err) {
        console.warn('⚠️ Gemini API request failed, switching to demo fallback:', err.message || err);
      }
    }

    // Demo Fallback Generator (when API key is missing or unreachable)
    return {
      reply: this.generateDemoFallback(problemTitle, promptType, verdict, errorOutput, userQuestion),
      promptType,
      isDemoFallback: true,
    };
  }

  static generateDemoFallback(problemTitle, promptType, verdict, errorOutput, userQuestion) {
    if (promptType === 'DEBUG' || verdict === 'WRONG_ANSWER') {
      return `### 🐛 Debugging Guidance for **${problemTitle}**

1. **Check Edge Cases**:
   - What happens when array elements are duplicates or negative?
   - Ensure your loop indices don't skip the first or last element.

2. **Diagnose Output Mismatch**:
   - Compare your code's intermediate return values against sample inputs.
   - If test cases fail on larger inputs, check if your variables overflow integer bounds.

> **Guiding Question**: Have you checked if your inner loop compares the same index twice? Try tracing `i` and `j` manually on a 2-element array.`;
    }

    if (promptType === 'OPTIMIZE') {
      return `### ⚡ Complexity Optimization Tips for **${problemTitle}**

- **Brute Force**: A nested two-loop approach takes **O(N²)** time complexity.
- **Optimal Approach**: Can we trade a little space for speed?
  - Consider using a **Hash Map / Unordered Map** to store visited elements in **O(1)** lookup time.
  - This reduces the overall time complexity from **O(N²)** down to **O(N)** linear time!

> **Guiding Question**: What value do you need to find at step \`i\` such that \`current + needed = target\`?`;
    }

    return `### 💡 Algorithmic Hint for **${problemTitle}**

1. **Understand the Goal**: Identify what inputs are passed to your \`Solution\` class method and what format the return value expects.
2. **Key Insight**:
   - Instead of checking every pair sequentially, ask: *"Can I check if the complementary value was already seen?"*
   - Consider storing key-value pairs of \`[element_value -> index]\`.

> **Guiding Question**: If you inspect an element \`x\`, what formula gives the matching pair value you need to look up?`;
  }
}
