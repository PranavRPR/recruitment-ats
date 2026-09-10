import React, { useEffect, useState } from "react";
import api, { getApiErrorMessage } from "../api";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

export default function Interviews() {
	const { currentUser: user, isAuthenticated, isInitialized } = useAuth();
	const canSchedule = ["RECRUITER", "HIRING_MANAGER"].includes(user?.role);
	const [items, setItems] = useState([]);
	const [form, setForm] = useState({ applicationId: "", type: "VIDEO", scheduledAt: "", durationMinutes: 30, interviewers: "", meetingLink: "", notes: "" });
	const [toast, setToast] = useState(null);
	const [loading, setLoading] = useState(false);
	const [loadingItems, setLoadingItems] = useState(true);
	const [updating, setUpdating] = useState(null);

	const set = (key, value) => setForm((current) => ({ ...current, [key]: value }));

	useEffect(() => {
		if (!isInitialized || !isAuthenticated || !user?.userId) return;
		let active = true;
		async function load() {
			setLoadingItems(true);
			try {
				const response = await api.get("/interviews");
				const data = response.data?.data ?? response.data;
				if (active) setItems(Array.isArray(data) ? data : []);
			} catch (error) {
				if (active) setToast({ message: getApiErrorMessage(error, "Unable to load interviews"), type: "error" });
			} finally {
				if (active) setLoadingItems(false);
			}
		}
		load();
		return () => { active = false; };
	}, [isAuthenticated, isInitialized, user?.userId]);

	async function submit(event) {
		event.preventDefault();
		setLoading(true);
		try {
			await api.post("/interviews", { ...form, applicationId: Number(form.applicationId), durationMinutes: Number(form.durationMinutes) });
			setToast({ message: "Interview scheduled", type: "success" });
			setForm((current) => ({ ...current, applicationId: "", scheduledAt: "" }));
		} catch (error) {
			setToast({ message: getApiErrorMessage(error, "Could not schedule interview"), type: "error" });
		} finally {
			setLoading(false);
		}
	}

	async function updateStatus(id, status) {
		setUpdating(id);
		try {
			await api.patch(`/interviews/${id}/status?status=${status}`);
			setItems((current) => current.map((item) => (getInterviewId(item) === id ? { ...item, status } : item)));
			setToast({ message: "Interview updated", type: "success" });
		} catch (error) {
			setToast({ message: getApiErrorMessage(error, "Unable to update interview"), type: "error" });
		} finally {
			setUpdating(null);
		}
	}

	return <div>
		<div className="page-head"><div><p className="eyebrow">INTERVIEWS</p><h1>Interview management</h1><p className="muted">Schedule interviews and keep interview details organized.</p></div></div>
		{canSchedule && <form className="panel form-grid" onSubmit={submit}><label>Application ID<input required type="number" value={form.applicationId} onChange={(event) => set("applicationId", event.target.value)} /></label><label>Type<select value={form.type} onChange={(event) => set("type", event.target.value)}>{["PHONE", "VIDEO", "TECHNICAL", "HR", "ONSITE", "FINAL"].map((type) => <option key={type}>{type}</option>)}</select></label><label>Date & time<input required type="datetime-local" value={form.scheduledAt} onChange={(event) => set("scheduledAt", event.target.value)} /></label><label>Duration<input type="number" min="1" value={form.durationMinutes} onChange={(event) => set("durationMinutes", event.target.value)} /></label><label>Interviewers<input value={form.interviewers} onChange={(event) => set("interviewers", event.target.value)} /></label><label>Meeting link<input value={form.meetingLink} onChange={(event) => set("meetingLink", event.target.value)} /></label><label className="span-2">Notes<textarea rows="4" value={form.notes} onChange={(event) => set("notes", event.target.value)} /></label><button className="primary" disabled={loading}>{loading ? "Scheduling..." : "Schedule interview"}</button></form>}
		<div className="panel"><div className="panel-head"><h3>{canSchedule ? "Scheduled interviews" : "My interviews"}</h3></div>{loadingItems && <div className="empty">Loading interviews...</div>}{!loadingItems && items.map((item) => { const id = getInterviewId(item); const status = item.status || "SCHEDULED"; return <div className="list-row" key={id}><div><b>{item.application?.job?.title || item.job?.title || item.jobTitle || `Application #${item.applicationId}`}</b><small>{item.scheduledAt ? new Date(item.scheduledAt).toLocaleString() : "Date pending"} · {item.type || "Interview"}</small></div>{canSchedule ? <select disabled={updating === id} value={status} onChange={(event) => updateStatus(id, event.target.value)}>{["SCHEDULED", "COMPLETED", "CANCELLED", "RESCHEDULED"].map((option) => <option key={option}>{option}</option>)}</select> : <span className={`status ${String(status).toLowerCase()}`}>{status}</span>}</div>; })}{!loadingItems && !items.length && <div className="empty">No interviews found.</div>}</div>
		<Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
	</div>;
}

function getInterviewId(interview) {
	return interview.id ?? interview.interviewId;
}