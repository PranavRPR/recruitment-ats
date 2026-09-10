import React from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Layout from "./components/Layout";
import Dashboard from "./pages/Dashboard";
import Jobs from "./pages/Jobs";
import Candidates from "./pages/Candidates";
import Applications from "./pages/Applications";
import Profile from "./pages/Profile";
import Resume from "./pages/Resume";
import Interviews from "./pages/Interviews";
import Notifications from "./pages/Notifications";
import Admin from "./pages/Admin";
import { useAuth } from "./context/AuthContext";

function ProtectedRoute({ children, allowedRoles }) {
  const { isAuthenticated, isInitialized, role } = useAuth();
  if (!isInitialized) return <div className="route-loading">Loading your workspace...</div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (allowedRoles && !allowedRoles.includes(role)) return <Navigate to={dashboardFor(role)} replace />;
  return children;
}

function dashboardFor(role) {
  return { ADMIN: "/admin/dashboard", RECRUITER: "/recruiter/dashboard", HIRING_MANAGER: "/manager/dashboard", CANDIDATE: "/candidate/dashboard" }[role] || "/login";
}

function RoleHome() {
  const { role } = useAuth();
  return <Navigate to={dashboardFor(role)} replace />;
}

function RoleResourceRedirect({ resource }) {
  const { role } = useAuth();
  const destinations = {
    jobs: { ADMIN: "/admin/jobs", RECRUITER: "/recruiter/jobs", HIRING_MANAGER: "/manager/jobs", CANDIDATE: "/candidate/jobs" },
    applications: { ADMIN: "/admin/applications", RECRUITER: "/recruiter/applications", HIRING_MANAGER: "/manager/applications", CANDIDATE: "/candidate/applications" },
    candidates: { RECRUITER: "/recruiter/candidates", HIRING_MANAGER: "/manager/candidates" },
    interviews: { RECRUITER: "/recruiter/interviews", HIRING_MANAGER: "/manager/interviews", CANDIDATE: "/candidate/interviews" },
    notifications: { ADMIN: "/admin/notifications", RECRUITER: "/recruiter/notifications", HIRING_MANAGER: "/manager/notifications", CANDIDATE: "/candidate/notifications" },
    profile: { CANDIDATE: "/candidate/profile" },
    resume: { CANDIDATE: "/candidate/resume" }
  };
  return <Navigate to={destinations[resource]?.[role] || "/unauthorized"} replace />;
}

function Unauthorized() {
  return <div className="empty large"><h2>Access restricted</h2><p>You do not have permission to view this workspace.</p></div>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route path="/" element={<RoleHome />} />
        <Route path="/dashboard" element={<RoleHome />} />
        <Route path="/jobs" element={<RoleResourceRedirect resource="jobs" />} />
        <Route path="/candidates" element={<RoleResourceRedirect resource="candidates" />} />
        <Route path="/applications" element={<RoleResourceRedirect resource="applications" />} />
        <Route path="/profile" element={<RoleResourceRedirect resource="profile" />} />
        <Route path="/resume" element={<RoleResourceRedirect resource="resume" />} />
        <Route path="/interviews" element={<RoleResourceRedirect resource="interviews" />} />
        <Route path="/notifications" element={<RoleResourceRedirect resource="notifications" />} />
        <Route path="/unauthorized" element={<Unauthorized />} />

        <Route path="/admin/dashboard" element={<ProtectedRoute allowedRoles={["ADMIN"]}><Admin /></ProtectedRoute>} />
        <Route path="/admin/users" element={<ProtectedRoute allowedRoles={["ADMIN"]}><Admin /></ProtectedRoute>} />
        <Route path="/admin/jobs" element={<ProtectedRoute allowedRoles={["ADMIN"]}><Jobs /></ProtectedRoute>} />
        <Route path="/admin/applications" element={<ProtectedRoute allowedRoles={["ADMIN"]}><Applications /></ProtectedRoute>} />
        <Route path="/admin/interviews" element={<ProtectedRoute allowedRoles={["ADMIN"]}><Interviews /></ProtectedRoute>} />
        <Route path="/admin/reports" element={<ProtectedRoute allowedRoles={["ADMIN"]}><Admin /></ProtectedRoute>} />
        <Route path="/admin/notifications" element={<ProtectedRoute allowedRoles={["ADMIN"]}><Notifications /></ProtectedRoute>} />

        <Route path="/recruiter/dashboard" element={<ProtectedRoute allowedRoles={["RECRUITER"]}><Dashboard /></ProtectedRoute>} />
        <Route path="/recruiter/jobs" element={<ProtectedRoute allowedRoles={["RECRUITER"]}><Jobs /></ProtectedRoute>} />
        <Route path="/recruiter/jobs/create" element={<ProtectedRoute allowedRoles={["RECRUITER"]}><Jobs /></ProtectedRoute>} />
        <Route path="/recruiter/applications" element={<ProtectedRoute allowedRoles={["RECRUITER"]}><Applications /></ProtectedRoute>} />
        <Route path="/recruiter/candidates" element={<ProtectedRoute allowedRoles={["RECRUITER"]}><Candidates /></ProtectedRoute>} />
        <Route path="/recruiter/interviews" element={<ProtectedRoute allowedRoles={["RECRUITER"]}><Interviews /></ProtectedRoute>} />
        <Route path="/recruiter/notifications" element={<ProtectedRoute allowedRoles={["RECRUITER"]}><Notifications /></ProtectedRoute>} />

        <Route path="/manager/dashboard" element={<ProtectedRoute allowedRoles={["HIRING_MANAGER"]}><Dashboard /></ProtectedRoute>} />
        <Route path="/manager/jobs" element={<ProtectedRoute allowedRoles={["HIRING_MANAGER"]}><Jobs /></ProtectedRoute>} />
        <Route path="/manager/candidates" element={<ProtectedRoute allowedRoles={["HIRING_MANAGER"]}><Candidates /></ProtectedRoute>} />
        <Route path="/manager/applications" element={<ProtectedRoute allowedRoles={["HIRING_MANAGER"]}><Applications /></ProtectedRoute>} />
        <Route path="/manager/interviews" element={<ProtectedRoute allowedRoles={["HIRING_MANAGER"]}><Interviews /></ProtectedRoute>} />
        <Route path="/manager/notifications" element={<ProtectedRoute allowedRoles={["HIRING_MANAGER"]}><Notifications /></ProtectedRoute>} />

        <Route path="/candidate/dashboard" element={<ProtectedRoute allowedRoles={["CANDIDATE"]}><Dashboard /></ProtectedRoute>} />
        <Route path="/candidate/jobs" element={<ProtectedRoute allowedRoles={["CANDIDATE"]}><Jobs /></ProtectedRoute>} />
        <Route path="/candidate/applications" element={<ProtectedRoute allowedRoles={["CANDIDATE"]}><Applications /></ProtectedRoute>} />
        <Route path="/candidate/resume" element={<ProtectedRoute allowedRoles={["CANDIDATE"]}><Resume /></ProtectedRoute>} />
        <Route path="/candidate/interviews" element={<ProtectedRoute allowedRoles={["CANDIDATE"]}><Interviews /></ProtectedRoute>} />
        <Route path="/candidate/notifications" element={<ProtectedRoute allowedRoles={["CANDIDATE"]}><Notifications /></ProtectedRoute>} />
        <Route path="/candidate/profile" element={<ProtectedRoute allowedRoles={["CANDIDATE"]}><Profile /></ProtectedRoute>} />
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}