import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { getApiErrorMessage } from "../api";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

export default function Dashboard() {
  const { currentUser: user, isAuthenticated, isInitialized } = useAuth();
  const [jobs,setJobs] = useState([]);
  const [apps,setApps] = useState([]);
  const [interviews,setInterviews] = useState([]);
  const [loading,setLoading] = useState(true);
  const [toast,setToast] = useState(null);

  useEffect(() => {
    if (!isInitialized || !isAuthenticated || !user?.userId) return;
    async function load() {
      setLoading(true);
      try {
        const requests = [api.get(user.role === "RECRUITER" ? `/jobs/recruiter/${user.userId}` : "/jobs"), api.get("/interviews")];
        if (user.role === "CANDIDATE") requests.push(api.get(`/applications/candidate/${user.userId}`));
        const responses = await Promise.allSettled(requests);
        const [jobsResponse, interviewsResponse, applicationsResponse] = responses;
        if (jobsResponse.status === "fulfilled") setJobs(jobsResponse.value.data?.data || jobsResponse.value.data || []);
        if (interviewsResponse.status === "fulfilled") setInterviews(interviewsResponse.value.data?.data || interviewsResponse.value.data || []);
        if (applicationsResponse?.status === "fulfilled") setApps(applicationsResponse.value.data?.data || applicationsResponse.value.data || []);
        if (responses.some((response) => response.status === "rejected")) setToast({type:"error",message:"Some dashboard data could not be loaded."});
      } catch (error) {
        setToast({type:"error",message:getApiErrorMessage(error,"Unable to load dashboard data")});
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [isAuthenticated, isInitialized, user?.role, user?.userId]);
  if (!isInitialized || !user) return <div className="panel empty">Loading dashboard...</div>;
  const recruiter = ["RECRUITER","HIRING_MANAGER","ADMIN"].includes(user.role);

  return (
    <div>
      <div className="page-head">
        <div><p className="eyebrow">OVERVIEW</p><h1>Good to see you, {user.name?.split(" ")[0]} 👋</h1><p className="muted">Here's what's happening in your recruitment workspace.</p></div>
        <Link className="primary button" to={recruiter ? "/jobs" : "/jobs"}>Browse jobs</Link>
      </div>

      <div className="stats-grid">
        <Stat label={recruiter ? "Active jobs" : "Available jobs"} value={loading ? "—" : jobs.length} icon="▣"/>
        <Stat label={recruiter ? "Applications" : "My applications"} value={apps.length || (recruiter ? "—" : 0)} icon="✓"/>
        <Stat label="Interviews" value={interviews.length || "—"} icon="◷"/>
        <Stat label={recruiter ? "AI screening" : "Profile status"} value={recruiter ? "Ready" : "Update"} icon="✦"/>
      </div>

      <div className="dashboard-grid">
        <div className="panel">
          <div className="panel-head"><h3>{recruiter ? "Recent jobs" : "Recommended jobs"}</h3><Link to="/jobs">View all</Link></div>
          {loading&&<Empty text="Loading dashboard data..."/>}
          {!loading&&jobs.slice(0,5).map(j=><div className="list-row" key={j.id}><div><b>{j.title}</b><small>{j.company?.name || "Company"} · {j.location || "Remote"}</small></div><span className="score">{j.status}</span></div>)}
          {!loading&&!jobs.length && <Empty text="No published jobs yet."/>}
        </div>
        <div className="panel">
          <div className="panel-head"><h3>Quick actions</h3></div>
          <div className="quick-grid">
            <Link to="/jobs" className="quick">▣<b>{recruiter ? "Manage jobs" : "Find jobs"}</b><small>Explore openings</small></Link>
            <Link to={recruiter ? "/candidates" : "/profile"} className="quick">◎<b>{recruiter ? "Candidates" : "My profile"}</b><small>View details</small></Link>
            <Link to="/resume" className="quick">▤<b>Resume</b><small>Upload & parse</small></Link>
            <Link to="/interviews" className="quick">◷<b>Interviews</b><small>Schedule & review</small></Link>
          </div>
        </div>
      </div>
      <Toast message={toast?.message} type={toast?.type} onClose={()=>setToast(null)}/>
    </div>
  );
}

function Stat({label,value,icon}) { return <div className="stat"><div className="stat-icon">{icon}</div><div><small>{label}</small><strong>{value}</strong></div></div> }
function Empty({text}) { return <div className="empty">{text}</div> }