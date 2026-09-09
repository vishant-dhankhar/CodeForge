import React, { useState, useEffect, useCallback } from 'react';
import { problemsApi } from '../services/api';
import { Search, ChevronRight, Tag, Clock, Cpu, Loader2, Sparkles, CheckCircle2 } from 'lucide-react';

export function ProblemList({ onSelectProblem }) {
  const [problems, setProblems] = useState([]);
  const [difficulty, setDifficulty] = useState('');
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchProblems = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await problemsApi.getProblems({
        difficulty: difficulty || undefined,
        search: search.trim() || undefined,
        page: 0,
        size: 50,
      });
      if (res.success && res.data) {
        setProblems(res.data.content || []);
      }
    } catch (err) {
      setError(err.message || 'Failed to load problems');
    } finally {
      setIsLoading(false);
    }
  }, [difficulty, search]);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchProblems();
    }, 200);
    return () => clearTimeout(timer);
  }, [fetchProblems]);

  const getDifficultyBadge = (diff) => {
    switch (diff) {
      case 'EASY':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">Easy</span>;
      case 'MEDIUM':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/30">Medium</span>;
      case 'HARD':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/30">Hard</span>;
      default:
        return null;
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      
      {/* Hero Banner */}
      <div className="rounded-2xl bg-gradient-to-r from-slate-900 via-slate-900/90 to-amber-950/20 border border-slate-800 p-6 md:p-8 relative overflow-hidden shadow-2xl">
        <div className="absolute right-0 top-0 bottom-0 w-1/3 bg-gradient-to-l from-amber-500/10 to-transparent pointer-events-none"></div>
        <div className="max-w-2xl relative z-10">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-400 text-xs font-medium mb-3">
            <Sparkles className="w-3.5 h-3.5" /> High-Performance Online Judge Engine
          </div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            Sharpen your algorithmic skills with <span className="text-amber-400">CodeForge</span>
          </h1>
          <p className="text-slate-400 text-sm mt-2 leading-relaxed">
            Write code in <strong>Java 21</strong> or <strong>C++ 17</strong>, submit to our sandboxed execution engine, and receive instant verdict evaluation with millisecond precision.
          </p>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 p-3 rounded-xl bg-slate-900/60 border border-slate-800/80">
        
        {/* Search */}
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search problems by title..."
            className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:border-amber-400"
          />
        </div>

        {/* Difficulty Filter Chips */}
        <div className="flex items-center gap-1.5 w-full sm:w-auto overflow-x-auto pb-1 sm:pb-0">
          {['', 'EASY', 'MEDIUM', 'HARD'].map((diff) => (
            <button
              key={diff}
              onClick={() => setDifficulty(diff)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                difficulty === diff
                  ? 'bg-gradient-to-r from-amber-500 to-yellow-500 text-slate-950 shadow-md shadow-amber-500/20 font-bold'
                  : 'bg-slate-950 text-slate-400 hover:text-slate-200 border border-slate-800/80 hover:border-slate-700'
              }`}
            >
              {diff === '' ? 'All Difficulties' : diff.charAt(0) + diff.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      {/* Problem Table */}
      <div className="rounded-2xl bg-slate-900/40 border border-slate-800/80 overflow-hidden shadow-xl">
        {isLoading ? (
          <div className="py-20 flex flex-col items-center justify-center gap-3 text-slate-400">
            <Loader2 className="w-8 h-8 animate-spin text-amber-400" />
            <span className="text-sm font-medium">Fetching problem catalog...</span>
          </div>
        ) : error ? (
          <div className="py-16 text-center text-rose-400 text-sm">
            <p>{error}</p>
            <button
              onClick={fetchProblems}
              className="mt-3 px-4 py-1.5 rounded-lg bg-slate-800 text-slate-200 text-xs hover:bg-slate-700"
            >
              Retry
            </button>
          </div>
        ) : problems.length === 0 ? (
          <div className="py-20 text-center text-slate-500 text-sm">
            No problems found matching your search or difficulty criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-950/60 border-b border-slate-800 text-xs uppercase text-slate-400 font-semibold tracking-wider">
                <tr>
                  <th className="py-3.5 px-4 w-12 text-center">#</th>
                  <th className="py-3.5 px-4">Title</th>
                  <th className="py-3.5 px-4">Tags</th>
                  <th className="py-3.5 px-4">Difficulty</th>
                  <th className="py-3.5 px-4 hidden md:table-cell">Constraints</th>
                  <th className="py-3.5 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {problems.map((problem) => (
                  <tr
                    key={problem.id}
                    onClick={() => onSelectProblem(problem.slug)}
                    className="hover:bg-slate-800/40 cursor-pointer transition-colors group"
                  >
                    <td className="py-4 px-4 text-center font-mono text-xs text-slate-500">
                      {problem.id}
                    </td>
                    <td className="py-4 px-4 font-medium text-slate-200 group-hover:text-amber-400 transition-colors">
                      {problem.title}
                    </td>
                    <td className="py-4 px-4">
                      <div className="flex flex-wrap gap-1.5">
                        {problem.tags?.map((tag) => (
                          <span
                            key={tag}
                            className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-slate-950 border border-slate-800 text-[11px] text-slate-400 font-medium"
                          >
                            <Tag className="w-2.5 h-2.5 text-slate-500" />
                            {tag}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="py-4 px-4">
                      {getDifficultyBadge(problem.difficulty)}
                    </td>
                    <td className="py-4 px-4 hidden md:table-cell text-xs text-slate-400 font-mono">
                      <div className="flex items-center gap-3">
                        <span className="flex items-center gap-1" title="Time Limit">
                          <Clock className="w-3.5 h-3.5 text-slate-500" /> {problem.timeLimitMs}ms
                        </span>
                        <span className="flex items-center gap-1" title="Memory Limit">
                          <Cpu className="w-3.5 h-3.5 text-slate-500" /> {problem.memoryLimitMb}MB
                        </span>
                      </div>
                    </td>
                    <td className="py-4 px-4 text-right">
                      <span className="inline-flex items-center gap-1 text-xs font-semibold text-amber-400 group-hover:translate-x-0.5 transition-transform">
                        Solve <ChevronRight className="w-3.5 h-3.5" />
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

    </div>
  );
}
