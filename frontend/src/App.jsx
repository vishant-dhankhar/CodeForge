import React, { useState } from 'react';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { ProblemList } from './components/ProblemList';
import { ProblemWorkspace } from './components/ProblemWorkspace';
import { UserProfile } from './components/UserProfile';
import { AuthModal } from './components/AuthModal';

export function App() {
  const [currentView, setCurrentView] = useState('problems'); // 'problems' | 'problem-detail' | 'profile'
  const [selectedSlug, setSelectedSlug] = useState(null);
  const [authModal, setAuthModal] = useState({ isOpen: false, mode: 'login' });

  const handleOpenAuth = (mode = 'login') => {
    setAuthModal({ isOpen: true, mode });
  };

  const handleCloseAuth = () => {
    setAuthModal({ isOpen: false, mode: 'login' });
  };

  const handleSelectProblem = (slug) => {
    setSelectedSlug(slug);
    setCurrentView('problem-detail');
  };

  const handleNavigate = (view) => {
    setCurrentView(view);
  };

  return (
    <AuthProvider>
      <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-sky-500/30 selection:text-sky-200">
        
        {/* Navigation Bar */}
        <Navbar
          onOpenAuth={handleOpenAuth}
          onNavigate={handleNavigate}
          currentView={currentView}
        />

        {/* Dynamic Views */}
        <main className="flex-1">
          {currentView === 'problems' && (
            <ProblemList onSelectProblem={handleSelectProblem} />
          )}

          {currentView === 'problem-detail' && selectedSlug && (
            <ProblemWorkspace
              slug={selectedSlug}
              onBack={() => setCurrentView('problems')}
              onOpenAuth={handleOpenAuth}
            />
          )}

          {currentView === 'profile' && (
            <UserProfile
              onNavigate={handleNavigate}
              onSelectProblem={handleSelectProblem}
            />
          )}
        </main>

        {/* Authentication Modal */}
        <AuthModal
          isOpen={authModal.isOpen}
          initialMode={authModal.mode}
          onClose={handleCloseAuth}
        />

      </div>
    </AuthProvider>
  );
}

export default App;
