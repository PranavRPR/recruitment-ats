import React, { useEffect, useState } from "react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const menus = {
  ADMIN: [["Dashboard", "/admin/dashboard", "⌂"], ["Users", "/admin/users", "◎"], ["Jobs", "/admin/jobs", "▣"], ["Applications", "/admin/applications", "✓"], ["Interviews", "/admin/interviews", "◷"], ["Reports", "/admin/reports", "▤"], ["Notifications", "/admin/notifications", "◉"]],
  RECRUITER: [["Dashboard", "/recruiter/dashboard", "⌂"], ["Jobs", "/recruiter/jobs", "▣"], ["Applications", "/recruiter/applications", "✓"], ["Candidates", "/recruiter/candidates", "◎"], ["Interviews", "/recruiter/interviews", "◷"], ["Notifications", "/recruiter/notifications", "◉"]],
  HIRING_MANAGER: [["Dashboard", "/manager/dashboard", "⌂"], ["Jobs", "/manager/jobs", "▣"], ["Applications", "/manager/applications", "✓"], ["Candidates", "/manager/candidates", "◎"], ["Interviews", "/manager/interviews", "◷"], ["Notifications", "/manager/notifications", "◉"]],
  CANDIDATE: [["Dashboard", "/candidate/dashboard", "⌂"], ["Jobs", "/candidate/jobs", "▣"], ["Applications", "/candidate/applications", "✓"], ["Resume", "/candidate/resume", "▤"], ["Interviews", "/candidate/interviews", "◷"], ["Profile", "/candidate/profile", "●"], ["Notifications", "/candidate/notifications", "◉"]]
};

export default function Layout() {
  const { currentUser, logout } = useAuth();
  const user = currentUser || {};
  const menu = menus[user.role] || [];
  const [toast, setToast] = useState(null);

  useEffect(() => {
    const handleApiError = (event) => setToast({ message: event.detail, type: "error" });
    const message = sessionStorage.getItem("toastMessage");
    if (message) {
      setToast({ message, type: "success" });
      sessionStorage.removeItem("toastMessage");
    }
    window.addEventListener("api-error", handleApiError);
    return () => window.removeEventListener("api-error", handleApiError);
  }, []);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">AI</div>
          <div><strong>RecruitAI</strong><small>ATS Platform</small></div>
        </div>

        <div className="role-badge">{user.role?.replace("_", " ")}</div>

        <nav>
          {menu.map(([label, path, icon]) => (
            <NavLink key={path} to={path} className={({isActive}) => isActive ? "nav-item active" : "nav-item"}>
              <span>{icon}</span>{label}
            </NavLink>
          ))}
        </nav>

        <button className="logout" onClick={logout}>↪ Sign out</button>
      </aside>

      <main className="main">
        <header className="topbar">
          <div className="mobile-brand">RecruitAI</div>
          <div className="top-actions">
            <span className="user-name">{user.name}</span>
            <div className="avatar">{(user.name || "U")[0]}</div>
          </div>
        </header>
        <section className="content"><Outlet /></section>
      </main>
      {toast && <div className={`toast ${toast.type}`} role="status"><span>!</span>{toast.message}<button className="icon-btn" onClick={() => setToast(null)} aria-label="Dismiss notification">×</button></div>}
    </div>
  );
}