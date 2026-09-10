import React, { useEffect, useState } from "react";
import api, { getApiErrorMessage } from "../api";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

export default function Notifications(){
 const { userId, isAuthenticated, isInitialized } = useAuth();
 const [items,setItems]=useState([]); const [loading,setLoading]=useState(true); const [toast,setToast]=useState(null);
 const load=async()=>{
  setLoading(true);
  try{
  const response=await api.get("/notifications");
  setItems(response.data.data||response.data||[]);
  }catch(error){
   setToast({type:"error",message:getApiErrorMessage(error,"Unable to load notifications")});
  }finally{
   setLoading(false);
  }
 };
 useEffect(()=>{
  if (isInitialized && isAuthenticated && userId) load();
  else if (isInitialized) setItems([]);
 },[isAuthenticated,isInitialized,userId]);
 async function read(id){
  try{
   await api.patch(`/notifications/${id}/read`);
   load();
  }catch(error){
    setToast({type:"error",message:getApiErrorMessage(error,"Unable to mark notification as read")});
  }
 }
 async function readAll(){
  try{
   await api.put("/notifications/read-all");
   load();
  }catch(error){
   setToast({type:"error",message:getApiErrorMessage(error,"Unable to mark notifications as read")});
  }
 }
 const unreadCount=items.filter((item)=>!item.read).length;
 return <div><div className="page-head"><div><p className="eyebrow">INBOX</p><h1>Notifications</h1><p className="muted">Stay updated on recruitment activity. {unreadCount ? `${unreadCount} unread` : "All caught up"}</p></div>{unreadCount>0&&<button className="secondary" onClick={readAll}>Mark all read</button>}</div><div className="panel">{loading&&<div className="empty">Loading notifications...</div>}{!loading&&items.map(n=><div className={`notification ${n.read?"read":""}`} key={n.id}><div className="notif-dot">●</div><div className="grow"><b>{n.title}</b><p>{n.message}</p><small>{n.createdAt?new Date(n.createdAt).toLocaleString():""}</small></div>{!n.read&&<button className="secondary" onClick={()=>read(n.id)}>Mark read</button>}</div>)}{!loading&&!items.length&&<div className="empty">You're all caught up.</div>}</div><Toast message={toast?.message} type={toast?.type} onClose={()=>setToast(null)}/></div>
}