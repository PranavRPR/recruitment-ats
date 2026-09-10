import React, { useEffect, useState } from "react";
import api, { getApiErrorMessage } from "../api";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

export default function Profile(){
 const { userId }=useAuth(); const [toast,setToast]=useState(null); const [loading,setLoading]=useState(true); const [saving,setSaving]=useState(false);
 const [f,setF]=useState({summary:"",phone:"",location:"",experienceYears:0,skills:"",education:"",workExperience:"",certifications:"",projects:"",languages:"",linkedIn:"",portfolio:"",github:""});
 useEffect(()=>{
    async function load(){setLoading(true);try{const response=await api.get(`/candidate/profile/${userId}`);setF((current)=>({...current,...(response.data.data||response.data)}));}catch(error){setToast({message:getApiErrorMessage(error,"Unable to load profile"),type:"error"})}finally{setLoading(false)}}
  if(userId) load();
 },[userId]);
 const set=(k,v)=>setF({...f,[k]:v});
 async function save(e){e.preventDefault();setSaving(true);try{await api.put(`/candidate/profile/${userId}`,{...f,experienceYears:Number(f.experienceYears)});setToast({message:"Profile saved",type:"success"})}catch(e){setToast({message:getApiErrorMessage(e,"Save failed"),type:"error"})}finally{setSaving(false)}}
 return <div><div className="page-head"><div><p className="eyebrow">PROFILE</p><h1>Candidate profile</h1><p className="muted">Keep your professional profile ready for recruiters.</p></div></div>{loading?<div className="panel empty">Loading profile...</div>:<form className="panel form-grid" onSubmit={save}>
 <label className="span-2">Professional summary<textarea rows="4" value={f.summary} onChange={e=>set("summary",e.target.value)}/></label>
 <label>Phone<input value={f.phone} onChange={e=>set("phone",e.target.value)}/></label><label>Location<input value={f.location} onChange={e=>set("location",e.target.value)}/></label>
 <label>Experience (years)<input type="number" value={f.experienceYears} onChange={e=>set("experienceYears",e.target.value)}/></label><label>Languages<input value={f.languages} onChange={e=>set("languages",e.target.value)}/></label>
 <label className="span-2">Skills<input placeholder="Java, Spring Boot, SQL" value={f.skills} onChange={e=>set("skills",e.target.value)}/></label>
 <label>Education<textarea rows="3" value={f.education} onChange={e=>set("education",e.target.value)}/></label><label>Work experience<textarea rows="3" value={f.workExperience} onChange={e=>set("workExperience",e.target.value)}/></label>
 <label>Certifications<textarea rows="3" value={f.certifications} onChange={e=>set("certifications",e.target.value)}/></label><label>Projects<textarea rows="3" value={f.projects} onChange={e=>set("projects",e.target.value)}/></label>
 <label>LinkedIn<input value={f.linkedIn} onChange={e=>set("linkedIn",e.target.value)}/></label><label>GitHub<input value={f.github} onChange={e=>set("github",e.target.value)}/></label>
 <label>Portfolio<input value={f.portfolio} onChange={e=>set("portfolio",e.target.value)}/></label><div></div>
 <button className="primary" disabled={saving}>{saving?"Saving...":"Save profile"}</button>
 </form>}<Toast message={toast?.message} type={toast?.type} onClose={()=>setToast(null)}/></div>
}