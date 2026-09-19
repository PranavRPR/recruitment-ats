import React, { useState } from "react";
import api, { getApiErrorMessage } from "../api";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

export default function Resume(){
 const { userId }=useAuth(); const [file,setFile]=useState(null); const [resume,setResume]=useState(null); const [loading,setLoading]=useState(false); const [toast,setToast]=useState(null);
 async function upload(e){e.preventDefault();if(!file)return;if(file.size>10*1024*1024){setToast({message:"Resume must be 10 MB or smaller",type:"error"});return;}setLoading(true);try{const fd=new FormData();fd.append("file",file);const r=await api.post(`/resumes/upload/${userId}`,fd);setResume(r.data.data||r.data);setToast({message:"Resume uploaded successfully",type:"success"})}catch(e){setToast({message:getApiErrorMessage(e,"Upload failed"),type:"error"})}finally{setLoading(false)}}
 return <div><div className="page-head"><div><p className="eyebrow">RESUME AI</p><h1>Resume management</h1><p className="muted">Upload PDF, DOC or DOCX and extract profile information.</p></div></div>
 <div className="upload-panel"><div className="upload-icon">▤</div><h2>Upload your resume</h2><p className="muted">PDF, DOC and DOCX · Maximum 10 MB</p><form onSubmit={upload}><input type="file" accept=".pdf,.doc,.docx" onChange={e=>setFile(e.target.files?.[0])}/><button className="primary" disabled={loading}>{loading?"Processing...":"Upload & parse"}</button></form></div>
 {resume&&<div className="panel"><div className="panel-head"><h3>Parsed resume</h3><span className="status published">PROCESSED</span></div><p><b>{resume.originalFileName}</b></p><pre className="resume-text">{resume.extractedText||resume.parsedData||"No extracted text available."}</pre></div>}
 <Toast message={toast?.message} type={toast?.type} onClose={()=>setToast(null)}/></div>
}
