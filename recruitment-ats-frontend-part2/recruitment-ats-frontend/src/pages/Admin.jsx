import React, { useEffect, useState } from "react";
import api, { getApiErrorMessage } from "../api";
import Toast from "../components/Toast";

export default function Admin(){
 const [data,setData]=useState({users:[],jobs:[],applications:[],reports:[]}); const [loading,setLoading]=useState(true); const [toast,setToast]=useState(null);
 useEffect(()=>{
  async function load(){
     try{
        const responses=await Promise.allSettled(["/admin/users","/admin/jobs","/admin/applications","/admin/reports"].map((url)=>api.get(url)));
        const values=responses.map((result)=>result.status==="fulfilled"?(result.value.data?.data||result.value.data||[]):[]);
        setData({users:Array.isArray(values[0])?values[0]:[],jobs:Array.isArray(values[1])?values[1]:[],applications:Array.isArray(values[2])?values[2]:[],reports:Array.isArray(values[3])?values[3]:[]});
        if(responses.some((result)=>result.status==="rejected")) setToast({type:"error",message:"Some administration data could not be loaded."});
     }
   catch(error){setToast({type:"error",message:getApiErrorMessage(error,"Unable to load users")});}
   finally{setLoading(false);}
  }
  load();
 },[]);
 const candidates=data.users.filter((user)=>user.role==="CANDIDATE").length; const recruiters=data.users.filter((user)=>user.role==="RECRUITER").length; const managers=data.users.filter((user)=>user.role==="HIRING_MANAGER").length; const shortlisted=data.applications.filter((application)=>application.status==="SHORTLISTED").length; const hired=data.applications.filter((application)=>application.status==="HIRED").length;
 return <div><div className="page-head"><div><p className="eyebrow">ADMINISTRATION</p><h1>Platform administration</h1><p className="muted">Monitor users and system activity.</p></div></div><div className="stats-grid"><Stat label="Total users" value={loading?"—":data.users.length} icon="◎"/><Stat label="Candidates" value={loading?"—":candidates} icon="○"/><Stat label="Recruiters" value={loading?"—":recruiters} icon="◉"/><Stat label="Managers" value={loading?"—":managers} icon="◌"/><Stat label="Jobs" value={loading?"—":data.jobs.length} icon="▣"/><Stat label="Applications" value={loading?"—":data.applications.length} icon="✓"/><Stat label="Shortlisted" value={loading?"—":shortlisted} icon="★"/><Stat label="Hired" value={loading?"—":hired} icon="+"/></div><div className="panel"><div className="panel-head"><h3>Users</h3></div>{loading&&<div className="empty">Loading users...</div>}{!loading&&data.users.map((user)=><div className="list-row" key={user.id}><div><b>{user.name||"Unnamed user"}</b><small>{user.email||"No email"} · {user.status||"ACTIVE"}</small></div><span className="status published">{user.role||"UNKNOWN"}</span></div>)}{!loading&&!data.users.length&&<div className="empty">No users found.</div>}</div><Toast message={toast?.message} type={toast?.type} onClose={()=>setToast(null)}/></div>
}

function Stat({label,value,icon}) { return <div className="stat"><div className="stat-icon">{icon}</div><div><small>{label}</small><strong>{value}</strong></div></div>; }