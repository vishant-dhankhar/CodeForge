import React, { useState, useEffect, useRef } from 'react';
import Editor from '@monaco-editor/react';
import confetti from 'canvas-confetti';
import { problemsApi, submissionsApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { 
  ArrowLeft, Play, Clock, Cpu, CheckCircle2, XCircle, AlertTriangle, 
  RotateCcw, Loader2, Sparkles, Terminal, FileCode2, History 
} from 'lucide-react';

const STARTER_TEMPLATES = {
  JAVA: `import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Write your solution here
        
    }
}`,
  CPP: `#include <iostream>
#include <vector>
#include <string>
#include <algorithm>

using namespace std;

int main() {
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);
    // Write your solution here
    
    return 0;
}`,
};

export function ProblemWorkspace({ slug, onBack, onOpenAuth }) {
  const { isAuthenticated, refreshProfile } = useAuth();
  const [problem, setProblem] = useState(null);
  const [loadingError, setLoadingError] = useState(null);
  const [language, setLanguage] = useState('JAVA');
  const [code, setCode] = useState(STARTER_TEMPLATES.JAVA);
  const [activeTab, setActiveTab] = useState('statement'); // 'statement' | 'submissions'
  const [problemSubmissions, setProblemSubmissions] = useState([]);
  
  // Submission state
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [activeSubmission, setActiveSubmission] = useState(null);
  const [pollingError, setPollingError] = useState(null);

  const pollIntervalRef = useRef(null);

  const loadProblem = async () => {
    setLoadingError(null);
    try {
      const res = await problemsApi.getProblemBySlug(slug);
      if (res.success && res.data) {
        setProblem(res.data);
      }
    } catch (err) {
      console.error('Failed to load problem:', err);
      setLoadingError(err.message || 'Failed to load problem statement');
    }
  };

  // Load problem details
  useEffect(() => {
    loadProblem();
  }, [slug]);

  // Load past submissions for this problem
  const loadSubmissions = async () => {
    try {
      const res = await submissionsApi.getProblemSubmissions(slug);
      if (res.success && res.data) {
        setProblemSubmissions(res.data.content || []);
      }
    } catch (err) {
      console.error('Failed to load problem submissions:', err);
    }
  };

  useEffect(() => {
    if (activeTab === 'submissions') {
      loadSubmissions();
    }
  }, [activeTab, slug]);

  // Handle language switch
  const handleLanguageChange = (newLang) => {
    setLanguage(newLang);
    setCode(STARTER_TEMPLATES[newLang]);
  };

  // Submit code & start polling
  const handleSubmit = async () => {
    if (!isAuthenticated) {
      onOpenAuth('login');
      return;
    }

    if (!code.trim()) return;

    setIsSubmitting(true);
    setActiveSubmission(null);
    setPollingError(null);

    try {
      const res = await submissionsApi.createSubmission({
        problemId: problem.id,
        language,
        sourceCode: code,
      });

      if (res.success && res.data) {
        const submissionId = res.data.id;
        setActiveSubmission(res.data);
        startPolling(submissionId);
      }
    } catch (err) {
      setPollingError(err.message || 'Submission failed');
      setIsSubmitting(false);
    }
  };

  const startPolling = (submissionId) => {
    if (pollIntervalRef.current) clearInterval(pollIntervalRef.current);

    pollIntervalRef.current = setInterval(async () => {
      try {
        const res = await submissionsApi.getSubmissionById(submissionId);
        if (res.success && res.data) {
          const sub = res.data;
          setActiveSubmission(sub);

          if (sub.status === 'COMPLETED' || sub.status === 'FAILED') {
            clearInterval(pollIntervalRef.current);
            setIsSubmitting(false);

            if (sub.verdict === 'ACCEPTED') {
              triggerConfetti();
              refreshProfile(); // Update solved stats in header
            }
          }
        }
      } catch (err) {
        clearInterval(pollIntervalRef.current);
        setIsSubmitting(false);
        setPollingError('Lost connection while polling submission status.');
      }
    }, 600);
  };

  useEffect(() => {
    return () => {
      if (pollIntervalRef.current) clearInterval(pollIntervalRef.current);
    };
  }, []);

  const triggerConfetti = () => {
    confetti({
      particleCount: 80,
      spread: 70,
      origin: { y: 0.6 },
    });
  };

  const getVerdictBadge = (verdict) => {
    switch (verdict) {
      case 'ACCEPTED':
        return (
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 font-bold text-sm">
            <CheckCircle2 className="w-4 h-4" /> Accepted
          </div>
        );
      case 'WRONG_ANSWER':
        return (
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400 font-bold text-sm">
            <XCircle className="w-4 h-4" /> Wrong Answer
          </div>
        );
      case 'TIME_LIMIT_EXCEEDED':
        return (
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-amber-500/10 border border-amber-500/30 text-amber-400 font-bold text-sm">
            <AlertTriangle className="w-4 h-4" /> Time Limit Exceeded
          </div>
        );
      case 'COMPILATION_ERROR':
        return (
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400 font-bold text-sm">
            <AlertTriangle className="w-4 h-4" /> Compilation Error
          </div>
        );
      case 'RUNTIME_ERROR':
        return (
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400 font-bold text-sm">
            <AlertTriangle className="w-4 h-4" /> Runtime Error
          </div>
        );
      default:
        return (
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-slate-800 text-slate-300 font-bold text-sm">
            {verdict || 'Evaluating'}
          </div>
        );
    }
  };

  if (loadingError) {
    return (
      <div className="h-[calc(100vh-4rem)] flex flex-col items-center justify-center text-slate-300 gap-4 p-4 text-center">
        <div className="p-3 rounded-full bg-rose-500/10 border border-rose-500/30 text-rose-400">
          <AlertTriangle className="w-8 h-8" />
        </div>
        <div>
          <h3 className="text-lg font-bold text-white">Unable to load problem</h3>
          <p className="text-xs text-slate-400 mt-1 max-w-md">{loadingError}</p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={onBack}
            className="px-4 py-2 rounded-lg bg-slate-900 border border-slate-800 text-xs font-semibold text-slate-300 hover:bg-slate-800 transition-colors"
          >
            Back to Catalog
          </button>
          <button
            onClick={loadProblem}
            className="px-4 py-2 rounded-lg bg-sky-500 text-white text-xs font-semibold hover:bg-sky-400 transition-colors shadow-md shadow-sky-500/20"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (!problem) {
    return (
      <div className="h-[calc(100vh-4rem)] flex items-center justify-center text-slate-400 gap-3">
        <Loader2 className="w-6 h-6 animate-spin text-sky-400" />
        <span>Loading problem workspace...</span>
      </div>
    );
  }

  return (
    <div className="h-[calc(100vh-4rem)] flex flex-col bg-slate-950 overflow-hidden">
      
      {/* Top Action Bar */}
      <div className="h-12 border-b border-slate-800/80 px-4 flex items-center justify-between bg-slate-950/60 shrink-0">
        <div className="flex items-center gap-4">
          <button
            onClick={onBack}
            className="flex items-center gap-1.5 text-xs font-semibold text-slate-400 hover:text-slate-200 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" /> Back to Catalog
          </button>
          <span className="text-slate-700">|</span>
          <div className="flex items-center gap-2">
            <span className="font-semibold text-sm text-slate-200">{problem.title}</span>
            <span className={`px-2 py-0.5 rounded text-[11px] font-bold ${
              problem.difficulty === 'EASY' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30' :
              problem.difficulty === 'MEDIUM' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' :
              'bg-rose-500/10 text-rose-400 border border-rose-500/30'
            }`}>
              {problem.difficulty}
            </span>
          </div>
        </div>

        {/* Right Action buttons */}
        <div className="flex items-center gap-3">
          {/* Language Selector */}
          <select
            value={language}
            onChange={(e) => handleLanguageChange(e.target.value)}
            className="bg-slate-900 border border-slate-800 text-slate-200 text-xs font-medium rounded-lg px-2.5 py-1.5 focus:outline-none focus:border-amber-400 cursor-pointer"
          >
            <option value="JAVA">Java 21 (OpenJDK)</option>
            <option value="CPP">C++ 17 (g++)</option>
          </select>

          {/* Reset Code */}
          <button
            onClick={() => setCode(STARTER_TEMPLATES[language])}
            className="p-1.5 text-slate-400 hover:text-slate-200 hover:bg-slate-900 rounded-lg transition-colors"
            title="Reset code template"
          >
            <RotateCcw className="w-4 h-4" />
          </button>

          {/* Submit Button */}
          <button
            onClick={handleSubmit}
            disabled={isSubmitting}
            className="px-4 py-1.5 rounded-lg bg-gradient-to-r from-amber-500 via-yellow-500 to-amber-600 hover:from-amber-400 hover:to-yellow-400 text-slate-950 font-bold text-xs shadow-md shadow-amber-500/20 flex items-center gap-1.5 transition-all disabled:opacity-50"
          >
            {isSubmitting ? (
              <>
                <Loader2 className="w-3.5 h-3.5 animate-spin" /> Evaluating...
              </>
            ) : (
              <>
                <Play className="w-3.5 h-3.5 fill-current" /> Submit Solution
              </>
            )}
          </button>
        </div>
      </div>

      {/* Split Workspace Area */}
      <div className="flex-1 flex flex-col md:flex-row overflow-hidden">
        
        {/* Left Panel: Statement / Submissions */}
        <div className="w-full md:w-1/2 border-r border-slate-800/80 flex flex-col bg-slate-950 overflow-hidden">
          
          {/* Tab Selector */}
          <div className="flex border-b border-slate-800/80 px-4 bg-slate-950/40 shrink-0">
            <button
              onClick={() => setActiveTab('statement')}
              className={`py-2.5 px-3 text-xs font-semibold flex items-center gap-1.5 transition-colors border-b-2 ${
                activeTab === 'statement'
                  ? 'border-amber-400 text-amber-400'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              <FileCode2 className="w-3.5 h-3.5" /> Problem Statement
            </button>
            <button
              onClick={() => setActiveTab('submissions')}
              className={`py-2.5 px-3 text-xs font-semibold flex items-center gap-1.5 transition-colors border-b-2 ${
                activeTab === 'submissions'
                  ? 'border-amber-400 text-amber-400'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              <History className="w-3.5 h-3.5" /> Problem Submissions
            </button>
          </div>

          {/* Tab Body */}
          <div className="flex-1 overflow-y-auto p-6 space-y-6">
            {activeTab === 'statement' ? (
              <>
                {/* Limits Banner */}
                <div className="flex items-center gap-4 text-xs font-mono text-slate-400 p-3 rounded-xl bg-slate-900/60 border border-slate-800/60">
                  <span className="flex items-center gap-1.5">
                    <Clock className="w-3.5 h-3.5 text-amber-400" /> Time Limit: {problem.timeLimitMs}ms
                  </span>
                  <span className="text-slate-700">|</span>
                  <span className="flex items-center gap-1.5">
                    <Cpu className="w-3.5 h-3.5 text-indigo-400" /> Memory Limit: {problem.memoryLimitMb}MB
                  </span>
                </div>

                {/* Description */}
                <div className="prose prose-invert max-w-none text-slate-300 text-sm leading-relaxed whitespace-pre-wrap font-sans">
                  {problem.description}
                </div>

                {/* Sample Test Cases */}
                {problem.sampleTestCases && problem.sampleTestCases.length > 0 && (
                  <div className="space-y-4 pt-2">
                    <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Sample Test Cases</h3>
                    {problem.sampleTestCases.map((tc, idx) => (
                      <div key={tc.id || idx} className="rounded-xl bg-slate-900/80 border border-slate-800 p-4 space-y-3">
                        <div className="text-xs font-semibold text-slate-400">Example {idx + 1}</div>
                        <div>
                          <div className="text-[11px] font-semibold text-slate-500 uppercase">Input:</div>
                          <pre className="mt-1 p-2.5 rounded-lg bg-slate-950 font-mono text-xs text-sky-300 border border-slate-800/80 overflow-x-auto">
                            {tc.inputData}
                          </pre>
                        </div>
                        <div>
                          <div className="text-[11px] font-semibold text-slate-500 uppercase">Expected Output:</div>
                          <pre className="mt-1 p-2.5 rounded-lg bg-slate-950 font-mono text-xs text-emerald-300 border border-slate-800/80 overflow-x-auto">
                            {tc.expectedOutput}
                          </pre>
                        </div>
                        {tc.explanation && (
                          <div className="text-xs text-slate-400 italic">
                            <span className="font-semibold text-slate-300">Explanation:</span> {tc.explanation}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </>
            ) : (
              <div className="space-y-3">
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">Recent Submissions for {problem.title}</h3>
                {problemSubmissions.length === 0 ? (
                  <div className="py-12 text-center text-slate-500 text-sm">No submissions recorded yet for this problem.</div>
                ) : (
                  <div className="divide-y divide-slate-800/80 border border-slate-800/80 rounded-xl overflow-hidden bg-slate-900/40">
                    {problemSubmissions.map((sub) => (
                      <div key={sub.id} className="p-3.5 flex items-center justify-between text-xs hover:bg-slate-900/80 transition-colors">
                        <div>
                          <div className="font-medium text-slate-200">{sub.username}</div>
                          <div className="text-[11px] text-slate-500 mt-0.5">{sub.language} • {new Date(sub.submittedAt).toLocaleTimeString()}</div>
                        </div>
                        <div className="flex items-center gap-3">
                          {sub.executionTimeMs != null && (
                            <span className="font-mono text-slate-400">{sub.executionTimeMs}ms</span>
                          )}
                          <span className={`px-2 py-0.5 rounded font-bold text-[11px] ${
                            sub.verdict === 'ACCEPTED' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'
                          }`}>
                            {sub.verdict || sub.status}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Right Panel: Monaco Editor + Live Verdict Drawer */}
        <div className="w-full md:w-1/2 flex flex-col bg-[#161613] overflow-hidden">
          
          {/* Monaco Editor Container */}
          <div className="flex-1 overflow-hidden relative">
            <Editor
              height="100%"
              language={language.toLowerCase() === 'cpp' ? 'cpp' : 'java'}
              theme="warm-honey-dark"
              beforeMount={(monaco) => {
                monaco.editor.defineTheme('warm-honey-dark', {
                  base: 'vs-dark',
                  inherit: true,
                  rules: [
                    { token: 'comment', foreground: '737367', fontStyle: 'italic' },
                    { token: 'keyword', foreground: 'eab308', fontStyle: 'bold' },
                    { token: 'string', foreground: '34d399' },
                    { token: 'number', foreground: 'f59e0b' },
                    { token: 'type', foreground: 'facc15' },
                  ],
                  colors: {
                    'editor.background': '#161613',
                    'editor.foreground': '#f5f5f0',
                    'editorCursor.foreground': '#facc15',
                    'editor.lineHighlightBackground': '#22221d',
                    'editorLineNumber.foreground': '#52524a',
                    'editorLineNumber.activeForeground': '#facc15',
                    'editor.selectionBackground': '#3a341c',
                  },
                });
              }}
              value={code}
              onChange={(value) => setCode(value || '')}
              options={{
                fontSize: 14,
                fontFamily: "'Fira Code', monospace",
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                automaticLayout: true,
                padding: { top: 12 },
                tabSize: 4,
              }}
            />
          </div>

          {/* Bottom Execution Verdict Drawer */}
          {(activeSubmission || isSubmitting || pollingError) && (
            <div className="h-44 border-t border-slate-800 bg-slate-950 p-4 flex flex-col justify-between shrink-0 shadow-2xl animate-fade-in">
              <div className="flex items-center justify-between border-b border-slate-800/80 pb-2">
                <div className="flex items-center gap-2">
                  <Terminal className="w-4 h-4 text-amber-400" />
                  <span className="text-xs font-bold text-slate-200 uppercase tracking-wider">Evaluation Console</span>
                </div>
                {activeSubmission && (
                  <div className="text-[11px] font-mono text-slate-500">
                    Submission #{activeSubmission.id}
                  </div>
                )}
              </div>

              {/* Status Display */}
              <div className="my-auto">
                {isSubmitting && activeSubmission?.status !== 'COMPLETED' ? (
                  <div className="flex items-center gap-3 text-amber-400">
                    <Loader2 className="w-5 h-5 animate-spin" />
                    <div>
                      <div className="text-sm font-bold">
                        {activeSubmission?.status === 'RUNNING' ? 'Running test cases in Docker sandbox...' : 'Queued in Redis pipeline...'}
                      </div>
                      <div className="text-xs text-slate-400">Evaluating execution time and memory limits</div>
                    </div>
                  </div>
                ) : activeSubmission?.verdict ? (
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      {getVerdictBadge(activeSubmission.verdict)}
                      
                      {activeSubmission.verdict === 'ACCEPTED' && (
                        <div className="flex items-center gap-4 text-xs font-mono text-slate-400">
                          <span>⏱️ Time: <strong className="text-slate-200">{activeSubmission.executionTimeMs}ms</strong></span>
                          <span>💾 Memory: <strong className="text-slate-200">{activeSubmission.memoryUsedKb}KB</strong></span>
                        </div>
                      )}
                    </div>

                    {/* Diagnostics / Error Output */}
                    {activeSubmission.errorOutput && (
                      <pre className="p-2.5 rounded-lg bg-slate-900 border border-slate-800 font-mono text-xs text-rose-300 max-h-16 overflow-y-auto whitespace-pre-wrap">
                        {activeSubmission.errorOutput}
                      </pre>
                    )}
                  </div>
                ) : pollingError ? (
                  <div className="text-xs text-rose-400 flex items-center gap-2">
                    <AlertTriangle className="w-4 h-4" /> {pollingError}
                  </div>
                ) : null}
              </div>
            </div>
          )}

        </div>

      </div>
    </div>
  );
}
