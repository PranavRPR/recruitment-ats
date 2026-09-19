import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api, { getApiErrorMessage } from "../api";
import Toast from "../components/Toast";

export default function Register() {
  const [form, setForm] = useState({name:"",email:"",password:"",role:"CANDIDATE"});
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function submit(e) {
    e.preventDefault();
    if (!form.name.trim() || !form.email.trim() || form.password.length < 8) {
      setToast({type:"error",message:"Enter a name, valid email, and password of at least 8 characters."});
      return;
    }
    setLoading(true);
    try {
      await api.post("/auth/register", form);
      navigate("/login", { replace: true, state: { message: "Account created. Sign in to continue." } });
    } catch (err) {
      setToast({type:"error",message:getApiErrorMessage(err, "Registration failed")});
    } finally {
      setLoading(false);
    }
  }

  const set = (k,v) => setForm({...form,[k]:v});

  return (
    <div className="register-page">
      <style>{`
        .register-page {
          min-height: 100vh;
          display: grid;
          grid-template-columns: minmax(430px, 45%) 1fr;
          background: #f8fafc;
          color: #0f172a;
          font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
        }
        .register-panel {
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 40px 32px;
          background: #f8fafc;
        }
        .register-card {
          width: min(500px, 100%);
          padding: 40px;
          border: 1px solid #e5e7eb;
          border-radius: 19px;
          background: rgba(255, 255, 255, 0.98);
          box-shadow: 0 20px 55px rgba(15, 23, 42, 0.10), 0 3px 10px rgba(15, 23, 42, 0.04);
        }
        .register-brand {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 31px;
        }
        .register-logo {
          display: grid;
          width: 42px;
          height: 42px;
          place-items: center;
          border-radius: 11px;
          background: #172554;
          color: #fff;
          font-size: 14px;
          font-weight: 800;
          letter-spacing: .4px;
          box-shadow: 0 7px 15px rgba(30, 64, 175, 0.20);
        }
        .register-brand strong { display: block; color: #111827; font-size: 18px; letter-spacing: -.3px; }
        .register-brand small { display: block; margin-top: 3px; color: #64748b; font-size: 11px; letter-spacing: .2px; }
        .register-card h1 { margin: 0; color: #0f172a; font-size: 32px; line-height: 1.15; letter-spacing: -.8px; }
        .register-subtitle { margin: 10px 0 29px; color: #64748b; font-size: 15px; line-height: 1.5; }
        .register-form { display: grid; gap: 17px; }
        .register-field { display: grid; gap: 7px; color: #334155; font-size: 13px; font-weight: 600; }
        .register-field input, .register-field select {
          width: 100%;
          height: 48px;
          padding: 0 14px;
          border: 1px solid #d1d5db;
          border-radius: 9px;
          outline: none;
          background: #fff;
          color: #172033;
          font-size: 14px;
          transition: border-color 180ms ease, box-shadow 180ms ease;
        }
        .register-field input::placeholder { color: #9aa4b2; }
        .register-field input:hover, .register-field select:hover { border-color: #94a3b8; }
        .register-field input:focus, .register-field select:focus {
          border-color: #4f46e5;
          box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
        }
        .register-submit {
          width: 100%;
          height: 50px;
          margin-top: 4px;
          border: 0;
          border-radius: 9px;
          background: #172554;
          color: #fff;
          font-size: 15px;
          font-weight: 650;
          transition: background 180ms ease, transform 180ms ease, box-shadow 180ms ease;
        }
        .register-submit:hover:not(:disabled) { background: #1e3a8a; box-shadow: 0 8px 18px rgba(30, 64, 175, 0.20); transform: translateY(-1px); }
        .register-submit:disabled { cursor: not-allowed; opacity: .65; }
        .register-signin { margin: 24px 0 0; color: #64748b; font-size: 13px; text-align: center; }
        .register-signin a { color: #4338ca; font-weight: 700; }
        .register-signin a:hover { color: #1e3a8a; text-decoration: underline; }
        .register-visual {
          position: relative;
          min-height: 100vh;
          overflow: hidden;
          background: linear-gradient(135deg, rgba(15, 23, 42, .15), rgba(37, 99, 235, .18)), url('/images/register-background.jpg') center / cover no-repeat;
        }
        .register-visual::after {
          position: absolute;
          inset: 0;
          background: linear-gradient(135deg, rgba(15, 23, 42, .15), rgba(37, 99, 235, .18));
          content: "";
        }
        @media (max-width: 900px) {
          .register-page { grid-template-columns: minmax(390px, 48%) 1fr; }
          .register-panel { padding: 28px 22px; }
          .register-card { padding: 32px; }
        }
        @media (max-width: 680px) {
          .register-page { display: block; }
          .register-visual { display: none; }
          .register-panel { min-height: 100vh; padding: 20px; }
          .register-card { padding: 30px 24px; border-radius: 16px; }
          .register-brand { margin-bottom: 26px; }
          .register-card h1 { font-size: 29px; }
        }
      `}</style>
      <section className="register-panel">
        <div className="register-card">
          <div className="register-brand">
            <div className="register-logo">AI</div>
            <div><strong>RecruitAI</strong><small>AI • ATS Platform</small></div>
          </div>
          <h1>Create your account</h1>
          <p className="register-subtitle">Join the AI-powered recruitment platform</p>
          <form onSubmit={submit} className="register-form">
            <label className="register-field">Full name<input required value={form.name} onChange={e=>set("name",e.target.value)} placeholder="Your full name"/></label>
            <label className="register-field">Email<input required type="email" value={form.email} onChange={e=>set("email",e.target.value)} placeholder="you@example.com"/></label>
            <label className="register-field">Password<input required minLength="8" type="password" value={form.password} onChange={e=>set("password",e.target.value)} placeholder="Minimum 8 characters"/></label>
            <label className="register-field">Account type<select value={form.role} onChange={e=>set("role",e.target.value)}>
              <option value="CANDIDATE">Candidate</option>
              <option value="RECRUITER">Recruiter</option>
              <option value="HIRING_MANAGER">Hiring Manager</option>
            </select></label>
            <button className="register-submit" disabled={loading}>{loading ? "Creating account..." : "Create account"}</button>
          </form>
          <p className="register-signin">Already registered? <Link to="/login">Sign in</Link></p>
        </div>
      </section>
      <aside className="register-visual" aria-hidden="true" />
      <Toast message={toast?.message} type={toast?.type} onClose={()=>setToast(null)}/>
    </div>
  );
}