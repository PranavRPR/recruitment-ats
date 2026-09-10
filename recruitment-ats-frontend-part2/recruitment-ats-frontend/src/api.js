import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
  headers: { "Content-Type": "application/json" }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const isAuthRequest = error.config?.url?.includes("/auth/");

    if (status === 401 && !isAuthRequest) {
      clearAuth();
      sessionStorage.setItem("authMessage", "Your session has expired. Please sign in again.");
      window.dispatchEvent(new Event("auth-expired"));
      if (window.location.pathname !== "/login") window.location.assign("/login");
    }

    if (status === 403) {
      window.dispatchEvent(new CustomEvent("api-error", { detail: "You are not authorized to perform this action." }));
    } else if (status === 400) {
      window.dispatchEvent(new CustomEvent("api-error", { detail: getApiErrorMessage(error, "Please check the submitted information.") }));
    } else if (status === 404) {
      window.dispatchEvent(new CustomEvent("api-error", { detail: "The requested resource was not found." }));
    } else if (status === 405) {
      window.dispatchEvent(new CustomEvent("api-error", { detail: "This operation is not supported by the backend." }));
    } else if (!error.response) {
      window.dispatchEvent(new CustomEvent("api-error", { detail: "Backend server is unavailable. Please start the Spring Boot server." }));
    } else if (status >= 500) {
      window.dispatchEvent(new CustomEvent("api-error", { detail: "The server could not complete the request. Please try again." }));
    }

    return Promise.reject(error);
  }
);

export default api;

export function saveAuth(data) {
  localStorage.setItem("token", data.token);
  localStorage.setItem("userId", String(data.userId));
  localStorage.setItem("name", data.name || "");
  localStorage.setItem("email", data.email || "");
  localStorage.setItem("role", data.role || "");
  window.dispatchEvent(new Event("auth-changed"));
}

export function clearAuth() {
  ["token", "userId", "name", "email", "role"].forEach((key) => localStorage.removeItem(key));
  window.dispatchEvent(new Event("auth-changed"));
}

export function logout() {
  clearAuth();
  window.location.assign("/login");
}

export function authUser() {
  return {
    token: localStorage.getItem("token"),
    userId: localStorage.getItem("userId"),
    name: localStorage.getItem("name"),
    email: localStorage.getItem("email"),
    role: localStorage.getItem("role")
  };
}

export function getApiErrorMessage(error, fallback = "Something went wrong. Please try again.") {
  if (!error?.response) return "Unable to connect to the server. Check your connection and try again.";
  const data = error.response.data;
  if (typeof data?.message === "string" && data.message.trim()) return data.message;
  if (typeof data?.error === "string" && data.error.trim()) return data.error;
  if (typeof data?.errors === "object" && data.errors) {
    const validationMessage = Object.values(data.errors).find((value) => typeof value === "string" && value.trim());
    if (validationMessage) return validationMessage;
  }
  if (typeof data === "string") return data;
  if (error.response.status === 400) return "Please check the submitted information.";
  if (error.response.status === 401) return "Invalid credentials.";
  if (error.response.status === 403) return "You are not authorized to perform this action.";
  if (error.response.status === 404) return "The requested resource was not found.";
  if (error.response.status === 409) return "This record already exists.";
  if (error.response.status >= 500) return "The server could not complete the request.";
  return fallback;
}