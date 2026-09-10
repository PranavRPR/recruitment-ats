import React, { useEffect } from "react";

export default function Toast({ message, type = "success", onClose }) {
  useEffect(() => {
    if (!message) return;
    const t = setTimeout(onClose, 3500);
    return () => clearTimeout(t);
  }, [message, onClose]);

  if (!message) return null;
  return <div className={`toast ${type}`}><span>{type === "success" ? "✓" : "!"}</span>{message}</div>;
}