import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Code2, User as UserIcon, LogOut, Terminal, CheckCircle2 } from 'lucide-react';

export function Navbar({ onOpenAuth, onNavigate, currentView }) {
  const { isAuthenticated, currentUser, profile, logout } = useAuth();

  return (
    <header className="sticky top-0 z-40 w-full border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        {/* Brand Logo */}
        <div 
          onClick={() => onNavigate('problems')}
          className="flex items-center gap-3 cursor-pointer group"
        >
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-amber-500 via-yellow-400 to-amber-600 p-[1px] shadow-lg shadow-amber-500/20 group-hover:shadow-amber-500/40 transition-all">
            <div className="w-full h-full bg-slate-950 rounded-[11px] flex items-center justify-center">
              <Terminal className="w-5 h-5 text-amber-400 group-hover:scale-110 transition-transform" />
            </div>
          </div>
          <div>
            <div className="text-lg font-bold tracking-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
              Code<span className="text-amber-400 font-extrabold">Forge</span>
            </div>
            <div className="text-[10px] text-slate-500 uppercase tracking-wider font-semibold -mt-1">Online Judge</div>
          </div>
        </div>

        {/* Center Nav Links */}
        <nav className="flex items-center gap-2">
          <button
            onClick={() => onNavigate('problems')}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${
              currentView === 'problems' || currentView === 'problem-detail'
                ? 'bg-slate-800/80 text-amber-400 border border-slate-700/60'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900/60'
            }`}
          >
            <Code2 className="w-4 h-4" />
            Problems
          </button>
        </nav>

        {/* Right Section / Auth */}
        <div className="flex items-center gap-3">
          {isAuthenticated ? (
            <div className="flex items-center gap-3">
              
              {/* Solved Stats Quick Badge */}
              {profile?.stats && (
                <div 
                  onClick={() => onNavigate('profile')}
                  className="hidden md:flex items-center gap-2.5 px-3 py-1.5 rounded-full bg-slate-900/90 border border-slate-800 text-xs font-mono cursor-pointer hover:border-slate-700 transition-colors"
                  title="View your statistics"
                >
                  <span className="text-emerald-400 flex items-center gap-1 font-semibold">
                    <span className="w-2 h-2 rounded-full bg-emerald-400 inline-block"></span>
                    {profile.stats.easySolved}E
                  </span>
                  <span className="text-amber-400 flex items-center gap-1 font-semibold">
                    <span className="w-2 h-2 rounded-full bg-amber-400 inline-block"></span>
                    {profile.stats.mediumSolved}M
                  </span>
                  <span className="text-rose-400 flex items-center gap-1 font-semibold">
                    <span className="w-2 h-2 rounded-full bg-rose-400 inline-block"></span>
                    {profile.stats.hardSolved}H
                  </span>
                </div>
              )}

              {/* User Button */}
              <button
                onClick={() => onNavigate('profile')}
                className={`flex items-center gap-2 px-3 py-1.5 rounded-lg border text-sm font-medium transition-colors ${
                  currentView === 'profile'
                    ? 'bg-amber-500/10 border-amber-500/40 text-amber-400'
                    : 'bg-slate-900/80 border-slate-800 text-slate-300 hover:bg-slate-800'
                }`}
              >
                <UserIcon className="w-4 h-4 text-amber-400" />
                <span>{currentUser?.username}</span>
              </button>

              {/* Logout Button */}
              <button
                onClick={logout}
                className="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-900 border border-transparent hover:border-slate-800 transition-colors"
                title="Log out"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <button
                onClick={() => onOpenAuth('login')}
                className="px-3.5 py-1.5 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-900/80 transition-colors"
              >
                Sign In
              </button>
              <button
                onClick={() => onOpenAuth('register')}
                className="px-4 py-1.5 rounded-lg text-sm font-semibold bg-gradient-to-r from-amber-500 via-yellow-500 to-amber-600 text-slate-950 hover:from-amber-400 hover:to-yellow-400 shadow-md shadow-amber-500/20 transition-all"
              >
                Get Started
              </button>
            </div>
          )}
        </div>

      </div>
    </header>
  );
}
