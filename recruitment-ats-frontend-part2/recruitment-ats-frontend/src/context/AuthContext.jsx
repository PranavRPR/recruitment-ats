import React, { createContext, useContext, useEffect, useState } from "react";
import api, { authUser, clearAuth, saveAuth } from "../api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    let active = true;
    async function restoreSession() {
      const stored = authUser();
      if (!stored.token) {
        if (active) {
          setCurrentUser(null);
          setIsInitialized(true);
        }
        return;
      }

      try {
        const response = await api.get("/auth/me");
        const profile = response.data?.data || response.data;
        if (active) {
          saveAuth({
            ...stored,
            ...profile,
            token: stored.token,
            userId: profile.userId ?? profile.id ?? stored.userId,
            name: profile.name ?? stored.name,
            email: profile.email ?? stored.email,
            role: profile.role ?? stored.role
          });
          setCurrentUser(authUser());
        }
      } catch {
        clearAuth();
        if (active) setCurrentUser(null);
      } finally {
        if (active) setIsInitialized(true);
      }
    }

    restoreSession();
    const syncUser = () => setCurrentUser(authUser().token ? authUser() : null);
    window.addEventListener("auth-changed", syncUser);
    window.addEventListener("auth-expired", syncUser);
    return () => {
      active = false;
      window.removeEventListener("auth-changed", syncUser);
      window.removeEventListener("auth-expired", syncUser);
    };
  }, []);

  const login = (data) => {
    saveAuth(data);
    setCurrentUser(authUser());
  };

  const logout = () => {
    clearAuth();
    setCurrentUser(null);
  };

  const value = {
    currentUser,
    token: currentUser?.token || null,
    role: currentUser?.role || null,
    userId: currentUser?.userId || null,
    isAuthenticated: Boolean(currentUser?.token),
    isInitialized,
    login,
    logout
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}