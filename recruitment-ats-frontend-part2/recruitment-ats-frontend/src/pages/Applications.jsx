import React, { useEffect, useState } from "react";
import api, { getApiErrorMessage } from "../api";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

const stages=["APPLIED","SCREENING","SHORTLISTED","INTERVIEW","OFFERED","HIRED","REJECTED"];

export default function Applications() {
  const { currentUser: user, isAuthenticated, isInitialized }=useAuth(); const [apps,setApps]=useState([]); const [jobId,setJobId]=useState(""); const [loading,setLoading]=useState(false); const [toast,setToast]=useState(null); const [updating,setUpdating]=useState(null);
  const recruiter=["RECRUITER","HIRING_MANAGER"].includes(user?.role);
  const load=async()=>{
    if(!isInitialized || !isAuthenticated || !user?.userId) return;
    if(recruiter&&!jobId){setApps([]);return;}
    setLoading(true);
    try{const response=await api.get(recruiter?`/applications/job/${jobId}`:`/applications/candidate/${user.userId}`);setApps(response.data.data||[]);}
    catch(error){setToast({message:getApiErrorMessage(error,"Unable to load applications"),type:"error"});}
    finally{setLoading(false);}
  };
  useEffect(()=>{ load(); },[isAuthenticated,isInitialized,jobId,user?.role,user?.userId]);
  async function updateStatus(id, status){
    setUpdating(id);
    try { await api.patch(`/applications/${id}/status?status=${status}`); setToast({message:"Application status updated",type:"success"}); load(); }
    catch(error){setToast({message:getApiErrorMessage(error,"Unable to update application"),type:"error"});}
    finally {setUpdating(null);}
  }
  return <div>
    <div className="page-head"><div><p className="eyebrow">ATS PIPELINE</p><h1>Applications</h1><p className="muted">{recruiter?"Review and move candidates through the hiring pipeline.":"Track your job applications."}</p></div>{recruiter&&<div className="inline-field"><span>Job ID</span><input type="number" value={jobId} onChange={e=>setJobId(e.target.value)} placeholder="Enter job ID"/></div>}</div>
    {!recruiter && <div className="pipeline">{stages.slice(0,6).map(s=><div className="stage" key={s}><small>{s.replace("_"," ")}</small><strong>{apps.filter(a=>a.status===s).length}</strong></div>)}</div>}
    <div className="panel"><div className="panel-head"><h3>{recruiter?"Candidate applications":"My applications"}</h3></div>
      {loading&&<div className="empty">Loading applications...</div>}
      {!loading&&apps.map(a=><div className="application-row" key={a.id}><div className="candidate-avatar">{(a.candidate?.user?.name||"C")[0]}</div><div className="grow"><b>{a.candidate?.user?.name || "Candidate"}</b><small>{a.job?.title || "Job"} · Applied {a.appliedAt ? new Date(a.appliedAt).toLocaleDateString():""}</small></div><span className={`status ${String(a.status).toLowerCase()}`}>{a.status}</span>{recruiter&&<select disabled={updating===a.id} value={a.status} onChange={e=>updateStatus(a.id,e.target.value)}>{stages.map(s=><option key={s}>{s}</option>)}</select>}</div>)}
      {!loading&&!apps.length&&<div className="empty">No applications found. {recruiter?"Enter a Job ID to load applications.":"Apply to a published job to see it here."}</div>}
    </div>
    <Toast message={toast?.message} type={toast?.type} onClose={()=>setToast(null)}/>
  </div>
}