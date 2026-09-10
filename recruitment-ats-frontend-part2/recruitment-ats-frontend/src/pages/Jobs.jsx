import React, { useEffect, useRef, useState } from "react";
import api, { getApiErrorMessage } from "../api";
import { useAuth } from "../context/AuthContext";
import Modal from "../components/Modal";
import Toast from "../components/Toast";

export default function Jobs() {
  const { currentUser: user, isAuthenticated, isInitialized } = useAuth();
  const recruiter = user?.role === "RECRUITER";
  const jobManager = ["RECRUITER","ADMIN"].includes(user?.role);
  const [jobs,setJobs] = useState([]);
  const [companies,setCompanies] = useState([]);
  const [companiesLoading,setCompaniesLoading] = useState(false);
  const [appliedJobIds,setAppliedJobIds] = useState(new Set());
  const [query,setQuery] = useState("");
  const [loading,setLoading] = useState(true);
  const [modal,setModal] = useState(false);
  const [companyModal,setCompanyModal] = useState(false);
  const [creating,setCreating] = useState(false);
  const [companyCreating,setCompanyCreating] = useState(false);
  const creatingRef = useRef(false);
  const companyCreatingRef = useRef(false);
  const [toast,setToast] = useState(null);
  const [companyForm,setCompanyForm] = useState({name:""});
  const [form,setForm] = useState({title:"",description:"",location:"",department:"",employmentType:"FULL_TIME",experienceRequired:"",requiredSkills:"",preferredSkills:"",education:"",responsibilities:"",qualifications:"",applicationDeadline:"",openings:"1",companyId:"",recruiterId:user?.userId || ""});

  const load = async () => {
    const url = jobManager && user.role === "RECRUITER" ? `/jobs/recruiter/${user.userId}` : "/jobs";
    setLoading(true);
    try {
      const requests = [api.get(url)];
      if (user.role === "CANDIDATE") requests.push(api.get(`/applications/candidate/${user.userId}`));
      const [jobsResponse, applicationsResponse] = await Promise.allSettled(requests);
      if (jobsResponse.status !== "fulfilled") throw jobsResponse.reason;
      const jobData = jobsResponse.value.data?.data ?? jobsResponse.value.data;
      setJobs(Array.isArray(jobData) ? jobData : []);
      if (applicationsResponse?.status === "fulfilled") {
        const applications = applicationsResponse.value.data?.data ?? applicationsResponse.value.data;
        setAppliedJobIds(new Set((Array.isArray(applications) ? applications : []).map((application) => application.job?.id ?? application.jobId)));
      } else if (user.role !== "CANDIDATE") {
        setAppliedJobIds(new Set());
      }
    } catch (error) {
      setToast({message:getApiErrorMessage(error,"Unable to load jobs"),type:"error"});
    } finally {
      setLoading(false);
    }
  };
  async function loadCompanies() {
    if (!recruiter || !user?.userId) return [];
    setCompaniesLoading(true);
    try {
      const response = await api.get(`/recruiter/companies/owner/${user.userId}`);
      const companyData = response.data?.data ?? response.data;
      const companyList = Array.isArray(companyData) ? companyData : companyData?.companies;
      const ownedCompanies = (Array.isArray(companyList) ? companyList : []).filter((company) => company.companyId ?? company.id);
      setCompanies(ownedCompanies);
      return ownedCompanies;
    } catch (error) {
      setCompanies([]);
      setToast({message:getApiErrorMessage(error,"Unable to load your companies"),type:"error"});
      return [];
    } finally {
      setCompaniesLoading(false);
    }
  }

  useEffect(() => {
    if (!isInitialized || !isAuthenticated || !user?.userId) return;
    load();
    if (recruiter) loadCompanies();
  }, [isAuthenticated, isInitialized, user?.userId, user?.role]);

  const set=(k,v)=>setForm((current)=>({...current,[k]:v}));

  async function openCreateModal() {
    setModal(true);
    setForm((current)=>({...current,companyId:""}));
    const loadedCompanies = await loadCompanies();
    if (!loadedCompanies.length) setToast({message:"Please create a company before creating a job.",type:"error"});
  }

  async function createCompany(e) {
    e.preventDefault();
    if (companyCreating || companyCreatingRef.current) return;
    const name = companyForm.name.trim();
    if (!name) {
      setToast({message:"Please enter a company name.",type:"error"});
      return;
    }
    companyCreatingRef.current = true;
    setCompanyCreating(true);
    try {
      const response = await api.post(`/recruiter/companies/${user.userId}`, {name});
      const created = response.data?.data ?? response.data;
      const createdCompanyId = created?.companyId ?? created?.id;
      if (createdCompanyId === undefined || createdCompanyId === null || Number(createdCompanyId) <= 0) {
        setToast({message:"Company was created, but the backend did not return its database ID. Please refresh your companies.",type:"error"});
        await loadCompanies();
        return;
      }
      await loadCompanies();
      setCompanyForm({name:""});
      setCompanyModal(false);
      setToast({message:"Company created successfully.",type:"success"});
    } catch (error) {
      setToast({message:getApiErrorMessage(error,"Could not create company"),type:"error"});
    } finally {
      companyCreatingRef.current = false;
      setCompanyCreating(false);
    }
  }

  async function create(e) {
    e.preventDefault();
    if (creating || creatingRef.current) return;
    if (!form.companyId) {
      setToast({message:"Please select a company before creating the job.",type:"error"});
      return;
    }
    const numericFields = ["experienceRequired", "openings", "companyId"];
    const payload = Object.fromEntries(Object.entries(form).filter(([, value]) => value !== "" && value !== undefined && value !== null));
    payload.recruiterId = user.userId;
    numericFields.forEach((field) => { if (payload[field] !== undefined) payload[field] = Number(payload[field]); });
    if (numericFields.some((field) => payload[field] !== undefined && Number.isNaN(payload[field]))) {
      setToast({message:"Enter valid numeric job details.",type:"error"});
      return;
    }
    creatingRef.current = true;
    setCreating(true);
    try {
      await api.post("/jobs", payload);
      setModal(false);
      setForm((current)=>({...current,title:"",description:"",companyId:""}));
      setToast({message:"Job created successfully.",type:"success"});
      load();
    } catch(err) {
      const status = err.response?.status;
      const message = status === 403
        ? "You are not authorized to use this company."
        : status === 404
          ? "Company not found. Please refresh your company list and try again."
          : getApiErrorMessage(err,"Could not create job");
      setToast({message,type:"error"});
    } finally {
      creatingRef.current = false;
      setCreating(false);
    }
  }

  async function status(id,status) {
    try { await api.patch(`/jobs/${id}/status?status=${status}`); setToast({message:`Job ${status.toLowerCase()}`,type:"success"}); load(); }
    catch(err){setToast({message:"Unable to update job",type:"error"});}
  }

  return <div>
    <div className="page-head"><div><p className="eyebrow">JOBS</p><h1>{jobManager ? "Job management" : "Find your next opportunity"}</h1><p className="muted">{jobManager ? "Create and manage your organization's openings." : "Explore published opportunities."}</p></div>{recruiter && <div className="top-actions"><button className="secondary button" onClick={()=>setCompanyModal(true)}>+ Create company</button><button className="primary button" onClick={openCreateModal}>+ Create job</button></div>}</div>
    {recruiter && <div className="panel" style={{marginBottom:20}}><div className="panel-head"><h3>My companies</h3><button className="secondary" onClick={loadCompanies} disabled={companiesLoading}>{companiesLoading ? "Refreshing..." : "Refresh"}</button></div>{companiesLoading&&!companies.length?<p className="muted">Loading companies...</p>:companies.length?<div className="list-row"><div>{companies.map((company)=><div key={company.companyId ?? company.id}><b>{company.name}</b></div>)}</div></div>:<p className="muted">No company found. Create a company before creating a job.</p>}</div>}
    <div className="searchbar"><span>⌕</span><input value={query} onChange={(event)=>setQuery(event.target.value)} placeholder="Search jobs, skills, locations..." /></div>
    <div className="job-grid">
      {loading&&<div className="empty large">Loading jobs...</div>}
      {!loading&&jobs.filter((job) => `${job.title} ${job.description} ${job.location} ${job.requiredSkills}`.toLowerCase().includes(query.toLowerCase())).map(j=><JobCard key={j.id} job={j} recruiter={jobManager} userId={user.userId} applied={appliedJobIds.has(j.id)} onApplied={()=>setAppliedJobIds((current)=>new Set(current).add(j.id))} onToast={setToast} onStatus={status}/>) }
      {!loading&&!jobs.filter((job) => `${job.title} ${job.description} ${job.location} ${job.requiredSkills}`.toLowerCase().includes(query.toLowerCase())).length && <div className="empty large">No jobs available.</div>}
    </div>
    {companyModal && <Modal title="Create a company" onClose={()=>!companyCreating&&setCompanyModal(false)}><form className="form-grid" onSubmit={createCompany}><label className="span-2">Company name<input required autoFocus value={companyForm.name} onChange={e=>setCompanyForm({name:e.target.value})}/></label><button className="primary span-2" disabled={companyCreating}>{companyCreating ? "Creating..." : "Create company"}</button></form></Modal>}
    {modal && <Modal title="Create a job" onClose={()=>!creating&&setModal(false)}><form className="form-grid" onSubmit={create}>
      <label className="span-2">Job title<input required value={form.title} onChange={e=>set("title",e.target.value)}/></label>
      <label>Description<textarea required rows="4" value={form.description} onChange={e=>set("description",e.target.value)}/></label>
      <label>Location<input value={form.location} onChange={e=>set("location",e.target.value)}/></label>
      <label>Department<input value={form.department} onChange={e=>set("department",e.target.value)}/></label>
      <label>Employment<select value={form.employmentType} onChange={e=>set("employmentType",e.target.value)}>{["FULL_TIME","PART_TIME","CONTRACT","INTERNSHIP","TEMPORARY"].map(x=><option key={x}>{x}</option>)}</select></label>
      <label>Experience (years)<input type="number" min="0" value={form.experienceRequired} onChange={e=>set("experienceRequired",e.target.value)}/></label>
      <label>Required skills<input placeholder="Java, Spring Boot, SQL" value={form.requiredSkills} onChange={e=>set("requiredSkills",e.target.value)}/></label>
      <label>Preferred skills<input value={form.preferredSkills} onChange={e=>set("preferredSkills",e.target.value)}/></label>
      <label>Education<input value={form.education} onChange={e=>set("education",e.target.value)}/></label>
      <label>Responsibilities<textarea rows="3" value={form.responsibilities} onChange={e=>set("responsibilities",e.target.value)}/></label>
      <label>Qualifications<textarea rows="3" value={form.qualifications} onChange={e=>set("qualifications",e.target.value)}/></label>
      <label>Application deadline<input type="date" value={form.applicationDeadline} onChange={e=>set("applicationDeadline",e.target.value)}/></label>
      <label>Company<select required value={form.companyId} disabled={companiesLoading || creating || !companies.length} onChange={e=>set("companyId",e.target.value)}><option value="">{companiesLoading ? "Loading companies..." : companies.length ? "Select a company" : "No companies found"}</option>{companies.map((company)=><option key={company.companyId ?? company.id} value={company.companyId ?? company.id}>{company.name}</option>)}</select></label>
      <label>Openings<input type="number" min="1" value={form.openings} onChange={e=>set("openings",e.target.value)}/></label>
      {!companiesLoading && !companies.length && <p className="muted span-2">No company found. Please create your company before creating a job.</p>}
      {!companiesLoading && !companies.length && <button type="button" className="secondary span-2" onClick={()=>{setModal(false);setCompanyModal(true);}}>Create company</button>}
      <button className="primary span-2" disabled={creating || companiesLoading || !companies.length}>{creating ? "Creating..." : "Create draft"}</button>
    </form></Modal>}
    <Toast message={toast?.message} type={toast?.type} onClose={()=>setToast(null)}/>
  </div>
}

function JobCard({ job, recruiter, userId, applied, onApplied, onToast, onStatus }) {
  const [applying, setApplying] = useState(false);
  async function apply() {
    setApplying(true);
    try { await api.post(`/applications/apply/${job.id}/${userId}`); onApplied(); onToast({message:"Application submitted successfully",type:"success"}); }
    catch (error) { onToast({message:getApiErrorMessage(error,"Application failed"),type:"error"}); }
    finally { setApplying(false); }
  }
  return <div className="job-card"><div className="job-top"><div className="company-logo">{(job.company?.name||"C")[0]}</div><span className={`status ${String(job.status).toLowerCase()}`}>{job.status}</span></div><h3>{job.title}</h3><p className="muted">{job.company?.name || "Company"} · {job.location || "Remote"}</p><p>{job.description?.slice(0,150)}{job.description?.length>150?"...":""}</p><div className="tags">{(job.requiredSkills||"").split(",").filter(Boolean).slice(0,5).map(s=><span key={s}>{s.trim()}</span>)}</div><div className="card-actions">{recruiter ? <select value={job.status} onChange={e=>onStatus(job.id,e.target.value)}><option>DRAFT</option><option>PUBLISHED</option><option>PAUSED</option><option>CLOSED</option></select> : <button className="primary small" disabled={applying || applied} onClick={apply}>{applied?"Applied":applying?"Applying...":"Apply now"}</button>}</div></div>;
}