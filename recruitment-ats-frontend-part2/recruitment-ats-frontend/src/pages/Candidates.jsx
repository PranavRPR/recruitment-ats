import React, { useEffect, useState } from "react";
import api, { getApiErrorMessage } from "../api";
import Toast from "../components/Toast";

export default function Candidates(){
  const [apps,setApps]=useState([]); const [jobId,setJobId]=useState(""); const [match,setMatch]=useState({}); const [toast,setToast]=useState(null); const [loading,setLoading]=useState(false);
  async function load(){if(!jobId)return; setLoading(true); try{const response=await api.get(`/applications/job/${jobId}`); setApps(response.data.data||[]);}catch(error){setToast({type:"error",message:getApiErrorMessage(error,"Unable to load candidates")});}finally{setLoading(false);}}
  async function analyze(id){try{const response=await api.post(`/ai/match/${id}`); setMatch({...match,[id]:response.data.data});}catch(error){setToast({type:"error",message:getApiErrorMessage(error,"Unable to generate AI match")});}}
  return <div><div className="page-head"><div><p className="eyebrow">TALENT POOL</p><h1>Candidate screening</h1><p className="muted">Review applications and generate explainable AI match scores.</p></div><div className="inline-field"><input type="number" value={jobId} onChange={e=>setJobId(e.target.value)} placeholder="Job ID"/><button className="primary button" onClick={load}>Load</button></div></div>
    <div className="panel">{loading&&<div className="empty">Loading candidates...</div>}{!loading&&apps.map(a=><div className="candidate-row" key={a.id}><div className="candidate-avatar">{(a.candidate?.user?.name||"C")[0]}</div><div className="grow"><b>{a.candidate?.user?.name||"Candidate"}</b><small>{a.candidate?.skills||"Skills not provided"}</small></div>{match[a.id]?<div className="ai-score"><strong>{match[a.id].score}%</strong><small>AI match</small></div>:<button className="secondary" onClick={()=>analyze(a.id)}>✦ Analyze</button>}</div>)}
    {!loading&&!apps.length&&<div className="empty">Enter a job ID and load its applications.</div>}</div><Toast message={toast?.message} type={toast?.type} onClose={()=>setToast(null)}/>
  </div>
}