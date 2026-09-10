import React, { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import api, { getApiErrorMessage } from "../api";
import Toast from "../components/Toast";
import { useAuth } from "../context/AuthContext";

const dashboardFor = {
  ADMIN: "/admin/dashboard",
  RECRUITER: "/recruiter/dashboard",
  HIRING_MANAGER: "/manager/dashboard",
  CANDIDATE: "/candidate/dashboard"
};

export default function Login() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated, role } = useAuth();

  useEffect(() => {
    const message = sessionStorage.getItem("authMessage");
    if (message) {
      setToast({ type: "error", message });
      sessionStorage.removeItem("authMessage");
    } else if (location.state?.message) {
      setToast({ type: "success", message: location.state.message });
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location.pathname, location.state, navigate]);

  useEffect(() => {
    if (isAuthenticated && role) navigate(dashboardFor[role] || "/dashboard", { replace: true });
  }, [isAuthenticated, navigate, role]);

  async function submit(e) {
    e.preventDefault();
    if (!form.email.trim() || !form.password) {
      setToast({ type: "error", message: "Enter your email and password." });
      return;
    }
    setLoading(true);
    try {
      const res = await api.post("/auth/login", form);
      const data = res.data.data || res.data;
      if (!data.token || !data.role) throw new Error("The login response is missing authentication details.");
      sessionStorage.setItem("toastMessage", "Login successful");
      login(data);
      navigate(dashboardFor[data.role] || "/dashboard", { replace: true });
    } catch (err) {
      setToast({ type: "error", message: err.response?.status === 401 ? "Invalid email or password" : (err.message?.includes("missing authentication") ? err.message : getApiErrorMessage(err, "Invalid email or password")) });
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-visual">
        <div className="hero-copy">
          <div className="brand-mark large">AI</div>
          <h1>Hire smarter.<br/>Move faster.</h1>
          <p>AI-assisted recruitment that keeps recruiters in control.</p>
          <div className="mini-stats"><span>Resume AI</span><span>ATS Pipeline</span><span>Analytics</span></div>
        </div>
      </div>
      <div className="auth-card">
        <h2>Welcome back</h2>
        <p className="muted">Sign in to your recruitment workspace</p>
        <form onSubmit={submit}>
          <label>Email<input type="email" required value={form.email} onChange={e => setForm({...form,email:e.target.value})} placeholder="you@company.com"/></label>
          <label>Password<input type="password" required value={form.password} onChange={e => setForm({...form,password:e.target.value})} placeholder="••••••••"/></label>
          <button className="primary full" disabled={loading}>{loading ? "Signing in..." : "Sign in"}</button>
        </form>
        <p className="auth-link">Don't have an account? <Link to="/register">Create account</Link></p>
        <div className="demo-box">Demo admin: <b>admin@ats.com</b> / <b>Admin@12345</b></div>
      </div>
      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </div>
  );
}