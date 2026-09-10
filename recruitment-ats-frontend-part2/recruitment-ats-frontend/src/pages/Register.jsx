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
    <div className="auth-page simple-auth">
      <div className="auth-card wide">
        <div className="brand center"><div className="brand-mark">AI</div><div><strong>RecruitAI</strong><small>ATS Platform</small></div></div>
        <h2>Create your account</h2>
        <p className="muted">Join the AI-powered recruitment platform</p>
        <form onSubmit={submit} className="form-grid">
          <label className="span-2">Full name<input required value={form.name} onChange={e=>set("name",e.target.value)} placeholder="Your full name"/></label>
          <label className="span-2">Email<input required type="email" value={form.email} onChange={e=>set("email",e.target.value)} placeholder="you@example.com"/></label>
          <label className="span-2">Password<input required minLength="8" type="password" value={form.password} onChange={e=>set("password",e.target.value)} placeholder="Minimum 8 characters"/></label>
          <label className="span-2">Account type<select value={form.role} onChange={e=>set("role",e.target.value)}>
            <option value="CANDIDATE">Candidate</option>
            <option value="RECRUITER">Recruiter</option>
            <option value="HIRING_MANAGER">Hiring Manager</option>
          </select></label>
          <button className="primary span-2" disabled={loading}>{loading ? "Creating account..." : "Create account"}</button>
        </form>
        <p className="auth-link">Already registered? <Link to="/login">Sign in</Link></p>
      </div>
      <Toast message={toast?.message} type={toast?.type} onClose={()=>setToast(null)}/>
    </div>
  );
}