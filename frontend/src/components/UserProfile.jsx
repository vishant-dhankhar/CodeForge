import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { submissionsApi } from '../services/api';
import { User, CheckCircle2, Award, Activity, Calendar, History, ArrowLeft, Loader2 } from 'lucide-react';

export function UserProfile({ onNavigate, onSelectProblem }) {
  const { currentUser, profile, refreshProfile } = useAuth();
  const [submissions, setSubmissions] = useState([]);
  const [isLoadingSubmissions, setIsLoadingSubmissions] = useState(true);

  useEffect(() => {
    refreshProfile();
    async function loadUserSubmissions() {
      try {
        const res = await submissionsApi.getMySubmissions({ page: 0, size: 20 });
        if (res.success && res.data) {
          setSubmissions(res.data.content || []);
        }
      } catch (err) {
        console.error('Failed to load user submissions:', err);
      } finally {
        setIsLoadingSubmissions(false);
      }
    }
    loadUserSubmissions();
  }, [refreshProfile]);

  const stats = profile?.stats || {
    easySolved: 0,
    mediumSolved: 0,
    hardSolved: 0,
    totalSolved: 0,
    totalSubmissions: 0,
    acceptedSubmissions: 0,
    acceptanceRate: 0,
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 animate-fade-in">
      
      {/* Back to Catalog */}
      <button
        onClick={() => onNavigate('problems')}
        className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-400 hover:text-slate-200 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" /> Back to Problem Catalog
      </button>

      {/* User Header Profile Card */}
      <div className="rounded-2xl bg-gradient-to-r from-slate-900 via-slate-900/90 to-amber-950/20 border border-slate-800 p-6 md:p-8 flex flex-col md:flex-row items-center justify-between gap-6 shadow-2xl">
        <div className="flex items-center gap-5">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-amber-500 to-yellow-600 p-[2px] shadow-lg shadow-amber-500/20">
            <div className="w-full h-full bg-slate-950 rounded-[14px] flex items-center justify-center">
              <User className="w-8 h-8 text-amber-400" />
            </div>
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white tracking-tight">{profile?.username || currentUser?.username}</h1>
            <p className="text-slate-400 text-xs mt-0.5">{profile?.email || currentUser?.email}</p>
            <div className="flex items-center gap-3 mt-2 text-xs text-slate-500 font-mono">
              <span className="flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5" /> Joined {profile?.joinedAt ? new Date(profile.joinedAt).toLocaleDateString() : 'Recently'}
              </span>
              <span>•</span>
              <span className="px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 font-semibold border border-amber-500/20">
                {profile?.role || 'USER'}
              </span>
            </div>
          </div>
        </div>

        {/* Global Solved Overview */}
        <div className="flex items-center gap-4 bg-slate-950/80 p-4 rounded-xl border border-slate-800">
          <div className="text-center px-4">
            <div className="text-2xl font-extrabold text-white font-mono">{stats.totalSolved}</div>
            <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider mt-0.5">Problems Solved</div>
          </div>
          <div className="h-10 w-[1px] bg-slate-800"></div>
          <div className="text-center px-4">
            <div className="text-2xl font-extrabold text-amber-400 font-mono">{stats.acceptanceRate}%</div>
            <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider mt-0.5">Acceptance Rate</div>
          </div>
        </div>
      </div>

      {/* Difficulty Breakdown Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        
        {/* Easy */}
        <div className="rounded-2xl bg-slate-900/60 border border-slate-800/80 p-5 shadow-lg relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-emerald-400">Easy Solved</span>
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 shadow-sm shadow-emerald-400/50"></span>
          </div>
          <div className="text-3xl font-extrabold text-white font-mono mt-3">{stats.easySolved}</div>
          <div className="text-xs text-slate-500 mt-1">Fundamental algorithm exercises</div>
        </div>

        {/* Medium */}
        <div className="rounded-2xl bg-slate-900/60 border border-slate-800/80 p-5 shadow-lg relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-amber-400">Medium Solved</span>
            <span className="w-2.5 h-2.5 rounded-full bg-amber-400 shadow-sm shadow-amber-400/50"></span>
          </div>
          <div className="text-3xl font-extrabold text-white font-mono mt-3">{stats.mediumSolved}</div>
          <div className="text-xs text-slate-500 mt-1">Core technical interview challenges</div>
        </div>

        {/* Hard */}
        <div className="rounded-2xl bg-slate-900/60 border border-slate-800/80 p-5 shadow-lg relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-rose-400">Hard Solved</span>
            <span className="w-2.5 h-2.5 rounded-full bg-rose-400 shadow-sm shadow-rose-400/50"></span>
          </div>
          <div className="text-3xl font-extrabold text-white font-mono mt-3">{stats.hardSolved}</div>
          <div className="text-xs text-slate-500 mt-1">Advanced systems & data structures</div>
        </div>
      </div>

      {/* Submission History Table */}
      <div className="rounded-2xl bg-slate-900/40 border border-slate-800/80 overflow-hidden shadow-xl space-y-4 p-6">
        <div className="flex items-center gap-2">
          <History className="w-4 h-4 text-amber-400" />
          <h2 className="text-sm font-bold uppercase tracking-wider text-slate-200">Recent Submission History</h2>
        </div>

        {isLoadingSubmissions ? (
          <div className="py-12 flex justify-center text-slate-400">
            <Loader2 className="w-6 h-6 animate-spin text-amber-400" />
          </div>
        ) : submissions.length === 0 ? (
          <div className="py-12 text-center text-slate-500 text-sm">
            No submissions recorded yet. Head over to the problem catalog and write your first solution!
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-800 text-xs uppercase text-slate-500 font-semibold tracking-wider">
                <tr>
                  <th className="py-3 px-4">Problem</th>
                  <th className="py-3 px-4">Language</th>
                  <th className="py-3 px-4">Verdict</th>
                  <th className="py-3 px-4">Execution Time</th>
                  <th className="py-3 px-4">Memory</th>
                  <th className="py-3 px-4 text-right">Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono text-xs">
                {submissions.map((sub) => (
                  <tr key={sub.id} className="hover:bg-slate-800/40 transition-colors">
                    <td 
                      onClick={() => onSelectProblem(sub.problemSlug)}
                      className="py-3.5 px-4 font-sans font-medium text-slate-200 hover:text-amber-400 cursor-pointer transition-colors"
                    >
                      {sub.problemTitle}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {sub.language}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className={`px-2.5 py-0.5 rounded font-bold text-[11px] ${
                        sub.verdict === 'ACCEPTED' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30' :
                        'bg-rose-500/10 text-rose-400 border border-rose-500/30'
                      }`}>
                        {sub.verdict || sub.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-300">
                      {sub.executionTimeMs != null ? `${sub.executionTimeMs}ms` : '—'}
                    </td>
                    <td className="py-3.5 px-4 text-slate-300">
                      {sub.memoryUsedKb != null ? `${sub.memoryUsedKb}KB` : '—'}
                    </td>
                    <td className="py-3.5 px-4 text-right text-slate-500 font-sans text-xs">
                      {new Date(sub.submittedAt).toLocaleDateString()} {new Date(sub.submittedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
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
